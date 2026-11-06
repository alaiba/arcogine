package com.arcogine.game.lastlightfoundry.web;

import static com.arcogine.game.lastlightfoundry.json.Json.num;
import static com.arcogine.game.lastlightfoundry.json.Json.obj;

import com.arcogine.challenge.attempt.ChallengeAttempt;
import com.arcogine.challenge.comparison.AttemptComparisonResult;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.engine.DesignChecker;
import com.arcogine.game.lastlightfoundry.engine.GameService;
import com.arcogine.game.lastlightfoundry.engine.RunDriver;
import com.arcogine.game.lastlightfoundry.json.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The local HTTP adapter: a JSON API over {@link GameService} plus the bundled static browser
 * client. It binds to the loopback interface unless told otherwise.
 *
 * <p>Event transport is cursor polling. {@code GET /api/runs/{id}} returns an atomic observation
 * carrying {@code latestEventSequence}; a client joins by remembering that sequence and polling
 * {@code GET /api/runs/{id}/events?after=<sequence>}. A client that sees a sequence that is not
 * exactly its cursor + 1 has a gap; a cursor older than the retained window is refused with
 * {@code 410 events.gap} and the client must resnapshot (historical event claims before the
 * retained window are then unavailable). A cursor beyond the run's latest sequence is refused
 * with {@code 409 events.cursor-ahead}. A run id that is not live (for example a reset run's
 * predecessor after eviction) answers {@code 404}, and every event names its run and model
 * fingerprint so a client can reject old-run messages.
 */
public final class GameServer {

    /** Largest accepted request body, in bytes. */
    public static final int MAX_BODY_BYTES = 64 * 1024;

    private static final int MAX_STEP = 1_000;
    private static final int MAX_PAGE = 500;
    private static final Pattern MISSION = Pattern.compile("^/api/missions/([a-z0-9-]{1,64})(/check|/runs)?$");
    private static final Pattern RUN = Pattern.compile(
            "^/api/runs/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(/step|/advance|/reset|/events)?$");

    private final GameService service;
    private final HttpServer http;

    private GameServer(GameService service, HttpServer http) {
        this.service = service;
        this.http = http;
    }

    /** Creates a server bound to {@code host:port}; port 0 picks a free port. */
    public static GameServer create(GameService service, String host, int port) throws IOException {
        HttpServer http = HttpServer.create(new InetSocketAddress(InetAddress.getByName(host), port), 0);
        GameServer server = new GameServer(service, http);
        http.createContext("/", server::dispatch);
        http.setExecutor(Executors.newFixedThreadPool(4));
        return server;
    }

    public void start() {
        http.start();
    }

    public void stop() {
        http.stop(0);
        ((java.util.concurrent.ExecutorService) http.getExecutor()).shutdownNow();
    }

    public int port() {
        return http.getAddress().getPort();
    }

    // ---- request handling ----

    private void dispatch(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                handleApi(exchange, path);
            } else {
                handleStatic(exchange, path);
            }
        } catch (ApiError error) {
            send(exchange, error.status, obj("error", obj("code", error.code, "message", error.getMessage())));
        } catch (Json.JsonException error) {
            send(exchange, 400, obj("error", obj("code", "request.malformed", "message", error.getMessage())));
        } catch (RuntimeException error) {
            send(exchange, 500, obj("error", obj("code", "server.fault", "message", "internal server error")));
        } finally {
            exchange.close();
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException {
        String method = exchange.getRequestMethod();
        if (path.equals("/api/missions")) {
            requireMethod(method, "GET");
            List<Object> missions = new ArrayList<>();
            service.library().all().forEach(mission -> missions.add(Views.missionSummary(mission)));
            send(exchange, 200, obj("missions", missions));
            return;
        }
        Matcher mission = MISSION.matcher(path);
        if (mission.matches()) {
            handleMission(exchange, method, mission.group(1), mission.group(2));
            return;
        }
        Matcher run = RUN.matcher(path);
        if (run.matches()) {
            handleRun(exchange, method, run.group(1), run.group(2));
            return;
        }
        throw new ApiError(404, "route.unknown", "no such API route");
    }

    private void handleMission(HttpExchange exchange, String method, String id, String action) throws IOException {
        Mission mission = service.library().find(id);
        if (mission == null) {
            throw new ApiError(404, "mission.unknown", "no mission named " + id);
        }
        if (action == null) {
            requireMethod(method, "GET");
            send(exchange, 200, Views.mission(mission));
        } else if (action.equals("/check")) {
            requireMethod(method, "POST");
            Draft draft = readDraft(exchange);
            send(exchange, 200, Views.design(service.check(mission, draft)));
        } else {
            requireMethod(method, "POST");
            Draft draft = readDraft(exchange);
            Optional<RunDriver> driver = service.start(mission, draft);
            if (driver.isEmpty()) {
                DesignChecker.DesignReport report = service.check(mission, draft);
                send(exchange, 422, obj(
                        "error", obj(
                                "code", "design.not-runnable",
                                "message", "The design cannot be run: it must be admissible for the contract "
                                        + "and publishable as a Factory model."),
                        "design", Views.design(report)));
                return;
            }
            send(exchange, 201, runBody(driver.get()));
        }
    }

    private void handleRun(HttpExchange exchange, String method, String id, String action) throws IOException {
        RunDriver driver = service.find(id)
                .orElseThrow(() -> new ApiError(404, "run.unknown", "no live run " + id));
        if (action == null) {
            requireMethod(method, "GET");
            send(exchange, 200, runBody(driver));
        } else if (action.equals("/step")) {
            requireMethod(method, "POST");
            Map<String, Object> body = readBody(exchange);
            int count = (int) Json.integral(body, "count", "request", 1, MAX_STEP);
            driver.step(count);
            send(exchange, 200, runBody(driver));
        } else if (action.equals("/advance")) {
            requireMethod(method, "POST");
            Map<String, Object> body = readBody(exchange);
            long maxEvents = Json.integral(body, "maxEvents", "request", 0, MAX_STEP);
            long untilTick = body.containsKey("untilTick")
                    ? Json.integral(body, "untilTick", "request", 0, Long.MAX_VALUE)
                    : Long.MAX_VALUE;
            driver.advance(maxEvents, untilTick);
            send(exchange, 200, runBody(driver));
        } else if (action.equals("/reset")) {
            requireMethod(method, "POST");
            RunDriver fresh = service.reset(id).orElseThrow(() -> new ApiError(404, "run.unknown", "no live run"));
            send(exchange, 201, runBody(fresh));
        } else {
            requireMethod(method, "GET");
            Map<String, String> query = query(exchange.getRequestURI());
            long after = parseLong(query.getOrDefault("after", "0"), "after");
            int limit = (int) Math.min(MAX_PAGE, Math.max(1, parseLong(query.getOrDefault("limit", "200"), "limit")));
            try {
                RunDriver.EventPage page = driver.eventsAfter(after, limit);
                List<Object> events = new ArrayList<>();
                page.events().forEach(event -> events.add(Views.event(event)));
                send(exchange, 200, obj(
                        "runId", id,
                        "after", num(after),
                        "latestSequence", num(page.latestSequence()),
                        "more", page.more(),
                        "events", events));
            } catch (RunDriver.GapException gap) {
                send(exchange, 410, obj("error", obj(
                        "code", "events.gap",
                        "message", gap.getMessage(),
                        "oldestRetained", num(gap.oldestRetained()))));
            } catch (RunDriver.CursorException cursor) {
                throw new ApiError(409, "events.cursor-ahead", cursor.getMessage());
            }
        }
    }

    private Map<String, Object> runBody(RunDriver driver) {
        String runId = driver.view().observation().metadata().runId().value().toString();
        List<ChallengeAttempt> attempts = service.recordAttempt(runId, driver);
        AttemptComparisonResult comparison = null;
        if (attempts.size() >= 2) {
            comparison = service.compare(attempts.get(attempts.size() - 2), attempts.get(attempts.size() - 1));
        }
        return Views.run(driver, attempts, comparison);
    }

    // ---- static client ----

    private void handleStatic(HttpExchange exchange, String path) throws IOException {
        requireMethod(exchange.getRequestMethod(), "GET");
        String name = path.equals("/") ? "index.html" : path.substring(1);
        if (!name.matches("[A-Za-z0-9._-]{1,64}") || name.contains("..") || name.startsWith(".")) {
            throw new ApiError(404, "asset.unknown", "no such asset");
        }
        try (InputStream in = GameServer.class.getResourceAsStream("/web/" + name)) {
            if (in == null) {
                throw new ApiError(404, "asset.unknown", "no such asset");
            }
            byte[] bytes = in.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType(name));
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }

    private static String contentType(String name) {
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }

    // ---- helpers ----

    private static void requireMethod(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new ApiError(405, "method.not-allowed", "use " + expected);
        }
    }

    private static Draft readDraft(HttpExchange exchange) throws IOException {
        Map<String, Object> body = readBody(exchange);
        return Draft.fromJson(body.get("installations"));
    }

    private static Map<String, Object> readBody(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("application/json")) {
            throw new ApiError(415, "request.content-type", "Content-Type must be application/json");
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = exchange.getRequestBody()) {
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) >= 0) {
                buffer.write(chunk, 0, read);
                if (buffer.size() > MAX_BODY_BYTES) {
                    throw new ApiError(413, "request.too-large", "request body exceeds " + MAX_BODY_BYTES + " bytes");
                }
            }
        }
        return Json.asObject(Json.parse(buffer.toString(StandardCharsets.UTF_8)), "request");
    }

    private static Map<String, String> query(URI uri) {
        Map<String, String> result = new LinkedHashMap<>();
        String raw = uri.getRawQuery();
        if (raw == null) {
            return result;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                result.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return result;
    }

    private static long parseLong(String text, String name) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ApiError(400, "request.malformed", name + " must be an integer");
        }
    }

    private static void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = Json.write(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /** A structured, client-visible failure. */
    private static final class ApiError extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final int status;
        private final String code;

        ApiError(int status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }
    }
}

package com.arcogine.game.lastlightfoundry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.game.lastlightfoundry.content.MissionLibrary;
import com.arcogine.game.lastlightfoundry.engine.GameService;
import com.arcogine.game.lastlightfoundry.json.Json;
import com.arcogine.game.lastlightfoundry.web.GameServer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Drives the real HTTP adapter over a loopback socket against the real engine. */
class GameServerTest {

    private static final String DEDICATED = installations(
            "equipment.cutter", "equipment.assembler", "equipment.inspector", "equipment.inspector");

    private GameServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void start() throws IOException {
        server = GameServer.create(new GameService(MissionLibrary.loadBundled(), 6), "127.0.0.1", 0);
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    private static String installations(String... items) {
        StringBuilder out = new StringBuilder("{\"installations\":[");
        for (int i = 0; i < items.length; i++) {
            out.append(i == 0 ? "" : ",").append("{\"number\":").append(i + 1).append(",\"itemId\":\"")
                    .append(items[i]).append("\",\"x\":").append(i).append(",\"y\":0}");
        }
        return out.append("]}").toString();
    }

    private record Reply(int status, Map<String, Object> body, HttpResponse<String> raw) {}

    private Reply send(String method, String path, String contentType, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path));
        if (contentType != null) {
            request.header("Content-Type", contentType);
        }
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        Map<String, Object> parsed = null;
        if (response.headers().firstValue("Content-Type").orElse("").startsWith("application/json")) {
            parsed = Json.asObject(Json.parse(response.body()), "$");
        }
        return new Reply(response.statusCode(), parsed, response);
    }

    private Reply get(String path) throws Exception {
        return send("GET", path, null, null);
    }

    private Reply post(String path, String body) throws Exception {
        return send("POST", path, "application/json", body);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) {
        return (List<Object>) value;
    }

    private String startRelayRun() throws Exception {
        Reply created = post("/api/missions/the-relay/runs", DEDICATED);
        assertEquals(201, created.status());
        return (String) created.body().get("runId");
    }

    @Test
    void listsMissionsAndDescribesTheRelayContract() throws Exception {
        Reply missions = get("/api/missions");
        assertEquals(200, missions.status());
        assertEquals(2, list(missions.body().get("missions")).size());

        Map<String, Object> relay = get("/api/missions/the-relay").body();
        assertEquals("12", relay.get("quantity"));
        assertEquals("1062", relay.get("budgetCredits"));
        assertEquals("57", relay.get("deadlineTicks"));
        assertEquals(5, list(relay.get("equipment")).size());
        assertEquals(3, list(relay.get("steps")).size());
        assertTrue(map(relay.get("identity")).get("contentFingerprint").toString().startsWith("sha256:"));
    }

    @Test
    void checkReportsEconomicsAdmissibilityAndPublicationIndependently() throws Exception {
        Map<String, Object> design = post("/api/missions/the-relay/check", DEDICATED).body();
        assertEquals(true, design.get("runnable"));
        assertEquals("850", map(design.get("economics")).get("committedCost"));
        assertEquals(true, map(design.get("admissibility")).get("admitted"));
        assertEquals(true, map(design.get("publication")).get("valid"));
        assertNotNull(map(design.get("publication")).get("fingerprint"));

        Map<String, Object> noInspector = post("/api/missions/the-relay/check",
                installations("equipment.cutter", "equipment.assembler")).body();
        assertEquals(true, map(noInspector.get("admissibility")).get("admitted"));
        assertEquals(false, map(noInspector.get("publication")).get("valid"));
        assertNull(map(noInspector.get("publication")).get("fingerprint"));
        assertFalse(list(map(noInspector.get("publication")).get("issues")).isEmpty());
        assertEquals(false, noInspector.get("runnable"));
    }

    @Test
    void aFullBrowserFreeRunReachesAnEvaluatedCompletionOverHttp() throws Exception {
        String runId = startRelayRun();
        Map<String, Object> run = get("/api/runs/" + runId).body();
        assertEquals("ACTIVE", run.get("status"));
        assertEquals("0", run.get("time"));
        assertNull(run.get("evaluation"));

        Map<String, Object> afterStep = post("/api/runs/" + runId + "/step", "{\"count\":1}").body();
        assertEquals("3", afterStep.get("time"));

        Map<String, Object> done = post("/api/runs/" + runId + "/advance", "{\"maxEvents\":1000}").body();
        assertEquals("COMPLETED", done.get("status"));
        assertEquals("56", done.get("time"));
        Map<String, Object> evaluation = map(done.get("evaluation"));
        assertEquals(true, evaluation.get("successful"));
        assertEquals("214", evaluation.get("score"));
        assertEquals("1", evaluation.get("deadlineMargin"));
        assertEquals(1, list(done.get("attempts")).size());
        assertNull(done.get("comparison"));
        Map<String, Object> order = map(list(done.get("orders")).get(0));
        assertEquals("12", order.get("completed"));
        assertEquals(true, order.get("primary"));
    }

    @Test
    void twoFinishedAttemptsAreComparedByTheChallengeComparator() throws Exception {
        String first = startRelayRun();
        post("/api/runs/" + first + "/advance", "{\"maxEvents\":1000}");
        String twin = (String) post("/api/missions/the-relay/runs", installations(
                "equipment.cutter", "equipment.twin-assembler", "equipment.inspector", "equipment.inspector"))
                .body().get("runId");
        Map<String, Object> done = post("/api/runs/" + twin + "/advance", "{\"maxEvents\":1000}").body();
        Map<String, Object> comparison = map(done.get("comparison"));
        assertEquals(true, comparison.get("comparable"));
        assertEquals("FIRST", comparison.get("winner"));
        assertEquals("-149", comparison.get("scoreDelta"));
        assertEquals("160", comparison.get("constructionCostDelta"));
        assertEquals(2, list(done.get("attempts")).size());
        // Re-reading a finished run never records the attempt twice.
        assertEquals(2, list(get("/api/runs/" + twin).body().get("attempts")).size());
    }

    @Test
    void eventsPageByCursorAndOldCursorsMustResnapshot() throws Exception {
        String runId = startRelayRun();
        post("/api/runs/" + runId + "/advance", "{\"maxEvents\":1000}");
        Map<String, Object> run = get("/api/runs/" + runId).body();
        long latest = Long.parseLong((String) run.get("latestEventSequence"));
        assertTrue(latest > 6);

        Reply tail = get("/api/runs/" + runId + "/events?after=" + (latest - 3) + "&limit=2");
        assertEquals(200, tail.status());
        List<Object> events = list(tail.body().get("events"));
        assertEquals(2, events.size());
        assertEquals(true, tail.body().get("more"));
        assertEquals(Long.toString(latest - 2), map(events.get(0)).get("sequence"));
        assertEquals(runId, map(events.get(0)).get("runId"));
        assertEquals(run.get("modelFingerprint"), map(events.get(0)).get("modelFingerprint"));

        Reply gap = get("/api/runs/" + runId + "/events?after=0");
        assertEquals(410, gap.status());
        assertEquals("events.gap", map(gap.body().get("error")).get("code"));
        assertEquals(run.get("oldestRetainedSequence"), map(gap.body().get("error")).get("oldestRetained"));

        Reply ahead = get("/api/runs/" + runId + "/events?after=" + (latest + 1));
        assertEquals(409, ahead.status());
        assertEquals("events.cursor-ahead", map(ahead.body().get("error")).get("code"));
        assertEquals(400, get("/api/runs/" + runId + "/events?after=x").status());
    }

    @Test
    void everyEventTypeProjectsToTheOutwardContract() throws Exception {
        GameServer wide = GameServer.create(new GameService(MissionLibrary.loadBundled(), 10_000), "127.0.0.1", 0);
        wide.start();
        try {
            HttpResponse<String> created = client.send(HttpRequest.newBuilder(
                            URI.create("http://127.0.0.1:" + wide.port() + "/api/missions/the-relay/runs"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(installations(
                            "equipment.cutter", "equipment.assembler", "equipment.inspector", "equipment.flex-cell")))
                    .build(), HttpResponse.BodyHandlers.ofString());
            String runId = (String) Json.asObject(Json.parse(created.body()), "$").get("runId");
            String base = "http://127.0.0.1:" + wide.port() + "/api/runs/" + runId;
            client.send(HttpRequest.newBuilder(URI.create(base + "/advance"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"maxEvents\":1000}")).build(),
                    HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> events = client.send(HttpRequest.newBuilder(
                    URI.create(base + "/events?after=0&limit=500")).build(), HttpResponse.BodyHandlers.ofString());
            String text = events.body();
            for (String type : List.of("ORDER_ACCEPTED", "JOB_DISPATCHED", "JOB_WAITING", "JOB_STEP_COMPLETED",
                    "ORDER_COMPLETED")) {
                assertTrue(text.contains("\"type\":\"" + type + "\""), type);
            }
            assertTrue(text.contains("\"kind\":\"order\"") && text.contains("\"kind\":\"job\""));
        } finally {
            wide.stop();
        }
    }

    @Test
    void resetStartsAFreshRunWhileTheOldRunStaysReadable() throws Exception {
        String runId = startRelayRun();
        post("/api/runs/" + runId + "/step", "{\"count\":4}");
        Reply fresh = post("/api/runs/" + runId + "/reset", "{}");
        assertEquals(201, fresh.status());
        assertNotEquals(runId, fresh.body().get("runId"));
        assertEquals("0", fresh.body().get("time"));
        assertEquals(get("/api/runs/" + runId).body().get("modelFingerprint"), fresh.body().get("modelFingerprint"));
        assertNotEquals("0", get("/api/runs/" + runId).body().get("time"));
    }

    @Test
    void unrunnableDesignsAreRefusedWithBothReports() throws Exception {
        Reply refused = post("/api/missions/the-relay/runs", installations("equipment.cutter"));
        assertEquals(422, refused.status());
        assertEquals("design.not-runnable", map(refused.body().get("error")).get("code"));
        assertEquals(false, map(refused.body().get("design")).get("runnable"));
    }

    @Test
    void malformedAndHostileRequestsAreRefusedWithStructuredErrors() throws Exception {
        assertEquals(400, post("/api/missions/the-relay/check", "{not json").status());
        assertEquals(400, post("/api/missions/the-relay/check", "{\"installations\":[{\"number\":-1}]}").status());
        assertEquals(400, post("/api/missions/the-relay/check", "{\"installations\":{}}").status());
        assertEquals(415, send("POST", "/api/missions/the-relay/check", "text/plain", "{}").status());
        assertEquals(415, send("POST", "/api/missions/the-relay/check", null, "{}").status());
        String huge = "{\"installations\":[],\"pad\":\"" + "x".repeat(GameServer.MAX_BODY_BYTES) + "\"}";
        assertEquals(413, post("/api/missions/the-relay/check", huge).status());
        assertEquals(404, get("/api/missions/nope").status());
        assertEquals(404, get("/api/nothing").status());
        assertEquals(404, get("/api/runs/00000000-0000-0000-0000-000000000000").status());
        assertEquals(404, get("/api/runs/not-a-run").status());
        assertEquals(405, post("/api/missions", "{}").status());
        assertEquals(405, get("/api/missions/the-relay/check").status());
        String runId = startRelayRun();
        assertEquals(400, post("/api/runs/" + runId + "/step", "{\"count\":0}").status());
        assertEquals(400, post("/api/runs/" + runId + "/step", "{\"count\":100000}").status());
        assertEquals(400, post("/api/runs/" + runId + "/advance", "{\"maxEvents\":1,\"untilTick\":\"-1\"}").status());
        assertEquals(405, get("/api/runs/" + runId + "/step").status());
        assertEquals(405, get("/api/runs/" + runId + "/reset").status());
        assertEquals(405, post("/api/runs/" + runId + "/events", "{}").status());
        assertEquals("request.malformed", map(post("/api/missions/the-relay/check", "{").body().get("error")).get("code"));
    }

    @Test
    void anExplicitTickBoundNeverMovesTheClockPastTheNextEvent() throws Exception {
        String runId = startRelayRun();
        Map<String, Object> bounded = post("/api/runs/" + runId + "/advance", "{\"maxEvents\":1000,\"untilTick\":\"2\"}").body();
        assertEquals("0", bounded.get("time"));
        Map<String, Object> next = post("/api/runs/" + runId + "/advance", "{\"maxEvents\":1000,\"untilTick\":3}").body();
        assertEquals("3", next.get("time"));
    }

    @Test
    void servesTheBundledClientWithoutPathTraversal() throws Exception {
        Reply index = get("/");
        assertEquals(200, index.status());
        assertTrue(index.raw().headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
        assertTrue(index.raw().body().contains("Last Light Foundry"));
        assertEquals("text/javascript; charset=utf-8",
                get("/app.js").raw().headers().firstValue("Content-Type").orElse(""));
        assertEquals("text/css; charset=utf-8",
                get("/styles.css").raw().headers().firstValue("Content-Type").orElse(""));
        assertEquals(404, get("/missing.js").status());
        assertEquals(404, get("/..%2Fmissions%2Fthe-relay%2Fmission.json").status());
        assertEquals(404, get("/missions/the-relay/mission.json").status());
        assertEquals(404, get("/.hidden").status());
        assertEquals(405, post("/", "{}").status());
    }

    @Test
    void theLauncherEntryPointStartsAServerOnTheRequestedPort() throws Exception {
        GameServer launched = Main.run("127.0.0.1", 0);
        try {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:" + launched.port() + "/api/missions")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
        } finally {
            launched.stop();
        }
    }

    @Test
    void concurrentCommandsOnOneRunAreSerialized() throws Exception {
        String runId = startRelayRun();
        List<Thread> threads = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Thread thread = new Thread(() -> {
                try {
                    post("/api/runs/" + runId + "/step", "{\"count\":10}");
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        Map<String, Object> run = get("/api/runs/" + runId).body();
        assertEquals("COMPLETED", run.get("status"));
        assertEquals("56", run.get("time"));
        // 12 jobs x 3 steps = 36 scheduler events, each applied exactly once.
        assertEquals("36", run.get("processedEvents"));
    }
}

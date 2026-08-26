package com.arcogine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

/**
 * Ported from crates/sim-api/tests/api_smoke.rs and the inline SSE tests in
 * crates/sim-api/src/sse.rs. Exercises the full HTTP contract against a booted
 * Spring Boot context, so this doubles as the integration/e2e check.
 *
 * <p>Each Rust test built a fresh {@code create_app_state()}; the Java
 * {@code SimThread} bean is a context singleton, so {@link DirtiesContext}
 * rebuilds the context (and its sim thread) after every test to preserve the
 * same isolation — important for the "without scenario" conflict cases.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiSmokeTest {

    @Value("${local.server.port}")
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUpClient() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    private static final String BASIC_SCENARIO_TOML =
            """
            [simulation]
            rng_seed = 42
            max_ticks = 100
            demand_eval_interval = 10

            [[equipment]]
            id = 1
            name = "Mill"

            [[material]]
            id = 1
            name = "Widget"
            routing_id = 1

            [[process_segment]]
            id = 1
            name = "Milling"
            equipment_id = 1
            duration = 5

            [[operations_definition]]
            id = 1
            name = "Widget routing"
            steps = [1]

            [economy]
            initial_price = 10.0
            base_demand = 3.0
            price_elasticity = 0.3
            lead_time_sensitivity = 0.0
            """;

    private WebTestClient longClient() {
        // SSE / run endpoints can block; widen the response timeout from the default 5s.
        return client.mutate().responseTimeout(Duration.ofSeconds(30)).build();
    }

    private EntityExchangeResult<JsonNode> loadScenario(String toml) {
        return client.post()
                .uri("/api/scenario")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"toml\":" + jsonString(toml) + "}")
                .exchange()
                .expectBody(JsonNode.class)
                .returnResult();
    }

    private static String jsonString(String raw) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    private JsonNode snapshot() {
        return client.get()
                .uri("/api/snapshot")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- Happy-path tests ---

    @Test
    void healthEndpointReturnsOk() {
        client.get()
                .uri("/api/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("ok");
    }

    @Test
    void loadScenarioSucceeds() {
        EntityExchangeResult<JsonNode> result = loadScenario(BASIC_SCENARIO_TOML);
        assertEquals(HttpStatus.OK, result.getStatus());
    }

    @Test
    void loadValidScenarioReturnsSuccess() {
        EntityExchangeResult<JsonNode> result = loadScenario(BASIC_SCENARIO_TOML);
        assertEquals(HttpStatus.OK, result.getStatus());
        JsonNode body = result.getResponseBody();
        assertNotNull(body);
        assertTrue(body.path("success").asBoolean());
    }

    @Test
    void stepAfterLoadReturnsUpdatedState() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post().uri("/api/sim/step").exchange().expectStatus().isOk();

        // The step handler runs on the sim thread; poll the snapshot for progress.
        for (int i = 0; i < 20; i++) {
            JsonNode snap = snapshot();
            if (snap.path("events_processed").asLong() > 0) {
                return;
            }
            sleepQuietly(50);
        }
        fail("step should process at least one event within timeout");
    }

    @Test
    void runAndQueryKpis() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        longClient().post().uri("/api/sim/run").exchange().expectStatus().isOk();
        sleepQuietly(100);

        JsonNode kpis = client.get()
                .uri("/api/kpis")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(kpis);
        assertTrue(kpis.isArray() && kpis.size() > 0, "KPIs should be returned");
    }

    @Test
    void changePriceReturnsUpdatedSnapshot() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"price\":15.0}")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.current_price")
                .isEqualTo(15.0);
    }

    @Test
    void topologyReturnsMachines() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        JsonNode body = client.get()
                .uri("/api/factory/topology")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(body);
        assertTrue(body.path("machines").isArray() && body.path("machines").size() > 0,
                "topology should contain machines");
    }

    @Test
    void topologyWithMultiStepRoutingReturnsEdges() {
        String multiStepToml =
                """
                [simulation]
                rng_seed = 42
                max_ticks = 100
                demand_eval_interval = 10

                [[equipment]]
                id = 1
                name = "Mill"

                [[equipment]]
                id = 2
                name = "Lathe"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[process_segment]]
                id = 2
                name = "Turning"
                equipment_id = 2
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1, 2]

                [economy]
                initial_price = 10.0
                base_demand = 3.0
                price_elasticity = 0.3
                lead_time_sensitivity = 0.0
                """;

        loadScenario(multiStepToml);
        sleepQuietly(100);

        JsonNode body = client.get()
                .uri("/api/factory/topology")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(body);
        assertTrue(body.path("edges").isArray() && body.path("edges").size() > 0,
                "topology should contain routing edges for a multi-step routing");
        JsonNode edge = body.path("edges").get(0);
        assertEquals(1L, edge.path("from_machine_id").asLong());
        assertEquals(2L, edge.path("to_machine_id").asLong());
    }

    @Test
    void exportEventsReturnsLog() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        for (int i = 0; i < 3; i++) {
            client.post().uri("/api/sim/step").exchange().expectStatus().isOk();
            sleepQuietly(50);
        }

        client.get().uri("/api/export/events").exchange().expectStatus().isOk();
    }

    @Test
    void pauseResumeStepSequence() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post().uri("/api/sim/pause").exchange().expectStatus().isOk();
        client.post().uri("/api/sim/step").exchange().expectStatus().isOk();

        for (int i = 0; i < 20; i++) {
            JsonNode snap = snapshot();
            if (snap.path("events_processed").asLong() >= 1) {
                return;
            }
            sleepQuietly(50);
        }
        fail("step should process at least 1 event within timeout");
    }

    @Test
    void runToCompletionReturnsFinalSnapshot() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        longClient().post().uri("/api/sim/run").exchange().expectStatus().isOk();

        for (int i = 0; i < 40; i++) {
            JsonNode snap = snapshot();
            if ("Completed".equals(snap.path("run_state").asString())) {
                assertTrue(snap.path("events_processed").asLong() > 0);
                return;
            }
            sleepQuietly(50);
        }
        fail("simulation should reach Completed state within timeout");
    }

    @Test
    void queryJobsReturnsList() {
        String highDemandToml =
                """
                [simulation]
                rng_seed = 42
                max_ticks = 200
                demand_eval_interval = 10

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1]

                [economy]
                initial_price = 5.0
                base_demand = 10.0
                price_elasticity = 0.3
                lead_time_sensitivity = 0.0
                """;

        loadScenario(highDemandToml);
        sleepQuietly(100);

        longClient().post().uri("/api/sim/run").exchange().expectStatus().isOk();

        for (int i = 0; i < 40; i++) {
            if ("Completed".equals(snapshot().path("run_state").asString())) {
                break;
            }
            sleepQuietly(50);
        }

        JsonNode jobs = client.get()
                .uri("/api/jobs")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(jobs);
        assertTrue(jobs.isArray() && jobs.size() > 0, "jobs should be populated after a run");
    }

    @Test
    void toggleAgentOnOff() {
        client.post()
                .uri("/api/agent")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"enabled\":true}")
                .exchange()
                .expectStatus()
                .isOk();

        client.post()
                .uri("/api/agent")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"enabled\":false}")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void sseEndpointReturnsEventStream() {
        longClient().get()
                .uri("/api/events/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }

    @Test
    void changeMachineUpdatesSnapshot() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post()
                .uri("/api/machines")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"machine_id\":1,\"online\":false}")
                .exchange()
                .expectStatus()
                .isOk();
    }

    // --- Error-path tests (F29) ---

    @Test
    void invalidTomlContentReturnsBadRequest() {
        String invalid =
                """
                [simulation]
                max_ticks = "not a number"
                """;
        EntityExchangeResult<JsonNode> result = loadScenario(invalid);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        JsonNode body = result.getResponseBody();
        assertNotNull(body);
        assertTrue(body.path("error").asString().contains("TOML parse error"),
                "error should mention TOML parse error, got: " + body.path("error"));
    }

    @Test
    void runWithoutScenarioReturnsConflict() {
        client.post().uri("/api/sim/run").exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void stepWithoutScenarioReturnsConflict() {
        client.post().uri("/api/sim/step").exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void negativePriceReturnsBadRequest() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"price\":-5.0}")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void malformedJsonReturnsError() {
        client.post()
                .uri("/api/scenario")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("not valid json")
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void priceChangeWithoutScenarioReturnsConflict() {
        client.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"price\":10.0}")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void resetAfterStepReturnsSnapshot() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post().uri("/api/sim/step").exchange().expectStatus().isOk();

        boolean stepped = false;
        for (int i = 0; i < 20; i++) {
            if (snapshot().path("events_processed").asLong() > 0) {
                stepped = true;
                break;
            }
            sleepQuietly(50);
        }
        assertTrue(stepped, "step should process at least one event before reset");

        client.post()
                .uri("/api/sim/reset")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.events_processed")
                .isEqualTo(0);
    }

    @Test
    void resetWithoutScenarioReturnsConflict() {
        client.post().uri("/api/sim/reset").exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    // --- §3.3 Handler error surfaces in snapshot ---

    @Test
    void handlerErrorSurfacesInSnapshot() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post()
                .uri("/api/machines")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"machine_id\":9999,\"online\":false}")
                .exchange()
                .expectStatus()
                .isOk();

        for (int i = 0; i < 20; i++) {
            JsonNode snap = snapshot();
            if (snap.path("last_error").isString()) {
                return;
            }
            sleepQuietly(50);
        }
        fail("snapshot should contain last_error after handler error");
    }

    // --- §3.2 Scenario load error propagation ---

    @Test
    void loadInvalidTomlReturnsBadRequest() {
        EntityExchangeResult<JsonNode> result = loadScenario("not valid [[ toml");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        JsonNode body = result.getResponseBody();
        assertNotNull(body);
        assertTrue(body.path("error").asString().contains("TOML parse error"),
                "error should mention TOML parse error, got: " + body.path("error"));
    }

    @Test
    void loadScenarioWithZeroMaxTicksReturnsBadRequest() {
        String toml =
                """
                [simulation]
                rng_seed = 42
                max_ticks = 0
                demand_eval_interval = 10

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1]

                [economy]
                initial_price = 10.0
                """;
        EntityExchangeResult<JsonNode> result = loadScenario(toml);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        JsonNode body = result.getResponseBody();
        assertNotNull(body);
        assertTrue(body.path("error").asString().contains("max_ticks"),
                "error should mention max_ticks, got: " + body.path("error"));
    }

    @Test
    void loadScenarioWithMissingEquipmentReturnsBadRequest() {
        String toml =
                """
                [simulation]
                rng_seed = 42
                max_ticks = 100
                demand_eval_interval = 10
                """;
        EntityExchangeResult<JsonNode> result = loadScenario(toml);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        JsonNode body = result.getResponseBody();
        assertNotNull(body);
        assertTrue(body.path("error").asString().contains("equipment"),
                "error should mention equipment, got: " + body.path("error"));
    }

    // --- §3.1 Body-size limit ---

    @Test
    void oversizedBodyReturnsPayloadTooLarge() {
        String oversized = "x".repeat(1024 * 1024 + 1);
        EntityExchangeResult<JsonNode> result = loadScenario(oversized);
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, result.getStatus());
    }

    @Test
    void bodyUnderLimitIsAccepted() {
        String padding = "a".repeat(500_000);
        String toml = "# " + padding + "\n" + BASIC_SCENARIO_TOML;
        EntityExchangeResult<JsonNode> result = loadScenario(toml);
        assertNotEquals(HttpStatus.CONTENT_TOO_LARGE, result.getStatus(),
                "body under 1MB should not be rejected for size");
    }

    // --- §3.9 SSE connection limit ---

    @Test
    void sseConnectionLimitReturns503() {
        WebTestClient sseClient = longClient();
        // Hold 64 connections open by subscribing to each event stream and not
        // disposing until the assertion is done — each open connection holds a
        // semaphore permit, so the 65th must be rejected with 503.
        List<Disposable> open = new ArrayList<>();
        try {
            for (int i = 0; i < 64; i++) {
                Flux<String> body = sseClient.get()
                        .uri("/api/events/stream")
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .returnResult(String.class)
                        .getResponseBody();
                open.add(body.subscribe());
            }

            sseClient.get()
                    .uri("/api/events/stream")
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        } finally {
            open.forEach(Disposable::dispose);
        }
    }

    // --- §3.11 Economy/price input validation ---

    @Test
    void extremePriceReturnsBadRequest() {
        loadScenario(BASIC_SCENARIO_TOML);
        sleepQuietly(100);

        client.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"price\":2000000.0}")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    // --- Inline sse.rs: SSE content-type ---

    @Test
    void eventStreamReturnsSseContentType() {
        longClient().get()
                .uri("/api/events/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .value("content-type", ct -> assertTrue(ct.contains("text/event-stream"), "got: " + ct));
    }

    // Sanity: a fresh GET on jobs without a scenario still returns an (empty) list.
    @Test
    void jobsWithoutScenarioReturnsEmptyArray() {
        JsonNode jobs = client.get()
                .uri("/api/jobs")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(jobs);
        assertTrue(jobs.isArray());
        assertFalse(jobs.size() > 0, "no scenario loaded yet means no jobs");
    }
}

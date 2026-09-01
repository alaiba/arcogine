package com.arcogine.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

/**
 * Ported from the CLI-parsing tests in the inline #[cfg(test)] module of
 * crates/sim-cli/src/main.rs. The Java CLI uses positional {@code mode}/{@code
 * scenarioPath} arguments plus an {@code --addr} option instead of clap
 * subcommands, so the assertions mirror the original intent against that shape.
 */
class ArcogineCommandTest {

    @Test
    void defaultBindAddressIsLocalhost() {
        ArcogineCommand command = new ArcogineCommand();
        int errors = new CommandLine(command).parseArgs("serve").errors().size();
        assertEquals(0, errors);
        assertEquals("serve", command.mode());
        assertEquals("127.0.0.1:3000", command.addr());
    }

    @Test
    void cliRunVariantParsesCorrectly() {
        ArcogineCommand command = new ArcogineCommand();
        int errors = new CommandLine(command)
                .parseArgs("run", "test.toml")
                .errors()
                .size();
        assertEquals(0, errors);
        assertEquals("run", command.mode());
        assertEquals("test.toml", command.scenarioPath());
    }

    @Test
    void cliServeWithCustomAddr() {
        ArcogineCommand command = new ArcogineCommand();
        int errors = new CommandLine(command)
                .parseArgs("serve", "--addr", "0.0.0.0:8080")
                .errors()
                .size();
        assertEquals(0, errors);
        assertEquals("serve", command.mode());
        assertEquals("0.0.0.0:8080", command.addr());
    }

    @Test
    void runModeWithoutScenarioReturnsErrorExitCode() {
        int exitCode = new CommandLine(new ArcogineCommand()).execute("run");
        assertEquals(1, exitCode);
    }

    @Test
    void serveModeCallReturnsZero() {
        ArcogineCommand command = new ArcogineCommand();
        new CommandLine(command).parseArgs("serve");
        assertEquals(0, command.call());
    }

    @Test
    void springCommandLineRunnerDoesNotExecuteTheCliMode() {
        ArcogineCommand command = new ArcogineCommand();
        command.run("run", "scenario.toml");
    }

    @Test
    void runModeWithMissingFileReturnsErrorExitCode() {
        int exitCode = new CommandLine(new ArcogineCommand())
                .execute("run", "does-not-exist.toml");
        assertEquals(1, exitCode);
    }

    @Test
    void runModeWithValidScenarioReturnsZeroAndPrintsSummary(@TempDir Path tempDir)
            throws IOException {
        Path scenario = tempDir.resolve("scenario.toml");
        Files.writeString(
                scenario,
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
                """);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        int exitCode;
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            exitCode = new CommandLine(new ArcogineCommand())
                    .execute("run", scenario.toString());
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(0, exitCode);
        String output = captured.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Simulation complete."), output);
    }
}

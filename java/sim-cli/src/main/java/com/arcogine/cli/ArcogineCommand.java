package com.arcogine.cli;

import com.arcogine.core.runner.SimResult;
import com.arcogine.core.runner.SimRunner;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.types.SimError;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication(scanBasePackages = "com.arcogine")
@CommandLine.Command(name = "arcogine", mixinStandardHelpOptions = true)
public class ArcogineCommand implements Callable<Integer>, CommandLineRunner {

    @CommandLine.Parameters(index = "0", defaultValue = "serve", description = "Mode: serve or run")
    private String mode;

    @CommandLine.Parameters(index = "1", defaultValue = "", description = "Scenario file path (for run mode)")
    private String scenarioPath;

    @CommandLine.Option(names = "--addr", defaultValue = "127.0.0.1:3000", description = "Bind address for serve mode")
    private String addr;

    String mode() {
        return mode;
    }

    String scenarioPath() {
        return scenarioPath;
    }

    String addr() {
        return addr;
    }

    public static void main(String[] args) {
        if (args.length > 0 && "run".equals(args[0])) {
            int exitCode = new CommandLine(new ArcogineCommand()).execute(args);
            System.exit(exitCode);
        }

        ArcogineCommand command = new ArcogineCommand();
        CommandLine cmdLine = new CommandLine(command);
        cmdLine.setUnmatchedArgumentsAllowed(true);
        cmdLine.parseArgs(args);

        String[] springArgs = withServerProperties(args, command.addr);
        SpringApplication.run(ArcogineCommand.class, springArgs);
    }

    @Override
    public Integer call() {
        if ("run".equals(mode)) {
            return runHeadless();
        }
        return 0;
    }

    @Override
    public void run(String... args) {
    }

    private int runHeadless() {
        if (scenarioPath == null || scenarioPath.isBlank()) {
            System.err.println("Error: scenario file path required for run mode");
            return 1;
        }

        try {
            ScenarioConfig config = ScenarioLoader.loadScenarioFile(scenarioPath);
            HeadlessHandler handler = HeadlessHandler.fromConfig(config);
            SimResult result = SimRunner.runScenario(config, handler);

            System.out.println("Simulation complete.");
            System.out.println("  Final time: " + result.finalTime().ticks() + " ticks");
            System.out.println("  Events processed: " + result.eventsProcessed());
            System.out.println("  Completed sales: " + handler.factory.completedSales);
            System.out.println("  Completed sales value: " + handler.factory.completedSalesValue);
            return 0;
        } catch (SimError e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private static String[] withServerProperties(String[] args, String addr) {
        String[] hostPort = addr.split(":", 2);
        List<String> springArgs = new ArrayList<>(Arrays.asList(args));
        springArgs.add("--server.address=" + hostPort[0]);
        springArgs.add("--server.port=" + (hostPort.length > 1 ? hostPort[1] : "3000"));
        return springArgs.toArray(new String[0]);
    }
}

package com.arcogine.game.lastlightfoundry;

import com.arcogine.game.lastlightfoundry.content.MissionLibrary;
import com.arcogine.game.lastlightfoundry.engine.GameService;
import com.arcogine.game.lastlightfoundry.web.GameServer;
import java.io.IOException;

/**
 * Starts Last Light Foundry: the Java session server and the bundled browser client, on the
 * loopback interface by default.
 *
 * <p>Configuration (environment variables): {@code LLF_HOST} (default {@code 127.0.0.1}) and
 * {@code LLF_PORT} (default {@code 8551}). Binding to a non-loopback host is an explicit opt-in;
 * the server has no authentication.
 */
public final class Main {

    private Main() {}

    /** Loads the bundled missions and starts a server on {@code host:port} (port 0 picks one). */
    public static GameServer run(String host, int port) throws IOException {
        GameServer server = GameServer.create(new GameService(MissionLibrary.loadBundled()), host, port);
        server.start();
        return server;
    }

    public static void main(String[] args) throws IOException {
        String host = System.getenv().getOrDefault("LLF_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("LLF_PORT", "8551"));
        GameServer server = run(host, port);
        System.out.println("Last Light Foundry is running at http://" + host + ":" + server.port() + "/");
        System.out.println("Press Ctrl+C to stop.");
    }
}

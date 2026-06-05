package com.arcogine.api.controller;

import com.arcogine.api.state.SimThread;
import com.arcogine.core.event.Event;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    private final SimThread simThread;
    private final Semaphore sseSemaphore = new Semaphore(64);

    public SseController(SimThread simThread) {
        this.simThread = simThread;
    }

    @GetMapping(value = "/api/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter eventStream() {
        if (!sseSemaphore.tryAcquire()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        SseEmitter emitter = new SseEmitter(0L);
        Consumer<Event> listener = event -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.eventType().name())
                        .data(event, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        };

        simThread.addEventListener(listener);

        Runnable cleanup = () -> {
            simThread.removeEventListener(listener);
            sseSemaphore.release();
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> cleanup.run());

        // Send a priming comment so the response headers flush immediately on
        // connect. Without this, a servlet SseEmitter does not commit the
        // response until the first event, so clients (and SSE tests) block
        // waiting for headers while the simulation is idle.
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}

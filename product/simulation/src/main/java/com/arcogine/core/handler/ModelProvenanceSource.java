package com.arcogine.core.handler;

/**
 * Implemented by an {@link EventHandler} that was instantiated from a published model, so {@link
 * com.arcogine.core.runner.SimRunner} can attach that provenance to the resulting {@code
 * SimResult} without depending on any specific handler implementation or domain module.
 */
public interface ModelProvenanceSource {

    /**
     * Content hash of the published model version this handler's runtime was instantiated from,
     * or {@code null} if unknown (e.g. a hand-built handler outside the canonical model boundary).
     */
    String modelContentHash();
}

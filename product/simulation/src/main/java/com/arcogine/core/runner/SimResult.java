package com.arcogine.core.runner;

import com.arcogine.core.log.EventLog;
import com.arcogine.types.SimTime;

/**
 * @param modelContentHash content hash of the published model version the simulation's handler
 *     was instantiated from (see {@link com.arcogine.core.handler.ModelProvenanceSource}), or
 *     {@code null} when the handler carries no such provenance.
 */
public record SimResult(
        SimTime finalTime, EventLog eventLog, long eventsProcessed, String modelContentHash) {}

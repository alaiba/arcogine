package com.arcogine.core.runner;

import com.arcogine.core.log.EventLog;
import com.arcogine.types.SimTime;

public record SimResult(SimTime finalTime, EventLog eventLog, long eventsProcessed) {}

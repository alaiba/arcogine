package com.arcogine.core.architecture;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Bytecode fixtures imported explicitly by guard tests; none of these methods is executed. */
public final class DeterminismGuardFixtures {
    private DeterminismGuardFixtures() {}

    public static final class LegacyDate {
        public Date read() { return new Date(); }
    }

    public static final class LegacyCalendar {
        public Calendar read() { return Calendar.getInstance(); }
    }

    public static final class CalendarSubclass {
        public Calendar read() { return new GregorianCalendar(); }
    }

    public static final class BackgroundTimer {
        public Timer start() { return new Timer(); }
    }

    public static final class ParallelStreamFactory {
        public Stream<Integer> start() {
            return StreamSupport.stream(List.of(1).spliterator(), true);
        }
    }

    public static final class SequentialCollectionStream {
        public Stream<Integer> start() { return List.of(1).stream(); }
    }
}

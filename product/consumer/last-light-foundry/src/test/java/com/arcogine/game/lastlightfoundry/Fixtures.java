package com.arcogine.game.lastlightfoundry;

import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.content.MissionLibrary;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.draft.Installation;
import com.arcogine.game.lastlightfoundry.engine.DesignChecker;
import com.arcogine.game.lastlightfoundry.engine.GameService;
import com.arcogine.game.lastlightfoundry.engine.RunDriver;
import java.util.ArrayList;
import java.util.List;

/** Shared test fixtures: the authored Relay designs whose results are derived from real engine runs. */
final class Fixtures {

    static final String CUTTER = "equipment.cutter";
    static final String ASSEMBLER = "equipment.assembler";
    static final String TWIN = "equipment.twin-assembler";
    static final String INSPECTOR = "equipment.inspector";
    static final String FLEX = "equipment.flex-cell";

    private Fixtures() {}

    static Mission relay() {
        return MissionLibrary.load("the-relay");
    }

    static Mission wake() {
        return MissionLibrary.load("wake-the-foundry");
    }

    /** Builds a draft from {@code itemId} values; installation numbers are 1..n in list order. */
    static Draft draft(String... items) {
        List<Installation> installations = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            installations.add(new Installation(i + 1, items[i], i % 12, i / 12));
        }
        return new Draft(installations);
    }

    /** One cutter, one assembler, two inspectors: 850 credits. */
    static Draft dedicated() {
        return draft(CUTTER, ASSEMBLER, INSPECTOR, INSPECTOR);
    }

    /** One cutter, one assembler, one inspector and a flex cell (numbered last): 913 credits. */
    static Draft flexLast() {
        return draft(CUTTER, ASSEMBLER, INSPECTOR, FLEX);
    }

    /** The same equipment as {@link #flexLast()} but with the flex cell numbered first. */
    static Draft flexFirst() {
        return draft(FLEX, CUTTER, ASSEMBLER, INSPECTOR);
    }

    /** One cutter, a twin assembler and two inspectors: 1,010 credits. */
    static Draft twin() {
        return draft(CUTTER, TWIN, INSPECTOR, INSPECTOR);
    }

    /** A plausible cheap single line: 600 credits, misses the deadline. */
    static Draft cheapLine() {
        return draft(CUTTER, ASSEMBLER, INSPECTOR);
    }

    static RunDriver run(Mission mission, Draft draft) {
        DesignChecker.DesignReport report = DesignChecker.check(mission, draft);
        return RunDriver.start(mission, draft, report, GameService.EVENT_RETENTION);
    }

    /** Runs to a terminal status one event at a time. */
    static RunDriver.RunView finish(RunDriver driver) {
        RunDriver.RunView view = driver.view();
        while (view.status() == RunDriver.Status.ACTIVE) {
            view = driver.step(1);
        }
        return view;
    }
}

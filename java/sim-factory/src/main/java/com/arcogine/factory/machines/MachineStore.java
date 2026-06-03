package com.arcogine.factory.machines;

import com.arcogine.types.MachineId;
import com.arcogine.types.SimError;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MachineStore {

    private final List<Machine> machines;

    public MachineStore() {
        this.machines = new ArrayList<>();
    }

    public void add(Machine machine) {
        machines.add(machine);
    }

    public Machine get(MachineId id) {
        return machines.stream()
                .filter(m -> m.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new SimError.UnknownId("machine", id.value()));
    }

    public Machine getMut(MachineId id) {
        return get(id);
    }

    public Stream<Machine> iter() {
        return machines.stream();
    }

    public List<Machine> machines() {
        return machines;
    }
}

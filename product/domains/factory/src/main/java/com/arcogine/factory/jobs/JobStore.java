package com.arcogine.factory.jobs;

import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JobStore {

    private final List<Job> jobs;
    private long nextId;

    public JobStore() {
        this.jobs = new ArrayList<>();
        this.nextId = 1;
    }

    public JobId createJob(
            ProductId productId, long quantity, int totalSteps, SimTime createdAt, double unitPrice) {
        JobId id = new JobId(nextId++);
        jobs.add(new Job(id, productId, quantity, totalSteps, createdAt, unitPrice));
        return id;
    }

    public Job get(JobId id) {
        return jobs.stream()
                .filter(j -> j.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new SimError.UnknownId("job", id.value()));
    }

    public Stream<Job> activeJobs() {
        return jobs.stream()
                .filter(j -> j.status() == JobStatus.Queued || j.status() == JobStatus.InProgress);
    }

    public Stream<Job> completedJobs() {
        return jobs.stream().filter(j -> j.status() == JobStatus.Completed);
    }

    public Stream<Job> allJobs() {
        return jobs.stream();
    }
}

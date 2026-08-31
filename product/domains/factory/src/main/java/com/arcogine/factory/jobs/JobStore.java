package com.arcogine.factory.jobs;

import com.arcogine.factory.orders.Order;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

public class JobStore {

    private final List<Job> jobs;
    private final java.util.Map<JobId, Job> byId;
    private long nextId;

    public JobStore() {
        this.jobs = new ArrayList<>();
        this.byId = new LinkedHashMap<>();
        this.nextId = 1;
    }

    public JobId createJob(Order order, long ordinalWithinOrder, int totalSteps, SimTime createdAt) {
        JobId id = new JobId(nextId++);
        Job job = new Job(id, order, ordinalWithinOrder, totalSteps, createdAt);
        jobs.add(job);
        byId.put(id, job);
        return id;
    }

    /** Compatibility helper for focused store tests; production supplies an ordinal. */
    public JobId createJob(Order order, int totalSteps, SimTime createdAt) {
        return createJob(order, 0, totalSteps, createdAt);
    }

    public Job get(JobId id) {
        Job job = byId.get(id);
        if (job == null) throw new SimError.UnknownId("job", id.value());
        return job;
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

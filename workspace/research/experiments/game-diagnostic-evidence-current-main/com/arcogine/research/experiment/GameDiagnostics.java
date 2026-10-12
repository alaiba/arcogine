package com.arcogine.research.experiment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The vocabulary of the game diagnostic-evidence investigation: a player-facing statement, what kind
 * of evidence licenses it, the attempt it describes, and the authored change set between two
 * attempts. Research custody only; none of this is a game API.
 */
final class GameDiagnostics {

    private GameDiagnostics() {}

    /** The fixed diagnostic questions of the brief, plus the factual context every candidate shows. */
    enum Question {
        Q1_WAITING,
        Q2_LIMITING_STEP,
        Q3_IDLE_RESOURCE,
        Q4_DELAY,
        Q5_CONTROLLED_COMPARISON,
        Q6_CONFOUNDED_COMPARISON,
        PROGRESS,
        ACTIVITY
    }

    /**
     * What licenses a statement, from weakest derivation to strongest.
     *
     * <ul>
     *   <li>{@code DIRECT_FACT}: one supported observation/event/published-model value restated.
     *   <li>{@code BOUNDARY_COUNT}: a count or grouping of direct facts at one observation, grouped by
     *       published-model facts; no interval, normalization or population choice.
     *   <li>{@code EVENT_INTERVAL}: the time between two supported events of one job-step occurrence,
     *       or the exact union of such intervals; no normalization.
     *   <li>{@code AGGREGATE_MEASUREMENT}: a sum or ratio over many occurrences with a stated basis.
     *   <li>{@code NAMED_INTERPRETATION}: a verdict produced by a named method.
     *   <li>{@code COMPARISON}: two attempts' authored change set beside their outcome facts.
     *   <li>{@code REFUSAL}: an explicit statement that the evidence does not license an answer.
     * </ul>
     */
    enum Kind {
        DIRECT_FACT,
        BOUNDARY_COUNT,
        EVENT_INTERVAL,
        AGGREGATE_MEASUREMENT,
        NAMED_INTERPRETATION,
        COMPARISON,
        REFUSAL
    }

    /** The evidence a statement cites: observation labels, supported-event sequences, authored design facts, fields. */
    record Support(List<String> observations, List<Long> events, List<String> designFacts, List<String> fields) {

        Support {
            observations = List.copyOf(observations);
            events = List.copyOf(events);
            designFacts = List.copyOf(designFacts);
            fields = List.copyOf(fields);
        }

        static Support none() {
            return new Support(List.of(), List.of(), List.of(), List.of());
        }

        static Support observation(String label, String... fields) {
            return new Support(List.of(label), List.of(), List.of(), List.of(fields));
        }

        static Support events(List<Long> sequences, String... fields) {
            return new Support(List.of(), sequences, List.of(), List.of(fields));
        }

        static Support design(String... facts) {
            return new Support(List.of(), List.of(), List.of(facts), List.of());
        }

        Support plus(Support other) {
            List<String> obs = new ArrayList<>(observations);
            other.observations.stream().filter(o -> !obs.contains(o)).forEach(obs::add);
            List<Long> ev = new ArrayList<>(events);
            other.events.stream().filter(e -> !ev.contains(e)).forEach(ev::add);
            List<String> design = new ArrayList<>(designFacts);
            other.designFacts.stream().filter(d -> !design.contains(d)).forEach(design::add);
            List<String> f = new ArrayList<>(fields);
            other.fields.stream().filter(x -> !f.contains(x)).forEach(f::add);
            return new Support(obs, ev.stream().sorted().toList(), design, f);
        }

        boolean isEmpty() {
            return observations.isEmpty() && events.isEmpty() && designFacts.isEmpty();
        }

        String summary() {
            List<String> parts = new ArrayList<>();
            if (!observations.isEmpty()) {
                parts.add("obs " + String.join(",", observations));
            }
            if (!events.isEmpty()) {
                parts.add("events " + compactRanges(events));
            }
            if (!designFacts.isEmpty()) {
                parts.add("design " + String.join("; ", designFacts));
            }
            if (!fields.isEmpty()) {
                parts.add("fields " + String.join(",", fields));
            }
            return String.join(" | ", parts);
        }
    }

    /** One player-facing statement and everything needed to audit it. */
    record Statement(
            Question question,
            Kind kind,
            String subject,
            String text,
            Optional<String> method,
            Support support,
            Map<String, String> facets) {

        Statement {
            Objects.requireNonNull(question, "question");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(subject, "subject");
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(method, "method");
            Objects.requireNonNull(support, "support");
            facets = Map.copyOf(facets);
        }

        static Builder of(Question question, Kind kind, String subject, String text) {
            return new Builder(question, kind, subject, text);
        }

        Optional<String> facet(String name) {
            return Optional.ofNullable(facets.get(name));
        }

        static final class Builder {
            private final Question question;
            private final Kind kind;
            private final String subject;
            private final String text;
            private Optional<String> method = Optional.empty();
            private Support support = Support.none();
            private final Map<String, String> facets = new LinkedHashMap<>();

            private Builder(Question question, Kind kind, String subject, String text) {
                this.question = question;
                this.kind = kind;
                this.subject = subject;
                this.text = text;
            }

            Builder method(String name) {
                this.method = Optional.of(name);
                return this;
            }

            Builder support(Support support) {
                this.support = support;
                return this;
            }

            Builder facet(String name, Object value) {
                facets.put(name, String.valueOf(value));
                return this;
            }

            Statement build() {
                return new Statement(question, kind, subject, text, method, support, facets);
            }
        }
    }

    /**
     * One completed attempt as a candidate contract may see it: the game-authored design (a draft
     * snapshot projected in an explicit resource order) and the supported evidence its run produced.
     */
    record Attempt(String label, LinearRoutingFamily design, ExperimentEvidence evidence, Optional<String> midRunLabel) {}

    /** One authored difference between two designs. */
    record Change(ChangeType type, String resource, String detail) {

        String describe() {
            return switch (type) {
                case ADDED -> "added " + resource + " (" + detail + ")";
                case REMOVED -> "removed " + resource + " (" + detail + ")";
                case CHANGED -> "changed " + resource + " (" + detail + ")";
                case ORDER -> "resource order " + detail;
            };
        }
    }

    enum ChangeType {
        ADDED,
        REMOVED,
        CHANGED,
        ORDER
    }

    /**
     * The authored change set from {@code first} to {@code second}, computed from game-owned design
     * facts alone. Resource identity is result-affecting under the current Engine (a remaining tie is
     * broken by the lower identifier), so a change in the identity or relative order of resources both
     * designs contain is itself a change.
     */
    static List<Change> changeSet(LinearRoutingFamily first, LinearRoutingFamily second) {
        Map<String, LinearRoutingFamily.Resource> before = byName(first);
        Map<String, LinearRoutingFamily.Resource> after = byName(second);
        List<Change> changes = new ArrayList<>();
        before.forEach((name, resource) -> {
            if (!after.containsKey(name)) {
                changes.add(new Change(ChangeType.REMOVED, name, describe(resource)));
            } else if (!after.get(name).equals(resource)) {
                changes.add(new Change(ChangeType.CHANGED, name, describe(resource) + " -> " + describe(after.get(name))));
            }
        });
        after.forEach((name, resource) -> {
            if (!before.containsKey(name)) {
                changes.add(new Change(ChangeType.ADDED, name, describe(resource)));
            }
        });
        boolean identityChanged = before.keySet().stream()
                .filter(after::containsKey)
                .anyMatch(name -> !first.resourceId(name).equals(second.resourceId(name)));
        if (identityChanged) {
            changes.add(new Change(ChangeType.ORDER, "",
                    order(first) + " -> " + order(second)));
        }
        return List.copyOf(changes);
    }

    static String order(LinearRoutingFamily design) {
        return design.resources().stream().map(LinearRoutingFamily.Resource::name).collect(Collectors.joining(", ", "[", "]"));
    }

    static String describe(LinearRoutingFamily.Resource resource) {
        String steps = resource.eligibleSteps().stream().sorted().collect(Collectors.joining("+"));
        return resource.concurrency() == 1 ? steps : steps + ", " + resource.concurrency() + " slots";
    }

    private static Map<String, LinearRoutingFamily.Resource> byName(LinearRoutingFamily design) {
        Map<String, LinearRoutingFamily.Resource> resources = new LinkedHashMap<>();
        design.resources().forEach(resource -> resources.put(resource.name(), resource));
        return resources;
    }

    /** A candidate player-facing evidence contract. */
    interface Contract {

        String id();

        String summary();

        List<Statement> attempt(Attempt attempt);

        List<Statement> compare(Attempt first, Attempt second);
    }

    static String compactRanges(List<Long> values) {
        List<Long> sorted = values.stream().distinct().sorted().toList();
        List<String> parts = new ArrayList<>();
        int i = 0;
        while (i < sorted.size()) {
            int j = i;
            while (j + 1 < sorted.size() && sorted.get(j + 1) == sorted.get(j) + 1) {
                j++;
            }
            parts.add(i == j ? String.valueOf(sorted.get(i)) : sorted.get(i) + "-" + sorted.get(j));
            i = j + 1;
        }
        return String.join(",", parts);
    }
}

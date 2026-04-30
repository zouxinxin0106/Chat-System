package com.chat.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

public class MetricRegistry {
    private static final MetricRegistry INSTANCE = new MetricRegistry();
    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    private MetricRegistry() {
        this.registry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
    }

    public static MetricRegistry getInstance() {
        return INSTANCE;
    }

    public Counter counter(String name, Tags tags) {
        String key = name + tags.toString();
        return counters.computeIfAbsent(key, k -> Counter.builder(name).tags(tags).register(registry));
    }

    public Timer timer(String name, Tags tags) {
        String key = name + tags.toString();
        return timers.computeIfAbsent(key, k -> Timer.builder(name).tags(tags).register(registry));
    }

    public <T> void gauge(String name, T obj, ToDoubleFunction<T> function, Tags tags) {
        registry.gauge(name, Tags.concat(tags), obj, function);
    }

    public MeterRegistry getMeterRegistry() {
        return registry;
    }

    public Counter messagesSentTotal() {
        return counter("messages_sent_total", Tags.empty());
    }

    public Counter messagesDeliveredTotal() {
        return counter("messages_delivered_total", Tags.empty());
    }

    public Counter connectionsCurrent() {
        return counter("connections_current", Tags.empty());
    }

    public Timer pipelineLatencySeconds() {
        return timer("pipeline_latency_seconds", Tags.empty());
    }

    public Timer grpcPushLatencySeconds() {
        return timer("gRPC_push_latency_seconds", Tags.empty());
    }

    public Counter grpcPushErrorsTotal() {
        return counter("gRPC_push_errors_total", Tags.empty());
    }
}

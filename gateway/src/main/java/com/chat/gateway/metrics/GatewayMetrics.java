package com.chat.gateway.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GatewayMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger currentConnections;
    private final Counter connectionClosesCounter;
    private final Counter heartbeatTimeoutCounter;
    private final Timer grpcPushLatencyTimer;
    private final Counter grpcPushErrorsCounter;

    private static volatile GatewayMetrics instance;

    private GatewayMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.currentConnections = new AtomicInteger(0);
        this.connectionClosesCounter = Counter.builder("gateway_connections_closes_total")
                .description("Total number of connection closes")
                .register(meterRegistry);
        this.heartbeatTimeoutCounter = Counter.builder("gateway_heartbeat_timeout_total")
                .description("Total number of heartbeat timeouts")
                .register(meterRegistry);
        this.grpcPushLatencyTimer = Timer.builder("gateway_grpc_push_latency_seconds")
                .description("gRPC push latency in seconds")
                .register(meterRegistry);
        this.grpcPushErrorsCounter = Counter.builder("gateway_grpc_push_errors_total")
                .description("Total number of gRPC push errors")
                .register(meterRegistry);

        Gauge.builder("gateway_connections_current", currentConnections, AtomicInteger::get)
                .description("Current number of active connections")
                .register(meterRegistry);
    }

    public static GatewayMetrics getInstance() {
        if (instance == null) {
            synchronized (GatewayMetrics.class) {
                if (instance == null) {
                    instance = new GatewayMetrics(new SimpleMeterRegistry());
                }
            }
        }
        return instance;
    }

    public static GatewayMetrics getInstance(MeterRegistry meterRegistry) {
        if (instance == null) {
            synchronized (GatewayMetrics.class) {
                if (instance == null) {
                    instance = new GatewayMetrics(meterRegistry);
                }
            }
        }
        return instance;
    }

    public void incrementConnections() {
        currentConnections.incrementAndGet();
    }

    public void decrementConnections() {
        currentConnections.decrementAndGet();
        connectionClosesCounter.increment();
    }

    public void incrementHeartbeatTimeouts() {
        heartbeatTimeoutCounter.increment();
    }

    public Timer.Sample startGrpcPushTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordGrpcPushLatency(Timer.Sample sample) {
        sample.stop(grpcPushLatencyTimer);
    }

    public void incrementGrpcPushErrors() {
        grpcPushErrorsCounter.increment();
    }

    public int getCurrentConnections() {
        return currentConnections.get();
    }
}

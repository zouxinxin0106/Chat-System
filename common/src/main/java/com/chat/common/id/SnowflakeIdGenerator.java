package com.chat.common.id;

import java.util.concurrent.atomic.AtomicLong;

public class SnowflakeIdGenerator {
    private static final long EPOCH = 1704067200000L;
    private static final long MACHINE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

    private final long machineId;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile long lastTimestamp = -1;

    public SnowflakeIdGenerator(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
        this.machineId = machineId;
    }

    public long nextId() {
        long timestamp = System.currentTimeMillis() - EPOCH;
        synchronized (this) {
            if (timestamp < lastTimestamp) {
                timestamp = lastTimestamp;
            }
            if (timestamp == lastTimestamp) {
                long seq = sequence.incrementAndGet() & MAX_SEQUENCE;
                if (seq == 0) {
                    timestamp = waitNextMillis(timestamp);
                }
            } else {
                sequence.set(0);
            }
            lastTimestamp = timestamp;
            return (timestamp << TIMESTAMP_SHIFT) | (machineId << MACHINE_ID_SHIFT) | sequence.get();
        }
    }

    private long waitNextMillis(long currentTimestamp) {
        while (System.currentTimeMillis() - EPOCH <= currentTimestamp) {
            Thread.yield();
        }
        return System.currentTimeMillis() - EPOCH;
    }

    public static long parseTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT) + EPOCH;
    }
}

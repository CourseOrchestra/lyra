package ru.curs.lyra.kernel;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Task for asynchronous grid refinement.
 */
public final class RefinementTask implements Delayed {
    /**
     * Sequence number to break scheduling ties, and in turn to
     * guarantee FIFO order among tied entries.
     */
    private static final AtomicLong SEQUENCER = new AtomicLong();
    private static final long MILLION = 1_000_000L;

    private final long time;

    private final BigInteger key;

    /**
     * Sequence number to break ties FIFO
     */
    private final long sequenceNumber;

    private final boolean immediate;


    public RefinementTask(BigInteger key, long delayMs) {
        this.immediate = delayMs <= 0;
        this.time = now() + delayMs * MILLION;
        this.sequenceNumber = SEQUENCER.getAndIncrement();
        this.key = key;
    }

    private long now() {
        return System.nanoTime();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(time - now(), NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this) {// compare zero if same object
            return 0;
        }
        if (other instanceof RefinementTask) {
            RefinementTask x = (RefinementTask) other;
            long diff = time - x.time;
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            } else if (sequenceNumber < x.sequenceNumber) {
                return -1;
            } else {
                return 1;
            }
        }
        long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
        return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    /**
     * True for immediate (out-of-queue) tasks. Task is immediate if it was created with zero delay.
     */
    public boolean isImmediate() {
        return immediate;
    }

    /**
     * Key of the record to refine position.
     */
    public BigInteger getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefinementTask that = (RefinementTask) o;
        return time == that.time &&
                sequenceNumber == that.sequenceNumber &&
                immediate == that.immediate &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, key, sequenceNumber, immediate);
    }
}

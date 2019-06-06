package ru.curs.lyra.kernel;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RefinementSchedulerTest {
    @Test
    void refinementSchedulerWorks() throws InterruptedException {
        DummyRefinementScheduler s = new DummyRefinementScheduler();

        RefinementTask t1 = new RefinementTask(BigInteger.ZERO, 0);
        RefinementTask t2 = new RefinementTask(BigInteger.ZERO, 300);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.submit(s);
            s.ctxLatch.await();
            assertEquals(1, s.ctxCount.get());
            s.setTask(t2);
            s.setTask(t1);
            Thread.sleep(20);
            assertEquals(0, s.count.get());
            assertEquals(Collections.singletonList(t1), s.l);
            Thread.sleep(300);
            assertEquals(Arrays.asList(t1, t2), s.l);
        } finally {
            executorService.shutdownNow();

        }
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
        assertEquals(0, s.ctxCount.get());
    }

    @Test
    void refinementSchedulerRefinesInterpolatorWhileWaiting() throws InterruptedException {
        DummyRefinementScheduler s = new DummyRefinementScheduler();
        RefinementTask t1 = new RefinementTask(BigInteger.ZERO, 0);
        RefinementTask t2 = new RefinementTask(BigInteger.ZERO, 10);
        RefinementTask t3 = new RefinementTask(BigInteger.ZERO, 300);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            s.setTask(t2);
            s.setTask(t1);
            s.setTask(t3);
            executorService.submit(s);
            Thread.sleep(310);
            assertEquals(Arrays.asList(t1, t3), s.l);
            assertEquals(0, s.count.get());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void onlyLastTaskInSeriesIsExecuted() throws InterruptedException {
        DummyRefinementScheduler s = new DummyRefinementScheduler();
        for (int i = 0; i < 100; i++) {
            s.setTask(new RefinementTask(BigInteger.ZERO, 100));
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.submit(s);
            RefinementTask t = new RefinementTask(BigInteger.ZERO, 100);
            s.setTask(t);
            Thread.sleep(200);
            assertEquals(Collections.singletonList(t), s.l);
        } finally {
            executorService.shutdownNow();
        }
    }

    class DummyRefinementScheduler extends RefinementScheduler {
        List<RefinementTask> l = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(10);
        AtomicInteger ctxCount = new AtomicInteger(0);
        CountDownLatch ctxLatch = new CountDownLatch(1);

        @Override
        protected boolean refineInterpolator() {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {

            }
            return count.getAndUpdate(
                    v -> v > 0 ? v - 1 : v) > 0;
        }

        @Override
        protected void refineAndNotify(RefinementTask task) {
            l.add(task);
        }

        @Override
        protected void acquireContext() {
            ctxCount.incrementAndGet();
            ctxLatch.countDown();
        }

        @Override
        protected void releaseContext() {
            assertTrue(ctxCount.decrementAndGet() >= 0);
        }
    }
}

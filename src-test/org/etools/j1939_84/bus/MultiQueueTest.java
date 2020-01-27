/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.Test;

public class MultiQueueTest {
    @Test
    public void bandwidthtest() throws InterruptedException {
        try (MultiQueue<Integer> queue = new MultiQueue<>()) {
            Stream<Integer> stream = queue.stream(1000, TimeUnit.MILLISECONDS);
            long COUNT = 10000;
            int THREADS = 25;
            ExecutorService e = Executors.newFixedThreadPool(THREADS);
            for (int thread = 0; thread < THREADS; thread++) {
                e.execute(() -> {
                    for (int i = 0; i < COUNT; i++) {
                        queue.add(i);
                    }
                });
            }
            assertEquals(COUNT * THREADS,
                    stream
                            // .peek(x -> System.err.println(x))
                            .count());
        }
    }

    @Test()
    public void simpleTest() throws BusException {
        try (MultiQueue<Integer> q = new MultiQueue<>()) {
            // Smaller timeouts are inconsistent. It looks like the JIT is sometimes
            // slow.
            Stream<Integer> stream = q.stream(300, TimeUnit.MILLISECONDS);
            Stream<Integer> stream1 = q.stream(400, TimeUnit.MILLISECONDS);
            Stream<Integer> stream3 = q.stream(400, TimeUnit.MILLISECONDS);
            Stream<Integer> streamn = q.stream(500, TimeUnit.MILLISECONDS);
            q.add(1);
            q.add(2);
            q.add(3);
            assertEquals(3, stream.count());
            assertEquals(1, (int) stream1.findFirst().get());
            assertEquals(3, (int) stream3.skip(2).findFirst().get());
            assertFalse(streamn.skip(3).findFirst().isPresent());
        }
    }

    @Test
    public void testInterruption() {
        try (MultiQueue<Integer> q = new MultiQueue<>()) {
            Stream<Integer> s = q.stream(5, TimeUnit.SECONDS);
            for (int i = 0; i < 100; i++) {
                q.add(i);
            }
            assertEquals(50, s.filter(MultiQueue.interruptFilter(v -> v >= 50)).count());
        }
    }

    @Test
    public void testTimedInterruption() throws Exception {
        try (MultiQueue<Integer> q = new MultiQueue<>()) {
            Stream<Integer> s1 = q.stream(200, TimeUnit.MILLISECONDS);
            Stream<Integer> s2 = q.stream(400, TimeUnit.MILLISECONDS);
            new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    q.add(i);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
            long s1Count = s1.count();
            assertTrue("s1Count is " + s1Count, s1Count > 15 && s1Count < 21);
            long s2Count = s2.count();
            assertTrue("s2Count is " + s2Count, s2Count > 30 && s2Count < 42);
        }
    }
}

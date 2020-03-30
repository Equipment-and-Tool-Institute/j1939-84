/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.Test;

public class MultiQueueTest {
    /** Add 10000 items from 25 different threads in less than 1 s */
    @Test(timeout = 1000)
    public void bandwidthTest() throws InterruptedException {
        try (MultiQueue<Integer> queue = new MultiQueue<>()) {
            long COUNT = 10000;
            int THREADS = 25;

            Stream<Integer> stream = queue.stream(10, TimeUnit.SECONDS).limit(COUNT * THREADS);
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

    /** Verify that building a stream with a timeout works. */
    @Test
    public void testTimedInterruption() throws Exception {
        try (MultiQueue<Integer> q = new MultiQueue<>()) {
            Stream<Integer> s1 = q.stream(200, TimeUnit.MILLISECONDS);
            Stream<Integer> s2 = q.stream(400, TimeUnit.MILLISECONDS);
            // asynchronously add a packet every 10 ms.
            new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    q.add(i);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
            // verify that roughly >= 20 packets were processed in 200 ms
            assertEquals(22, s1.count(), 3);
            // verify that roughly >= 40 packets were processed in 200 ms
            assertEquals(42, s2.count(), 3);
        }
    }
}

/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.etools.testdoc.TestDoc;
import org.junit.Test;

@TestDoc(description = "Verifies queuing system works as expected.")
public class MultiQueueTest {
    /** Add 10000 items from 25 different threads in less than 1 s */
    @Test(timeout = 1000)
    @TestDoc(description = "Verifies that 10,000 items can be added from 25 threads and all 250,000 items are read by one thread in less than 1 s.")
    public void bandwidthTest() {
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
    @TestDoc(description = "Verifies that the streams generated from the MultiQueue can correctly support multiple items, findFirst(), skip() and an empty stream.")
    public void simpleTest() throws BusException {
        try (MultiQueue<Integer> q = new MultiQueue<>()) {
            // Smaller timeouts are inconsistent. It looks like the JIT is sometimes
            // slow.
            Stream<Integer> stream = q.stream(30, TimeUnit.MILLISECONDS);
            Stream<Integer> stream1 = q.stream(40, TimeUnit.MILLISECONDS);
            Stream<Integer> stream3 = q.stream(40, TimeUnit.MILLISECONDS);
            Stream<Integer> streamn = q.stream(50, TimeUnit.MILLISECONDS);
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
    @TestDoc(description = "Verify that closed streams are empty.")
    public void testClose() {
        try (MultiQueue<Integer> queue = new MultiQueue<>()) {
            Stream<Integer> stream1 = queue.stream(10, TimeUnit.MILLISECONDS);
            Stream<Integer> stream2 = queue.stream(10, TimeUnit.MILLISECONDS);

            queue.add(1);
            queue.add(2);
            assertEquals(2, stream1.count());
            queue.close();
            assertEquals(0, stream2.count());
        }
    }

    @Test
    @TestDoc(description = "Verify that duplicate streams are of the same size.")
    public void testDuplicate() throws Exception {
        try (MultiQueue<Integer> queue = new MultiQueue<>()) {
            Stream<Integer> stream1 = queue.stream(10, TimeUnit.MILLISECONDS);
            queue.add(1);
            Stream<Integer> stream2 = queue.duplicate(stream1, 20, TimeUnit.MILLISECONDS);
            queue.add(2);
            Stream<Integer> stream3 = queue.duplicate(stream1, 30, TimeUnit.MILLISECONDS);

            queue.add(3);
            assertEquals(3, stream1.count());
            assertEquals(3, stream2.count());
            assertEquals(3, stream3.count());
        }
    }

    @Test(expected = IllegalStateException.class)
    @TestDoc(description = "Verify that closed streams can not be duplicated.")
    public void testDuplicateOnClosedStream() throws Exception {
        try (MultiQueue<Integer> queue = new MultiQueue<>()) {
            Stream<Integer> stream1 = queue.stream(10, TimeUnit.MILLISECONDS);
            queue.add(1);
            queue.add(2);
            queue.add(3);
            assertEquals(3, stream1.count());
            Stream<Integer> stream4 = queue.duplicate(stream1, 10, TimeUnit.MILLISECONDS);
            assertEquals(0, stream4.count());
        }
    }

    @Test
    @TestDoc(description = "Verify that duplicating paritally consumed streams results in streams that are smaller than the original.")
    public void testDuplicateOpen() throws Exception {
        try (MultiQueue<Integer> queue = new MultiQueue<>()) {
            Stream<Integer> stream1 = queue.stream(50, TimeUnit.MILLISECONDS);
            queue.add(3);
            queue.add(2);
            queue.add(1);
            assertEquals(3,
                         stream1.peek(n -> {
                             System.err.println("Verify test:" + n);
                             assertEquals(n - 1, queue.duplicate(stream1, 10, TimeUnit.MILLISECONDS).count());
                         })
                                .count());
        }
    }

    /** Verify that building a stream with a timeout works. */
    @Test
    @TestDoc(description = "Verify that timeouts interrupts streams, so that a 410 ms stream only has 410 ms of data in it.")
    public void testTimedInterruption() throws Exception {
        // sync on q, because thread startup is unpredictably slow
        try (MultiQueue<Integer> q = new MultiQueue<>()) {
            synchronized (q) {
                // asynchronously add a packet every 10 ms.
                new Thread(() -> {
                    synchronized (q) {
                        // notify main thread that we are starting.
                        q.notifyAll();
                    }
                    for (int i = 0; i < 100; i++) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignored) {
                        }
                        q.add(i);
                    }
                }).start();
                // wait on notify
                q.wait();
            }
            Stream<Integer> s1 = q.stream(410, TimeUnit.MILLISECONDS);
            Stream<Integer> s2 = q.stream(810, TimeUnit.MILLISECONDS);
            assertEquals("40 packets were processed in 400 ms", 40, s1.count(), 10);
            assertEquals("80 packets were processed in 800 ms", 80, s2.count(), 10);
        }
    }
}

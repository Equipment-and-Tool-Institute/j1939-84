/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MultiQueue<T> implements AutoCloseable {
    static private class Item<T> {
        MultiQueue.Item<T> next;
        final T value;

        Item(T v) {
            value = v;
        }

        synchronized MultiQueue.Item<T> add(T v) {
            next = new MultiQueue.Item<>(v);
            notifyAll();
            return next;
        }

        synchronized MultiQueue.Item<T> next(long end) throws TimeOutException {
            long now;
            while ((now = System.currentTimeMillis()) < end) {
                if (next != null) {
                    return next;
                }
                try {
                    // We poll here, so that we catch timeouts even without
                    // traffic.
                    wait(Math.min(5, end - now));
                } catch (InterruptedException e) {
                    throw new TimeOutException();
                }
            }

            throw new TimeOutException();
        }
    }

    private static class TimeOutException extends RuntimeException {
        private static final long serialVersionUID = -7120465100481000186L;
    }

    static public <T> Predicate<T> interruptFilter(Predicate<T> p) {
        return t -> {
            if (p.test(t)) {
                throw new TimeOutException();
            }
            return true;
        };
    }

    private boolean closed;

    private MultiQueue.Item<T> list = new MultiQueue.Item<>(null);

    synchronized public void add(T v) {
        list = list.add(v);
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }

    /**
     *
     * @param timeout
     *                The stream will be valid for a period of timeout. If the
     *                stream is not read prior to timeout, then it will be empty.
     * @param unit
     *                the TimeUnit for the timeout
     * @return the stream
     */
    public Stream<T> stream(long timeout, TimeUnit unit) {
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        return StreamSupport.stream(new Spliterator<T>() {
            Item<T> item = list;

            @Override
            public int characteristics() {
                return IMMUTABLE | ORDERED;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                try {
                    action.accept((item = item.next(end)).value);
                    return closed;
                } catch (TimeOutException e) {
                    // fall through
                }
                return false;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }
        }, false);
    }
}

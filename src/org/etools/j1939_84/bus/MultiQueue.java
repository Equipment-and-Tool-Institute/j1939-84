/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.util.Spliterator;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The multiqueue is a linked list that multiple visitors can traverse
 * concurrently. Adding items only adds them to open streams. The MultiQueue is
 * at most 1 element long.
 *
 * The head item holds no value. It only points to the most recent value (which
 * is also pointed to by the second most recent item. Notice that once a second
 * value is added, nothing references the first value, except for any open
 * streams.
 *
 * @param <T>
 */
public class MultiQueue<T> implements AutoCloseable {

    private final class SpliteratorImplementation implements Spliterator<T> {
        long end;
        Item<T> item = list;
        long startStep = step;

        private SpliteratorImplementation(long timeout, TimeUnit unit) {
            setTimeout(timeout, unit);
        }

        public void setTimeout(long timeout, TimeUnit unit) {
            end = System.currentTimeMillis() + unit.toMillis(timeout);
        }

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
            while (true) {
                try {
                    action.accept((item = item.next(end)).value);
                    return startStep == step;
                } catch (TimeOutException e) {
                    break;
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

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

    private MultiQueue.Item<T> list = new MultiQueue.Item<>(null);

    private long step = 0;

    synchronized public void add(T v) {
        list = list.add(v);
    }

    @Override
    public void close() {
        step++;
    }

    private WeakHashMap<Stream<T>, SpliteratorImplementation> sliterators = new WeakHashMap<>();

    public void resetTimeout(Stream<T> stream, int time, TimeUnit unit) {
        sliterators.get(stream).setTimeout(time, unit);
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
        SpliteratorImplementation spliterator = new SpliteratorImplementation(timeout, unit);
        Stream<T> stream = StreamSupport.stream(spliterator, false);
        sliterators.put(stream, spliterator);
        return stream;
    }
}

/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.util.Spliterator;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
 * @param <T> type of MultiQueue to be implemented
 */
public class MultiQueue<T> implements AutoCloseable {

    private final WeakHashMap<Stream<T>, SpliteratorImplementation<T>> spliterators = new WeakHashMap<>();
    private MultiQueue.Item<T> list = new MultiQueue.Item<>(null);

    synchronized public void add(T v) {
        list = list.add(v);
    }

    @Override
    public void close() {
        // close all of the spliterators.
        spliterators.values().forEach(SpliteratorImplementation::close);
    }

    /**
     * Duplicates a stream. Remember, if the stream is not read before the timeout,
     * then the stream will be empty.
     *
     * @param  stream Original stream.
     * @param  time   New timeout for this stream.
     * @param  unit   the unit of the timeout
     * @return        The new stream, independent of the original, but starting at the same
     *                location the original is right now.
     */
    public Stream<T> duplicate(Stream<T> stream, int time, TimeUnit unit) {
        SpliteratorImplementation<T> oldSpliterator = spliterators.get(stream);
        if (oldSpliterator.item == null) {
            throw new IllegalStateException("stream has already been closed.");
        }
        SpliteratorImplementation<T> newSpliterator = new SpliteratorImplementation<>(oldSpliterator);
        Stream<T> newStream = StreamSupport.stream(newSpliterator, false);
        spliterators.put(newStream, newSpliterator);
        newSpliterator.setTimeout(time, unit);
        return newStream.onClose(newSpliterator::close);
    }

    /**
     * Reset the timeout for the given stream. This is the original stream returned
     * from stream(timeout, unit), not some stream derived from stream(timeout,
     * unit).
     *
     * @param stream Stream created from stream(timeout, unit)
     * @param time   the new timeout
     * @param unit   the unit of the new timeout
     */
    public void resetTimeout(Stream<T> stream, int time, TimeUnit unit) {
        MultiQueue.SpliteratorImplementation<T> spliterator = spliterators.get(stream);
        if (spliterator == null) {
            throw new IllegalArgumentException("Invalid stream.");
        }
        spliterator.setTimeout(time, unit);
    }

    /**
     *
     * @param  timeout
     *                     The stream will be valid for a period of timeout. If the
     *                     stream is not read prior to timeout, then it will be empty.
     * @param  unit
     *                     the TimeUnit for the timeout
     * @return         the stream
     */
    synchronized public Stream<T> stream(long timeout, TimeUnit unit) {
        SpliteratorImplementation<T> spliterator = new SpliteratorImplementation<>(list, timeout, unit);
        Stream<T> stream = StreamSupport.stream(spliterator, false);
        spliterators.put(stream, spliterator);
        return stream.onClose(spliterator::close);
    }

    static private class Item<T> {
        final T value;
        MultiQueue.Item<T> next;

        Item(T v) {
            value = v;
        }

        synchronized MultiQueue.Item<T> add(T v) {
            next = new MultiQueue.Item<>(v);
            notifyAll();
            return next;
        }

        @SuppressWarnings("SameParameterValue")
        synchronized MultiQueue.Item<T> next(long delay) {
            if (next == null) {
                try {
                    wait(delay);
                } catch (InterruptedException e) {
                    // no problem
                }
            }
            return next;
        }
    }

    private final static class SpliteratorImplementation<T> implements Spliterator<T> {
        // how often to check for new items
        private static final int POLLING_PERIOD = 2;
        // end time of stream
        private long end;
        // reference to tail
        private Item<T> item;

        private SpliteratorImplementation(Item<T> list, long timeout, TimeUnit unit) {
            item = list;
            setTimeout(timeout, unit);
        }

        public void close() {
            end = 0;
            item = null;
        }

        public SpliteratorImplementation(MultiQueue.SpliteratorImplementation<T> that) {
            item = that.item;
            end = that.end;
        }

        public void setTimeout(long timeout, TimeUnit unit) {
            end = System.currentTimeMillis() + unit.toMillis(timeout);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            /*
             * While not timed out, wait up to POLLING_PERIOD ms for next packet, then loop.
             * This allows us to check for timeout and next packet in same thread.
             */
            while (System.currentTimeMillis() < end) {
                Item<T> n = item.next(POLLING_PERIOD);
                if (n != null) {
                    item = n;
                    action.accept(n.value);
                    return true;
                }
            }
            // lose reference to tail, to allow for faster cleanup
            item = null;
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            // Do not split.
            return null;
        }

        @Override
        public long estimateSize() {
            int count = 0;
            Item<T> i = item;
            while (i != null) {
                i = i.next;
                count++;
            }
            return count;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE | ORDERED;
        }
    }
}

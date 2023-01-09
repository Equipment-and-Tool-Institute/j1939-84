/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    // list of weak references to all queues ever allocated to check for abondonded streams.
    static List<MultiQueue<?>> queues = Collections.synchronizedList(new ArrayList<>());
    {
        queues.add(this);
    }
    private final WeakHashMap<Stream<T>, SpliteratorImplementation<T>> spliterators = new WeakHashMap<>();
    private Item<T> list = new Item<>(null);

    static {
        // monitor for stream leaks greater than 10,000 items.
        new Thread(() -> {
            try {
                while (true) {
                    // only check every 30 s
                    Thread.sleep(30 * 1000);
                    for (MultiQueue<?> q : queues) {
                        List<SpliteratorImplementation<?>> list;
                        synchronized (q.spliterators) {
                            list = new ArrayList<>(q.spliterators.values());
                        }
                        list.forEach(sp -> {
                            long es = sp.estimateSize();
                            if (es > 10_000) {
                                System.err.println(sp + " size:" + es + " end:" + sp.end);
                                // sp.stack.printStackTrace();
                            }
                        });
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, "MultiQueue Monitor").start();
    }

    synchronized public void add(T v) {
        list = list.add(v);
    }

    @Override
    public void close() {
        // close all of the spliterators.
        synchronized (spliterators) {
            spliterators.values().forEach(SpliteratorImplementation<T>::close);
        }
        queues.remove(this);
    }

    /**
     * Duplicates a stream. Remember, if the stream is not read before the timeout,
     * then the stream will be empty.
     *
     * @param  stream Original stream.
     * @param  time   New timeout for this stream.
     * @param  unit
     * @return        The new stream, independent of the original, but starting at the same
     *                location the original is right now.
     */
    public Stream<T> duplicate(Stream<T> stream, int time, TimeUnit unit) {
        synchronized (spliterators) {
            SpliteratorImplementation<T> oldSpliterator = spliterators.get(stream);
            if (oldSpliterator.item == null) {
                throw new IllegalStateException("stream has already been closed.");
            }
            SpliteratorImplementation<T> newSpliterator = new SpliteratorImplementation<>(oldSpliterator);
            Stream<T> newStream = StreamSupport.stream(newSpliterator, false);
            spliterators.put(newStream, newSpliterator);
            newSpliterator.setTimeout(time, unit);
            newStream.onClose(newSpliterator::close);
            return newStream;
        }
    }

    /**
     * Reset the timeout for the given stream. This is the original stream returned
     * from stream(timeout, unit), not some stream derived from stream(timeout,
     * unit).
     *
     * @param stream Stream created from stream(timeout, unit)
     * @param time
     * @param unit
     */
    public void resetTimeout(Stream<T> stream, int time, TimeUnit unit) {
        synchronized (spliterators) {
            SpliteratorImplementation<T> spliterator = spliterators.get(stream);
            if (spliterator == null) {
                throw new IllegalArgumentException("Invalid stream.");
            }
            spliterator.setTimeout(time, unit);
        }
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
        synchronized (spliterators) {
            spliterators.put(stream, spliterator);
        }
        stream.onClose(spliterator::close);
        return stream;
    }

    static private class Item<T> {
        final T value;
        Item<T> next;

        Item(T v) {
            value = v;
        }

        synchronized Item<T> add(T v) {
            next = new Item<>(v);
            notifyAll();
            return next;
        }

        synchronized Item<T> next(long delay) {
            if (next == null) {
                try {
                    wait(delay);
                } catch (InterruptedException e) {
                    // no problem
                }
            }
            return next;
        }

        synchronized public boolean isReady() {
            return next != null;
        }
    }

    private final static class SpliteratorImplementation<T> implements Spliterator<T> {
        // end time of stream
        private long end;
        // reference to tail
        private Item<T> item;
        // final private Error stack;

        private SpliteratorImplementation(Item<T> list, long timeout, TimeUnit unit) {
            item = list;
            setTimeout(timeout, unit);
            // stack = new Error();
        }

        public void close() {
            end = 0;
            item = null;
        }

        public SpliteratorImplementation(SpliteratorImplementation<T> that) {
            item = that.item;
            end = that.end;
            // stack = new Error();
        }

        public void setTimeout(long timeout, TimeUnit unit) {
            end = System.currentTimeMillis() + unit.toMillis(timeout);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            /*
             * While not timed out, wait next packet.
             * 
             * Include isReady, because this is based on wall clock and there is no indication when item.next was added,
             * except that it is added now.
             */
            while (item != null && (item.isReady() || System.currentTimeMillis() < end)) {
                Item<T> n = item.next(end - System.currentTimeMillis());
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
            Item<T> item = this.item;
            while (item != null) {
                item = item.next;
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

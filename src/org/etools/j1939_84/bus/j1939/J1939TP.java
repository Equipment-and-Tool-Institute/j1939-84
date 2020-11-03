package org.etools.j1939_84.bus.j1939;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;

public class J1939TP implements Bus {
    /**
     * Used to jump out of internal predicaments that should not be exposed to
     * callers.
     */
    static private class BusControlException extends RuntimeException {
        public final BusException busException;

        public BusControlException(BusException e) {
            busException = e;
        }
    }

    static public class CanceledBusException extends BusException {
        public CanceledBusException() {
            super("Canceled.");
        }
    }

    static public class CtsBusException extends BusException {
        private static final long serialVersionUID = 425016130552597972L;

        public CtsBusException() {
            super("CTS not received.");
        }
    }

    static public class EomBusException extends BusException {
        public EomBusException() {
            super("EOM not received.");
        }
    }

    static public class TpDtBusException extends BusException {
        public TpDtBusException() {
            super("DT not received.");
        }
    }

    /** Constants from J1939-21 */
    final static public int CM = 0xEC00;

    final static public int CM_BAM = 0x20;
    final static public int CM_ConnAbort = 255;

    final static public int CM_CTS = 17;

    final static public int CM_EndOfMessageACK = 19;

    final static public int CM_RTS = 16;
    final static public int DT = 0xEB00;

    static private final Logger logger = Logger.getLogger(J1939TP.class.getName());
    final static public int T1 = 750;
    final static public int T2 = 1250;

    final static public int T3 = 1250;

    final static public int T4 = 1050;
    final static public Map<Integer, String> table7;
    final static public int Th = 500;
    final static public int Tr = 200;

    final static public int TrPlus = 220;
    static {
        Map<Integer, String> err = new HashMap<>();
        err.put(1, "Already in one or more connection managed sessions and cannot support another.");
        err.put(2,
                "System resources were needed for another task so this connection managed session was terminated.");
        err.put(3, "A timeout occurred and this is the connection abort to close the session.");
        err.put(4, "CTS messages received when data transfer is in progress.");
        err.put(5, "Maximum retransmit request limit reached");
        err.put(6, "Unexpected data transfer packet");
        err.put(7, "Bad sequence number (software cannot recover)");
        err.put(8, "Duplicate sequence number (software cannot recover)");
        err.put(9, "\"Total Message Size\" is greater than 1785 bytes");
        err.put(250, "If a Connection Abort reason is identified that is not listed in the table use code 250");

        table7 = Collections.unmodifiableMap(err);
    }

    static private String getAbortError(int code) {
        return table7.getOrDefault(code, "Unknown");
    }

    /** We do not care about interruptions. */
    static private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
    }

    private final Bus bus;

    // Support up to 255 concurrent TP sessions plus main kickoff thread, but we
    // only expect there to normally be 5, so shut down idle threads after 5 ms.
    private final ExecutorService exec = new ThreadPoolExecutor(5 + 1,
            255 + 1,
            5L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    private final EchoBus inbound;
    /**
     * map of active requests (pgn<<32)|addr -> callbacks that are called when
     * RTS is received.
     */
    private final Map<Long, Runnable> requestCBs = new WeakHashMap<>();

    private final Stream<Packet> stream;

    {
        logger.setLevel(Level.FINEST);
    }

    public J1939TP(Bus bus) throws BusException {
        this(bus, bus.getAddress());
    }

    public J1939TP(Bus bus, int address) throws BusException {
        this.bus = bus;
        stream = bus.read(9999, TimeUnit.DAYS);
        inbound = new EchoBus(address);
        // start processing
        exec.execute(() -> stream.forEach(p -> receive(p)));
    }

    @Override
    public void close() {
        exec.shutdownNow();
        stream.close();
        bus.close();
    }

    /**
     * @param stream
     *            base stream originally returned from bus.read().
     */
    @Override
    public Stream<Packet> duplicate(Stream<Packet> stream, int time, TimeUnit unit) {
        return inbound.duplicate(stream, time, unit);
    }

    public void error(String msg, Throwable e) {
        // FIXME where do we want error messages to go?
        System.err.println(msg);
        e.printStackTrace();
    }

    @Override
    public int getAddress() {
        return inbound.getAddress();
    }

    @Override
    public int getConnectionSpeed() throws BusException {
        return bus.getConnectionSpeed();
    }

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        // FIXME this has to have a timeout to RTS
        return inbound.read(timeout, unit);
    }

    private void receive(Packet packet) {
        // ignore the packet if it is from this
        if (packet.getSource() != getAddress()) {
            switch (packet.getPgn()) {
            case CM: // TP connection management
                switch (packet.get(0)) {
                case CM_RTS: { // Request to send
                    if (packet.getDestination() == getAddress()) {
                        exec.execute(() -> {
                            try {
                                inbound.send(receiveDestinationSpecific(packet));
                            } catch (BusControlException e) {
                                error("Failed to receive destination specific TP.", e.busException);
                            } catch (BusException e) {
                                error("Failed to receive destination specific TP.", e);
                            }
                        });
                    }
                    return;
                }
                case CM_BAM: {
                    // Duplicate the current stream. Opening a new stream starts
                    // from "now" a may
                    // miss packets already queued up in stream. This is not
                    // needed for DA, because
                    // DA has a CTS.
                    // Timeout is reset inside receiveBam to account for time
                    // spent
                    // starting this thread.
                    Stream<Packet> bamStream = bus.duplicate(stream, T2, TimeUnit.MILLISECONDS);
                    exec.execute(() -> {
                        try {
                            inbound.send(receiveBam(packet, bamStream));
                        } catch (BusControlException e) {
                            error("Failed to receive BAM TP.", e.busException);
                        } catch (BusException e) {
                            error("Failed to receive BAM TP.", e);
                        } finally {
                            bamStream.close();
                        }
                    });
                    return;
                }
                case CM_ConnAbort:
                    // handled in receive calls
                    return;
                }
                break;
            case DT: // data
                return;
            }
            // everything else, pass through
            inbound.send(packet);
        }
    }

    private Packet receiveBam(Packet rts, Stream<Packet> stream) throws BusException {
        logger.fine("rx BAM: " + rts);

        int numberOfPackets = rts.get(3);

        byte[] data = new byte[rts.get16(1)];
        BitSet received = new BitSet(numberOfPackets + 1);
        int dataId = DT | rts.getDestination();
        int controlId = CM | (rts.getId() & 0xFF);
        int source = rts.getSource();

        bus.resetTimeout(stream, T2, TimeUnit.MILLISECONDS);
        Optional<Packet> o = stream
                .filter(p -> {
                    int id = p.getId() & 0xFFFF;
                    return p.getSource() == source && (id == dataId || id == controlId);
                })
                .peek(p -> bus.resetTimeout(stream, T1, TimeUnit.MILLISECONDS))
                .flatMap(p -> {
                    if ((p.getId() & 0xFFFF) == controlId) {
                        warn("BAM canceled or aborted: " + rts + " -> " + p);
                        throw new BusControlException(new CanceledBusException());
                    }
                    logger.fine("rx DT: " + p);
                    received.set(p.get(0));
                    int offset = (p.get(0) - 1) * 7;
                    System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
                    return (received.cardinality() == numberOfPackets)
                            ? Stream.of(Packet.create(rts.get24(5), rts.getSource(), data))
                            : Stream.empty();

                }).findFirst();
        if (!o.isPresent()) {
            warn("BAM missing DT %d != %d", received.cardinality(), numberOfPackets);
            throw new TpDtBusException();
        }
        return o.get();
    }

    public Packet receiveDestinationSpecific(Packet rts) throws BusException {
        logger.fine("rx RTS: " + rts);
        int numberOfPackets = rts.get(3);
        int maxResponsePackets = rts.get(4);

        byte[] data = new byte[rts.get16(1)];
        BitSet received = new BitSet(numberOfPackets + 1);
        int receivedNone = 0;
        int lastCardinality = -1;
        int cardinality;

        long key = ((long) rts.get24Big(5) << 32) | rts.getSource();
        System.err.println("key:" + key + "\n map:" + requestCBs);
        Runnable requestCallback = requestCBs.get(key);
        if (requestCallback != null) {
            requestCallback.run();
        }

        while ((cardinality = received.cardinality()) < numberOfPackets) {
            if (cardinality == lastCardinality) {
                if (receivedNone++ > 3) {
                    throw new BusException("Failed to receive DT");
                }
            } else {
                lastCardinality = cardinality;
                receivedNone = 0;
            }
            int nextPacket = received.nextClearBit(1);
            int packetCount = received.nextSetBit(nextPacket) - nextPacket;
            if (packetCount < 0) {
                packetCount = numberOfPackets - nextPacket + 1;
            }
            if (packetCount > maxResponsePackets) {
                packetCount = maxResponsePackets;
            }
            // FIXME should we warn on priority issues? Check with -21 and then
            // with Eric
            Stream<Packet> dataStream = bus.read(T2, TimeUnit.MILLISECONDS);
            Stream<Packet> stream = dataStream
                    .filter(p -> p.getSource() == rts.getSource())
                    .peek(p -> {
                        if ((p.getId() & 0xFFFF) == (CM | (rts.getId() & 0xFF))) {
                            if (p.get(0) == CM_ConnAbort) {
                                warn(getAbortError(p.get(1)));
                            }
                            warn("TP canceled: " + p); // FIXME
                            throw new BusControlException(new CanceledBusException());
                        }
                    })
                    // only consider DT packet that are part of this connection
                    .filter(p -> (p.getId() & 0xFFFF) == (DT | (rts.getId() & 0xFF)))
                    // After every TP.DT, reset timeout to T1 from now.
                    .peek(p -> {
                        bus.resetTimeout(dataStream, T1, TimeUnit.MILLISECONDS);
                        if (requestCallback != null) {
                            requestCallback.run();
                        }
                    })
                    .limit(packetCount);
            Packet cts = Packet.create(CM | rts.getSource(),
                    getAddress(),
                    CM_CTS,
                    packetCount,
                    nextPacket,
                    0xFF,
                    0xFF,
                    rts.get(5),
                    rts.get(6),
                    rts.get(7));
            logger.fine("tx CTS: " + cts);

            bus.send(cts);
            stream.forEach(p -> {
                logger.fine("rx DT: " + rts);
                received.set(p.get(0));
                int offset = (p.get(0) - 1) * 7;
                System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
            });
        }
        Packet eom = Packet.create(CM | rts.getSource(),
                getAddress(),
                CM_EndOfMessageACK,
                rts.get(1),
                rts.get(2),
                rts.get(3),
                0xFF,
                rts.get(5),
                rts.get(6),
                rts.get(7));
        logger.fine("tx EOM: " + eom);

        bus.send(eom);
        int pgn = rts.get24(5);
        if (pgn < 0xF000) {
            pgn |= getAddress();
        }
        return Packet.create(pgn, rts.getSource(), data);
    }

    @Override
    public void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit) {
        bus.resetTimeout(stream, time, unit);
    }

    @Override
    public void send(Packet packet) throws BusException {
        if (packet.getLength() <= 8) {
            bus.send(packet);
        } else if (packet.getPgn() >= 0xF000) {
            sendBam(packet);
        } else {
            sendDestinationSpecific(packet);
        }
    }

    @SafeVarargs
    final public void send(Packet packet, Stream<Packet>... streams) throws BusException {
        long pgn = packet.getPgn();
        requestCBs.put((pgn << 32) | packet.getDestination(),
                () -> {
                    for (var stream : streams) {
                        inbound.resetTimeout(stream, TrPlus, TimeUnit.MILLISECONDS);
                    }
                });
        send(packet);
    }

    void sendBam(Packet packet) throws BusException {
        int pgn = packet.getPgn();
        int packetsToSend = packet.getLength() / 7 + 1;
        int sourceAddress = getAddress();
        Packet bam = Packet.create(CM | 0xFF,
                sourceAddress,
                CM_BAM,
                packet.getLength(),
                packet.getLength() >> 8,
                packetsToSend,
                0xFF,
                pgn,
                pgn >> 8,
                (0b111 & (pgn >> 16)));
        logger.fine("tx BAM: " + bam);

        bus.send(bam);
        // send data
        int id = DT | 0xFF;
        for (int i = 0; i < packetsToSend; i++) {
            byte[] buf = new byte[8];

            int end = Math.min(packet.getLength() - i * 7, 7);
            System.arraycopy(packet.getBytes(),
                    i * 7,
                    buf,
                    1,
                    end);
            Arrays.fill(buf, end + 1, buf.length, (byte) 0xFF);
            buf[0] = (byte) (i + 1);
            sleep(50);
            Packet dp = Packet.create(id, sourceAddress, buf);
            logger.fine("tx DT.DP: " + dp);
            bus.send(dp);
        }
    }

    void sendDestinationSpecific(Packet packet) throws BusException {
        // int pgn = packet.getPgn();
        // int destinationAddress = packet.getDestination();
        int pgn = packet.getId();
        int destinationAddress = packet.getId() & 0xFF;
        Predicate<Packet> controlMessageFilter = p -> p.getSource() == destinationAddress
                && (0xFFFF & p.getId()) == (CM | packet.getSource());

        Stream<Packet> ctsStream = bus.read(T3, TimeUnit.MILLISECONDS)
                .filter(controlMessageFilter);

        // send RTS
        int totalPacketsToSend = packet.getLength() / 7 + 1;
        Packet rts = Packet.create(CM | packet.getDestination(),
                getAddress(),
                CM_RTS,
                packet.getLength(),
                packet.getLength() >> 8,
                totalPacketsToSend,
                0xFF,
                pgn,
                pgn >> 8,
                pgn >> 16);
        logger.fine("tx RTS: " + rts);

        bus.send(rts);

        // wait for CTS
        Optional<Packet> ctsOptional = ctsStream.findFirst();
        while (ctsOptional.map(p -> p.get(0) == CM_CTS).orElse(false)) {
            Packet cts = ctsOptional.get();
            logger.fine("rx CTS: " + cts);

            int packetsToSend = Math.min(cts.get(1), totalPacketsToSend);
            if (packetsToSend == 0) {
                if ((cts.get64() & 0x0000FFFFFFFFFFFFL) != 0x0000FFFFFFFFFFFFL) {
                    warn("TP.CM_CTS \"hold the connection open\" should be: %04X  %s",
                            0x0000FFFFFFFFFFFFL,
                            cts.toString());
                }
                // wait for CTS
                ctsOptional = bus.read(T4, TimeUnit.MILLISECONDS).filter(controlMessageFilter).findFirst();
            } else {
                int offset = cts.get(2);
                if (cts.get16(3) != 0xFFFF) {
                    warn("TP.CM_CTS bytes 4-5 should be FFFF: %04X  %s", cts.get16(3), cts.toString());
                }
                if (cts.get24(5) != pgn) {
                    warn("TP.CM_CTS bytes 6-8 should be the PGN: %04X  %s", cts.get24(5), cts.toString());
                }
                // send data
                for (int i = 0; i < packetsToSend; i++) {
                    byte[] buf = new byte[8];
                    System.arraycopy(packet.getBytes(),
                            (i + offset - 1) * 7,
                            buf,
                            1,
                            Math.min(packet.getLength() - i * 7, 7));
                    buf[0] = (byte) (i + 1);
                    Packet dp = Packet.create(DT | destinationAddress, getAddress(), buf);
                    logger.fine("tx DP: " + dp);
                    bus.send(dp);
                }
                // wait for CTS or EOM
                ctsOptional = bus.read(T3, TimeUnit.MILLISECONDS).filter(controlMessageFilter).findFirst();
            }
        }
        ctsOptional.ifPresent(eom -> logger.fine("rx EOM: " + eom));

        if (ctsOptional.map(p -> p.get(0) == CM_ConnAbort).orElse(false)) {
            // FAIL
            warn("Abort received: " + getAbortError(ctsOptional.get().get(1)));
        } else if (ctsOptional.map(p -> p.get(0) != CM_EndOfMessageACK).orElse(true)) {
            // verify EOM
            warn((ctsOptional.isPresent() ? "CTS" : "EOM") + " not received.");
            throw ctsOptional.map(p -> (BusException) new EomBusException())
                    .orElse(new CtsBusException());
        }
    }

    public void warn(String msg, Object... a) {
        // FIXME where do we want warning messages to go?
        System.err.println("WARN: " + String.format(msg, a));
    }
}

package org.etools.j1939_84.bus.j1939;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class J1939TP implements Bus {
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

    final static public int CM = 0xEC00;

    final static public int CM_BAM = 0x20;
    final static public int CM_ConnAbort = 255;

    final static public int CM_CTS = 17;

    final static public int CM_EndOfMessageACK = 19;

    final static public int CM_RTS = 16;
    final static public int DT = 0xEB00;

    final static public Map<Integer, String> errors;

    static private final Logger logger = J1939_84.getLogger();

    final static public int T1 = 750;

    final static public int T2 = 1250;
    final static public int T3 = 1250;
    final static public int T4 = 1050;
    final static public int Th = 500;

    final static public int Tr = 200;

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
        errors = Collections.unmodifiableMap(err);
    }

    static private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
    }

    private final Map<Integer, AtomicBoolean> bamSessions = new HashMap<>();

    private final Bus bus;

    private final Map<Integer, AtomicBoolean> destinationSpecificSessions = new HashMap<>();

    // Support up to 255 concurrent TP sessions, but we only expect there to
    // normally be 5, so shut down idle threads after 5 ms.
    private final ExecutorService exec = new ThreadPoolExecutor(5,
            255,
            5L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    private final EchoBus inbound;
    private final Stream<Packet> stream;

    public J1939TP(Bus bus) throws BusException {
        this(bus, bus.getAddress());
    }

    @SuppressFBWarnings(value = { "UW_UNCOND_WAIT", "WA_NOT_IN_LOOP" }, justification = "Wait for stream open.")
    public J1939TP(Bus bus, int address) throws BusException {
        this.bus = bus;
        stream = bus.read(9999, TimeUnit.DAYS);
        inbound = new EchoBus(address);
        synchronized (inbound) {
            exec.execute(this::processPackets);
            try {
                // wait for run() to get a reference to bus
                inbound.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        exec.shutdownNow();
        stream.close();
        bus.close();
    }

    @Override
    public int getAddress() {
        return inbound.getAddress();
    }

    @Override
    public int getConnectionSpeed() throws BusException {
        return bus.getConnectionSpeed();
    }

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification = "Notify of stream open.")
    private void processPackets() {
        synchronized (inbound) {
            inbound.notifyAll();
        }
        stream.forEach(p -> receive(p));
    }

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return inbound.read(timeout, unit);
    }

    private void receive(Packet packet) {
        if (packet.getSource() != getAddress()) {
            switch (packet.getId() & 0xFF00) {
                case CM:
                    switch (packet.get(0)) {
                        case CM_RTS: {
                            if ((packet.getId() & 0xFF) == getAddress()) {
                                AtomicBoolean canceled = new AtomicBoolean();
                                AtomicBoolean existingSession = destinationSpecificSessions.put(getAddress(), canceled);
                                // there was already a session, cancel it
                                if (existingSession != null) {
                                    existingSession.set(true);
                                }
                                exec.execute(() -> {
                                    try {
                                        inbound.send(receiveDestinationSpecific(packet, () -> canceled.get()));
                                    } catch (BusException e) {
                                        warn("Failed to receive destination specific TP.", e);
                                    }
                                });
                            }
                            return;
                        }
                        case CM_BAM: {
                            AtomicBoolean canceled = new AtomicBoolean();
                            AtomicBoolean existingSession = bamSessions.put(getAddress(), canceled);
                            // there was already a session, cancel it
                            if (existingSession != null) {
                                existingSession.set(true);
                            }
                            exec.execute(() -> {
                                try {
                                    inbound.send(receiveBam(packet, () -> canceled.get()));
                                } catch (BusException e) {
                                    e.printStackTrace();
                                    warn("Failed to receive BAM TP.", e);
                                }
                            });
                            return;
                        }
                        case CM_ConnAbort: {
                            AtomicBoolean existingBamSession = bamSessions.get(getAddress());
                            // there was already a session, cancel it
                            if (existingBamSession != null) {
                                existingBamSession.set(true);
                            }
                            AtomicBoolean existingDaSession = destinationSpecificSessions.get(getAddress());
                            // there was already a session, cancel it
                            if (existingDaSession != null) {
                                existingDaSession.set(true);
                            }
                            return;
                        }
                    }
                    break;
                case DT:
                    return;
            }
            inbound.send(packet);
        }
    }

    private Packet receiveBam(Packet rts, Supplier<Boolean> canceled) throws BusException {
        logger.fine("rx BAM: " + rts);

        int numberOfPackets = rts.get(3);

        byte[] data = new byte[rts.get16(1)];
        BitSet received = new BitSet(numberOfPackets + 1);
        int id = DT | (rts.getId() & 0xFF);
        int source = rts.getSource();

        Stream<Packet> streamBase = bus.read(T2, TimeUnit.MILLISECONDS);
        Stream<Packet> stream = streamBase
                .filter(p -> p.getSource() == source && (p.getId() & 0xFFFF) == id)
                // if canceled, ignore all packets and timeout.
                .filter(p -> !canceled.get())
                .peek(p -> bus.resetTimeout(streamBase, T1, TimeUnit.MILLISECONDS))
                .limit(numberOfPackets);
        stream.forEach(p -> {
            logger.fine("rx DT: " + p);
            received.set(p.get(0));
            int offset = (p.get(0) - 1) * 7;
            System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
        });
        if (canceled.get()) {
            warn("BAM canceled");
            throw new CanceledBusException();
        } else if (received.cardinality() != numberOfPackets) {
            warn(canceled.get() ? "BAM canceled" : "BAM missing DT");
            throw new TpDtBusException();
        }
        return Packet.create(rts.get24(5), rts.getSource(), data);
    }

    public Packet receiveDestinationSpecific(Packet rts, Supplier<Boolean> canceled) throws BusException {
        logger.fine("rx RTS: " + rts);
        int numberOfPackets = rts.get(3);
        int maxResponsePackets = rts.get(4);

        byte[] data = new byte[rts.get16(1)];
        BitSet received = new BitSet(numberOfPackets + 1);
        int receivedNone = 0;
        int lastCardinality = -1;
        int cardinality;
        while ((cardinality = received.cardinality()) < numberOfPackets) {
            if (canceled.get()) {
                throw new BusException("Canceled");
            }
            if (cardinality == lastCardinality) {
                if (receivedNone++ > 3) {
                    throw new BusException("Failed to receive DT");
                }
            } else {
                lastCardinality = cardinality;
            }
            int nextPacket = received.nextClearBit(1);
            int packetCount = received.nextSetBit(nextPacket) - nextPacket;
            if (packetCount < 0) {
                packetCount = numberOfPackets - nextPacket + 1;
            }
            if (packetCount > maxResponsePackets) {
                packetCount = maxResponsePackets;
            }
            Stream<Packet> streamBase = bus.read(T2, TimeUnit.MILLISECONDS);
            // FIXME should we warn on priority issues? Check with -21 and then with Eric
            Stream<Packet> stream = streamBase
                    .filter(p -> p.getSource() == rts.getSource()
                            && (p.getId() & 0xFFFF) == (DT | (rts.getId() & 0xFF)))
                    // if this is canceled, ignore all the packets and let this timeout
                    .filter(p -> !canceled.get())
                    .peek(p -> bus.resetTimeout(streamBase, T1, TimeUnit.MILLISECONDS))
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
            logger.fine("tx CTS: " + rts);

            bus.send(cts);
            stream.filter(p -> !canceled.get())
                    .forEach(p -> {
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
        if (pgn <= 0xF000) {
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
        } else if ((0xFF00 & packet.getId()) >= 0xF000) {
            sendBam(packet);
        } else {
            sendDestinationSpecific(packet);
        }
    }

    public void sendBam(Packet packet) throws BusException {
        int pgn = packet.getId();
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

    public void sendDestinationSpecific(Packet packet) throws BusException {
        int pgn = packet.getId();
        int destinationAddress = packet.getId() & 0xFF;
        Predicate<Packet> controlMessageFilter = p -> p.getSource() == destinationAddress
                && p.getId() == (CM | packet.getSource());

        Stream<Packet> ctsStream = bus.read(T3, TimeUnit.MILLISECONDS)
                .filter(controlMessageFilter);

        // send RTS
        int totalPacketsToSend = packet.getLength() / 7 + 1;
        Packet rts = Packet.create(CM | (0xFF & packet.getId()),
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
            warn("Abort received: " + ctsOptional.map(p -> p.toString()).orElse("ERROR"));
        } else
        // verify EOM
        if (ctsOptional.map(p -> p.get(0) != CM_EndOfMessageACK).orElse(true)) {
            // FAIL
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

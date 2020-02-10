package org.etools.j1939_84.bus.j1939;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;

public class J1939TP implements Bus {
    public class CtsBusException extends BusException {
        private static final long serialVersionUID = 425016130552597972L;

        public CtsBusException() {
            super("CTS not received.");
        }
    }

    public class EomBusException extends BusException {
        private static final long serialVersionUID = -4409123099288626185L;

        public EomBusException() {
            super("EOM not received.");
        }
    }

    public class TpDtBusException extends BusException {
        private static final long serialVersionUID = -4409123099288626185L;

        public TpDtBusException() {
            super("DT not received.");
        }
    }

    final static public int CM = 0xEC00;
    private static final int CM_BAM = 32;

    private static final int CM_ConnAbort = 255;

    static final int CM_CTS = 17;

    private static final int CM_EndOfMessageACK = 19;
    private static final int CM_RTS = 16;
    // Most number of packet that can be sent in one CTS
    private static final int COUNT_MAX = 0xFF;
    public final static int DT = 0xEB00;
    private static final Map<Integer, String> errors = new HashMap<>();

    static final int T1 = 750;
    static final int T2 = 1250;
    static final int T3 = 1250;
    static final int T4 = 1050;

    private static final int Th = 500;
    private static final int Tr = 200;

    static {
        errors.put(1, "Already in one or more connection managed sessions and cannot support another.");
        errors.put(2,
                "System resources were needed for another task so this connection managed session was terminated.");
        errors.put(3, "A timeout occurred and this is the connection abort to close the session.");
        errors.put(4, "CTS messages received when data transfer is in progress.");
        errors.put(5, "Maximum retransmit request limit reached");
        errors.put(6, "Unexpected data transfer packet");
        errors.put(7, "Bad sequence number (software cannot recover)");
        errors.put(8, "Duplicate sequence number (software cannot recover)");
        errors.put(9, "\"Total Message Size\" is greater than 1785 bytes");
        errors.put(250, "If a Connection Abort reason is identified that is not listed in the table use code 250");
    }

    static public void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
    }

    private final Bus bus;

    private final Executor exec = Executors.newFixedThreadPool(10);

    private final EchoBus inbound;

    J1939TP(Bus bus) {
        this(bus, bus.getAddress());
    }

    J1939TP(Bus bus, int address) {
        this.bus = bus;
        inbound = new EchoBus(address);
        synchronized (inbound) {
            exec.execute(this::run);
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

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return inbound.read(timeout, unit);
    }

    private void receive(Packet packet) {
        if (packet.getSource() != getAddress()) {
            switch (packet.getId() & 0xFF00) {
                case CM:
                    switch (packet.get(0)) {
                        case CM_RTS:
                            if ((packet.getId() & 0xFF) == getAddress()) {
                                exec.execute(() -> {
                                    try {
                                        inbound.send(receiveDestinationSpecific(packet));
                                    } catch (BusException e) {
                                        warn("Failed to receive destination specific TP.", e);
                                    }
                                });
                            }
                            return;
                        case CM_BAM:
                            exec.execute(() -> {
                                try {
                                    inbound.send(receiveBam(packet));
                                } catch (BusException e) {
                                    warn("Failed to receive BAM TP.", e);
                                }
                            });
                            return;
                    }
                    break;
                case DT:
                    return;
            }
            inbound.send(packet);
        }
    }

    private Packet receiveBam(Packet rts) throws BusException {
        int numberOfPackets = rts.get(3);

        byte[] data = new byte[rts.get16Big(1)];
        BitSet received = new BitSet(numberOfPackets + 1);

        Stream<Packet> streamBase = bus.read(T2, TimeUnit.MILLISECONDS);
        Stream<Packet> stream = streamBase
                .filter(p -> p.getSource() == rts.getSource() && p.getId() == (DT | (rts.getId() & 0xFF)))
                .peek(p -> bus.resetTimeout(streamBase, T1, TimeUnit.MILLISECONDS))
                .limit(numberOfPackets);
        stream.forEach(p -> {
            received.set(p.get(0));
            int offset = (p.get(0) - 1) * 7;
            System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
        });
        if (received.isEmpty()) {
            warn("Missing DT");
            throw new TpDtBusException();
        }
        return Packet.create(rts.get24(5), rts.getSource(), data);
    }

    public Packet receiveDestinationSpecific(Packet rts) throws BusException {
        int numberOfPackets = rts.get(3);
        int maxResponsePackets = rts.get(4);

        byte[] data = new byte[rts.get16Big(1)];
        BitSet received = new BitSet(numberOfPackets + 1);
        int receivedSome = 0;
        while (received.cardinality() < numberOfPackets) {
            if (receivedSome++ > 3) {
                throw new BusException("Failed to receive DT");
            }
            int packetCount = received.nextSetBit(1);
            if (packetCount < 0) {
                packetCount = numberOfPackets;
            }
            if (packetCount > maxResponsePackets) {
                packetCount = maxResponsePackets;
            }
            Stream<Packet> stream1 = bus.read(T2, TimeUnit.MILLISECONDS);
            Stream<Packet> stream = stream1
                    .filter(p -> p.getSource() == rts.getSource() && p.getId() == (DT | (rts.getId() & 0xFF)))
                    .peek(p -> bus.resetTimeout(stream1, T1, TimeUnit.MILLISECONDS))
                    .limit(packetCount);
            bus.send(Packet.create(CM | rts.getSource(),
                    getAddress(),
                    CM_CTS,
                    packetCount,
                    received.nextClearBit(1),
                    0xFF,
                    0xFF,
                    rts.get(5),
                    rts.get(6),
                    rts.get(7)));
            Iterator<Packet> it = stream.iterator();
            while (it.hasNext()) {
                Packet p = it.next();
                receivedSome = 0;
                received.set(p.get(0));
                int offset = (p.get(0) - 1) * 7;
                System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
            }
        }
        bus.send(Packet.create(CM | rts.getSource(),
                getAddress(),
                CM_EndOfMessageACK,
                rts.get(1),
                rts.get(2),
                rts.get(3),
                0xFF,
                rts.get(5),
                rts.get(6),
                rts.get(7)));
        return Packet.create(rts.get24(5), rts.getSource(), data);
    }

    @Override
    public void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit) {
        bus.resetTimeout(stream, time, unit);
    }

    private void run() {
        try {
            Iterator<Packet> i = bus.read(9999, TimeUnit.DAYS).iterator();
            synchronized (inbound) {
                inbound.notifyAll();
            }
            while (i.hasNext()) {
                receive(i.next());
            }
        } catch (BusException e) {
            warn("Failed to process packet.", e);
        }
    }

    @Override
    public void send(Packet packet) throws BusException {
        if (packet.getLength() <= 8) {
            bus.send(packet);
        } else {
            sendDestinationSpecific(packet);
        }
    }

    public void sendBam(Packet packet) throws BusException {
        int pgn = packet.getId();
        int packetsToSend = packet.getLength() / 7 + 1;
        int sourceAddress = getAddress();
        bus.send(Packet.create(CM | 0xFF,
                sourceAddress,
                CM_BAM,
                packet.getLength() >> 8,
                packet.getLength(),
                packetsToSend,
                0xFF,
                pgn,
                pgn >> 8,
                (0b111 & (pgn >> 16))));
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
            bus.send(Packet.create(id, sourceAddress, buf));
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
        bus.send(Packet.create(CM | (0xFF & packet.getId()),// fixme what about BAM
                getAddress(),
                CM_RTS,
                packet.getLength() >> 8,
                packet.getLength(),
                packet.getLength() / 7 + 1,
                0xFF,
                pgn,
                pgn >> 8,
                pgn >> 16));

        // wait for CTS
        Optional<Packet> cts = ctsStream.findFirst();
        while (cts.map(p -> p.get(0) == CM_CTS).orElse(false)) {
            Packet p2 = cts.get();// FIXME
            int packetsToSend = p2.get(1);
            if (packetsToSend == 0) {
                if ((p2.get64() & 0x0000FFFFFFFFFFFFL) != 0x0000FFFFFFFFFFFFL) {
                    warn("tp.CM_CTS \"hold he connection open\" should be: ");
                }
                // wait for CTS
                cts = bus.read(T4, TimeUnit.MILLISECONDS).filter(controlMessageFilter).findFirst();
            } else {
                int offset = p2.get(2);
                if (p2.get16(3) != 0xFFFF) {
                    warn("TP.CM_CTS bytes 4-5 should be FFFF: %04X", p2.get16(3));
                }
                if (p2.get24(5) != pgn) {
                    warn("TP.CM_CTS bytes 6-8 should be the PGN: %04X", p2.get24(5));
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
                    bus.send(Packet.create(DT | destinationAddress, getAddress(), buf));
                }
                // wait for CTS or EOM
                cts = bus.read(T3, TimeUnit.MILLISECONDS).filter(controlMessageFilter).findFirst();
            }
        }
        // verify EOM
        if (cts.map(p -> p.get(0) != CM_EndOfMessageACK).orElse(true)) {
            // FAIL
            warn((cts.isPresent() ? "CTS" : "EOM") + " not received.");
            throw cts.map(p -> (BusException) new EomBusException())
                    .orElse(new CtsBusException());
        }
    }

    private void warn(String msg, Object... a) {
        System.err.println("WARN: " + String.format(msg, a));
    }
}

package org.etools.j1939tools.j1939;

import static org.etools.j1939tools.bus.Packet.PacketException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.EchoBus;
import org.etools.j1939tools.bus.Packet;

public class J1939TP implements Bus {

    /** Constants from J1939-21 */
    final static public int CM = 0xEC00;
    final static public int CM_BAM = 0x20;
    final static public int CM_ConnAbort = 255;
    final static public int CM_CTS = 17;
    final static public int CM_EndOfMessageACK = 19;
    final static public int CM_RTS = 16;
    final static public int DT = 0xEB00;
    final static public int T1 = 750;
    final static public int T2 = 1250;
    final static public int T3 = 1250;
    final static public int T4 = 1050;
    final static public Map<Integer, String> table7;
    final static public int Th = 500;
    final static public int Tr = 200;
    final static public int TrPlus = 220;
    static private final Logger logger = Logger.getLogger(J1939TP.class.getName());

    static {
        Map<Integer, String> err = new HashMap<>();
        err.put(1, "Already in one or more connection managed sessions and cannot support another.");
        err.put(2, "System resources were needed for another task so this connection managed session was terminated.");
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

    /** bus representing CAN bus */
    private final Bus bus;
    /**
     * Support up to 255 concurrent TP sessions plus main kickoff thread, but we
     * only expect there to normally be 5, so shut down idle threads after 1 s.
     */
    private final ExecutorService exec = new ThreadPoolExecutor(5 + 1,
                                                                255 + 1,
                                                                1L,
                                                                TimeUnit.SECONDS,
                                                                new LinkedBlockingQueue<>());
    /** Application side bus. */
    private final EchoBus inbound;

    private boolean passAll;

    /**
     * The inbound stream that RTS and BAM announcements will be detected on.
     */
    private final Stream<Packet> stream;

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

    @Override
    public void close() {
        exec.shutdownNow();
        stream.close();
        bus.close();
    }

    /**
     * @param stream
     *                   base stream originally returned from bus.read().
     */
    @Override
    public Stream<Packet> duplicate(Stream<Packet> stream, int time, TimeUnit unit) {
        return inbound.duplicate(stream, time, unit);
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

    @Override
    public void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit) {
        bus.resetTimeout(stream, time, unit);
    }

    @Override
    public Packet send(Packet packet) throws BusException {
        if (packet.getLength() <= 8) {
            return bus.send(packet);
        } else if (packet.getPgn() >= 0xF000) {
            return sendBam(packet);
        } else {
            return sendDestinationSpecific(packet.getDestination(), packet);
        }
    }

    /** Record an error, which is more than just a warning. */
    private void error(String msg, Throwable e) {
        logger.log(Level.SEVERE, msg, e);
    }

    /** Used for debugging. */
    private void fine(String str, Packet p) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(str + ": " + p.toTimeString());
        }
    }

    private void receive(Packet packet) {
        // ignore the packet if it is from this
        try {
            if (passAll) // pass all fragments
                inbound.send(packet);
            if (packet.getSource() != getAddress()) {
                switch (packet.getPgn()) {
                    case CM: // TP connection management
                        switch (packet.get(0)) {
                            case CM_RTS: { // Request to send
                                if (packet.getDestination() == getAddress()) {
                                    exec.execute(() -> {
                                        try {
                                            receiveDestinationSpecific(packet);
                                        } catch (BusException e) {
                                            error("Failed to receive destination specific TP:" + packet, e);
                                        }
                                    });
                                }
                                return;
                            }
                            case CM_BAM: {
                                /*
                                 * Duplicate the current stream. Opening a new stream starts
                                 * from "now" and may miss packets already queued up in
                                 * stream. This is not needed for DA, because DA has a CTS.
                                 * Timeout is reset inside receiveBam to account for time
                                 * spent starting this thread.
                                 */
                                Stream<Packet> bamStream = bus.duplicate(stream, T2, TimeUnit.MILLISECONDS);
                                exec.execute(() -> {
                                    try {
                                        receiveBam(packet, bamStream);
                                    } catch (Throwable t) {
                                        error("Failed to process packet:" + packet, t);
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
                if (!passAll)
                    inbound.send(packet);
            }
        } catch (Throwable t) {
            error("Failed to process packet:" + packet, t);
        }
    }

    private void receiveBam(Packet bam, Stream<Packet> stream) {
        fine("rx BAM", bam);

        int numberOfPackets = bam.get(3);

        byte[] data = new byte[bam.get16(1)];
        BitSet received = new BitSet(numberOfPackets + 1);
        int dataId = DT | bam.getDestination();
        int controlId = CM | bam.getId(0xFF);

        int pgn = bam.get24(5);
        int source = bam.getSource();
        int packetId = pgn < 0xF000 ? pgn | bam.getDestination() : pgn;
        Packet packet = createEmptyPacket(packetId, source);
        packet.setFragments(new ArrayList<>());
        packet.getFragments().add(bam);
        packet.setTimestamp(bam.getTimestamp());
        synchronized (packet) {
            inbound.send(packet);

            bus.resetTimeout(stream, T2, TimeUnit.MILLISECONDS);
            if (stream
                      .filter(p -> {
                          int id = p.getId(0xFFFF);
                          return p.getSource() == source && (id == dataId || id == controlId);
                      })
                      .peek(p -> bus.resetTimeout(stream, T1, TimeUnit.MILLISECONDS))
                      .map(p -> {
                          if (p.getId(0xFFFF) == controlId) {
                              packet.fail();
                              warn("BAM canceled or aborted: " + bam + " -> " + p);
                              return true;
                          }
                          fine("rx DT", p);
                          packet.getFragments().add(p);
                          received.set(p.get(0));
                          int offset = (p.get(0) - 1) * 7;
                          System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
                          packet.setTimestamp(p.getTimestamp());
                          return received.cardinality() == numberOfPackets;
                      })
                      .filter(b -> b)
                      .findFirst()
                      .orElse(false)
                    && received.cardinality() == numberOfPackets) {
                packet.setData(data);
            } else {
                warn("BAM missing DT %d != %d %s",
                     received.cardinality(),
                     numberOfPackets,
                     packet.getFragments());
                packet.fail();
            }
        }
    }

    private Packet createEmptyPacket(int id, int source) {
        return new Packet(LocalDateTime.now(), 7, id, source, false, (int[]) null);
    }

    public void receiveDestinationSpecific(Packet rts) throws BusException {
        fine("rx RTS", rts);
        int numberOfPackets = rts.get(3);
        int maxResponsePackets = rts.get(4);

        byte[] data = new byte[rts.get16(1)];
        BitSet received = new BitSet(numberOfPackets + 1);
        int receivedNone = 0;
        int lastCardinality = -1;
        int cardinality;

        int pgn = rts.get24(5);
        int source = rts.getSource();
        int id = pgn < 0xF000 ? pgn | rts.getDestination() : pgn;
        Packet packet = createEmptyPacket(id, source);
        packet.setFragments(new ArrayList<>());
        packet.getFragments().add(rts);
        synchronized (packet) {
            inbound.send(packet);
            while ((cardinality = received.cardinality()) < numberOfPackets) {
                if (cardinality == lastCardinality) {
                    if (receivedNone++ > 3) {
                        packet.fail();
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
                Stream<Packet> dataStream = bus.read(T2, TimeUnit.MILLISECONDS);
                Stream<Packet> stream = dataStream
                                                  .filter(p -> p.getSource() == source)
                                                  .peek(p -> {
                                                      if (p.getId(0xFFFF) == (CM | rts.getId(0xFF))) {
                                                          if (p.get(0) == CM_ConnAbort) {
                                                              warn(getAbortError(p.get(1)), p);
                                                          }
                                                          warn("TP canceled", p);
                                                          packet.fail();
                                                          throw new PacketException("TP canceled");
                                                      }
                                                  })
                                                  // only consider DT packet that are part of this
                                                  // connection
                                                  .filter(p -> p.getId(0xFFFF) == (DT | rts.getId(0xFF)))
                                                  // After every TP.DT, reset timeout to T1 from now.
                                                  .peek(p -> bus.resetTimeout(dataStream, T1, TimeUnit.MILLISECONDS))
                                                  .limit(packetCount);
                Packet cts = createPacket(CM | source,
                                          getAddress(),
                                          CM_CTS,
                                          packetCount,
                                          nextPacket,
                                          0xFF,
                                          0xFF,
                                          rts.get(5),
                                          rts.get(6),
                                          rts.get(7));
                fine("tx CTS", cts);
                packet.getFragments().add(bus.send(cts));
                try {
                    stream.forEach(p -> {
                        packet.getFragments().add(p);
                        fine("rx DT", rts);
                        received.set(p.get(0));
                        packet.setTimestamp(p.getTimestamp());
                        int offset = (p.get(0) - 1) * 7;
                        System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
                    });
                } catch (PacketException e) {
                    // TP failed.
                    packet.fail();
                    return;
                }
            }
            Packet eom = createPacket(CM | source,
                                      getAddress(),
                                      CM_EndOfMessageACK,
                                      rts.get(1),
                                      rts.get(2),
                                      rts.get(3),
                                      0xFF,
                                      rts.get(5),
                                      rts.get(6),
                                      rts.get(7));
            fine("tx EOM", eom);
            packet.getFragments().add(bus.send(eom));

            // signal done collecting packet data
            packet.setData(data);
        }
    }

    private Packet sendBam(Packet packet) throws BusException {
        int pgn = packet.getPgn();
        int packetsToSend = packet.getLength() / 7 + 1;
        int sourceAddress = getAddress();
        Packet bam = createPacket(CM | 0xFF,
                                  sourceAddress,
                                  CM_BAM,
                                  packet.getLength(),
                                  packet.getLength() >> 8,
                                  packetsToSend,
                                  0xFF,
                                  0xFF & pgn,
                                  0xFF & (pgn >> 8),
                                  (0b111 & (pgn >> 16)));
        fine("tx BAM", bam);

        Packet response = bus.send(bam);
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
            Packet dp = createPacket(id, sourceAddress, buf);

            fine("tx DT.DP", dp);
            bus.send(dp);
        }
        return response;
    }

    public Packet sendDestinationSpecific(int destinationAddress, Packet packet) throws BusException {
        int pgn = packet.getPgn();
        Predicate<Packet> controlMessageFilter = p -> //
        p.getSource() == destinationAddress
                && p.getId(0xFFFF) == (CM | packet.getSource());

        // send RTS
        int totalPacketsToSend = packet.getLength() / 7 + 1;
        Packet rts = createPacket(CM | destinationAddress,
                                  getAddress(),
                                  CM_RTS,
                                  packet.getLength(),
                                  packet.getLength() >> 8,
                                  totalPacketsToSend,
                                  0xFF,
                                  0xFF & pgn,
                                  0xFF & (pgn >> 8),
                                  0xFF & (pgn >> 16));
        fine("tx RTS", rts);

        Stream<Packet> ctsStream = bus.read(T3, TimeUnit.MILLISECONDS)
                                      .filter(controlMessageFilter);
        Packet response = bus.send(rts);

        // wait for CTS
        Optional<Packet> ctsOptional = ctsStream.findFirst();
        while (ctsOptional.map(p -> p.get(0) == CM_CTS).orElse(false)) {
            Packet cts = ctsOptional.get();
            fine("rx CTS", cts);

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
                    Packet dp = createPacket(DT | destinationAddress, getAddress(), buf);

                    fine("tx DP", dp);
                    response = bus.send(dp);
                }
                // wait for CTS or EOM
                ctsOptional = bus.read(T3, TimeUnit.MILLISECONDS).filter(controlMessageFilter).findFirst();
            }
        }
        ctsOptional.ifPresent(eom -> fine("rx EOM", eom));

        if (ctsOptional.map(p -> p.get(0) == CM_ConnAbort).orElse(false)) {
            // FAIL
            warn("Abort received: " + getAbortError(ctsOptional.get().get(1)));
        } else if (ctsOptional.map(p -> p.get(0) != CM_EndOfMessageACK).orElse(true)) {
            // verify EOM
            warn((ctsOptional.isPresent() ? "CTS" : "EOM") + " not received.");
            throw ctsOptional.map(p -> (BusException) new EomBusException())
                             .orElse(new CtsBusException());
        }
        return response;
    }

    private Packet createPacket(int id,
                                int source,
                                int... buf) {
        return new Packet(LocalDateTime.now(), 7, id, source, false, buf);
    }

    private Packet createPacket(int id, int source, byte... buf) {
        return Packet.create(7, id, source, false, buf);
    }

    public void warn(String msg, Object... a) {
        logger.warning(String.format(msg, a));
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

    @Override
    public boolean imposterDetected() {
        return bus.imposterDetected();
    }

    public void setPassAll(boolean b) {
        passAll = b;
    }

    public boolean isPassAll() {
        return passAll;
    }

    @Override
    public Bus getRawBus() {
        return bus;
    }
}

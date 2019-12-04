package org.etools.j1939_84.bus.j1939;

import java.util.BitSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.MultiQueue;
import org.etools.j1939_84.bus.Packet;

public class J1939TP {
    final static public int CM = 0xEC00;
    final static public int DT = 0xEB00;
    final static private int RTS = 16;
    final static private int CTS = 17;
    final static private int EOM = 19;
    final static private int ABORT = 255;
    final static private int BAM = 32;

    final static public int T1 = 750;
    final static public int T2 = 1250;
    final static public int T3 = 1250;
    final static public int T4 = 1050;
    final static public int Th = 500;
    final static public int Tr = 200;

    final private Bus bus;

    public J1939TP(Bus bus) {
        this.bus = bus;
    }

    public Packet receive(Packet rts) throws BusException {
        int numberOfPackets = rts.get(3);
        int maxResponsePackets = rts.get(4);

        byte[] data = new byte[rts.get16(1)];
        BitSet received = new BitSet(numberOfPackets);

        while (true) {
            Stream<Packet> s = bus.read(T2, TimeUnit.MILLISECONDS)
                    .filter(p -> p.getSource() == rts.getSource() && (p.getId() & 0xFF00) == DT)
                    .peek(p -> { // reset timeout for each DT
                        throw new MultiQueue.ResetTimeoutException(T1, TimeUnit.MILLISECONDS);
                    })
                    .limit(maxResponsePackets);
            bus.send(Packet.create(CM | rts.getSource(),
                    bus.getAddress(),
                    CTS,
                    Math.min(maxResponsePackets, received.clearbits()),
                    received.nextClearBit(0) + 1,
                    0xFF,
                    0xFF,
                    rts.get(5),
                    rts.get(6),
                    rts.get(7)));
            s.forEach(p -> {
                received.set(p.get(0));
                int offset = p.get(0) * 7;
                System.arraycopy(p.getBytes(), 1, data, offset, Math.min(offset + 7, data.length) - offset);
            });
        }
        return Packet.create(rts.get24(5), rts.getSource(), data);
    }

    public void send(Packet packet) throws BusException {
        if (packet.getLength() <= 8) {
            bus.send(packet);
        } else {
            int destinationAddress = packet.getSource();
            int CM = 0xEC00;

            Stream<Packet> s = bus.read(T3, TimeUnit.MILLISECONDS)
                    .filter(p -> p.getSource() == destinationAddress && (p.getId() & 0xFF00) == CM
                            && p.get(0) == CTS);
            // send RTS
            s.send(tpCmRts(packet));

            // wait for CTS
            Optional<Packet> cts = s.findFirst();

            while (cts.map(p -> p.get(0) == 17).orElse(false)) {
                Packet p2 = cts.get();// FIXME
                int packetsToSend = p2.get(1);
                int nextPacket = p2.get(2);
                if (p2.get16(3) != 0xFFFF) {
                    warn("TP.CM_CTS bytes 4-5 should be FFFF");
                }
                int pgn = p2.get24(5);
                // send data
                for (int i = 0; i < packetsToSend; i++) {
                    bus.send(tpDt(i, nextPacket, packet));
                }
                // wait for CTS or EOM
                cts = bus.read(T3, TimeUnit.MILLISECONDS)
                        .filter(p -> p.getSource() == destinationAddress && (p.getId() & 0xFF00) == CM)
                        .findFirst();
            }
            verify EOM
        }
    }

    private void warn(String msg) {
        System.err.println("WARN: " + msg);
    }
}

package org.etools.j1939tools.modules;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.etools.j1939_84.J1939_84.NL;

public class NOxBinningModuleTest {

    private static final int ADDR = 0;

    private NOxBinningModule instance;

    @Before
    public void setUp() throws Exception {
        instance = new NOxBinningModule(new TestDateTimeModule());
    }

    @Test
    public void testFormat() {
        List<Packet> packets = new ArrayList<>();

        packets.add(Packet.create(0xFB02, ADDR,
        // @formatter:off
                                  0x40, 0x84, 0x00, 0x10, 0x41, 0x84, 0x00, 0x10,
                                  0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                  0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                  0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                  0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                  0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                  0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                  0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                  0x5F, 0x84, 0x00, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB03, ADDR,
        // @formatter:off
                                  0x60, 0x84, 0x00, 0x10, 0x61, 0x84, 0x00, 0x10,
                                  0x63, 0x84, 0x00, 0x10, 0x65, 0x84, 0x00, 0x10,
                                  0x67, 0x84, 0x00, 0x10, 0x69, 0x84, 0x00, 0x10,
                                  0x6B, 0x84, 0x00, 0x10, 0x6D, 0x84, 0x00, 0x10,
                                  0x6F, 0x84, 0x00, 0x10, 0x71, 0x84, 0x00, 0x10,
                                  0x73, 0x84, 0x00, 0x10, 0x75, 0x84, 0x00, 0x10,
                                  0x77, 0x84, 0x00, 0x10, 0x79, 0x84, 0x00, 0x10,
                                  0x7B, 0x84, 0x00, 0x10, 0x7D, 0x84, 0x00, 0x10,
                                  0x7F, 0x84, 0x08, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB04, ADDR,
        // @formatter:off
                                  0x80, 0x84, 0x00, 0x10, 0x81, 0x84, 0x00, 0x10,
                                  0x83, 0x84, 0x00, 0x10, 0x85, 0x84, 0x00, 0x10,
                                  0x87, 0x84, 0x00, 0x10, 0x89, 0x84, 0x00, 0x10,
                                  0x8B, 0x84, 0x00, 0x10, 0x8D, 0x84, 0x00, 0x10,
                                  0x8F, 0x84, 0x00, 0x10, 0x91, 0x84, 0x00, 0x10,
                                  0x93, 0x84, 0x00, 0x10, 0x95, 0x84, 0x00, 0x10,
                                  0x97, 0x84, 0x00, 0x10, 0x99, 0x84, 0x00, 0x10,
                                  0x9B, 0x84, 0x00, 0x10, 0x9D, 0x84, 0x00, 0x10,
                                  0x9F, 0x84, 0x00, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB05, ADDR,
        // @formatter:off
                                  0xA0, 0x84, 0x00, 0x10, 0xA1, 0x84, 0x00, 0x10,
                                  0xA3, 0x84, 0x00, 0x10, 0xA5, 0x84, 0x00, 0x10,
                                  0xA7, 0x84, 0x00, 0x10, 0xA9, 0x84, 0x00, 0x10,
                                  0xAB, 0x84, 0x00, 0x10, 0xAD, 0x84, 0x00, 0x10,
                                  0xAF, 0x84, 0x00, 0x10, 0xB1, 0x84, 0x00, 0x10,
                                  0xB3, 0x84, 0x00, 0x10, 0xB5, 0x84, 0x00, 0x10,
                                  0xB7, 0x84, 0x00, 0x10, 0xB9, 0x84, 0x00, 0x10,
                                  0xBB, 0x84, 0x00, 0x10, 0xBD, 0x84, 0x00, 0x10,
                                  0xBF, 0x84, 0x00, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB06, ADDR,
        // @formatter:off
                                  0x00, 0x04, 0x00, 0x0D, 0x01, 0x04, 0x00, 0x0D,
                                  0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                  0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                  0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                  0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                  0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                  0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                  0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                  0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB07, ADDR,
        // @formatter:off
                                  0x20, 0x04, 0x00, 0x0D, 0x21, 0x04, 0x00, 0x0D,
                                  0x23, 0x04, 0x00, 0x0D, 0x25, 0x04, 0x00, 0x0D,
                                  0x27, 0x04, 0x00, 0x0D, 0x29, 0x04, 0x00, 0x0D,
                                  0x2B, 0x04, 0x00, 0x0D, 0x2D, 0x04, 0x00, 0x0D,
                                  0x2F, 0x04, 0x00, 0x0D, 0x31, 0x04, 0x00, 0x0D,
                                  0x33, 0x04, 0x00, 0x0D, 0x35, 0x04, 0x00, 0x0D,
                                  0x37, 0x04, 0x00, 0x0D, 0x39, 0x04, 0x00, 0x0D,
                                  0x3B, 0x04, 0x00, 0x0D, 0x3D, 0x04, 0x00, 0x0D,
                                  0x3F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB08, ADDR,
        // @formatter:off
                                  0x40, 0x04, 0x00, 0x0D, 0x41, 0x04, 0x00, 0x0D,
                                  0x43, 0x04, 0x00, 0x0D, 0x45, 0x04, 0x00, 0x0D,
                                  0x47, 0x04, 0x00, 0x0D, 0x49, 0x04, 0x00, 0x0D,
                                  0x4B, 0x04, 0x00, 0x0D, 0x4D, 0x04, 0x00, 0x0D,
                                  0x4F, 0x04, 0x00, 0x0D, 0x51, 0x04, 0x00, 0x0D,
                                  0x53, 0x04, 0x00, 0x0D, 0x55, 0x04, 0x00, 0x0D,
                                  0x57, 0x04, 0x00, 0x0D, 0x59, 0x04, 0x00, 0x0D,
                                  0x5B, 0x04, 0x00, 0x0D, 0x5D, 0x04, 0x00, 0x0D,
                                  0x5F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB09, ADDR,
        // @formatter:off
                                  0x60, 0x04, 0x00, 0x0D, 0x61, 0x04, 0x00, 0x0D,
                                  0x63, 0x04, 0x00, 0x0D, 0x65, 0x04, 0x00, 0x0D,
                                  0x67, 0x04, 0x00, 0x0D, 0x69, 0x04, 0x00, 0x0D,
                                  0x6B, 0x04, 0x00, 0x0D, 0x6D, 0x04, 0x00, 0x0D,
                                  0x6F, 0x04, 0x00, 0x0D, 0x71, 0x04, 0x00, 0x0D,
                                  0x73, 0x04, 0x00, 0x0D, 0x75, 0x04, 0x00, 0x0D,
                                  0x77, 0x04, 0x00, 0x0D, 0x79, 0x04, 0x00, 0x0D,
                                  0x7B, 0x04, 0x00, 0x0D, 0x7D, 0x04, 0x00, 0x0D,
                                  0x7F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB0A, ADDR,
        // @formatter:off
                                  0x80, 0x04, 0x00, 0x0D, 0x81, 0x04, 0x00, 0x0D,
                                  0x83, 0x04, 0x00, 0x0D, 0x85, 0x04, 0x00, 0x0D,
                                  0x87, 0x04, 0x00, 0x0D, 0x89, 0x04, 0x00, 0x0D,
                                  0x8B, 0x04, 0x00, 0x0D, 0x8D, 0x04, 0x00, 0x0D,
                                  0x8F, 0x04, 0x00, 0x0D, 0x91, 0x04, 0x00, 0x0D,
                                  0x93, 0x04, 0x00, 0x0D, 0x95, 0x04, 0x00, 0x0D,
                                  0x97, 0x04, 0x00, 0x0D, 0x99, 0x04, 0x00, 0x0D,
                                  0x9B, 0x04, 0x00, 0x0D, 0x9D, 0x04, 0x00, 0x0D,
                                  0x9F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB0B, ADDR,
        // @formatter:off
                                  0xA0, 0x04, 0x00, 0x0D, 0xA1, 0x04, 0x00, 0x0D,
                                  0xA3, 0x04, 0x00, 0x0D, 0xA5, 0x04, 0x00, 0x0D,
                                  0xA7, 0x04, 0x00, 0x0D, 0xA9, 0x04, 0x00, 0x0D,
                                  0xAB, 0x04, 0x00, 0x0D, 0xAD, 0x04, 0x00, 0x0D,
                                  0xAF, 0x04, 0x00, 0x0D, 0xB1, 0x04, 0x00, 0x0D,
                                  0xB3, 0x04, 0x00, 0x0D, 0xB5, 0x04, 0x00, 0x0D,
                                  0xB7, 0x04, 0x00, 0x0D, 0xB9, 0x04, 0x00, 0x0D,
                                  0xBB, 0x04, 0x00, 0x0D, 0xBD, 0x04, 0x00, 0x0D,
                                  0xBF, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB0C, ADDR,
        // @formatter:off
                                  0x00, 0x84, 0x01, 0x84, 0x03, 0x84, 0x05, 0x84,
                                  0x07, 0x84, 0x09, 0x84, 0x0B, 0x84, 0x0D, 0x84,
                                  0x0F, 0x84, 0x11, 0x84, 0x13, 0x84, 0x15, 0x84,
                                  0x17, 0x84, 0x19, 0x84, 0x1B, 0x84, 0x1D, 0x84,
                                  0x1F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB0D, ADDR,
        // @formatter:off
                                  0x20, 0x84, 0x21, 0x84, 0x23, 0x84, 0x25, 0x84,
                                  0x27, 0x84, 0x29, 0x84, 0x2B, 0x84, 0x2D, 0x84,
                                  0x2F, 0x84, 0x31, 0x84, 0x33, 0x84, 0x35, 0x84,
                                  0x37, 0x84, 0x39, 0x84, 0x3B, 0x84, 0x3D, 0x84,
                                  0x3F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB0E, ADDR,
        // @formatter:off
                                  0x40, 0x84, 0x41, 0x84, 0x43, 0x84, 0x45, 0x84,
                                  0x47, 0x84, 0x49, 0x84, 0x4B, 0x84, 0x4D, 0x84,
                                  0x4F, 0x84, 0x51, 0x84, 0x53, 0x84, 0x55, 0x84,
                                  0x57, 0x84, 0x59, 0x84, 0x5B, 0x84, 0x5D, 0x84,
                                  0x5F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB0F, ADDR,
        // @formatter:off
                                  0x60, 0x84, 0x61, 0x84, 0x63, 0x84, 0x65, 0x84,
                                  0x67, 0x84, 0x69, 0x84, 0x6B, 0x84, 0x6D, 0x84,
                                  0x6F, 0x84, 0x71, 0x84, 0x73, 0x84, 0x75, 0x84,
                                  0x77, 0x84, 0x79, 0x84, 0x7B, 0x84, 0x7D, 0x84,
                                  0x7F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB10, ADDR,
        // @formatter:off
                                  0x80, 0x84, 0x00, 0x00, 0x81, 0x84, 0x00, 0x00,
                                  0x83, 0x84, 0x00, 0x00, 0x85, 0x84, 0x00, 0x00,
                                  0x87, 0x84, 0x00, 0x00, 0x89, 0x84, 0x00, 0x00,
                                  0x8B, 0x84, 0x00, 0x00, 0x8D, 0x84, 0x00, 0x00,
                                  0x8F, 0x84, 0x00, 0x00, 0x91, 0x84, 0x00, 0x00,
                                  0x93, 0x84, 0x00, 0x00, 0x95, 0x84, 0x00, 0x00,
                                  0x97, 0x84, 0x00, 0x00, 0x99, 0x84, 0x00, 0x00,
                                  0x9B, 0x84, 0x00, 0x00, 0x9D, 0x84, 0x00, 0x00,
                                  0x9F, 0x84, 0x00, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFB11, ADDR,
        // @formatter:off
                                  0xA0, 0x84, 0x00, 0x00, 0xA1, 0x84, 0x00, 0x00,
                                  0xA3, 0x84, 0x00, 0x00, 0xA5, 0x84, 0x00, 0x00,
                                  0xA7, 0x84, 0x00, 0x00, 0xA9, 0x84, 0x00, 0x00,
                                  0xAB, 0x84, 0x00, 0x00, 0xAD, 0x84, 0x00, 0x00,
                                  0xAF, 0x84, 0x00, 0x00, 0xB1, 0x84, 0x00, 0x00,
                                  0xB3, 0x84, 0x00, 0x00, 0xB5, 0x84, 0x00, 0x00,
                                  0xB7, 0x84, 0x00, 0x00, 0xB9, 0x84, 0x00, 0x00,
                                  0xBB, 0x84, 0x00, 0x00, 0xBD, 0x84, 0x00, 0x00,
                                  0xBF, 0x84, 0x00, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFB12, ADDR,
        // @formatter:off
                                  0x00, 0x04, 0x01, 0x04, 0x03, 0x04, 0x05, 0x04,
                                  0x07, 0x04, 0x09, 0x04, 0x0B, 0x04, 0x0D, 0x04,
                                  0x0F, 0x04, 0x11, 0x04, 0x13, 0x04, 0x15, 0x04,
                                  0x17, 0x04, 0x19, 0x04, 0x1B, 0x04, 0x1D, 0x04,
                                  0x1F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB13, ADDR,
        // @formatter:off
                                  0x20, 0x04, 0x21, 0x04, 0x23, 0x04, 0x25, 0x04,
                                  0x27, 0x04, 0x29, 0x04, 0x2B, 0x04, 0x2D, 0x04,
                                  0x2F, 0x04, 0x31, 0x04, 0x33, 0x04, 0x35, 0x04,
                                  0x37, 0x04, 0x39, 0x04, 0x3B, 0x04, 0x3D, 0x04,
                                  0x3F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB14, ADDR,
        // @formatter:off
                                  0x40, 0x04, 0x41, 0x04, 0x43, 0x04, 0x45, 0x04,
                                  0x47, 0x04, 0x49, 0x04, 0x4B, 0x04, 0x4D, 0x04,
                                  0x4F, 0x04, 0x51, 0x04, 0x53, 0x04, 0x55, 0x04,
                                  0x57, 0x04, 0x59, 0x04, 0x5B, 0x04, 0x5D, 0x04,
                                  0x5F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB15, ADDR,
        // @formatter:off
                                  0x60, 0x04, 0x61, 0x04, 0x63, 0x04, 0x65, 0x04,
                                  0x67, 0x04, 0x69, 0x04, 0x6B, 0x04, 0x6D, 0x04,
                                  0x6F, 0x04, 0x71, 0x04, 0x73, 0x04, 0x75, 0x04,
                                  0x77, 0x04, 0x79, 0x04, 0x7B, 0x04, 0x7D, 0x04,
                                  0x7F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB16, ADDR,
        // @formatter:off
                                  0x80, 0x04, 0x00, 0x00, 0x81, 0x04, 0x00, 0x00,
                                  0x83, 0x04, 0x00, 0x00, 0x85, 0x04, 0x00, 0x00,
                                  0x87, 0x04, 0x00, 0x00, 0x89, 0x04, 0x00, 0x00,
                                  0x8B, 0x04, 0x00, 0x00, 0x8D, 0x04, 0x00, 0x00,
                                  0x8F, 0x04, 0x00, 0x00, 0x91, 0x04, 0x00, 0x00,
                                  0x93, 0x04, 0x00, 0x00, 0x95, 0x04, 0x00, 0x00,
                                  0x97, 0x04, 0x00, 0x00, 0x99, 0x04, 0x00, 0x00,
                                  0x9B, 0x04, 0x00, 0x00, 0x9D, 0x04, 0x00, 0x00,
                                  0x9F, 0x04, 0x00, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFB17, ADDR,
        // @formatter:off
                                  0xA0, 0x04, 0x00, 0x00, 0xA1, 0x04, 0x00, 0x00,
                                  0xA3, 0x04, 0x00, 0x00, 0xA5, 0x04, 0x00, 0x00,
                                  0xA7, 0x04, 0x00, 0x00, 0xA9, 0x04, 0x00, 0x00,
                                  0xAB, 0x04, 0x00, 0x00, 0xAD, 0x04, 0x00, 0x00,
                                  0xAF, 0x04, 0x00, 0x00, 0xB1, 0x04, 0x00, 0x00,
                                  0xB3, 0x04, 0x00, 0x00, 0xB5, 0x04, 0x00, 0x00,
                                  0xB7, 0x04, 0x00, 0x00, 0xB9, 0x04, 0x00, 0x00,
                                  0xBB, 0x04, 0x00, 0x00, 0xBD, 0x04, 0x00, 0x00,
                                  0xBF, 0x04, 0x00, 0x00));
        // @formatter:on

        var genericPackets = packets.stream().map(GenericPacket::new).collect(Collectors.toList());

        String actual = instance.format(genericPackets);

        String expected = "";
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |"
                + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |"
                + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "| Bin  1 (Total)            |            0 |            1 |        1,120 |          512 |          176 |          272 |"
                + NL;
        expected += "| Bin  2 (Idle)             |            0 |            1 |        1,121 |          512 |          176 |          272 |"
                + NL;
        expected += "| Bin  3 (<25%, <16kph)     |            0 |            1 |        1,123 |          514 |          176 |          273 |"
                + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |            0 |            1 |        1,125 |          514 |          177 |          273 |"
                + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |            0 |            1 |        1,127 |          516 |          177 |          274 |"
                + NL;
        expected += "| Bin  6 (<25%, >64kph)     |            0 |            1 |        1,129 |          516 |          178 |          274 |"
                + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |            0 |            1 |        1,131 |          518 |          178 |          275 |"
                + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |            0 |            1 |        1,133 |          518 |          178 |          275 |"
                + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |            0 |            1 |        1,135 |          520 |          178 |          276 |"
                + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |            0 |            1 |        1,137 |          520 |          179 |          276 |"
                + NL;
        expected += "| Bin 11 (>50%, <16kph)     |            0 |            1 |        1,139 |          522 |          179 |          277 |"
                + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |            0 |            1 |        1,141 |          522 |          180 |          277 |"
                + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |            0 |            1 |        1,143 |          524 |          180 |          278 |"
                + NL;
        expected += "| Bin 14 (>50%, >64kph)     |            0 |            1 |        1,145 |          524 |          180 |          278 |"
                + NL;
        expected += "| Bin 15 (NTE)              |            0 |            1 |        1,147 |          526 |          180 |          279 |"
                + NL;
        expected += "| Bin 16 (Regen)            |            0 |            1 |        1,149 |          526 |          181 |          279 |"
                + NL;
        expected += "| Bin 17 (MIL On)           |            0 |            1 |        1,151 |          528 |          181 |          280 |"
                + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |"
                + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |"
                + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "| Bin  1 (Total)            |            7 |           17 |       33,888 |       16,896 |        5,637 |        8,464 |"
                + NL;
        expected += "| Bin  2 (Idle)             |            7 |           17 |       33,889 |       16,896 |        5,638 |        8,464 |"
                + NL;
        expected += "| Bin  3 (<25%, <16kph)     |            7 |           17 |       33,891 |       16,898 |        5,638 |        8,465 |"
                + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |            7 |           17 |       33,893 |       16,898 |        5,638 |        8,465 |"
                + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |            7 |           17 |       33,895 |       16,900 |        5,638 |        8,466 |"
                + NL;
        expected += "| Bin  6 (<25%, >64kph)     |            7 |           17 |       33,897 |       16,900 |        5,639 |        8,466 |"
                + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |            7 |           17 |       33,899 |       16,902 |        5,639 |        8,467 |"
                + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |            7 |           17 |       33,901 |       16,902 |        5,640 |        8,467 |"
                + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |            7 |           17 |       33,903 |       16,904 |        5,640 |        8,468 |"
                + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |            7 |           17 |       33,905 |       16,904 |        5,640 |        8,468 |"
                + NL;
        expected += "| Bin 11 (>50%, <16kph)     |            7 |           17 |       33,907 |       16,906 |        5,640 |        8,469 |"
                + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |            7 |           17 |       33,909 |       16,906 |        5,641 |        8,469 |"
                + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |            7 |           17 |       33,911 |       16,908 |        5,641 |        8,470 |"
                + NL;
        expected += "| Bin 14 (>50%, >64kph)     |            7 |           17 |       33,913 |       16,908 |        5,642 |        8,470 |"
                + NL;
        expected += "| Bin 15 (NTE)              |            7 |           17 |       33,915 |       16,910 |        5,642 |        8,471 |"
                + NL;
        expected += "| Bin 16 (Regen)            |            7 |           17 |       33,917 |       16,910 |        5,642 |        8,471 |"
                + NL;
        expected += "| Bin 17 (MIL On)           |            7 |           17 |       33,919 |       16,912 |        5,642 |        8,472 |"
                + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |"
                + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |"
                + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "| Bin  1 (Total)            |  218,104,992 |  218,104,960 |  218,104,928 |   10,905,242 |    3,635,081 |    1,090,524 |"
                + NL;
        expected += "| Bin  2 (Idle)             |  218,104,993 |  218,104,961 |  218,104,929 |   10,905,242 |    3,635,081 |    1,090,524 |"
                + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,995 |  218,104,963 |  218,104,931 |   10,905,242 |    3,635,081 |    1,090,524 |"
                + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,997 |  218,104,965 |  218,104,933 |   10,905,242 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,999 |  218,104,967 |  218,104,935 |   10,905,242 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,105,001 |  218,104,969 |  218,104,937 |   10,905,242 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,105,003 |  218,104,971 |  218,104,939 |   10,905,242 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,105,005 |  218,104,973 |  218,104,941 |   10,905,242 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,105,007 |  218,104,975 |  218,104,943 |   10,905,242 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,105,009 |  218,104,977 |  218,104,945 |   10,905,242 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,105,011 |  218,104,979 |  218,104,947 |   10,905,243 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,105,013 |  218,104,981 |  218,104,949 |   10,905,243 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,105,015 |  218,104,983 |  218,104,951 |   10,905,243 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,105,017 |  218,104,985 |  218,104,953 |   10,905,243 |    3,635,081 |    1,090,525 |"
                + NL;
        expected += "| Bin 15 (NTE)              |  218,105,019 |  218,104,987 |  218,104,955 |   10,905,243 |    3,635,082 |    1,090,525 |"
                + NL;
        expected += "| Bin 16 (Regen)            |  218,105,021 |  218,104,989 |  218,104,957 |   10,905,243 |    3,635,082 |    1,090,525 |"
                + NL;
        expected += "| Bin 17 (MIL On)           |  218,105,023 |  218,104,991 |  218,104,959 |   10,905,243 |    3,635,082 |    1,090,525 |"
                + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|"
                + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  268,469,408 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  2 (Idle)             |  268,469,409 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  268,469,411 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  268,469,413 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  268,469,415 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  268,469,417 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  268,469,419 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  268,469,421 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  268,469,423 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  268,469,425 |   13,423,466 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  268,469,427 |   13,423,467 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  268,469,429 |   13,423,467 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  268,469,431 |   13,423,467 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  268,469,433 |   13,423,467 |    4,474,489 |    1,342,347 |" + NL;
        expected += "| Bin 15 (NTE)              |  268,469,435 |   13,423,467 |    4,474,490 |    1,342,347 |" + NL;
        expected += "| Bin 16 (Regen)            |  268,469,437 |   13,423,467 |    4,474,490 |    1,342,347 |" + NL;
        expected += "| Bin 17 (MIL On)           |  268,469,439 |   13,423,467 |    4,483,228 |    1,342,347 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "" + NL;

        assertEquals(expected, actual);
    }

    static public String expectedReqests() {
        String expectedMsg = "Requesting NOx Tracking Valid NOx Lifetime System Out NOx Mass Bins (NTSNV) from Engine #1 (0)"
                + NL
                + "Requesting NOx Tracking Valid NOx Lifetime Engine Out NOx Mass Bins (NTENV) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Valid NOx Lifetime Engine Output Energy Bins (NTEEV) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Valid NOx Lifetime Fuel Consumption Bins (NTFCV) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Valid NOx Lifetime Engine Run Time Bins (NTEHV) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Valid NOx Lifetime Vehicle Distance Bins (NTVMV) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Engine Activity Lifetime Engine Output Energy Bins (NTEEEA) from Engine #1 (0)"
                + NL
                + "Requesting NOx Tracking Engine Activity Lifetime Fuel Consumption Bins (NTFCEA) from Engine #1 (0)"
                + NL
                + "Requesting NOx Tracking Engine Activity Lifetime Engine Run Time Bins (NTEHEA) from Engine #1 (0)"
                + NL
                + "Requesting NOx Tracking Engine Activity Lifetime Vehicle Distance Bins (NTVMEA) from Engine #1 (0)"
                + NL
                + "Requesting NOx Tracking Active 100 Hour System Out NOx Mass Bins (NTSNA) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Active 100 Hour Engine Out NOx Mass Bins (NTENA) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Active 100 Hour Engine Output Energy Bins (NTEEA) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Active 100 Hour Fuel Consumption Bins (NTFCA) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Active 100 Hour Engine Run Time Bins (NTEHA) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Active 100 Hour Vehicle Distance Bins (NTVMA) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Stored 100 Hour System Out NOx Mass Bins (NTSNS) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Stored 100 Hour Engine Out NOx Mass Bins (NTENS) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Stored 100 Hour Engine Output Energy Bins (NTEES) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Stored 100 Hour Fuel Consumption Bins (NTFCS) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Stored 100 Hour Engine Run Time Bins (NTEHS) from Engine #1 (0)" + NL
                + "Requesting NOx Tracking Stored 100 Hour Vehicle Distance Bins (NTVMS) from Engine #1 (0)";
        return expectedMsg;
    }
}

package org.etools.j1939tools.modules;

import static junit.framework.TestCase.assertEquals;
import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.junit.Before;
import org.junit.Test;

public class GhgTrackingArrayModuleTest {
    private static final int ADDR = 0;

    private GhgTrackingArrayModule instance;

    @Before
    public void setUp() throws Exception {
        instance = new GhgTrackingArrayModule(new TestDateTimeModule());
    }

    @Test
    public void testFormat() {
        List<Packet> packets = new ArrayList<>();

        packets.add(Packet.create(0xFAFE, ADDR,
                                  // @formatter:off
                                  0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                  0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                  0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                  0x05, 0x00, 0x46, 0x05));
        // @formatter:on

        packets.add(Packet.create(0xFAFC, ADDR,
                                  // @formatter:off
                                  0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                  0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                  0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                  0x07, 0x00, 0x08, 0x07));
        // @formatter:on

        packets.add(Packet.create(0xFAFD, ADDR,
                                  // @formatter:off
                                  0xB0, 0x30, 0x2C, 0x02, 0x58, 0x94, 0x62, 0x06,
                                  0x7A, 0xD6, 0x05, 0x00, 0x5D, 0x30, 0x1D, 0x00,
                                  0x27, 0x76, 0x4A, 0x00, 0x4F, 0xD6, 0xF8, 0x0B,
                                  0x9D, 0xE7, 0x0D, 0x00, 0x2E, 0x06, 0x00, 0x00,
                                  0x4A, 0x18, 0x61, 0x01, 0xCC, 0x3D, 0x00, 0x00,
                                  0xD9, 0x02, 0x00, 0x00, 0x3B, 0xCF, 0x1B, 0x00));
        // @formatter:on
        packets.add(Packet.create(0xFAF6, ADDR,
                                  // @formatter:off
                                  0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                  0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on

        packets.add(Packet.create(0xFAF5, ADDR,
                                  // @formatter:off
                                  0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                  0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on

        packets.add(Packet.create(0xFAF4,
                                  ADDR,
                                  // @formatter:off
                                  0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                  0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                  0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                  0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFB00,
                                  ADDR,
                                  // @formatter:off
                                  0x06, 0xAD, 0x01, 0x68, 0x01,
                                  0x04, 0x03, 0x04, 0x58, 0x03,
                                  0x02, 0x23, 0x00, 0x1C, 0x00,
                                  0xF9, 0x3D, 0x01, 0x08, 0x01,
                                  0xF7, 0x49, 0x00, 0x3C, 0x00,
                                  0xF5, 0xDB, 0x00, 0xB8, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFAFF,
                                  ADDR,
                                  // @formatter:off
                                  0x06, 0x5B, 0x32, 0x34, 0x04,
                                  0x04, 0x06, 0x08, 0xB0, 0x06,
                                  0x02, 0x6B, 0x00, 0x58, 0x00,
                                  0xF9, 0x7A, 0x02, 0x10, 0x02,
                                  0xF7, 0xDC, 0x00, 0x74, 0x22,
                                  0xF5, 0x91, 0x02, 0x24, 0x02));
        // @formatter:on

        packets.add(Packet.create(0xFB01,
                                  ADDR,
                                  // @formatter:off
                                  0x06, 0x7D, 0x60, 0x10, 0x00, 0xC0, 0xBC, 0x05, 0x00,
                                  0x04, 0xCE, 0x31, 0x02, 0x00, 0x02, 0x49, 0x1D, 0x00,
                                  0x02, 0x00, 0xE0, 0x79, 0x00, 0x00, 0x00, 0x00, 0x00,
                                  0xF9, 0x86, 0xAD, 0x00, 0x00, 0xA8, 0xD2, 0x02, 0x00,
                                  0xF7, 0x4B, 0xC3, 0x00, 0x00, 0x90, 0xFB, 0x00, 0x00,
                                  0xF5, 0xD0, 0xB3, 0x00, 0x00, 0x38, 0xED, 0x02, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFAF3, ADDR, 0x78, 0x69, 0x8C, 0x0A, 0x8E, 0x44, 0xFF, 0xFF));

        packets.add(Packet.create(0xFAF2, ADDR, 0xA0, 0x8C, 0x10, 0x0E, 0x68, 0x5B, 0xFF, 0xFF));

        packets.add(Packet.create(0xFAF1,
                                  ADDR,
                                  // @formatter:off
                                  0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                  0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on

        var genericPackets = packets.stream().map(GenericPacket::new).collect(Collectors.toList());

        String actual = instance.format(genericPackets);

        String expected = "";
        expected += "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       2,077 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |       7,053 |         139 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |      18,988 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |         817 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |      14,170 |      54,906 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 | 811,401,221 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |           5 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |       5,041 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |          37 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       9,144 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |       1,532 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |      59,293 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |           2 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        assertEquals(expected, actual);
    }

}
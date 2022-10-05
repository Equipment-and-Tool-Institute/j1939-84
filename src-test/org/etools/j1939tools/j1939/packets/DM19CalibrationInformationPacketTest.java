/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.EchoBus;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.J1939TP;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.junit.Test;

/**
 * Unit tests the {@link DM19CalibrationInformationPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM19CalibrationInformationPacketTest {

    /**
     * Test method for {@link DM19CalibrationInformationPacket#toString()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one good CVN/Cal Id <br>
     * w/ padding</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalibrationInformationAndToStringWithOne() {
        Packet packet = Packet.create(0,
                                      0x00,
                                      0x51,     // CVN
                                      0xBA,
                                      0xFE,
                                      0xBD,
                                      0x41,         // CALID
                                      0x4E,
                                      0x54,
                                      0x35,
                                      0x41,
                                      0x53,
                                      0x52,
                                      0x31,
                                      0x20,
                                      0x20,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(1, calInfos.size());
        CalibrationInformation calInfo = calInfos.get(0);
        assertEquals("ANT5ASR1        ", calInfo.getCalibrationIdentification());
        assertEquals("0xBDFEBA51", calInfo.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 65, 78, 84, 53, 65, 83, 82, 49, 32, 32, 0, 0, 0, 0, 0, 0 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 81, -70, -2, -67 }, calInfo.getRawCvn());

        String expected = "DM19 from Engine #1 (0): CAL ID of ANT5ASR1 and CVN of 0xBDFEBA51";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">5 CVN/Cal Id combos<br>
     * 4 good<br>
     * 1 empty</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testRealDm19PulledFromTruck() {
        Packet packet = Packet.create(DM19CalibrationInformationPacket.PGN,
                                      0x00,
                                      // LSB CVN; 4 bytes (checksum value of entire calibration) padding at MSB w/ 0x00
                                      0x06, // ASCII value of ACK - unprintable char
                                      0x7A, // z
                                      0x6E, // n
                                      0xC9, // É - unprintable char
                                      // LSB Cal ID; 16 bytes; Padding at LSB w/ 0x00
                                      0x41, // A
                                      0x32, // 2
                                      0x36, // 36
                                      0x31, // 1
                                      0x58, // X
                                      0x58, // X
                                      0x4D,  // M
                                      0x5F,  // -
                                      0x45, // E
                                      0x37,  // 7
                                      0x31, // 1
                                      0x31, // 1
                                      0x45, // E
                                      0x33, // 3
                                      0x31, // 1
                                      0x44, // D
                                      // LSB; 4 bytes (checksum value of entire calibration)
                                      0xA8, // - unprintable
                                      0x73, // 5
                                      0x89, // undefined
                                      0x13, // DC3 Ascii char undefined?
                                      // LSB Cal ID; 16 bytes; Padding at LSB
                                      0x4E, // N
                                      0x4F, // O
                                      0x78, // x
                                      0x2D, // -
                                      0x53, // S
                                      0x41, // A
                                      0x45, // E
                                      0x31, // 1
                                      0x34, // 4
                                      0x61, // a
                                      0x20, // " " - space
                                      0x41, // A
                                      0x54, // T
                                      0x49, // I
                                      0x31, // 1
                                      0x00, // NUL
                                      // LSB; 4 bytes (checksum value of entire calibration)
                                      0x8C, // ¼ - unprintable
                                      0x4B, // K
                                      0xF9, // ù - unprintable
                                      0xC9, // É - unprintable
                                      // LSB Cal ID; 16 bytes; Padding at LSB
                                      0x4E, // N
                                      0x4F, // O
                                      0x78, // x
                                      0x2D, // -
                                      0x53, // S
                                      0x41, // A
                                      0x45, // E
                                      0x31, // 1
                                      0x34, // 4
                                      0x61, // a
                                      0x20, // " " - space
                                      0x41, // A
                                      0x54, // T
                                      0x4F, // 0
                                      0x31, // 1
                                      0x00, // NUL
                                      // LSB; 4 bytes (checksum value of entire calibration)
                                      0x00, // NUL
                                      0x00, // NUL
                                      0x00, // NUL
                                      0x00, // NUL
                                      // LSB Cal ID; 16 bytes; Padding at LSB
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      0xFF, // ÿ
                                      // LSB; 4 bytes (checksum value of entire calibration)
                                      0xD2, // Ò - unprintable
                                      0xBF, // ¿
                                      0x0F, // undefined
                                      0xA9, // ©
                                      // LSB Cal ID; 16 bytes; Padding at LSB
                                      0x50, // P
                                      0x4D, // M
                                      0x53, // S
                                      0x31, // 1
                                      0x32, // 2
                                      0x33, // 3
                                      0x34, // 4
                                      0x31, // 1
                                      0x41, // A
                                      0x31, // 1
                                      0x30, // 0
                                      0x31, // 1
                                      0x00, // NUL
                                      0x00, // NUL
                                      0x00, // NUL
                                      0x00 // NUL
        );
        List<CalibrationInformation> expectedCalInfos = new ArrayList<>();
        expectedCalInfos.add(new CalibrationInformation("A261XXM_E711E31D",
                                                        "0xC96E7A06",
                                                        new byte[] {
                                                                // LSB Cal ID; 16 bytes; Padding at LSB w/ 0x00
                                                                0x41, // A
                                                                0x32, // 2
                                                                0x36, // 36
                                                                0x31, // 1
                                                                0x58, // X
                                                                0x58, // X
                                                                0x4D,  // M
                                                                0x5F,  // -
                                                                0x45, // E
                                                                0x37,  // 7
                                                                0x31, // 1
                                                                0x31, // 1
                                                                0x45, // E
                                                                0x33, // 3
                                                                0x31, // 1
                                                                0x44 // D
                                                        },
                                                        new byte[] {
                                                                // LSB CVN; 4 bytes (checksum value of entire
                                                                // calibration) padding at MSB w/ 0x00
                                                                0x06, // ASCII value of ACK - unprintable char
                                                                0x7A, // z
                                                                0x6E, // n
                                                                (byte) 0xC9 // É - unprintable char
                                                        }));
        expectedCalInfos.add(new CalibrationInformation("NOx-SAE14a ATI1",
                                                        "0x138973A8",
                                                        new byte[] {
                                                                // LSB Cal ID; 16 bytes; Padding at LSB
                                                                0x4E, // N
                                                                0x4F, // O
                                                                0x78, // x
                                                                0x2D, // -
                                                                0x53, // S
                                                                0x41, // A
                                                                0x45, // E
                                                                0x31, // 1
                                                                0x34, // 4
                                                                0x61, // a
                                                                0x20, // " " - space
                                                                0x41, // A
                                                                0x54, // T
                                                                0x49, // I
                                                                0x31, // 1
                                                                0x00 // NUL
                                                        },
                                                        new byte[] {
                                                                // LSB; 4 bytes (checksum value of entire calibration)
                                                                (byte) 0xA8, // - unprintable
                                                                0x73, // 5
                                                                (byte) 0x89, // undefined
                                                                0x13 // DC3 Ascii char undefined?
                                                        }));
        expectedCalInfos.add(new CalibrationInformation("NOx-SAE14a ATO1",
                                                        "0xC9F94B8C",
                                                        new byte[] {
                                                                // LSB Cal ID; 16 bytes; Padding at LSB
                                                                0x4E, // N
                                                                0x4F, // O
                                                                0x78, // x
                                                                0x2D, // -
                                                                0x53, // S
                                                                0x41, // A
                                                                0x45, // E
                                                                0x31, // 1
                                                                0x34, // 4
                                                                0x61, // a
                                                                0x20, // " " - space
                                                                0x41, // A
                                                                0x54, // T
                                                                0x4F, // 0
                                                                0x31, // 1
                                                                0x00 // NUL
                                                        },
                                                        new byte[] {
                                                                // LSB; 4 bytes (checksum value of entire calibration)
                                                                (byte) 0x8C, // ¼ - unprintable
                                                                0x4B, // K
                                                                (byte) 0xF9, // ù - unprintable
                                                                (byte) 0xC9 // É - unprintable
                                                        }));
        expectedCalInfos.add(new CalibrationInformation("ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ",
                                                        "0x00000000",
                                                        new byte[] {
                                                                // LSB Cal ID; 16 bytes; Padding at LSB
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF, // ÿ
                                                                (byte) 0xFF // ÿ
                                                        },
                                                        new byte[] {
                                                                0x00, // NUL
                                                                0x00, // NUL
                                                                0x00, // NUL
                                                                0x00 // NUL
                                                        }

        ));
        expectedCalInfos.add(new CalibrationInformation("PMS12341A101",
                                                        "0xA90FBFD2",
                                                        new byte[] {
                                                                // LSB Cal ID; 16 bytes; Padding at LSB
                                                                0x50, // P
                                                                0x4D, // M
                                                                0x53, // S
                                                                0x31, // 1
                                                                0x32, // 2
                                                                0x33, // 3
                                                                0x34, // 4
                                                                0x31, // 1
                                                                0x41, // A
                                                                0x31, // 1
                                                                0x30, // 0
                                                                0x31, // 1
                                                                0x00, // NUL
                                                                0x00, // NUL
                                                                0x00, // NUL
                                                                0x00 // NUL
                                                        },
                                                        new byte[] {
                                                                (byte) 0xD2,
                                                                // Ò - unprintable
                                                                (byte) 0xBF, // ¿
                                                                0x0F, // undefined
                                                                (byte) 0xA9 } // ©
        ));
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(5, calInfos.size());
        assertNotEquals(expectedCalInfos, calInfos);

        CalibrationInformation calInfo0 = calInfos.get(0);
        assertEquals("A261XXM_E711E31D", calInfo0.getCalibrationIdentification());
        assertEquals("0xC96E7A06", calInfo0.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 65, 50, 54, 49, 88, 88, 77, 95, 69, 55, 49, 49, 69, 51, 49, 68 },
                          calInfo0.getRawCalId());
        assertArrayEquals(new byte[] { 6, 122, 110, -55 }, calInfo0.getRawCvn());
        CalibrationInformation calInfo1 = calInfos.get(1);
        assertEquals("NOx-SAE14a ATI1 ", calInfo1.getCalibrationIdentification());
        assertEquals("0x138973A8", calInfo1.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 78, 79, 120, 45, 83, 65, 69, 49, 52, 97, 32, 65, 84, 73, 49, 0 },
                          calInfo1.getRawCalId());
        assertArrayEquals(new byte[] { -88, 115, -119, 19 }, calInfo1.getRawCvn());
        CalibrationInformation calInfo2 = calInfos.get(2);
        assertEquals("NOx-SAE14a ATO1 ", calInfo2.getCalibrationIdentification());
        assertEquals("0xC9F94B8C", calInfo2.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 78, 79, 120, 45, 83, 65, 69, 49, 52, 97, 32, 65, 84, 79, 49, 0 },
                          calInfo2.getRawCalId());
        assertArrayEquals(new byte[] { -116, 75, -7, -55 }, calInfo2.getRawCvn());
        CalibrationInformation calInfo3 = calInfos.get(3);
        assertEquals("ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ", calInfo3.getCalibrationIdentification());
        assertEquals("0x00000000", calInfo3.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
                          calInfo3.getRawCalId());
        assertArrayEquals(new byte[] { 0, 0, 0, 0 }, calInfo3.getRawCvn());
        CalibrationInformation calInfo4 = calInfos.get(4);
        assertEquals("PMS12341A101    ", calInfo4.getCalibrationIdentification());
        assertEquals("0xA90FBFD2", calInfo4.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 80, 77, 83, 49, 50, 51, 52, 49, 65, 49, 48, 49, 0, 0, 0, 0 },
                          calInfo4.getRawCalId());
        assertArrayEquals(new byte[] { -46, -65, 15, -87 }, calInfo4.getRawCvn());

        String expected = "DM19 from Engine #1 (0): [" + NL;
        expected += "  CAL ID of A261XXM_E711E31D and CVN of 0xC96E7A06" + NL;
        expected += "  CAL ID of NOx-SAE14a ATI1 and CVN of 0x138973A8" + NL;
        expected += "  CAL ID of NOx-SAE14a ATO1 and CVN of 0xC9F94B8C" + NL;
        expected += "  CAL ID of ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ and CVN of 0x00000000" + NL;
        expected += "  CAL ID of PMS12341A101 and CVN of 0xA90FBFD2" + NL;
        expected += "]";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#toString()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one good CVN/Cal Id</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalibrationInformationAndToStringWithOneNoBlanks() {
        Packet packet = Packet.create(0,
                                      0x00,
                                      0x51, // CVN
                                      0xBA,
                                      0xFE,
                                      0xBD,
                                      0x30, // Cal Id
                                      0x31,
                                      0x32,
                                      0x33,
                                      0x34,
                                      0x35,
                                      0x36,
                                      0x37,
                                      0x38,
                                      0x39,
                                      0x30,
                                      0x31,
                                      0x32,
                                      0x33,
                                      0x34,
                                      0x35);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(1, calInfos.size());
        CalibrationInformation calInfo = calInfos.get(0);
        assertEquals("0123456789012345", calInfo.getCalibrationIdentification());
        assertEquals("0xBDFEBA51", calInfo.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 81, -70, -2, -67 }, calInfo.getRawCvn());

        String expected = "DM19 from Engine #1 (0): CAL ID of 0123456789012345 and CVN of 0xBDFEBA51";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * cal id <15 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalIdLengthLessThanFifteen() {
        CalibrationInformation calInfo = new CalibrationInformation("LessThan15Char", "1234");
        DM19CalibrationInformationPacket instance = DM19CalibrationInformationPacket.create(0, 0x00, calInfo);
        CalibrationInformation instanceCalInfo = instance.getCalibrationInformation().get(0);
        assertNotNull(instanceCalInfo);
        assertEquals("LessThan15Char  ", instanceCalInfo.getCalibrationIdentification());
        assertEquals("0x34333231", instanceCalInfo.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 0x4C, 0x65, 0x73, 0x73, 0x54, 0x68, 0x61, 0x6E, 0x31, 0x35, 0x43, 0x68, 0x61,
                0x72, 0x00, 0x00 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 49, 50, 51, 52 }, calInfo.getRawCvn());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * cvn <4 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCvnLengthLessThanFour() {
        CalibrationInformation calInfo = new CalibrationInformation("Equals15Characts", "123");
        DM19CalibrationInformationPacket instance = DM19CalibrationInformationPacket.create(0, 0, calInfo);
        CalibrationInformation instanceCalInfo = instance.getCalibrationInformation().get(0);
        assertNotNull(instanceCalInfo);
        assertEquals("Equals15Characts", instanceCalInfo.getCalibrationIdentification());
        assertEquals("0x00333231", instanceCalInfo.getCalibrationVerificationNumber());

        assertArrayEquals(new byte[] { 0x45, 0x71, 0x75, 0x61, 0x6C, 0x73, 0x31, 0x35, 0x43, 0x68, 0x61, 0x72, 0x61,
                0x63, 0x74, 0x73 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 49, 50, 51, 0 }, calInfo.getRawCvn());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * cvn >4 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCvnLengthGreaterThanFour() {
        CalibrationInformation calInfo = new CalibrationInformation("Equals15Characs", "12345");
        DM19CalibrationInformationPacket instance = DM19CalibrationInformationPacket.create(0,
                                                                                            0xF9,
                                                                                            calInfo);
        assertNotNull(instance.getCalibrationInformation());
        CalibrationInformation instanceCalInfo = instance.getCalibrationInformation().get(0);
        assertEquals("5Equals15Characs", instanceCalInfo.getCalibrationIdentification());
        assertEquals("0x34333231", instanceCalInfo.getCalibrationVerificationNumber());

        assertArrayEquals(new byte[] { 0x45, 0x71, 0x75, 0x61, 0x6C, 0x73, 0x31, 0x35, 0x43, 0x68, 0x61, 0x72, 0x61,
                0x63, 0x73, 0x00 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 49, 50, 51, 52, 53 }, calInfo.getRawCvn());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * cal Id >16 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalIdLengthGreaterThanSixteen() {
        CalibrationInformation calInfo = new CalibrationInformation("GreaterThan16Chars", "1234");
        DM19CalibrationInformationPacket instance = DM19CalibrationInformationPacket.create(0x00, 0, calInfo);
        assertNotNull(instance.getCalibrationInformation());
        assertEquals("GreaterThan16Char", calInfo.getCalibrationIdentification());
        assertEquals("1234", calInfo.getCalibrationVerificationNumber());

        assertArrayEquals(new byte[] { 0x47, 0x72, 0x65, 0x61, 0x74, 0x65, 0x72, 0x54, 0x68, 0x61, 0x6E, 0x31, 0x36,
                0x43, 0x68, 0x61, 0x72 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 49, 50, 51, 52 }, calInfo.getRawCvn());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">three good CVN/Cal Id
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalibrationInformationWithThree() {
        Packet packet = Packet.create(0,
                                      0x00,
                                      // Cal #1
                                      0x51, // CVN
                                      0xBA,
                                      0xFE,
                                      0xBD,
                                      0x41, // Cal Id
                                      0x4E,
                                      0x54,
                                      0x35,
                                      0x41,
                                      0x53,
                                      0x52,
                                      0x31,
                                      0x20,
                                      0x20,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,

                                      // Cal #2
                                      0x96, // CVN
                                      0xBF,
                                      0xDC,
                                      0x40,
                                      0x50, // Cal Id
                                      0x42,
                                      0x54,
                                      0x35,
                                      0x4D,
                                      0x50,
                                      0x52,
                                      0x33,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,

                                      // Cal #3
                                      0x40, // CVN
                                      0x91,
                                      0xB9,
                                      0x3E,
                                      0x52, // Cal Id
                                      0x50,
                                      0x52,
                                      0x42,
                                      0x42,
                                      0x41,
                                      0x31,
                                      0x30,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);

        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(3, calInfos.size());
        CalibrationInformation calInfo1 = calInfos.get(0);
        assertEquals("ANT5ASR1        ", calInfo1.getCalibrationIdentification());
        assertEquals("0xBDFEBA51", calInfo1.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 65, 78, 84, 53, 65, 83, 82, 49, 32, 32, 0, 0, 0, 0, 0, 0 },
                          calInfo1.getRawCalId());
        assertArrayEquals(new byte[] { 81, -70, -2, -67 }, calInfo1.getRawCvn());

        CalibrationInformation calInfo2 = calInfos.get(1);
        assertEquals("PBT5MPR3        ", calInfo2.getCalibrationIdentification());
        assertEquals("0x40DCBF96", calInfo2.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 80, 66, 84, 53, 77, 80, 82, 51, 0, 0, 0, 0, 0, 0, 0, 0 },
                          calInfo2.getRawCalId());
        assertArrayEquals(new byte[] { -106, -65, -36, 64 }, calInfo2.getRawCvn());

        CalibrationInformation calInfo3 = calInfos.get(2);
        assertEquals("RPRBBA10        ", calInfo3.getCalibrationIdentification());
        assertEquals("0x3EB99140", calInfo3.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 82, 80, 82, 66, 66, 65, 49, 48, 0, 0, 0, 0, 0, 0, 0, 0 },
                          calInfo3.getRawCalId());
        assertArrayEquals(new byte[] { 64, -111, -71, 62 }, calInfo3.getRawCvn());

        String expected = "DM19 from Engine #1 (0): [" + NL + "  CAL ID of ANT5ASR1 and CVN of 0xBDFEBA51" + NL
                + "  CAL ID of PBT5MPR3 and CVN of 0x40DCBF96" + NL + "  CAL ID of RPRBBA10 and CVN of 0x3EB99140" + NL
                + "]";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * Cal Id <16 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalIsLengthLessThanSixteen() {
        Packet packet = Packet.create(0,
                                      0,
                                      0x00, // CVN
                                      0xAC,
                                      0xFF,
                                      0x33,
                                      0x41, // Cal Id
                                      0x4E,
                                      0x54,
                                      0x35,
                                      0x41,
                                      0x53,
                                      0x52,
                                      0x31,
                                      0x20,
                                      0x20,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(1, calInfos.size());
        CalibrationInformation calInfo = calInfos.get(0);
        assertEquals("ANT5ASR1        ", calInfo.getCalibrationIdentification());
        assertEquals("0x33FFAC00", calInfo.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 65, 78, 84, 53, 65, 83, 82, 49, 32, 32, 0, 0, 0, 0, 0, 0 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 0, -84, -1, 51 }, calInfo.getRawCvn());

        String expected = "DM19 from Engine #1 (0): CAL ID of ANT5ASR1 and CVN of 0x33FFAC00";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testNullCalId() {
        Packet packet = Packet.create(0,
                                      0,
                                      0x00, // CVN
                                      0xAC,
                                      0xFF,
                                      0x33,
                                      0x00, // Cal Id
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(1, calInfos.size());
        CalibrationInformation calInfo = calInfos.get(0);
        assertEquals("                ", calInfo.getCalibrationIdentification());
        assertEquals("0x33FFAC00", calInfo.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 0, -84, -1, 51 }, calInfo.getRawCvn());

        String expected = "DM19 from Engine #1 (0): CAL ID of  and CVN of 0x33FFAC00";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * Cal Id <16 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalibrationInformationWithTwoDigitCVN() {
        Packet packet = Packet.create(0,
                                      0,
                                      0xDE,
                                      0xE5,
                                      0x00,
                                      0x00,
                                      0x31,
                                      0x32,
                                      0x44,
                                      0x42,
                                      0x42,
                                      0x32,
                                      0x30,
                                      0x30,
                                      0x30,
                                      0x32,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(1, calInfos.size());
        CalibrationInformation calInfo = calInfos.get(0);
        assertEquals("12DBB20002      ", calInfo.getCalibrationIdentification());
        assertEquals("0x0000E5DE", calInfo.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 49, 50, 68, 66, 66, 50, 48, 48, 48, 50, 0, 0, 0, 0, 0, 0 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { -34, -27, 0, 0 }, calInfo.getRawCvn());

        String expected = "DM19 from Engine #1 (0): CAL ID of 12DBB20002 and CVN of 0x0000E5DE";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for {@link DM19CalibrationInformationPacket#getCalibrationInformation()}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * cvn all 0x00
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testCalibrationInformationWithZeroDigitCVN() {
        Packet packet = Packet.create(0,
                                      0,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x41,
                                      0x4E,
                                      0x54,
                                      0x35,
                                      0x41,
                                      0x53,
                                      0x52,
                                      0x31,
                                      0x20,
                                      0x20,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        List<CalibrationInformation> calInfos = instance.getCalibrationInformation();
        assertNotNull(calInfos);
        assertEquals(1, calInfos.size());
        CalibrationInformation calInfo = calInfos.get(0);
        assertEquals("ANT5ASR1        ", calInfo.getCalibrationIdentification());
        assertEquals("0x00000000", calInfo.getCalibrationVerificationNumber());
        assertArrayEquals(new byte[] { 65, 78, 84, 53, 65, 83, 82, 49, 32, 32, 0, 0, 0, 0, 0, 0 },
                          calInfo.getRawCalId());
        assertArrayEquals(new byte[] { 0, 0, 0, 0 }, calInfo.getRawCvn());

        String expected = "DM19 from Engine #1 (0): CAL ID of ANT5ASR1 and CVN of 0x00000000";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for {@link Packet#equals(Object)} via {@link DM19CalibrationInformationPacket#equals(Object)}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0xFE</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * Cal Id <16 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testEqualsAndHashcode() {
        Packet packet1 = Packet.create(0xBADF, 0xFE, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        DM19CalibrationInformationPacket instance1 = new DM19CalibrationInformationPacket(packet1);

        Packet packet2 = Packet.create(0xBADF, 0xFE, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        DM19CalibrationInformationPacket instance2 = new DM19CalibrationInformationPacket(packet2);

        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }

    /**
     * Test method for {@link Packet#equals(Object)} via {@link DM19CalibrationInformationPacket#equals(Object)}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0xFE</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * Cal Id <16 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testEqualsThis() {
        Packet packet = Packet.create(0xBADF, 0xFE, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        assertTrue(instance.equals(instance));
        assertTrue(instance.hashCode() == instance.hashCode());
    }

    /**
     * Test method for {@link Packet#equals(Object)} via {@link DM19CalibrationInformationPacket#equals(Object)}
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM19 Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Cal Info Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0xFE</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM19 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">one bad CVN/Cal Id<br>
     * Cal Id <16 char
     * </td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testNotEqualsObject() {
        Packet packet = Packet.create(0xBADF, 0xFE, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        DM19CalibrationInformationPacket instance = new DM19CalibrationInformationPacket(packet);
        assertFalse(instance.equals(new Object()));
    }

    /**
     * Test method for
     * {@link DM19CalibrationInformationPacket#PGN}
     */
    @Test
    public void testPGN() {
        assertEquals(54016, DM19CalibrationInformationPacket.PGN);
    }

    /**
     * real data test demonstrating missing DM19
     * 
     * @throws BusException
     */
    @Test
    public void testRawTraffic() throws BusException {
        // preload db
        J1939DaRepository.getInstance().getPgnDefinitions();
        try (EchoBus bus = new EchoBus(0xF9);
             J1939TP tp = new J1939TP(bus, 0xF9)) {
            var stream = new J1939(tp).processedStream(700, TimeUnit.MILLISECONDS);
            new Thread(() -> {
                try {
                    double start = 64.711482;// 1 18EAFFF9x Tx d 3 00 D3 00
                    this.start = System.currentTimeMillis();
                    waitUntil(64.722482, start);
                    bus.send(Packet.parse("18ECFF27 20 14 00 03 FF 00 D3 00"));
                    waitUntil(64.724482, start);
                    bus.send(Packet.parse("18ECFF00 20 8C 00 14 FF 00 D3 00"));
                    waitUntil(64.729982, start);
                    bus.send(Packet.parse("18ECFF3D 20 14 00 03 FF 00 D3 00"));
                    waitUntil(64.777482, start);
                    bus.send(Packet.parse("18EBFF27 01 23 66 70 E6 49 31 33"));
                    waitUntil(64.783982, start);
                    bus.send(Packet.parse("18EBFF00 01 E2 58 7B B4 45 43 55"));
                    waitUntil(64.789982, start);
                    bus.send(Packet.parse("18EBFF3D 01 4C 1C BE 7E 00 00 00"));
                    waitUntil(64.832482, start);
                    bus.send(Packet.parse("18EBFF27 02 58 58 58 58 5F 47 31"));
                    waitUntil(64.834482, start);
                    bus.send(Packet.parse("18EBFF00 02 2D 53 57 20 6E 75 6D"));
                    waitUntil(64.839982, start);
                    bus.send(Packet.parse("18EBFF3D 02 00 00 00 00 00 00 00"));
                    waitUntil(64.883982, start);
                    bus.send(Packet.parse("18EBFF00 03 62 65 72 00 00 00 3F"));
                    waitUntil(64.887482, start);
                    bus.send(Packet.parse("18EBFF27 03 30 30 30 47 31 30 FF"));
                    waitUntil(64.889982, start);
                    bus.send(Packet.parse("18EBFF3D 03 00 00 00 00 00 00 FF"));
                    waitUntil(64.934482, start);
                    bus.send(Packet.parse("18EBFF00 04 6C 00 00 35 38 34 33"));
                    waitUntil(64.983982, start);
                    bus.send(Packet.parse("18EBFF00 05 53 30 30 36 2E 30 30"));
                    waitUntil(65.034482, start);
                    bus.send(Packet.parse("18EBFF00 06 35 00 00 00 00 15 2B"));
                    waitUntil(65.083982, start);
                    bus.send(Packet.parse("18EBFF00 07 DF 8C 53 43 41 4E 4F"));
                    waitUntil(65.134482, start);
                    bus.send(Packet.parse("18EBFF00 08 78 4E 2D 30 34 34 30"));
                    waitUntil(65.183982, start);
                    bus.send(Packet.parse("18EBFF00 09 41 54 49 32 61 59 D4"));
                    waitUntil(65.234482, start);
                    bus.send(Packet.parse("18EBFF00 0A FB 53 43 41 4E 4F 78"));
                    waitUntil(65.283982, start);
                    bus.send(Packet.parse("18EBFF00 0B 4E 2D 30 34 34 30 41"));
                    waitUntil(65.334482, start);
                    bus.send(Packet.parse("18EBFF00 0C 54 4F 32 15 2B DF 8C"));
                    waitUntil(65.383982, start);
                    bus.send(Packet.parse("18EBFF00 0D 53 43 41 4E 4F 78 4E"));
                    waitUntil(65.434482, start);
                    bus.send(Packet.parse("18EBFF00 0E 2D 30 34 34 30 41 54"));
                    waitUntil(65.483982, start);
                    bus.send(Packet.parse("18EBFF00 0F 49 32 8A FC 90 37 50"));
                    waitUntil(65.534482, start);
                    bus.send(Packet.parse("18EBFF00 10 4D 53 50 2A 31 32 2A"));
                    waitUntil(65.583982, start);
                    bus.send(Packet.parse("18EBFF00 11 33 35 30 2A 41 31 30"));
                    waitUntil(65.634482, start);
                    bus.send(Packet.parse("18EBFF00 12 30 C2 90 45 C4 30 31"));
                    waitUntil(65.683982, start);
                    bus.send(Packet.parse("18EBFF00 13 38 38 30 30 36 31 30"));
                    waitUntil(65.734482, start);
                    bus.send(Packet.parse("18EBFF00 14 51 20 20 20 20 20 00"));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }).start();
            var rx = stream.collect(Collectors.toList());
            assertEquals(rx.toString(), 3, rx.size());
        }
    }

    long start;

    private void waitUntil(double a, double b) throws InterruptedException {
        long c = System.currentTimeMillis() - start;
        long d = (long) (1000 * (a - b));
        long w = d - c;
        if (w > 0)
            Thread.sleep(w);
    }

}

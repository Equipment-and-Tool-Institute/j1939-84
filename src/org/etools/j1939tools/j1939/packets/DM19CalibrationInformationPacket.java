/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * Parses the Calibration Information Packet (DM19)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM19CalibrationInformationPacket extends GenericPacket {
    public static final int PGN = 54016; // 0xD300

    public static DM19CalibrationInformationPacket create(int address,
                                                          int destination,
                                                          CalibrationInformation... calInfos) {
        // DM19 are 20 bytes long
        int totalBytes = 20;
        byte[] data = new byte[0];

        for (CalibrationInformation calInfo : calInfos) {
            // Correctly pad message - Java pads/cuts to specified length
            data = Arrays.copyOf(calInfo.getBytes(), totalBytes);

        }
        return new DM19CalibrationInformationPacket(Packet.create(PGN | destination, address, data));
    }

    private List<CalibrationInformation> info;

    public DM19CalibrationInformationPacket(Packet packet) {
        super(packet);
    }

    /**
     * Returns the {@link CalibrationInformation} from the controller
     *
     * @return List of {@link CalibrationInformation}
     */
    public List<CalibrationInformation> getCalibrationInformation() {
        if (info == null || info.size() == 0) {
            info = parseAllInformation();
        }
        return info;
    }

    @Override
    public String getName() {
        return "DM19";
    }

    @Override
    public String toString() {
        boolean moreThanOne = getCalibrationInformation().size() > 1;
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix());
        sb.append(moreThanOne ? "[" + NL : "");
        for (CalibrationInformation info : getCalibrationInformation()) {
            sb.append(moreThanOne ? "  " : "");
            sb.append(info.toString());
            sb.append(moreThanOne ? NL : "");
        }
        sb.append(moreThanOne ? "]" : "");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return getPacket().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ParsedPacket)) {
            return false;
        }

        ParsedPacket that = (ParsedPacket) obj;
        return getPacket().equals(that.getPacket());
    }

    /**
     * Parses all the data to return all the {@link CalibrationInformation} sent
     *
     * @return a List of {@link CalibrationInformation}
     */
    private List<CalibrationInformation> parseAllInformation() {
        List<CalibrationInformation> result = new ArrayList<>();
        int length = getPacket().getLength();
        for (int i = 0; i + 20 <= length; i = i + 20) {
            CalibrationInformation info = parseInformation(i);
            result.add(info);
        }
        return result;
    }

    /**
     * Parses one calibration information from the packet
     *
     * @param  startingIndex
     *                           the index of the data to start the parsing at
     * @return               The parsed {@link CalibrationInformation}
     */
    private CalibrationInformation parseInformation(int startingIndex) {
        String cvn = String.format("%08X", getPacket().get32(startingIndex) & 0xFFFFFFFFL);
        byte[] bytes = getPacket().getBytes();
        byte[] cvnBytes = Arrays.copyOfRange(bytes, startingIndex, startingIndex + 4);
        byte[] idBytes = Arrays.copyOfRange(bytes, startingIndex + 4, startingIndex + 20);
        String calId = format(idBytes);
        if (!cvn.isEmpty()) {
            cvn = "0x" + cvn;
        }
        return new CalibrationInformation(calId, cvn, idBytes, cvnBytes);
    }

    /**
     * Contains the Calibration Identification and Calibration Verification
     * Number
     *
     * @author Matt Gumbel (matt@soliddesign.net)
     *
     */
    public static class CalibrationInformation {

        private final String calibrationIdentification;
        private final String calibrationVerificationNumber;
        private final byte[] rawCalId;
        private final byte[] rawCvn;

        public CalibrationInformation(String calId, String cvn) {

            if (calId.length() >= 17) {
                calibrationIdentification = calId.substring(0, 17);
            } else {
                calibrationIdentification = String.format("%0$-16s", calId).replace(' ', (char) 0x00);
            }

            if (cvn.length() >= 5) {
                calibrationVerificationNumber = cvn.substring(0, 5);
            } else {
                calibrationVerificationNumber = String.format("%0$-4s", cvn).replace(' ', (char) 0x00);
            }

            rawCalId = calibrationIdentification.getBytes(StandardCharsets.UTF_8);
            rawCvn = calibrationVerificationNumber.getBytes(StandardCharsets.UTF_8);

        }

        public CalibrationInformation(String id, String cvn, byte[] rawCalId, byte[] rawCvn) {
            calibrationIdentification = id;
            calibrationVerificationNumber = cvn;
            this.rawCalId = Arrays.copyOf(rawCalId, rawCalId.length);
            this.rawCvn = Arrays.copyOf(rawCvn, rawCvn.length);
        }

        public String getCalibrationIdentification() {
            return calibrationIdentification;
        }

        public String getCalibrationVerificationNumber() {
            return calibrationVerificationNumber;
        }

        public byte[] getRawCalId() {
            return Arrays.copyOf(rawCalId, rawCalId.length);
        }

        public byte[] getRawCvn() {
            return Arrays.copyOf(rawCvn, rawCvn.length);
        }

        public byte[] getBytes() {
            return CollectionUtils.join(Arrays.copyOf(rawCvn, rawCvn.length), Arrays.copyOf(rawCalId, rawCalId.length));
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCalibrationIdentification(), getCalibrationVerificationNumber());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CalibrationInformation)) {
                return false;
            }

            CalibrationInformation that = (CalibrationInformation) obj;

            return Objects.equals(getCalibrationIdentification(), that.getCalibrationIdentification())
                    && Objects.equals(getCalibrationVerificationNumber(), that.getCalibrationVerificationNumber());
        }

        @Override
        public String toString() {
            return "CAL ID of " + getCalibrationIdentification().trim()
                    + " and CVN of " + getCalibrationVerificationNumber();
        }
    }
}

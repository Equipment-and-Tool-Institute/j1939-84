/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;


import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.utils.CollectionUtils.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.etools.j1939tools.bus.Packet;


/**
 * Class that represents a packet that contains Diagnostic Trouble Codes
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DiagnosticTroubleCodePacket extends GenericPacket {

    private LampStatus awlStatus;
    private List<DiagnosticTroubleCode> dtcs;
    private LampStatus milStatus;
    private LampStatus plStatus;
    private LampStatus rslStatus;

    /**
     * Constructor
     *
     * @param packet
     *                   the {@link Packet} to parse
     */
    public DiagnosticTroubleCodePacket(Packet packet) {
        super(packet);
    }

    protected static Packet create(int address,
                                   int pgn,
                                   LampStatus mil,
                                   LampStatus stop,
                                   LampStatus amber,
                                   LampStatus protect,
                                   DiagnosticTroubleCode... dtcs) {

        int[] milData = LampStatus.getBytes(mil);
        int[] stopData = LampStatus.getBytes(stop);
        int[] amberData = LampStatus.getBytes(amber);
        int[] protectData = LampStatus.getBytes(protect);

        int[] data = new int[2];
        data[0] = milData[0] << 6 | stopData[0] << 4 | amberData[0] << 2 | protectData[0];
        data[1] = milData[1] << 6 | stopData[1] << 4 | amberData[1] << 2 | protectData[1];

        if (dtcs.length == 0) {
            data = join(data, new int[] { 0, 0, 0, 0 });
        } else {
            for (DiagnosticTroubleCode dtc : dtcs) {
                data = join(data, dtc.getData());
            }
        }

        if (data.length < 8) {
            data = join(data, new int[] { 0xFF, 0xFF });
        }

        return Packet.create(pgn, address, data);
    }

    /**
     * Returns the Amber Warning Lamp (AWL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getAmberWarningLampStatus() {
        if (awlStatus == null) {
            awlStatus = getLampStatus(0x0C, 2);
        }
        return awlStatus;
    }

    /**
     * Returns the {@link List} of {@link DiagnosticTroubleCode}. If the only
     * "DTC" has an SPN of 0 or 524287, the list will be empty, but never null
     *
     * @return List of DTCs
     */
    public List<DiagnosticTroubleCode> getDtcs() {
        if (dtcs == null) {
            dtcs = parseDTCs();
        }
        return Collections.unmodifiableList(dtcs);
    }

    public boolean hasDTCs() {
        return !getDtcs().isEmpty();
    }

    /**
     * Helper method to get a {@link LampStatus}
     *
     * @param  mask
     *                   the bit mask
     * @param  shift
     *                   the number of bits to shift to the right
     * @return       the {@link LampStatus} that corresponds to the value
     */
    private LampStatus getLampStatus(int mask, int shift) {
        int onOff = getShaveAndAHaircut(0, mask, shift);
        int flash = getShaveAndAHaircut(1, mask, shift);
        return LampStatus.getStatus(onOff, flash);
    }

    /**
     * Returns the Malfunction Indicator Lamp (MIL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getMalfunctionIndicatorLampStatus() {
        if (milStatus == null) {
            milStatus = getLampStatus(0xC0, 6);
        }
        return milStatus;
    }

    @Override
    public String getName() {
        return "DM";
    }

    @Override
    public String toString() {
        String result = getStringPrefix() + "MIL: " + getMalfunctionIndicatorLampStatus() + ", RSL: "
                + getRedStopLampStatus() + ", AWL: " + getAmberWarningLampStatus() + ", PL: " + getProtectLampStatus();

        if (getDtcs().isEmpty()) {
            result += ", No DTCs";
        } else {
            result += NL;
            String joinedDtcs = getDtcs().stream().map(DiagnosticTroubleCode::toString).collect(Collectors.joining(NL));
            result += joinedDtcs;
        }

        return result;
    }

    /**
     * Returns the Protect Lamp Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getProtectLampStatus() {
        if (plStatus == null) {
            plStatus = getLampStatus(0x03, 0);
        }
        return plStatus;
    }

    /**
     * Returns the Red Stop Lamp (RSL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getRedStopLampStatus() {
        if (rslStatus == null) {
            rslStatus = getLampStatus(0x30, 4);
        }
        return rslStatus;
    }

    private DiagnosticTroubleCode parseDTC(int i) {
        int[] data = getPacket().getData(i, i + 4);
        return new DiagnosticTroubleCode(data);
    }

    /**
     * Parses the data to create a {@link List} of {@link DiagnosticTroubleCode}
     *
     * @return List
     */
    private List<DiagnosticTroubleCode> parseDTCs() {
        List<DiagnosticTroubleCode> dtcs = new ArrayList<>();
        // Every 4 bytes is a DTC; there might be two extra bytes
        int length = getPacket().getLength();
        for (int i = 2; i + 4 <= length; i = i + 4) {
            DiagnosticTroubleCode dtc = parseDTC(i);
            int spn = dtc.getSuspectParameterNumber();
            if (spn != 0 && spn != 524287) {
                dtcs.add(dtc);
            }
        }
        return dtcs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                            getAmberWarningLampStatus(),
                            getDtcs(),
                            getMalfunctionIndicatorLampStatus(),
                            getProtectLampStatus(),
                            getRedStopLampStatus());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DiagnosticTroubleCodePacket that = (DiagnosticTroubleCodePacket) o;
        return getAmberWarningLampStatus() == that.getAmberWarningLampStatus()
                && getDtcs().equals(that.getDtcs())
                && getMalfunctionIndicatorLampStatus() == that.getMalfunctionIndicatorLampStatus()
                && getProtectLampStatus() == that.getProtectLampStatus()
                && getRedStopLampStatus() == that.getRedStopLampStatus();
    }
}

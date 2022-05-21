/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * The {@link ParsedPacket} for Expanded Freeze Frame Codes (DM25)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM25ExpandedFreezeFrame extends GenericPacket {
    public static final int PGN = 64951; // 0xFDB7
    private List<FreezeFrame> freezeFrames;

    public DM25ExpandedFreezeFrame(Packet packet) {
        super(packet);
    }

    public static DM25ExpandedFreezeFrame create(int sourceAddress, FreezeFrame... freezeFrames) {

        int[] data = new int[0];
        if (freezeFrames.length > 0) {
            for (FreezeFrame freezeFrame : freezeFrames) {
                data = CollectionUtils.join(data, freezeFrame.getData());
            }
        } else {
            data = new int[] { 0, 0, 0, 0, 0, 0xFF, 0xFF, 0xFF };
        }

        return new DM25ExpandedFreezeFrame(Packet.create(PGN, sourceAddress, data));
    }

    /**
     * Returns the {@link List} of {@link SupportedSPN}
     *
     * @return {@link List}
     */
    public List<FreezeFrame> getFreezeFrames() {
        if (freezeFrames == null) {
            parsePacket();
        }
        return freezeFrames;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    public FreezeFrame getFreezeFrameWithDTC(DiagnosticTroubleCode dtc) {
        return getFreezeFrames().stream().filter(f -> f.getDtc().equals(dtc)).findFirst().orElse(null);
    }

    @Override
    public String getName() {
        return "DM25";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix()).append(NL);
        sb.append("Freeze Frames: [").append(NL);
        if (getFreezeFrames().isEmpty()) {
            sb.append("  No Freeze Frames").append(NL);
        } else {
            for (FreezeFrame frameFrame : getFreezeFrames()) {
                sb.append(frameFrame).append(NL);
            }
        }
        sb.append("]").append(NL);

        return sb.toString();
    }

    private void parseChunk(int chunkLength) {
        int index = 0;
        boolean done = false;
        while (!done) {

            int[] bytes = getPacket().getData(index + 1, index + chunkLength + 1);
            if (bytes.length == 0) {
                done = true;
            } else {
                DiagnosticTroubleCode dtc = new DiagnosticTroubleCode(bytes);
                int[] data = Arrays.copyOfRange(bytes, 4, bytes.length);
                FreezeFrame freezeFrame = new FreezeFrame(dtc, data);
                freezeFrames.add(freezeFrame);

                if (getPacket().getLength() > index + chunkLength + 1) {
                    index += chunkLength + 1;
                    chunkLength = getPacket().getData(index, index + 1)[0];
                } else {
                    done = true;
                }
            }
        }
    }

    /**
     * Parses the packet to populate all the {@link FreezeFrame}s
     */
    private void parsePacket() {
        freezeFrames = new ArrayList<>();
        int chunkLength = getPacket().get(0);
        if (chunkLength == 0) {
            int[] spnBytes = getPacket().getData(0, 5);
            int[] dataBytes = getPacket().getData(5, 8);
            if (Arrays.equals(spnBytes, new int[] { 0, 0, 0, 0, 0 })
                    && Arrays.equals(dataBytes, new int[] { 0xFF, 0xFF, 0xFF })) {
                return;
            }
            chunkLength = 8; // The data doesn't match spec
        }
        parseChunk(chunkLength);
    }

}

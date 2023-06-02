/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.utils;

import static org.etools.j1939_84.J1939_84.getLogger;
import static org.etools.j1939tools.utils.CollectionUtils.toByteArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.j1939.packets.FreezeFrame;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.j1939.packets.SupportedSPN;

public class FreezeFrameDataTranslator {

    private final J1939DaRepository j1939DaRepository;

    public FreezeFrameDataTranslator() {
        this(J1939DaRepository.getInstance());
    }

    private FreezeFrameDataTranslator(J1939DaRepository j1939DaRepository) {
        this.j1939DaRepository = j1939DaRepository;
    }

    /**
     * Uses the data from the FreezeFrame and the list of Freeze Frame Supported
     * SPNs to produce a List of SPNs which will have the data populated.
     */
    public List<Spn> getFreezeFrameSPNs(FreezeFrame freezeFrame, List<SupportedSPN> supportedSPNs) {
        List<SupportedSPN> supportedFreezeFrameSPNs = supportedSPNs.stream()
                                                                   .filter(SupportedSPN::supportsExpandedFreezeFrame)
                                                                   .collect(Collectors.toList());

        byte[] spnData = toByteArray(freezeFrame.getSpnData());

        int expectedLength = supportedFreezeFrameSPNs.stream().mapToInt(SupportedSPN::getLength).sum();
        int actualLength = spnData.length;
        if (actualLength != expectedLength) {
            getLogger().log(Level.SEVERE,
                            "The expected (" + expectedLength + ") and actual (" + actualLength
                                    + ") data lengths are different");
            return List.of();
        }

        List<Spn> spns = new ArrayList<>();
        int index = 0;
        for (SupportedSPN supportedSPN : supportedFreezeFrameSPNs) {
            byte[] bytes = Arrays.copyOfRange(spnData, index, index + supportedSPN.getLength());
            index += supportedSPN.getLength();

            int spnId = supportedSPN.getSpn();
            SpnDefinition spnDefinition = getSpnDefinition(spnId);
            String label = spnDefinition.getLabel();
            int slotNumber = spnDefinition.getSlotNumber();
            Slot slot = getSlotDefinition(slotNumber, spnId);

            spns.add(new Spn(spnId, label, slot, bytes));
        }
        return spns;
    }

    private Slot getSlotDefinition(int slotNumber, int spnId) {
        return j1939DaRepository.findSLOT(slotNumber, spnId);
    }

    private SpnDefinition getSpnDefinition(int spnId) {
        return j1939DaRepository.findSpnDefinition(spnId);
    }

}

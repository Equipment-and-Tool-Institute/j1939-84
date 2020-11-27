/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDataParser;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;

import static org.etools.j1939_84.J1939_84.NL;

public class GenericPacket extends ParsedPacket {

    private List<Spn> spns;
    private PgnDefinition pgnDefinition;
    private final SpnDataParser parser;

    public GenericPacket(Packet packet) {
        this(packet, new SpnDataParser());
    }

    GenericPacket(Packet packet, SpnDataParser parser) {
        super(packet);
        this.parser = parser;
    }

    public String getAcronym() {
        return getPgnDefinition().acronym;
    }

    @Override
    public String getName() {
        return getPgnDefinition().label;
    }

    public List<Spn> getSpns() {
        if (spns == null) {
            spns = new ArrayList<>();

            //Sort so these are in the order they are on the packet.
            List<SpnDefinition> spnDefinitions = getPgnDefinition().spnDefinitions;
            spnDefinitions.sort(Comparator.comparing(d -> (d.startByte << 8) + d.startBit));

            byte[] bytes = getPacket().getBytes();
            for (SpnDefinition definition : spnDefinitions) {
                byte[] data = parser.parse(bytes, definition);
                spns.add(new Spn(definition.spnId, definition.slotNumber, data));
            }
        }
        return spns;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getStringPrefix()).append(NL);
        for (Spn spn : getSpns()) {
            result.append("  ").append(spn.toString()).append(NL);
        }
        return result.toString();
    }

    private PgnDefinition getPgnDefinition() {
        if (pgnDefinition == null) {
            pgnDefinition = lookupPgnDefinition(getPacket().getId());
        }
        return pgnDefinition;
    }


    private static PgnDefinition lookupPgnDefinition(int id) {
        //FIXME this plugs into the 1939DA repository
        PgnDefinition pgnDefinition = new PgnDefinition();
        pgnDefinition.id = id;
        pgnDefinition.label = "Torque/Speed Control 1";
        pgnDefinition.acronym = "TSC1";
        pgnDefinition.spnDefinitions = getSpnDefs();
        return pgnDefinition;
    }

    private static List<SpnDefinition> getSpnDefs() {
        List<SpnDefinition> results = new ArrayList<>();
        {
            // 1.1 695 Engine Override Control Mode
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 695;
            spn.startByte = 1;
            spn.startBit = 1;
            spn.bitLength = 2;
            spn.slotNumber = 87;
            results.add(spn);
        }

        {
            // 1.3 696 Engine Requested Speed Control Conditions
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 696;
            spn.startByte = 1;
            spn.startBit = 3;
            spn.bitLength = 2;
            spn.slotNumber = 87;
            results.add(spn);
        }

        {
            // 1.5 897 Override Control Mode Priority
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 897;
            spn.startByte = 1;
            spn.startBit = 5;
            spn.bitLength = 2;
            spn.slotNumber = 87;
            results.add(spn);
        }

        {
            // 2-3 898 Engine Requested Speed/Speed Limit
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 898;
            spn.startByte = 2;
            spn.startBit = 1;
            spn.bitLength = 16;
            spn.slotNumber = 76;
            results.add(spn);
        }

        {
            // 4 518 Engine Requested Torque/Torque Limit
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 518;
            spn.startByte = 4;
            spn.startBit = 1;
            spn.bitLength = 8;
            spn.slotNumber = 45;
            results.add(spn);
        }

        {
            // 5.1 3349 TSC1 Transmission Rate
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 3349;
            spn.startByte = 5;
            spn.startBit = 1;
            spn.bitLength = 3;
            spn.slotNumber = 88;
            results.add(spn);
        }

        {
            // 5.4 3350 TSC1 Control Purpose
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 3350;
            spn.startByte = 5;
            spn.startBit = 4;
            spn.bitLength = 5;
            spn.slotNumber = 90;
            results.add(spn);
        }

        {
            // 6.1 4191 Engine Requested Torque (Fractional)
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 4191;
            spn.startByte = 6;
            spn.startBit = 1;
            spn.bitLength = 4;
            spn.slotNumber = 268;
            results.add(spn);
        }

        {
            // 8.1 4206 Message Counter
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 4206;
            spn.startByte = 8;
            spn.startBit = 1;
            spn.bitLength = 4;
            spn.slotNumber = 220;
            results.add(spn);
        }

        {
            // 8.5 4207 Message Checksum
            SpnDefinition spn = new SpnDefinition();
            spn.spnId = 4207;
            spn.startByte = 8;
            spn.startBit = 5;
            spn.bitLength = 4;
            spn.slotNumber = 220;
            results.add(spn);
        }
        return results;
    }

}

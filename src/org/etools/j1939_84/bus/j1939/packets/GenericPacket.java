/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDataParser;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;

public class GenericPacket extends ParsedPacket {

    private final SpnDataParser parser;
    private final PgnDefinition pgnDefinition;
    // FIXME this should be final
    private List<Spn> spns;

    public GenericPacket(Packet packet, PgnDefinition pgnDefinition) {
        this(packet, pgnDefinition, new SpnDataParser());
    }

    GenericPacket(Packet packet, PgnDefinition pgnDefinition, SpnDataParser parser) {
        super(packet);
        this.pgnDefinition = pgnDefinition;
        this.parser = parser;
    }

    public String getAcronym() {
        return getPgnDefinition().acronym;
    }

    @Override
    public String getName() {
        return getPgnDefinition().label;
    }

    public PgnDefinition getPgnDefinition() {
        return pgnDefinition;
    }

    public List<Spn> getSpns() {
        if (spns == null) {
            spns = new ArrayList<>();

            List<SpnDefinition> spnDefinitions = getPgnDefinition().spnDefinitions;
            byte[] bytes = getPacket().getBytes();
            for (SpnDefinition definition : spnDefinitions) {
                Slot slot = Slot.findSlot(definition.slotNumber);
                byte[] data = parser.parse(bytes, definition, slot.getLength());
                spns.add(new Spn(definition.spnId, slot, data));
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

}

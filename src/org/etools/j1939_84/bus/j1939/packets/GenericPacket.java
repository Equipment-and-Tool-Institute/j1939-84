/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDataParser;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;

public class GenericPacket extends ParsedPacket {

    private final SpnDataParser parser;
    private final PgnDefinition pgnDefinition;
    private List<Spn> spns;

    public GenericPacket(Packet packet) {
        this(packet, getJ1939DaRepository().findPgnDefinition(packet.getPgn()));
    }

    public GenericPacket(Packet packet, PgnDefinition pgnDefinition) {
        this(packet, pgnDefinition, new SpnDataParser());
    }

    GenericPacket(Packet packet, PgnDefinition pgnDefinition, SpnDataParser parser) {
        super(packet);
        this.pgnDefinition = pgnDefinition;
        this.parser = parser;
    }

    private static J1939DaRepository getJ1939DaRepository() {
        return J1939DaRepository.getInstance();
    }

    public String getAcronym() {
        return getPgnDefinition().getAcronym();
    }

    @Override
    public String getName() {
        return getPgnDefinition().getLabel();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        try {
            result.append(getStringPrefix()).append(NL);
            for (Spn spn : getSpns()) {
                result.append("  ").append(spn.toString()).append(NL);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error creating string", e);
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public PgnDefinition getPgnDefinition() {
        return pgnDefinition;
    }

    public Optional<Spn> getSpn(int spn) {
        return getSpns().stream().filter(s -> s.getId() == spn).findAny();
    }

    public Stream<Double> getSpnValue(int spn) {
        return getSpns().stream()
                        .filter(s -> s.getId() == spn)
                        .filter(Spn::hasValue)
                        .map(Spn::getValue)
                        .findAny()
                        .stream();
    }

    public List<Spn> getSpns() {
        if (spns == null) {
            spns = new ArrayList<>();

            List<SpnDefinition> spnDefinitions = getPgnDefinition().getSpnDefinitions();
            byte[] bytes = getPacket().getBytes();
            for (SpnDefinition definition : spnDefinitions) {
                Slot slot = getJ1939DaRepository().findSLOT(definition.getSlotNumber(), definition.getSpnId());
                byte[] data = parser.parse(bytes, definition, slot.getLength());
                spns.add(new Spn(definition.getSpnId(), definition.getLabel(), slot, data));
            }
        }
        return spns;
    }

}

/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDataParser;
import org.etools.j1939tools.j1939.model.SpnDefinition;

public class GenericPacket extends ParsedPacket {

    private final PgnDefinition pgnDefinition;
    private List<Spn> spns;

    public GenericPacket(Packet packet) {
        super(packet);
        pgnDefinition = getJ1939DaRepository().findPgnDefinition(packet.getPgn());
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
            J1939_84.getLogger().log(Level.SEVERE, "Error creating string", e);
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

    public Optional<Double> getSpnValue(int spn) {
        return getSpn(spn).filter(Spn::hasValue).map(Spn::getValue);
    }

    public List<Spn> getSpns() {
        if (spns == null) {
            spns = new ArrayList<>();

            List<SpnDefinition> spnDefinitions = getPgnDefinition().getSpnDefinitions();
            byte[] bytes = getPacket().getBytes();
            for (SpnDefinition definition : spnDefinitions) {
                Slot slot = getJ1939DaRepository().findSLOT(definition.getSlotNumber(), definition.getSpnId());
                if (slot.getLength() != 0) {
                    byte[] data = SpnDataParser.parse(bytes, definition, slot.getLength());
                    spns.add(new Spn(definition.getSpnId(), definition.getLabel(), slot, data));
                }
            }
        }
        return spns;
    }

    @SafeVarargs
    static protected <T> Stream<T> concat(Stream<T>... s) {
        Stream<T> a = Stream.empty();
        for (Stream<T> t : s) {
            a = Stream.concat(a, t);
        }
        return a;
    }

    public List<Value> getSpnValues(int spnId) {
        return getSpn(spnId).stream()
                .map(SpnValue::new)
                .collect(Collectors.toList());
    }

}

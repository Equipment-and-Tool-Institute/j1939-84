package org.etools.j1939tools.j1939.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.utils.CollectionUtils;

import static java.util.Comparator.comparingInt;
import static org.etools.j1939_84.J1939_84.NL;

public class ActiveTechnology {

    public static ActiveTechnology create(int pgn, int[] data) {
        final int[] marker = {0}; //to keep tracker of last used data byte

        List<Spn> spns = getSpnDefinitions(pgn)
                .stream()
                .map(def -> {
                    int spnId = def.getSpnId();
                    Slot slot = getSlot(def);
                    String label = def.getLabel();
                    int byteLength = slot.getByteLength();
                    int[] dataArray = Arrays.copyOfRange(data, marker[0], marker[0] + byteLength);
                    marker[0] += byteLength;

                    return new Spn(spnId, label, slot, CollectionUtils.toByteArray(dataArray));
                })
                .sorted(comparingInt(Spn::getId))
                .collect(Collectors.toList());

        return new ActiveTechnology(spns.get(0), spns.get(1), spns.get(2));
    }

    private final Spn indexSpn;
    private final Spn timeSpn;
    private final Spn distanceSpn;

    private ActiveTechnology(Spn indexSpn, Spn timeSpn, Spn distanceSpn) {
        this.indexSpn = indexSpn;
        this.timeSpn = timeSpn;
        this.distanceSpn = distanceSpn;
    }

    public String getLabel() {
        return indexSpn.getStringValueNoUnit();
    }

    public int getIndex() {
        return indexSpn.getValue().intValue();
    }

    public Spn getIndexSpn() {
        return indexSpn;
    }

    public Double getTime() {
        return timeSpn.getValue();
    }

    public Spn getTimeSpn() {
        return timeSpn;
    }

    public Double getDistance() {
        return distanceSpn.getValue();
    }

    public Spn getDistanceSpn() {
        return distanceSpn;
    }

    public String getTimeAsStringNoUnits() {
        return timeSpn.getStringValueNoUnit();
    }

    public String getDistanceAsStringNoUnits() {
        return distanceSpn.getStringValueNoUnit();
    }

    @Override
    public String toString() {
        return "Active Technology:  " + getLabel() + " (" + getIndex() + "), " +
               "Time = " + timeSpn.getStringValue() + ", " +
               "Vehicle Distance = " + distanceSpn.getStringValue() + NL;
    }

    private static List<SpnDefinition> getSpnDefinitions(int pgn) {
        return J1939DaRepository.getInstance()
                                .findPgnDefinition(pgn)
                                .getSpnDefinitions();
    }

    private static Slot getSlot(SpnDefinition def) {
        return J1939DaRepository.getInstance().findSLOT(def.getSlotNumber(), def.getSpnId());
    }
}

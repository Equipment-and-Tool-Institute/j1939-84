package org.etools.j1939tools.j1939.model;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.utils.CollectionUtils;

public class ActiveTechnology {

    public static ActiveTechnology create(int pgn, int[] data) {
        List<SpnDefinition> spnDefinitions = new ArrayList<>(getSpnDefinitions(pgn));
        spnDefinitions.sort(Comparator.comparing(s -> s.getSpnId()));

        int pos = 0;
        SpnDefinition def;

        def = spnDefinitions.get(0);
        var index = new Spn(def.getSpnId(),
                            def.getLabel(),
                            getSlot(def),
                            CollectionUtils.toByteArray(Arrays.copyOfRange(data,
                                                                           pos,
                                                                           pos + getSlot(def).getByteLength())));

        pos += getSlot(def).getByteLength();
        def = spnDefinitions.get(1);
        var time = new Spn(def.getSpnId(),
                           def.getLabel(),
                           getSlot(def),
                           CollectionUtils.toByteArray(Arrays.copyOfRange(data,
                                                                          pos,
                                                                          pos + getSlot(def).getByteLength())));

        pos += getSlot(def).getByteLength();
        def = spnDefinitions.get(2);
        int to = pos + getSlot(def).getByteLength();
        var distance = new Spn(def.getSpnId(),
                               def.getLabel(),
                               getSlot(def),
                               CollectionUtils.toByteArray(Arrays.copyOfRange(data,
                                                                              pos,
                                                                              to)));

        return new ActiveTechnology(index, time, distance);
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

/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;


import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.List;

import net.soliddesign.j1939tools.j1939.model.Spn;
import net.soliddesign.j1939tools.j1939.packets.FreezeFrame;

public class TableA2ValueValidator {

    private static final List<Integer> ENGINE_SPEED_SPNS = List.of(190, 4201, 723, 4202);
    private static final List<Integer> TEMPERATURE_SPNS = List.of(110, 1637, 4076, 4193);

    private final int partNumber;
    private final int stepNumber;

    public TableA2ValueValidator(int partNumber, int stepNumber) {
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
    }

    public void reportWarnings(FreezeFrame freezeFrame, ResultsListener listener, String section) {
        for (int spnId : TEMPERATURE_SPNS) {
            Spn spn = freezeFrame.getSpn(spnId);
            Double value = spn == null ? null : spn.getValue();
            if (value != null && (value < 7 || value > 110)) {
                addWarning(listener,
                           section,
                           spn.toString() + " is < 7 C or > 110 C");
            }
        }

        for (int spnId : ENGINE_SPEED_SPNS) {
            Spn spn = freezeFrame.getSpn(spnId);
            Double value = spn == null ? null : spn.getValue();
            if (value != null && value <= 300) {
                addWarning(listener, section, spn.toString() + " is <= 300 rpm");
            }
        }

        Double engineSpeed = freezeFrame.getSPNs()
                                        .stream()
                                        .filter(s -> ENGINE_SPEED_SPNS.contains(s.getId()))
                                        .filter(Spn::hasValue)
                                        .map(Spn::getValue)
                                        .findFirst()
                                        .orElse(null);

        if (engineSpeed == null) {
            addWarning(listener, section, "Unable to determine engine speed from freeze frame data");
        } else if (engineSpeed > 300) {
            Spn spn = freezeFrame.getSpn(92);
            Double value = spn == null ? null : spn.getValue();
            if (value != null && value <= 0) {
                addWarning(listener, section, spn.toString() + " is <= 0% with rpm > 300");
            }

            spn = freezeFrame.getSpn(512);
            value = spn == null ? null : spn.getValue();
            if (value != null && value < 0) {
                addWarning(listener, section, spn.toString() + " is < 0% with rpm > 300");
            }

            spn = freezeFrame.getSpn(513);
            value = spn == null ? null : spn.getValue();
            if (value != null && value <= 0) {
                addWarning(listener, section, spn.toString() + " is <= 0% with rpm > 300");
            }

            spn = freezeFrame.getSpn(3301);
            value = spn == null ? null : spn.getValue();
            if (value != null && value < 1) { // < 1 to account for the precision of using a Double
                addWarning(listener,
                           section,
                           spn.toString() + " is = 0 seconds with rpm > 300");
            }
        }
    }

    private void addWarning(ResultsListener listener, String section, String message) {
        listener.addOutcome(partNumber, stepNumber, WARN, section + " - " + message);
    }

}

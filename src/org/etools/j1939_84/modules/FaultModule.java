/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.modules;

import static net.soliddesign.j1939tools.j1939.J1939.GLOBAL_ADDR;

import org.etools.j1939_84.controllers.ResultsListener;

import net.soliddesign.j1939tools.modules.FunctionalModule;

public class FaultModule extends FunctionalModule {

    public void implantFaultA(ResultsListener listener) {
        int pgn = 0x1FFFA;
        getJ1939().requestGlobal("Requesting Fault A to be implanted - REPORT IF SEEN IN THE FIELD",
                                 pgn,
                                 getJ1939().createRequestPacket(pgn, GLOBAL_ADDR),
                                 listener);
    }

    public void implantFaultB(ResultsListener listener) {
        int pgn = 0x1FFFB;
        getJ1939().requestGlobal("Requesting Fault B to be implanted - REPORT IF SEEN IN THE FIELD",
                                 pgn,
                                 getJ1939().createRequestPacket(pgn, GLOBAL_ADDR),
                                 listener);
    }
}

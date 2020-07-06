/**
 * Copyright 2019 Equipment & Tool Institute
 */

package org.etools.j1939_84.bus.j1939;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;

public class UnknownParsedPacket extends ParsedPacket {

    public UnknownParsedPacket(Packet packet) {
        super(packet);
    }

}

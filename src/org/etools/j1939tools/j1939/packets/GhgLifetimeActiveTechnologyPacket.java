package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

public class GhgLifetimeActiveTechnologyPacket extends GhgActiveTechnologyPacket {

    public GhgLifetimeActiveTechnologyPacket(Packet packet) {
        super(packet);
    }

    protected int getChunkLength() {
        return 9;
    }

}

package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.getLogger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.model.ActiveTechnology;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDataParser;
import org.etools.j1939tools.j1939.model.SpnDefinition;

public class GhgActiveTechnologyPacket extends GenericPacket {

    private List<ActiveTechnology> activeTechnologies;

    public GhgActiveTechnologyPacket(Packet packet) {
        super(packet);
    }

    protected int getChunkLength() {
        return 5;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Not a concern in desktop app.")
    public List<ActiveTechnology> getActiveTechnologies() {
        if (activeTechnologies == null) {
            activeTechnologies = new ArrayList<>();

            int chunkSize = getChunkLength();
            int dataLength = getPacket().getLength();

            for (int i = 0; i + chunkSize <= dataLength; i = i + chunkSize) {
                int[] data = getPacket().getData(i, i + chunkSize);
                ActiveTechnology activeTechnology = ActiveTechnology.create(getPacket().getPgn(), data);
                activeTechnologies.add(activeTechnology);
            }
        }
        return activeTechnologies;
    }

    //shouldn't be called on Ghg packet
    @Override
    public List<Spn> getSpns(){
        throw new IllegalStateException("Use getActiveTechnologies() instead.");
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        try {
            result.append(getStringPrefix()).append(NL);
            for (ActiveTechnology at : getActiveTechnologies()) {
                result.append("  ").append(at.toString());
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

}

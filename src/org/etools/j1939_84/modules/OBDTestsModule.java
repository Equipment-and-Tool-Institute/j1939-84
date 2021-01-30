/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.ResultsListener;

/**
 * The {@link FunctionalModule} that collects the Scaled Test Results from the
 * OBD Modules
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class OBDTestsModule extends FunctionalModule {
    
    private Packet createDM7Packet(int destination, int spn) {
        return Packet.create(DM7CommandTestsPacket.PGN | destination,
                             getJ1939().getBusAddress(),
                             true,
                             247,
                             spn & 0xFF,
                             (spn >> 8) & 0xFF,
                             (((spn >> 16) & 0xFF) << 5) | 31,
                             0xFF,
                             0xFF,
                             0xFF,
                             0xFF);
    }

    public List<DM30ScaledTestResultsPacket> getDM30Packets(ResultsListener listener, int address, SupportedSPN spn) {
        int spnId = spn.getSpn();
        Packet request = createDM7Packet(address, spnId);
        BusResult<DM30ScaledTestResultsPacket> result = getJ1939().requestDm7("DM7 for DM30 from " + Lookup.getAddressName(
                address) + " for SPN " + spnId, listener, request);
        listener.onResult("");
        return result.requestResult().getPackets();
    }

    public BusResult<DM24SPNSupportPacket> requestDM24(ResultsListener listener, int obdModuleAddress) {
        return requestDMPackets("DM24", DM24SPNSupportPacket.class, obdModuleAddress, listener).busResult();
    }

}

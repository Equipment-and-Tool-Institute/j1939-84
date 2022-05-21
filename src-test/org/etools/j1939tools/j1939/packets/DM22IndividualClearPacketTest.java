/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_ACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_REQ;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByteSpecificIndicator;
import org.junit.Test;

public class DM22IndividualClearPacketTest {

    @Test
    public void testRequestCreate() {
        Packet packet = DM22IndividualClearPacket.createRequest(0xF9, 0x17, CLR_PA_REQ, 1208, 3);
        DM22IndividualClearPacket instance = new DM22IndividualClearPacket(packet);
        assertEquals(CLR_PA_REQ, instance.getControlByte());
        assertEquals(ControlByteSpecificIndicator.NOT_SUPPORTED, instance.getControlByteSpecificIndicator());
        assertEquals(0xFF, instance.getAcknowledgementCode());

        String expected = "Individual Clear/Reset Of Active And Previously Active DTC from Off Board Diagnostic-Service Tool #1 (249): "
                + NL;
        expected += "Control Byte: Request to Clear Previously Active DTC (1)" + NL;
        expected += "Acknowledgement Code: Not Available (255)" + NL;
        expected += "SPN = 1208; FMI = 3" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testResponse() {
        DM22IndividualClearPacket instance = DM22IndividualClearPacket.create(0,
                                                                              0,
                                                                              CLR_PA_ACK,
                                                                              ControlByteSpecificIndicator.NOT_SUPPORTED,
                                                                              1208,
                                                                              3);
        assertEquals(CLR_PA_ACK, instance.getControlByte());
        assertEquals(ControlByteSpecificIndicator.NOT_SUPPORTED, instance.getControlByteSpecificIndicator());
        assertEquals(0xFF, instance.getAcknowledgementCode());

        String expected = "Individual Clear/Reset Of Active And Previously Active DTC from Engine #1 (0): " + NL;
        expected += "Control Byte: Positive Acknowledgement of Clear Previously Active DTC (2)" + NL;
        expected += "Acknowledgement Code: Not Available (255)" + NL;
        expected += "SPN = 1208; FMI = 3" + NL;
        assertEquals(expected, instance.toString());
    }
}

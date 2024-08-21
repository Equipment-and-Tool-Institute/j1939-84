package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.model.ActiveTechnology;
import org.etools.j1939tools.utils.CollectionUtils;
import org.junit.Test;

public class GhgActiveTechnologyPacketTest {

    @Test
    public void testAll() {
        int[] data = new int[0];
        for (int i = 0; i <= 250; i++) {
            data = CollectionUtils.join(data, new int[]{i, i, 0, i, 0});
        }
        var packet = Packet.create(64255, 0, data);
        var instance = new GhgActiveTechnologyPacket(packet);

        List<ActiveTechnology> activeTechnologies = instance.getActiveTechnologies();
        assertEquals(251, activeTechnologies.size());
        System.out.println(instance.toString());

    }

    @Test
    public void testWithOneEntry() {
        var packet = Packet.create(64255, 0, 0x01, 0xA5, 0xA5, 0xA4, 0xA5);
        var instance = new GhgActiveTechnologyPacket(packet);

        assertEquals(1, instance.getActiveTechnologies().size());
        ActiveTechnology at = instance.getActiveTechnologies().get(0);
        assertEquals(1, at.getIndex());
        assertEquals(424050, at.getTime(), 0);
        assertEquals(10601, at.getDistance(), 0);
    }

    @Test
    public void testWithTwoEntries() {
        var packet = Packet.create(64255, 0, 0x01, 0xA5, 0xA5, 0xA4, 0xA5, 0x02, 0xA6, 0xA6, 0xA4, 0xA6);
        var instance = new GhgActiveTechnologyPacket(packet);

        assertEquals(2, instance.getActiveTechnologies().size());

        ActiveTechnology at1 = instance.getActiveTechnologies().get(0);
        assertEquals(1, at1.getIndex());
        assertEquals(424050, at1.getTime(), 0);
        assertEquals(10601, at1.getDistance(), 0);

        ActiveTechnology at2 = instance.getActiveTechnologies().get(1);
        assertEquals(2, at2.getIndex());
        assertEquals(426620, at2.getTime(), 0);
        assertEquals(10665, at2.getDistance(), 0);
    }

    @Test(expected=RuntimeException.class)
     public void testGetSpns(){
        var packet = Packet.create(64255, 0, 0x01, 0xA5, 0xA5, 0xA5, 0xA5);
        var instance = new GhgActiveTechnologyPacket(packet);
        instance.getSpns();
    }

}
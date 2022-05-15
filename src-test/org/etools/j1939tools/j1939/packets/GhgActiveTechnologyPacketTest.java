package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;
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

        System.out.println(instance.toString());

    }

    @Test
    public void testWithOneEntry() {
        var packet = Packet.create(64255, 0, 0x01, 0xA5, 0xA5, 0xA5, 0xA5);
        var instance = new GhgActiveTechnologyPacket(packet);

        System.out.println(instance.toString());

    }

    @Test
    public void testWithTwoEntries() {
        var packet = Packet.create(64255, 0, 0x01, 0xA5, 0xA5, 0xA5, 0xA5, 0x02, 0xA5, 0xA5, 0xA5, 0xA5);
        var instance = new GhgActiveTechnologyPacket(packet);

        System.out.println(instance.toString());

    }

}
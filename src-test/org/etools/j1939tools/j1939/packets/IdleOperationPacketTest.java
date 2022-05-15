/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IdleOperationPacketTest {

    @Test
    public void getEngineTotalIdleHours() {
        var instance = IdleOperationPacket.create(0, 123456789);
        assertEquals(123456789, instance.getEngineIdleHours(), 0.0);
        // 4294967295
    }
}

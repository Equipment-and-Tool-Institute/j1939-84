/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package net.soliddesign.bus;

import java.util.concurrent.TimeUnit;

import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.J1939TP;
import org.junit.Before;
import org.junit.Test;

public class FileBusTest {

    private J1939 j1939;

    @Before
    public void setUp() throws BusException {
        J1939TP bus = new J1939TP(new FileBus(0xF9));
        j1939 = new J1939(bus);
        bus.setJ1939(j1939);
    }

    @Test
    public void testFileBus() throws BusException {
        j1939.read(1, TimeUnit.DAYS)
             .forEach(System.out::println);
    }
}

/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.J1939TP;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.ini4j.InvalidFileFormatException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for the {@link RP1210} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class RP1210Test {

    private static RP1210 createInstance(String iniFile) throws URISyntaxException {
        URL url = RP1210Test.class.getResource(iniFile);
        Path parent = Paths.get(url.toURI()).getParent();
        assertNotNull(parent);
        return new RP1210(parent.toString());
    }

    /** simple CAN logger for bench testing */
    static public void main(String[] args) throws IOException, BusException {
        List<Adapter> adapters = new RP1210().getAdapters();
        if (args.length == 0) {
            System.err.println("Choose and adapter!");
            for (Adapter a : adapters) {
                System.err.println(a.getDLLName() + " : " + a.getDeviceId() + "\t" + a.getName());
            }
            System.exit(-1);
        }

        Optional<Adapter> adapter = adapters.stream()
                                            .filter(a -> a.getDLLName().equals(args[0])
                                                    && a.getDeviceId() == Integer.parseInt(args[1]))
                                            .findFirst();
        if (!adapter.isPresent()) {
            throw new IllegalArgumentException("Unknown RP1210 Adapter");
        }
        RP1210Bus rawBus = new RP1210Bus(adapter.get(), "J1939:Baud=Auto", 0xF9, true);
        // rawBus.log(p -> "raw: " + p.toTimeString());
        Bus tpBus = new J1939TP(rawBus);
        // try (Stream<Packet> read = bus.read(2000, TimeUnit.MILLISECONDS);)
        {
            final Class<DM29DtcCounts> cls = DM29DtcCounts.class;
            new J1939(tpBus)
                            .requestGlobal("Request " + cls, cls, new TestResultsListener() {
                                @Override
                                public void onResult(String result) {
                                    System.err.println("RST: " + result);
                                }
                            })
                            .getEither()
                            .stream()
                            .map(Either::resolve)
                            .forEach(p -> System.err.format("Response: %s%n", p));
            // read.forEach(p -> System.err.format("%8s %s%n", p.isTransmitted()
            // ? "echoed" : "", p.toTimeString()));
        }
        System.exit(0);
    }

    @After
    public void tearDown() throws Exception {
        J1939_84.setTesting(false);
    }

    @Test
    public void testGetAdapters() throws Exception {
        J1939_84.setTesting(false);

        RP1210 instance = createInstance("test/rp1210/RP121032.INI");
        List<Adapter> actual = instance.getAdapters();
        assertEquals(5, actual.size());
        {
            Adapter adapter = actual.get(0);
            assertEquals("NEXIQ Technologies USB-Link - Bluetooth USB-Link", adapter.getName());
            assertEquals("NXULNK32", adapter.getDLLName());
            assertEquals(2, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(1);
            assertEquals("NEXIQ Technologies USB-Link - USB-Link", adapter.getName());
            assertEquals("NXULNK32", adapter.getDLLName());
            assertEquals(1, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(2);
            assertEquals("NEXIQ Technologies USB-Link 2 - Bluetooth USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(2, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(3);
            assertEquals("NEXIQ Technologies USB-Link 2 - USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(1, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(4);
            assertEquals("NEXIQ Technologies USB-Link 2 - WiFi USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(3, adapter.getDeviceId());
        }
    }

    @Test
    public void testGetAdaptersWithBadIniDriverFile() throws Exception {
        J1939_84.setTesting(false);
        RP1210 instance = createInstance("test/rp1210_badDriver/RP121032.INI");
        List<Adapter> actual = instance.getAdapters();
        assertEquals(3, actual.size());
        {
            Adapter adapter = actual.get(0);
            assertEquals("NEXIQ Technologies USB-Link 2 - Bluetooth USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(2, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(1);
            assertEquals("NEXIQ Technologies USB-Link 2 - USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(1, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(2);
            assertEquals("NEXIQ Technologies USB-Link 2 - WiFi USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(3, adapter.getDeviceId());
        }
    }

    @Test
    public void testGetAdaptersWithBadIniFile() throws Exception {
        J1939_84.setTesting(false);
        RP1210 instance = createInstance("test/rp1210Bad/RP121032.INI");
        try {
            instance.getAdapters();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertTrue(e.getCause() instanceof InvalidFileFormatException);
        }
    }

    @Test
    public void testGetAdaptersWithTestingTrue() throws Exception {
        J1939_84.setTesting(true);
        RP1210 instance = createInstance("test/rp1210/RP121032.INI");
        List<Adapter> actual = instance.getAdapters();
        {
            Adapter adapter = actual.get(0);
            assertEquals("Loop Back Adapter", adapter.getName());
            assertEquals("Simulated", adapter.getDLLName());
            assertEquals(-1, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(1);
            assertEquals("NEXIQ Technologies USB-Link - Bluetooth USB-Link", adapter.getName());
            assertEquals("NXULNK32", adapter.getDLLName());
            assertEquals(2, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(2);
            assertEquals("NEXIQ Technologies USB-Link - USB-Link", adapter.getName());
            assertEquals("NXULNK32", adapter.getDLLName());
            assertEquals(1, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(3);
            assertEquals("NEXIQ Technologies USB-Link 2 - Bluetooth USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(2, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(4);
            assertEquals("NEXIQ Technologies USB-Link 2 - USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(1, adapter.getDeviceId());
        }
        {
            Adapter adapter = actual.get(5);
            assertEquals("NEXIQ Technologies USB-Link 2 - WiFi USB-Link 2", adapter.getName());
            assertEquals("NULN2R32", adapter.getDLLName());
            assertEquals(3, adapter.getDeviceId());
        }
    }

    @Test
    @Ignore
    // What is this trying to test? There is no Simulated Adapter DLL to load
    public void testSetAdapterWithLookBackAdapter() throws Exception {
        J1939_84.setTesting(true);
        RP1210 instance = createInstance("test/rp1210/RP121032.INI");
        List<Adapter> adapters = instance.getAdapters();
        Adapter adapter = adapters.get(0);
        Bus bus = RP1210.createBus(adapter, "J1939:Baud=Auto", 0xA5);
        assertTrue(bus instanceof EchoBus);
        assertEquals(0xA5, bus.getAddress());
    }
}

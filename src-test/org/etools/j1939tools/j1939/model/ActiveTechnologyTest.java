package org.etools.j1939tools.j1939.model;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.packets.GhgActiveTechnologyPacket;
import org.etools.j1939tools.j1939.packets.GhgLifetimeActiveTechnologyPacket;
import org.etools.j1939tools.modules.GhgTrackingModule;
import org.junit.Test;

public class ActiveTechnologyTest {
    @Test
    public void testPgns() {
        var pgns = List.of(GhgTrackingModule.GHG_STORED_100_HR,
                           GhgTrackingModule.GHG_STORED_GREEN_HOUSE_100_HR,
                           GhgTrackingModule.GHG_STORED_HYBRID_CHG_DEPLETING_100_HR,
                           GhgTrackingModule.GHG_ACTIVE_100_HR,
                           GhgTrackingModule.GHG_ACTIVE_GREEN_HOUSE_100_HR,
                           GhgTrackingModule.GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR);
        for (int pgn : pgns) {
            var ghg = new GhgActiveTechnologyPacket(Packet.create(pgn,
                                                                  0,
                                                                  1,
                                                                  2,
                                                                  3,
                                                                  4,
                                                                  5,
                                                                  6,
                                                                  7,
                                                                  8,
                                                                  9,
                                                                  0,
                                                                  1,
                                                                  2,
                                                                  3,
                                                                  4,
                                                                  5,
                                                                  6));
            for (var at : ghg.getActiveTechnologies())
                System.err.println(at);
        }
    }

    @Test
    public void testLifeTimePgns() {
        var pgns = List.of(GhgTrackingModule.GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG,
                           GhgTrackingModule.GHG_TRACKING_LIFETIME_PG);
        for (int pgn : pgns) {
            var ghg = new GhgLifetimeActiveTechnologyPacket(Packet.create(pgn,
                                                                          0,
                                                                          1,
                                                                          2,
                                                                          3,
                                                                          4,
                                                                          5,
                                                                          6,
                                                                          7,
                                                                          8,
                                                                          9,
                                                                          0,
                                                                          1,
                                                                          2,
                                                                          3,
                                                                          4,
                                                                          5,
                                                                          6));
            for (var at : ghg.getActiveTechnologies())
                System.err.println(at);
        }

    }

    @Test
    public void testToString2() {
        var instance = ActiveTechnology.create(64255, new int[] { 2, 0xA5, 0xA5, 0xA5, 0xA5 });
        String expected = "Active Technology:  Cylinder Deactivation (2), Time = 424050.000 s, Vehicle Distance = 10601.250 km"
                + NL;
        String actual = instance.toString();
        assertEquals(expected, actual);
    }
}

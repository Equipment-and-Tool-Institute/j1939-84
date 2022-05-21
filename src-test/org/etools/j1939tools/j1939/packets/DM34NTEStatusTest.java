/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.INSIDE;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.NOT_AVAILABLE;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.OUTSIDE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DM34NTEStatusTest {

    @Test
    public void test() {
        DM34NTEStatus instance = DM34NTEStatus.create(0,
                                                      0,
                                                      OUTSIDE,
                                                      INSIDE,
                                                      NOT_AVAILABLE,
                                                      NOT_AVAILABLE,
                                                      OUTSIDE,
                                                      INSIDE);

        String expected = "";
        expected += "DM34 NTE Status from Engine #1 (0):  {" + NL;
        expected += "                          NOx NTE Control Area Status = Outside Area (0)" + NL;
        expected += "  Manufacturer-specific NOx NTE Carve-out Area Status = Inside Area (1)" + NL;
        expected += "                       NOx NTE Deficiency Area Status = Not available (3)" + NL;
        expected += "                           PM NTE Control Area Status = Not available (3)" + NL;
        expected += "   Manufacturer-specific PM NTE Carve-out Area Status = Outside Area (0)" + NL;
        expected += "                        PM NTE Deficiency Area Status = Inside Area (1)" + NL;
        expected += "}" + NL;

        assertEquals(expected, instance.toString());

        assertEquals(OUTSIDE, instance.getNoxNTEControlAreaStatus());
        assertEquals(INSIDE, instance.getNoxNTECarveOutAreaStatus());
        assertEquals(NOT_AVAILABLE, instance.getNoxNTEDeficiencyAreaStatus());
        assertEquals(NOT_AVAILABLE, instance.getPmNTEControlAreaStatus());
        assertEquals(OUTSIDE, instance.getPmNTECarveOutAreaStatus());
        assertEquals(INSIDE, instance.getPmNTEDeficiencyAreaStatus());
    }
}

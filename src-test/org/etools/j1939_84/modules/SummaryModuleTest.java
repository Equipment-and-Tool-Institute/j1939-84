/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.controllers.PartResultRepository;
import org.etools.j1939_84.model.Outcome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SummaryModuleTest {

    private SummaryModule instance;

    @Before
    public void setUp() {
        PartResultRepository.setInstance(null);
        instance = new SummaryModule();
    }

    @After
    public void tearDown() {
        PartResultRepository.setInstance(null);
    }

    @Test
    public void testOutput() {
        instance.addOutcome(1, 1, Outcome.FAIL, "Part 1 Step 1 Fail");
        instance.addOutcome(1, 2, Outcome.INCOMPLETE, "Part 1 Step 2 Incomplete");
        instance.addOutcome(1, 3, Outcome.WARN, "Part 1 Step 1 Warning");
        // TIMING removed
        // instance.addOutcome(1, 4, Outcome.TIMING, "Part 1 Step 1 Timing");
        instance.addOutcome(1, 5, Outcome.PASS, "Part 1 Step 1 Pass");
        instance.addOutcome(1, 6, Outcome.INFO, "Part 1 Step 1 Info");
        instance.addOutcome(1, 7, Outcome.ABORT, "Part 1 Step 1 Fail");

        String actual = instance.generateSummary();
        String expected = "";
        expected += "Part 1 KOEO Data Collection............................................(FAIL)" + NL;
        expected += "" + NL;
        expected += "Step 1.1. Test vehicle data collection.................................(FAIL)" + NL;
        expected += "Step 1.2. Verify engine operation................................(INCOMPLETE)" + NL;
        expected += "Step 1.3. DM5: Diagnostic readiness 1..................................(WARN)" + NL;
        expected += "Step 1.4. DM24: SPN support......................................(INCOMPLETE)" + NL;
        expected += "Step 1.5. PGN 65260 VIN verification...................................(PASS)" + NL;
        expected += "Step 1.6. DM56: Model year and certification engine family.............(INFO)" + NL;
        expected += "Step 1.7. DM19: Calibration information...............................(ABORT)" + NL;
        expected += "Step 1.8. DM20: Monitor Performance Ratio........................(INCOMPLETE)" + NL;
        expected += "Step 1.9. Component ID: Make.....................................(INCOMPLETE)" + NL;
        expected += "Step 1.10. DM11: Diagnostic Data Clear/Reset for Active DTCs.....(INCOMPLETE)" + NL;
        expected += "Step 1.11. DM21: Diagnostic readiness 2..........................(INCOMPLETE)" + NL;
        expected += "Step 1.12. DM7/DM30: Command Non-continuously Monitored Test/S...(INCOMPLETE)" + NL;
        expected += "Step 1.13. DM5: Diagnostic Readiness 1: Monitor Readiness........(INCOMPLETE)" + NL;
        expected += "Step 1.14. DM26: Diagnostic readiness 3..........................(INCOMPLETE)" + NL;
        expected += "Step 1.15. DM1: Active DTCs......................................(INCOMPLETE)" + NL;
        expected += "Step 1.16. DM2: Previously Active DTCs...........................(INCOMPLETE)" + NL;
        expected += "Step 1.17. DM6: Emission related pending DTCs....................(INCOMPLETE)" + NL;
        expected += "Step 1.18. DM12: Emissions related active DTCs...................(INCOMPLETE)" + NL;
        expected += "Step 1.19. DM23: Emission Related Previously Active DTCs.........(INCOMPLETE)" + NL;
        expected += "Step 1.20. DM28: Permanent DTCs..................................(INCOMPLETE)" + NL;
        expected += "Step 1.21. DM27: All Pending DTCs................................(INCOMPLETE)" + NL;
        expected += "Step 1.22. DM29: Regulated DTC counts............................(INCOMPLETE)" + NL;
        expected += "Step 1.23. DM31: DTC to Lamp Association.........................(INCOMPLETE)" + NL;
        expected += "Step 1.24. DM25: Expanded freeze frame...........................(INCOMPLETE)" + NL;
        expected += "Step 1.25. DM20: Monitor performance ratio.......................(INCOMPLETE)" + NL;
        expected += "Step 1.26. Data stream support verification......................(INCOMPLETE)" + NL;
        expected += "Step 1.27. Part 1 to Part 2 Transition...........................(INCOMPLETE)" + NL;
        expected += "" + NL;

        assertEquals(expected, actual);
    }
}
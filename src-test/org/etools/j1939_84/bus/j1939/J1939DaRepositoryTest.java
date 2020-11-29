package org.etools.j1939_84.bus.j1939;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class J1939DaRepositoryTest {
    @Test
    public void verifySpnOrder() {
        new J1939DaRepository()
                .getPgnDefinitions()
                .values()
                .forEach(p -> {
                    int offset = -1;
                    for (var spn : p.spnDefinitions) {
                        // don't worry about the weird SPNs with no positions.
                        if (spn.startByte >= 0) {
                            int spnOffset = spn.startByte * 8 + spn.startBit;
                            assertTrue("Spn " + spn.spnId + " " + spn.label + " is out of order.",
                                    spnOffset >= offset);
                            offset = spnOffset;
                        }
                    }
                });
    }
}

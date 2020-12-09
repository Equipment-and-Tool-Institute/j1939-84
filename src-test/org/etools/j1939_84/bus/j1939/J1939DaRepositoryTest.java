package org.etools.j1939_84.bus.j1939;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class J1939DaRepositoryTest {
    @Test
    public void testMultiline() {
        assertEquals("", new J1939DaRepository().findSpnDefinition(695).getLabel());
    }

    @Test
    public void verifySpnOrder() {
        new J1939DaRepository()
                .getPgnDefinitions()
                .values()
                .forEach(p -> {
                    int offset = -1;
                    for (var spn : p.getSpnDefinitions()) {
                        // don't worry about the weird SPNs with no positions.
                        if (spn.getStartByte() >= 0) {
                            int spnOffset = spn.getStartByte() * 8 + spn.getStartBit();
                            assertTrue("Spn " + spn.getSpnId() + " " + spn.getLabel() + " is out of order.",
                                    spnOffset >= offset);
                            offset = spnOffset;
                        }
                    }
                });
    }
}

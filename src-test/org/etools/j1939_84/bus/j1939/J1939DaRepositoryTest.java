package org.etools.j1939_84.bus.j1939;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;

import org.etools.j1939_84.bus.j1939.packets.Slot;
import org.junit.Test;

public class J1939DaRepositoryTest {
    @Test
    public void verifyDMs() {
        J1939DaRepository j1939Da = J1939DaRepository.getInstance();
        int missingCount = new ArrayList<>(j1939Da.getPgnDefinitions().values()).stream()
                                                                                // only consider DMs that are not
                                                                                // manually implemented in
                                                                                // J1939.processRaw
                                                                                .filter(d -> d.getAcronym()
                                                                                              .startsWith("DM")
                                                                                        && !J1939.isManual(d.getId()))
                                                                                // order them numerically
                                                                                .sorted(Comparator.comparing(t -> Integer.parseInt(t.getAcronym()
                                                                                                                                    .substring(2))))
                                                                                .peek(d -> System.err.format("PGN: %6d (%04X): %6d %s%n",
                                                                                                             d.getId(),
                                                                                                             d.getId(),
                                                                                                             d.getBroadcastPeriod(),
                                                                                                             d.getAcronym()))
                                                                                // map to SPNs
                                                                                .flatMap(d -> d.getSpnDefinitions()
                                                                                               .stream())
                                                                                // if SPN missing, report and count
                                                                                .mapToInt(s -> {
                                                                                    Slot slot = j1939Da.findSLOT(s.getSlotNumber(),
                                                                                                                 s.getSpnId());

                                                                                    if ("UNK".equals(slot.getType())) {
                                                                                        System.err.format("  SPN: %6d (%04X): %3d.%-3d %3d %6d %s%n",
                                                                                                          s.getSpnId(),
                                                                                                          s.getSpnId(),
                                                                                                          s.getStartByte(),
                                                                                                          s.getStartBit(),
                                                                                                          slot.getLength(),
                                                                                                          s.getSlotNumber(),
                                                                                                          s.getLabel());
                                                                                        return 1;
                                                                                    }
                                                                                    return 0;
                                                                                })
                                                                                .sum();
        assertEquals(0, missingCount);
    }

    @Test
    public void verifySpnOrder() {
        J1939DaRepository.getInstance()
                               .getPgnDefinitions()
                               .values()
                               .forEach(p -> {
                                   int offset = -1;
                                   for (var spn : p.getSpnDefinitions()) {
                                       // don't worry about the weird SPNs with no positions.
                                       if (spn.getStartByte() >= 0) {
                                           int spnOffset = spn.getStartByte() * 8 + spn.getStartBit();
                                           assertTrue("Spn " + spn.getSpnId() + " " + spn.getLabel()
                                                   + " is out of order.",
                                                      spnOffset >= offset);
                                           offset = spnOffset;
                                       }
                                   }
                               });
    }
}

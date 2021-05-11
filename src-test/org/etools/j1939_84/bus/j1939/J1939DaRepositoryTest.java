package org.etools.j1939_84.bus.j1939;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.j1939.packets.Slot;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;
import org.junit.Test;

public class J1939DaRepositoryTest {
    @Test
    public void test3069() {
        J1939DaRepository j1939 = J1939DaRepository.getInstance();
        assertEquals(1, j1939.findSpnDefinition(3069).getStartByte());
        assertEquals("DM21",j1939.findPgnDefinition(49408).getAcronym());
    }

    @Test
    public void listMissingDMs() {
        J1939DaRepository j1939da = J1939DaRepository.getInstance();
        List<PgnDefinition> dmPgns = j1939da
                                            .getPgnDefinitions()
                                            .values()
                                            .stream()
                                            .filter(d -> d.getAcronym().startsWith("DM"))
                                            .sorted(Comparator.comparing(t -> Integer.parseInt(t.getAcronym()
                                                                                                .substring(2))))
                                            .collect(Collectors.toList());
        // just report to stderr for visual verification
        dmPgns.stream()
              .peek(p -> System.err.println(p.getAcronym()))
              .flatMap(d -> d.getSpnDefinitions().stream())
              .filter(s -> s.getStartByte() == -1)
              .forEach(s -> System.err.println("  " + s.getStartByte() + "." + s.getStartBit() + "\t"
                      + s.getLabel() + "\t"
                      // indicate missing SLOTs
                      + (j1939da.findSLOT(-1, s.getSpnId()) == null ? "MISSING SLOT" : "ok")));

        // We expect to not have definitions for DMs that have no fixed length information. The fault DMs(DM1, DM2, et.
        // al.) are variable length, but do have fixed length data in them.
        String[] variableLengthDMs = new String[] { "DM19", "DM24", "DM25", "DM30", "DM31", "DM33" };
        Object[] missingDms = dmPgns.stream()
                                    .filter(p -> p.getSpnDefinitions().stream().anyMatch(s -> s.getStartByte() == -1))
                                    .map(PgnDefinition::getAcronym)
                                    .toArray(String[]::new);
        assertArrayEquals(variableLengthDMs, missingDms);
    }

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

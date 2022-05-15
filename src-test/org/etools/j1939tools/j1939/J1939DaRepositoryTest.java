package org.etools.j1939tools.j1939;

import static org.etools.j1939tools.j1939.J1939DaRepository.parseNumber;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.Slot;
import org.junit.Test;

public class J1939DaRepositoryTest {

    private static final String[] variableLengthDMs = new String[] { "DM19", "DM24", "DM25", "DM30", "DM31", "DM33" };

    @Test
    public void test3069() {
        final int SPN = 3069;
        J1939DaRepository j1939 = J1939DaRepository.getInstance();
        assertEquals(1, j1939.findSpnDefinition(SPN).getStartByte());
        assertEquals("DM21", j1939.findPgnDefinition(49408).getAcronym());
        assertNotEquals("Slot", "UNK", j1939.findSLOT(j1939.findSpnDefinition(SPN).getSlotNumber(), SPN).getType());
    }

    @Test
    public void test5843() {
        J1939DaRepository j1939 = J1939DaRepository.getInstance();
        assertEquals("A warm-up cycle has not occurred on this engine start",
                     j1939.findSLOT(-5843).asString(new byte[] { 0 }));
        assertEquals("DM57", j1939.findPgnDefinition(64710).getAcronym());
        assertEquals("UNK", j1939.findSLOT(j1939.findSpnDefinition(-5843).getSlotNumber()).getType());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testGeneralPacketContaining3069() {
        Packet packet = Packet.create(0xC1FF, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        var genericPacket = new GenericPacket(packet);
        System.out.println(genericPacket);
        assertEquals(8721.0, genericPacket.getSpn(3069).get().getValue(), 0.0);
    }

    @Test
    public void testDMMissingSlots() {
        J1939DaRepository j1939da = J1939DaRepository.getInstance();
        boolean fail = false;
        for (var p : j1939da.getPgnDefinitions().values()) {
            String acronym = p.getAcronym();
            if (acronym.startsWith("DM") && !Arrays.asList(variableLengthDMs).contains(acronym)) {
                for (var s : p.getSpnDefinitions()) {
                    if ("UNK".equals(j1939da.findSLOT(s.getSlotNumber(), s.getSpnId()).getType())) {
                        fail = true;
                        System.err.println("SLOT: " + acronym + " " + s.getSpnId() + " " + s.getLabel());
                    }
                }
            }
        }
        assertFalse(fail);
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
              // .filter(s -> s.getStartByte() == -1) filter for missing only
              .forEach(s -> System.err.println("  " + s.getStartByte() + "." + s.getStartBit() + "\t"
                      + s.getLabel() + "\t"
                      // indicate missing SLOTs
                      + ("UNK".equals(j1939da.findSLOT(-1, s.getSpnId()).getType()) ? "MISSING SLOT" : "ok")));

        // We expect to not have definitions for DMs that have no fixed length information. The fault DMs(DM1, DM2, et.
        // al.) are variable length, but do have fixed length data in them.
        Object[] missingDms = dmPgns.stream()
                                    .filter(p -> p.getSpnDefinitions().stream().anyMatch(s -> s.getStartByte() == -1))
                                    .map(PgnDefinition::getAcronym)
                                    .toArray(String[]::new);
        assertArrayEquals("Missing j1939da addendum.", variableLengthDMs, missingDms);
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

    @Test
    public void parsingNumbers() {
        // 0
        assertEquals(0, parseNumber("0"));
        assertEquals(0, parseNumber("0b"));
        assertEquals(0, parseNumber("0x"));
        assertEquals(0, parseNumber("0h"));
        assertEquals(0, parseNumber("x0"));
        assertEquals(0, parseNumber("$0"));
        // 1
        assertEquals(1, parseNumber("1"));
        assertEquals(1, parseNumber("1b"));
        assertEquals(1, parseNumber("1x"));
        assertEquals(1, parseNumber("1h"));
        assertEquals(1, parseNumber("x1"));
        assertEquals(1, parseNumber("$1"));
        assertEquals(1, parseNumber("01"));
        assertEquals(1, parseNumber("01b"));
        assertEquals(1, parseNumber("01x"));
        assertEquals(1, parseNumber("01h"));
        assertEquals(1, parseNumber("x01"));
        assertEquals(1, parseNumber("$01"));
        // 5
        assertEquals(5, parseNumber("5"));
        assertEquals(5, parseNumber("101b"));
        assertEquals(5, parseNumber("5x"));
        assertEquals(5, parseNumber("5h"));
        assertEquals(5, parseNumber("x5"));
        assertEquals(5, parseNumber("$5"));
        assertEquals(5, parseNumber("05"));
        assertEquals(5, parseNumber("0101b"));
        assertEquals(5, parseNumber("05x"));
        assertEquals(5, parseNumber("05h"));
        assertEquals(5, parseNumber("x05"));
        assertEquals(5, parseNumber("$05"));
        // 255
        assertEquals(255, parseNumber("255"));
        assertEquals(255, parseNumber("1111_1111b"));
        assertEquals(255, parseNumber("11111111b"));
        assertEquals(255, parseNumber("FFx"));
        assertEquals(255, parseNumber("ffx"));
        assertEquals(255, parseNumber("FFh"));
        assertEquals(255, parseNumber("ffh"));
        assertEquals(255, parseNumber("xFF"));
        assertEquals(255, parseNumber("xff"));
        assertEquals(255, parseNumber("$FF"));
        assertEquals(255, parseNumber("$ff"));
        // 256
        assertEquals(256, parseNumber("256"));
        assertEquals(256, parseNumber("1_0000_0000b"));
        assertEquals(256, parseNumber("100000000b"));
        assertEquals(256, parseNumber("100x"));
        assertEquals(256, parseNumber("100h"));
        assertEquals(256, parseNumber("x100"));
        assertEquals(256, parseNumber("$100"));
    }
}

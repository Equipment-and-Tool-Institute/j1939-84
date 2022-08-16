package org.etools.j1939tools.j1939;

import static java.lang.Integer.parseInt;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.j1939.packets.BitSlot;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.resources.J1939ToolsResources;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class J1939DaRepository {

    private static class ParseError extends Exception {
        public ParseError(String string) {
            super(string);
        }
    }

    private static final J1939DaRepository instance = new J1939DaRepository();

    private Map<Integer, Slot> slots;

    private Map<Integer, PgnDefinition> pgnLut;

    public Slot findSLOT(int id, int spn) {
        if (slots == null) {
            slots = new HashMap<>();
            slots.putAll(loadSlots());
            slots.putAll(loadBitSlots());
        }

        // first check for overriding custom slot
        Slot slot = slots.get(-spn);
        // otherwise use SAE specified slot
        if (slot == null) {
            slot = slots.get(id);
        }
        if (slot == null) {
            if (id != -1) {
                J1939_84.getLogger().log(Level.INFO, "Unable to find SLOT " + id);
            }
            return new Slot(id, "Unknown", "UNK", 1.0, 0.0, null, 0);
        }
        return slot;
    }

    private Map<Integer, SpnDefinition> spnLut;

    private Map<Integer, Set<Integer>> spnToPgnMap = null;

    private J1939DaRepository() {
    }

    /**
     * These SPNs represent SP which appear in multiple PGs.
     */
    private final List<Integer> omittedSPNs = Collections.unmodifiableList(List.of(588,
                                                                                   1213,
                                                                                   1220,
                                                                                   12675,
                                                                                   12730,
                                                                                   12783,
                                                                                   12797));

    public List<Integer> getOmittedDataStreamSPNs() {
        return omittedSPNs;
    }

    public boolean isOmittedDataStreamSpS(int sp) {
        return omittedSPNs.contains(sp);
    }

    public static J1939DaRepository getInstance() {
        return instance;
    }

    public static Slot findSlot(int slotId, int spn) {
        return getInstance().findSLOT(slotId, spn);
    }

    @SuppressFBWarnings(value = {
            "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            "REC_CATCH_EXCEPTION" }, justification = "Several places in the calls down the stack can return null")
    private synchronized void loadLookUpTables() {
        if (pgnLut == null) {
            // parse the selected columns from J1939DA. The source data is
            // unaltered, so some processing is required to convert byte.bit
            // specifications into ints.
            InputStream is = new SequenceInputStream(J1939ToolsResources.class.getResourceAsStream("j1939da-extract.csv"),
                                                     J1939ToolsResources.class.getResourceAsStream("j1939da-addendum.csv"));
            InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
            try (CSVReader reader = new CSVReaderBuilder(isReader).withSkipLines(2).build()) {
                // collect [pgn,spn]
                Collection<Object[]> table = StreamSupport.stream(reader.spliterator(), false)
                                                          // allow for blank lines
                                                          .filter(line -> line.length > 1)
                                                          // map line to [pgn,spn] where pgn may be null
                                                          .map(line -> {
                                                              try {
                                                                  String position = line[4];
                                                                  int startByte;
                                                                  int startBit;
                                                                  position = position.toLowerCase();
                                                                  if (position.isBlank()) {
                                                                      // must be a non-pgn spn
                                                                      startByte = -1;
                                                                      startBit = -1;
                                                                  } else if (position.matches("[a-z]")) {
                                                                      startByte = position.charAt(0) - 'a' + 1;
                                                                      startBit = 1;
                                                                  } else if (position.matches("\\d+((,|-| to ).*)?")) {
                                                                      startByte = parseInt(position.split("[^\\d]")[0]);
                                                                      startBit = 1;
                                                                  } else if (position.matches("\\d+\\.\\d+((,|-| to ).*)?")) {
                                                                      String[] a = position.split("[^\\d]");
                                                                      startByte = parseInt(a[0]);
                                                                      startBit = parseInt(a[1]);
                                                                  } else if ("a (starts at byte 10)".equals(position)) {
                                                                      startByte = 10;
                                                                      startBit = 1;
                                                                  } else {
                                                                      throw new ParseError("Unable to parse position: "
                                                                              + position);
                                                                  }

                                                                  SpnDefinition spnDef = null;
                                                                  String spnIdStr = line[5];
                                                                  if (!isBlankOrNA(spnIdStr)) {
                                                                      String label = shortenLabel(line[6]);
                                                                      int spnId = parseInt(spnIdStr);
                                                                      spnDef = new SpnDefinition(spnId,
                                                                                                 label,
                                                                                                 startByte,
                                                                                                 startBit,
                                                                                                 isBlankOrNA(line[7])
                                                                                                         ? -1
                                                                                                         : parseInt(line[7]));
                                                                  }
                                                                  String pgnIdStr = line[0];
                                                                  PgnDefinition pgnDef = null;
                                                                  // we don't care about the PGN that have no
                                                                  // SPNs.
                                                                  if ( !isBlankOrNA(pgnIdStr)) {
                                                                      if(spnDef == null){
                                                                          spnDef = new SpnDefinition(-1, "Unknown", 0, 0, -1);
                                                                      }
                                                                      int transmissionRate = parseTransmissionRate(line[3]);
                                                                      String label = shortenLabel(line[1]).trim();
                                                                      pgnDef = new PgnDefinition(parseInt(pgnIdStr),
                                                                                                 label,
                                                                                                 line[2].trim(),
                                                                                                 transmissionRate == 0,
                                                                                                 transmissionRate < 0,
                                                                                                 Math.abs(transmissionRate),
                                                                                                 Collections.singletonList(spnDef));
                                                                  }
                                                                  return new Object[] { pgnDef, spnDef };
                                                              } catch (ParseError e) {
                                                                  System.err.format("%d %s %n\t%s%n",
                                                                                    reader.getLinesRead(),
                                                                                    e.getMessage(),
                                                                                    Arrays.asList(line));
                                                                  return null;
                                                              }
                                                          })
                                                          .filter(Objects::nonNull)
                                                          .collect(Collectors.toList());
                spnLut = table.stream()
                              .map(row -> ((SpnDefinition) row[1]))
                              .filter(Objects::nonNull)
                              // prefer the spn with a start byte over the one without
                              .sorted(Comparator.comparing(SpnDefinition::getStartByte)
                                                .reversed()
                                                // then prefer the one with custom slot definition
                                                .thenComparing(SpnDefinition::getSlotNumber))
                              .collect(Collectors.toMap(SpnDefinition::getSpnId, s -> s, (a, b) -> a));

                pgnLut = table.stream()
                              .flatMap(row -> row[0] == null ? Stream.empty() : Stream.of((PgnDefinition) row[0]))
                              .collect(Collectors.toMap(PgnDefinition::getId,
                                                        pgnDef -> pgnDef,
                                                        (a, b) -> new PgnDefinition(a.getId(),
                                                                                    shortenLabel(a.getLabel()),
                                                                                    a.getAcronym(),
                                                                                    a.isOnRequest(),
                                                                                    a.isVariableBroadcast(),
                                                                                    a.getBroadcastPeriod(),
                                                                                    Stream.concat(a.getSpnDefinitions()
                                                                                                   .stream(),
                                                                                                  b
                                                                                                   .getSpnDefinitions()
                                                                                                   .stream())
                                                                                          .map(SpnDefinition::getSpnId)
                                                                                          .distinct()
                                                                                          .map(id -> spnLut.get(id))
                                                                                          .sorted(Comparator
                                                                                                            .comparing(s -> s.getStartByte()
                                                                                                                    * 8
                                                                                                                    + s
                                                                                                                       .getStartBit()))
                                                                                          .collect(Collectors.toList()))));

                spnToPgnMap = new HashMap<>();
                for (PgnDefinition pgnDefinition : pgnLut.values()) {
                    for (SpnDefinition spnDefinition : pgnDefinition.getSpnDefinitions()) {
                        Set<Integer> pgns = spnToPgnMap.getOrDefault(spnDefinition.getSpnId(), new HashSet<>());
                        pgns.add(pgnDefinition.getId());
                        spnToPgnMap.put(spnDefinition.getSpnId(), pgns);
                    }
                }
            } catch (Exception e) {
                logError("Error loading J1939DA data.", e);
                throw new RuntimeException("Unable to load J1939DA", e);
            }
        }
    }

    static private boolean isBlankOrNA(String str) {
        return str.isBlank() || "N/A".equals(str.toUpperCase());
    }

    /**
     * Read the slots.csv file which contains all the SLOTs
     *
     * @return Map of SLOT ID to Slot
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "Several places in the calls down the stack can return null")
    private Map<Integer, Slot> loadSlots() {
        Map<Integer, Slot> slots = new HashMap<>();
        String[] values;

        InputStream is = new SequenceInputStream(J1939ToolsResources.class.getResourceAsStream("j1939da-slots.csv"),
                                                 J1939ToolsResources.class.getResourceAsStream("j1939da-slots-addendum.csv"));
        InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        try (CSVReader reader = new CSVReaderBuilder(isReader)
                                                              .withSkipLines(2)
                                                              .build()) {
            while ((values = reader.readNext()) != null) {
                if (values.length > 1 && !values[0].startsWith(";")) {
                    try {
                        int id = parseInt(values[0]);
                        String name = values[1];
                        String type = values[2];
                        Double scaling = Double.parseDouble(values[3]);
                        String unit = values[4];
                        Double offset = Double.parseDouble(values[5]);
                        int length = parseInt(values[6]);
                        Slot slot = new Slot(id, name, type, scaling, offset, unit, length);
                        slots.put(id, slot);
                    } catch (Exception e) {
                        logError("Error loading slot:" + Arrays.asList(values), e);
                    }
                }
            }
        } catch (Exception e) {
            logError("Error loading map from slots", e);
        }
        return slots;
    }

    private Map<Integer, BitSlot> loadBitSlots() {
        Map<Integer, BitSlot> bitSlotMap = new HashMap<>();
        String fileName = "bit-slots.csv";

        InputStream is = J1939ToolsResources.class.getResourceAsStream(fileName);

        InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
        try (CSVReader reader = new CSVReader(isReader)) {
            String[] values;
            while ((values = reader.readNext()) != null) {
                // ignore empty and comment lines
                if (values.length > 1 && !values[0].startsWith(";")) {
                    int currentSlotId = parseInt(values[0].trim());
                    BitSlot bitSlot = bitSlotMap.get(currentSlotId);
                    if (bitSlot == null) {
                        int bitSlotDataLength = Integer.parseInt(values[3].trim());
                        bitSlot = new BitSlot(currentSlotId,
                                              "UNKNOWN",
                                              bitSlotDataLength);
                        bitSlotMap.put(currentSlotId, bitSlot);
                    }
                    bitSlot.addValue(parseNumber(values[1].trim()), values[2].trim());
                }
            }
        } catch (Exception e) {
            J1939_84.getLogger().log(Level.SEVERE, "Error loading map from " + fileName, e);
        }
        return bitSlotMap;
    }

    // support (b)inary and ($xh)ex encoding of slot data.
    static public int parseNumber(String number) {
        number = number.replaceAll("_", "");
        return
        // binary
        number.endsWith("b") ? Integer.parseInt(number.substring(0, number.length() - 1), 2)
                // hex
                : number.startsWith("$") ? Integer.parseInt(number.substring(1), 16)
                : number.startsWith("x") ? Integer.parseInt(number.substring(1), 16)
                : number.endsWith("x") ? Integer.parseInt(number.substring(0, number.length() - 1), 16)
                : number.endsWith("h") ? Integer.parseInt(number.substring(0, number.length() - 1), 16)
                // decimal
                : Integer.parseInt(number);
    }

    private void logError(String s, Exception e) {
        J1939_84.getLogger().log(Level.SEVERE, s, e);
    }

    static private int parseTransmissionRate(String transmissionRate) throws ParseError {
        switch (transmissionRate) {

            // variety of ways to describe on request
            case "":
            case "As needed":
            case "On request":
            case "On Request":
            case "As requested":
            case "As required":
            case "On powerup and on request":
            case "As required but no more often than 500 ms":
            case "When needed":
            case "On request then 1 s until key off":
                return 0;

            // 10 ms
            case "To engine: Control Purpose dependent or 10 ms\n"
                    + "To retarder: 50 ms":
                return 10;

            // 20 ms
            case "Manufacturer defined, not faster than 20 ms":
            case "Default broadcast rate of 20 ms unless the sending device has received Engine Start Control Message Rate (SPN 7752) from the engine start arbitrator indicating a switch to 250 ms and on change, but no faster than 20 ms.":
            case "Default broadcast rate of 20 ms unless the arbitrator is transmitting Engine Start Control Message Rate (SPN 7752) indicating a switch to 250 ms or on change, but no faster than 20 ms.":
                return 20;

            // 50 ms
            case "System dependent; either 50 ms as needed for active control, or as a continuous 50 ms periodic broadcast.":
                return 50;

            // < 50 ms
            case "Fixed rate of 10 to 50 ms or engine speed dependent":
            case "Every 50ms and on change of \"AEBS state\" or change of \"Collision warning level\" but no faster than every 10 ms":
            case "Every 50ms and on change of \"Blind Spot Detection state\" or change of \"Collision Warning Level\" but no faster than every 10 ms":
                return -50;

            case "Engine speed dependent": // Got a better guess?
            case "manufacturer defined, not faster than 100 ms":
            case "Manufacturer defined, not faster than 100 ms":
            case "100 ms\n"
                    + "\n"
                    + "Note: Systems developed to the standard published before January, 2015 transmit at a 1s rate.":
            case "100 ms\n"
                    + "\n"
                    + "Note: Systems developed to the standard published before May, 2016 might not be transmitted at a 100 ms rate, but be transmitted on request":
                return 100;

            case "Application dependent, but no faster than 10 ms and no slower than 100 ms.":
            case "Engine speed dependent when active, otherwise every 100 ms":
                return -100;

            case "When active: 20 ms; else 200 ms":
                return -200;

            case "Fixed rate of 10 to 250 ms or engine speed dependent":
                return -250;

            case "Devices developed to the standard published after June 2019 will transmit this message at a rate of 500 ms.  Additionally, these devices will also transmit this message every 20 ms for at least 3 s when a crash is detected.\n"
                    + "\n"
                    + "Devices developed to the standard published before June 2019 will only transmit this message in case of a crash event every 20 msec for the first 100 ms and then broadcast every 1 s for 10 s in case of a crash event.":
                return 500;

            case "1 s, See PGN description for information on message instance timing.":
            case "1 s \n"
                    + "\n"
                    + "See PGN description for information on message instance timing.":
            case "1 s\n"
                    + "\n"
                    + "Note: Systems developed to the standard published before June, 2014 might not be transmitted at a 1 s rate, but be transmitted on request.":
            case "1 s\n"
                    + "\n"
                    + "Note: Systems developed to the standard published before June, 2015 might not be transmitted at a 1 s rate, but be transmitted on request.":
            case "On start-up, and every 1 s until the dewpoint signal state = 1 (SPN 3240) has been received by the transmitter":
            case "On start-up, and every 1 s until the dewpoint signal state = 1 (SPN 3239) has been received by the transmitter":
            case "On start-up, and every 1 s until the dewpoint signal state = 1 (SPN 3238) has been received by the transmitter":
            case "On start-up, and every 1 s until the dewpoint signal state = 1 (SPN 3237) has been received by the transmitter":
            case "1 s\n"
                    + "\n"
                    + "Note: Systems developed to the standard published before SEP2015 transmit at a 5s rate.":
                return 1000;

            case "Engine speed dependent when active, otherwise every 1 s.":
            case "Once per engine combustion cycle when running, otherwise every 1 s":
            case "On detection of each attack event and, after the initial attack event detection, every 1 s for the remainder of the current ECU power cycle.":
            case "Manufacturer-specific fixed rate; every 1 second recommended.\n"
                    + "\n"
                    + "May be sent on change if necessary to convey an instantaneous peak twist value above some threshold has occurred.":
            case "Transmitted only if DC EVSE is connected.\n"
                    + "Every 1 s and on change of state but no faster than every 100 ms.":
                return -1000;

            case "Every 5 s and on change of torque/speed points of more than 10% since last transmission but no faster than every 500 ms":
                return 5000;

            case "Engine speed dependent when there is no combustion, once every 5 s otherwise.":
            case "Engine speed dependent when knock present, once every 5 s otherwise.":
            case "Transmitted every 5 s and on change of PGN 64791 but no faster than every 250 ms":
                return -5000;

            case "10 s and on change but no faster than 1 s\n"
                    + "\n"
                    + "Note: Systems developed to the standard published before December, 2016 may be transmitted on request.":
            case "Cycles through all available axle groups once every ten seconds, with at least a 20 ms gap and at most a 200 ms gap between the transmission of each of the available axle groups.":
                return -10000;

            // no clue what to do with these
            case "On event":
            case "Transmission of this message is interrupt driven.  This message is also transmitted upon power-up of the interfacing device sending this message.":
                return -30000;

            // oddball on request descriptions
            case "As required but no faster than once every 100 ms.":
            case "One or more message instances transmitted monthly, on request via PGN 59904 or PGN 51456, or at the transmitter's discretion. See Appendix D for further requirements. Global requests are not recommended due to the potential response volume. When a series of message instances are broadcast, transmitters are advised to space these instances at least 1 minute apart.":
            case "One or more message instances transmitted weekly, on request via PGN 59904 or PGN 51456, or at the transmitter's discretion. See Appendix D for further requirements. Global requests are not recommended due to the potential response volume. When a series of message instances are broadcast, transmitters are advised to space these instances at least 1 minute apart.":
            case "One or more message instances transmitted daily, on request via PGN 59904 or PGN 51456, or at the transmitter's discretion. See Appendix D for further requirements. Global requests are not recommended due to the potential response volume. When a series of message instances are broadcast, transmitters are advised to space these instances at least 1 minute apart.":
            case "This message is transmitted in response to an Anti-Theft Request message. This message is also sent when the component has an abnormal power interruption.  In this situation the Anti-Theft Status Report is sent without the Anti-Theft Request.":
            case "Transmitted only after requested.  After request, broadcast rate is engine speed dependent.  Update stopped after key switch cycle.":
            case "As needed\n"
                    + "\n"
                    + "In response to receiving the Configurable Receive SPNs Command (PGN 28160) message.":
            case "Not defined":
            case "As needed\n"
                    + "\n"
                    + "In response to receiving the Configurable Transmit PGNs Command (PGN 28928) message.":
            case "Transmitted only after requested.  After request, broadcast rate is 1 s.  Update stopped after key switch cycle.":
            case "On request.  Upon request, will be broadcast as many times as required to transmit all available axle groups.":
            case "As needed.  Broadcast whenever an axle group equipped with an on-board scale joined or left the on-board scale subset.":
            case "On request or sender may transmit every 5 s until acknowledged by reception of the engine configuration message PGN 65251 SPN 7828.":
                return 0;

            default:
                if (transmissionRate.startsWith("Every ")) {
                    try {
                        Matcher matcher = Pattern.compile("Every (\\d+ \\w+)")
                                                 .matcher(transmissionRate);
                        matcher.lookingAt();
                        transmissionRate = matcher.group(1);
                    } catch (IllegalStateException e) {
                        throw new ParseError("Unable to parse transmission rate:" + transmissionRate);
                    }
                }

                var m = Pattern.compile("(\\d+) ?s.*").matcher(transmissionRate);
                if (m.matches()) {
                    return parseInt(m.group(1)) * 1000;
                } else if ((m = Pattern.compile("(\\d+) ?ms.*").matcher(transmissionRate)).matches()) {
                    return parseInt(m.group(1));
                } else {
                    throw new ParseError("Unknown transmission rate unit: " + transmissionRate);
                }
        }
    }

    public PgnDefinition findPgnDefinition(int pgn) {
        loadLookUpTables();
        PgnDefinition pgnDefinition = pgnLut.get(pgn);
        if (pgnDefinition == null) {
            J1939_84.getLogger().log(Level.INFO, "Unable to find PgnDefinition for " + pgn);
            return new PgnDefinition(pgn, "Unknown", "UNK", false, false, 0, List.of());
        }
        return pgnDefinition;
    }

    private static String shortenLabel(String label) {
        label = label.replaceAll("Aftertreatment", "AFT");
        label = label.replaceAll("Diesel Particulate Filter", "DPF");
        label = label.replaceAll("Diesel Exhaust Fluid", "DEF");
        label = label.replaceAll("Selective Catalytic Reduction", "SCR");
        label = label.replaceAll("Exhaust Gas Recirculation", "EGR");
        return label;
    }

    public Slot findSLOT(int id) {
        return findSLOT(id, 0);
    }

    public SpnDefinition findSpnDefinition(int spn) {
        loadLookUpTables();
        SpnDefinition spnDefinition = spnLut.get(spn);
        if (spnDefinition == null) {
            J1939_84.getLogger().log(Level.INFO, "Unable to find SpnDefinition for " + spn);
            return new SpnDefinition(spn, "Unknown", 0, 0, -1);
        }
        return spnDefinition;
    }

    public Map<Integer, PgnDefinition> getPgnDefinitions() {
        loadLookUpTables();
        return Collections.unmodifiableMap(pgnLut);
    }

    public Set<Integer> getPgnForSpn(int spn) {
        loadLookUpTables();
        return spnToPgnMap.get(spn);
    }

    public Map<Integer, SpnDefinition> getSpnDefinitions() {
        loadLookUpTables();
        return Collections.unmodifiableMap(spnLut);
    }

}

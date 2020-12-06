package org.etools.j1939_84.bus.j1939;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.j1939.packets.Slot;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;
import org.etools.j1939_84.resources.Resources;

public class J1939DaRepository {
    static class ParseError extends Exception {
        public ParseError(String string) {
            super(string);
        }

    }

    private static Map<Integer, PgnDefinition> pgnLut;

    private static Map<Integer, SpnDefinition> spnLut;

    private static Map<Integer, Integer> spnToPgnMap = null;

    static private void loadLookUpTables() {
        if (pgnLut == null) {
            new HashMap<>();
            // parse the selected columns from J1939DA. The source data is
            // unaltered, so some procesing is required to convert byte.bit
            // specifications into ints.
            final InputStream is = Resources.class.getResourceAsStream("j1939da-extract.csv");
            final InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
            try (CSVReader reader = new CSVReaderBuilder(isReader).withSkipLines(2).build()) {
                // collect spns under the pgn
                Collection<Object[]> table = StreamSupport.stream(reader.spliterator(), false)
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
                                    startByte = Integer.parseInt(position.split("[^\\d]")[0]);
                                    startBit = 1;
                                } else if (position.matches("\\d+\\.\\d+((,|-| to ).*)?")) {
                                    String[] a = position.split("[^\\d]");
                                    startByte = Integer.parseInt(a[0]);
                                    startBit = Integer.parseInt(a[1]);
                                } else if ("a (starts at byte 10)".equals(position)) {
                                    startByte = 10;
                                    startBit = 1;
                                } else {
                                    throw new ParseError("Unable to parse position: " + position);
                                }

                                SpnDefinition spnDef = null;
                                String spnIdStr = line[5];
                                if (!spnIdStr.isBlank()) {
                                    spnDef = new SpnDefinition(Integer.parseInt(spnIdStr), line[6], startByte,
                                            startBit,
                                            line[7].isBlank() ? -1 : Integer.parseInt(line[7]));
                                }
                                String pgnIdStr = line[0];
                                PgnDefinition pgnDef = null;
                                // we don't care about the PGN that have no
                                // SPNs.
                                if (spnDef != null && !pgnIdStr.isBlank()) {
                                    int transmissionRate = parseTransmissionRate(line[3]);
                                    pgnDef = new PgnDefinition(Integer.parseInt(pgnIdStr), line[1], line[2],
                                            transmissionRate == 0, transmissionRate < 0, Math.abs(transmissionRate),
                                            Collections.singletonList(spnDef));
                                }
                                return new Object[] { pgnDef, spnDef };
                            } catch (ParseError e) {
                                System.err.format("%d %s \n\t%s%n", reader.getLinesRead(), e.getMessage(),
                                        Arrays.asList(line));
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                pgnLut = table.stream()
                        .flatMap(row -> row[0] == null ? Stream.empty() : Stream.of((PgnDefinition) row[0]))
                        .collect(Collectors.toMap(pgnDef -> pgnDef.getId(), pgnDef -> pgnDef,
                                                  (a, b) -> new PgnDefinition(a.getId(),
                                                                              a.getLabel(),
                                                                              a.getAcronym(),
                                                                              a.isOnRequest(),
                                                                              a.isVariableBroadcast(),
                                                                              a.getBroadcastPeriod(),
                                                                              Stream.concat(a.getSpnDefinitions().stream(), b
                                                                                      .getSpnDefinitions()
                                                                                      .stream())
                                                .sorted(Comparator.comparing(s -> s.getStartByte() * 8 + s.getStartBit()))
                                                .collect(Collectors.toList()))));
                spnLut = table.stream()
                        .map(row -> ((SpnDefinition) row[1]))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(s -> s.getSpnId(), s -> s, (a, b) -> a));

                spnToPgnMap = new HashMap<>();
                for (PgnDefinition pgnDefinition : pgnLut.values()) {
                    for (SpnDefinition spnDefinition : pgnDefinition.getSpnDefinitions()) {
                        spnToPgnMap.put(spnDefinition.getSpnId(), pgnDefinition.getId());
                    }
                }
            } catch (Exception e) {
                J1939_84.getLogger().log(Level.SEVERE, "Error loading J1939DA data.", e);
                throw new RuntimeException("Unable to load J1939DA", e);
            }
        }
    }

    static public void main(String... a) {
        loadLookUpTables();
        List<PgnDefinition> pgns = new ArrayList<>(pgnLut.values());
        Collections.sort(pgns, Comparator.comparing(d -> d.getId()));
        for (PgnDefinition d : pgns) {
            System.err.format("PGN: %6d: %6d %s%n", d.getId(), d.getBroadcastPeriod(), d.getLabel());
            for (SpnDefinition s : d.getSpnDefinitions()) {
                System.err.format("  SPN: %6d: %3d.%-3d %3d %6d %s%n", s.getSpnId(), s.getStartByte(), s.getStartBit(),
                                  Slot.findSlot(s.getSlotNumber()).getLength(), s.getSlotNumber(), s.getLabel());
            }
        }
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
        case "One or more message instances transmitted monthly, on request via PGN 59904 or PGN 51456, or at the transmitters discretion. See Appendix D for further requirements. Global requests are not recommended due to the potential response volume. When a series of message instances are broadcast, transmitters are advised to space these instances at least 1 minute apart.":
        case "One or more message instances transmitted weekly, on request via PGN 59904 or PGN 51456, or at the transmitters discretion. See Appendix D for further requirements. Global requests are not recommended due to the potential response volume. When a series of message instances are broadcast, transmitters are advised to space these instances at least 1 minute apart.":
        case "One or more message instances transmitted daily, on request via PGN 59904 or PGN 51456, or at the transmitters discretion. See Appendix D for further requirements. Global requests are not recommended due to the potential response volume. When a series of message instances are broadcast, transmitters are advised to space these instances at least 1 minute apart.":
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
                return Integer.parseInt(m.group(1)) * 1000;
            } else if ((m = Pattern.compile("(\\d+) ?ms.*").matcher(transmissionRate)).matches()) {
                return Integer.parseInt(m.group(1));
            } else {
                throw new ParseError("Unknown transmission rate unit: " + transmissionRate);
            }
        }
    }

    public PgnDefinition findPgnDefinition(int pgn) {
        loadLookUpTables();
        return pgnLut.get(pgn);
    }

    public SpnDefinition findSpnDefinition(int spn) {
        loadLookUpTables();
        return spnLut.get(spn);
    }

    public Map<Integer, PgnDefinition> getPgnDefinitions() {
        loadLookUpTables();
        return Collections.unmodifiableMap(pgnLut);
    }

    public Map<Integer, SpnDefinition> getSpnDefinitions() {
        loadLookUpTables();
        return Collections.unmodifiableMap(spnLut);
    }

    public Integer getPgnForSpn(int spn) {
        loadLookUpTables();
        return spnToPgnMap.get(spn);
    }

}

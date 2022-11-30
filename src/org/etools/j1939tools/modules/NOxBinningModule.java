package org.etools.j1939tools.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.utils.StringUtils;

public class NOxBinningModule {

    private final DateTimeModule dateTimeModule;

    public NOxBinningModule(DateTimeModule dateTimeModule) {
        this.dateTimeModule = dateTimeModule;
    }

    private static final String[] BIN_LABELS = new String[] { "",
            "Bin  1 (Total)           ",
            "Bin  2 (Idle)            ",
            "Bin  3 (<25%, <16kph)    ",
            "Bin  4 (<25%, 16-40kph)  ",
            "Bin  5 (<25%, 40-64kph)  ",
            "Bin  6 (<25%, >64kph)    ",
            "Bin  7 (25-50%, <16kph)  ",
            "Bin  8 (25-50%, 16-40kph)",
            "Bin  9 (25-50%, 40-64kph)",
            "Bin 10 (25-50%, >64kph)  ",
            "Bin 11 (>50%, <16kph)    ",
            "Bin 12 (>50%, 16-40kph)  ",
            "Bin 13 (>50%, 40-64kph)  ",
            "Bin 14 (>50%, >64kph)    ",
            "Bin 15 (NTE)             ",
            "Bin 16 (Regen)           ",
            "Bin 17 (MIL On)          "
    };

    private static final String[] HEADER_TOP = new String[] {
            "                         ", "Tail Pipe", "Eng. Out.", "", "", "Engine", "Vehicle"
    };

    private static final String[] HEADER_BOTTOM = new String[] {
            "                         ", "NOx Mass, g", "NOx Mass, g", "EOE, kWh", "Fuel, l", "Hours, min", "Dist, km"
    };

    // 64262 NOx Tracking Valid NOx Lifetime Fuel Consumption Bins
    // 64263 NOx Tracking Valid NOx Lifetime Engine Run Time Bins
    // 64264 NOx Tracking Valid NOx Lifetime Vehicle Distance Bins
    // 64265 NOx Tracking Valid NOx Lifetime Engine Output Energy Bins
    // 64266 NOx Tracking Valid NOx Lifetime Engine Out NOx Mass Bins
    // 64267 NOx Tracking Valid NOx Lifetime System Out NOx Mass Bins
    public static final int[] NOx_LIFETIME_PGs = { 64267, 64266, 64265, 64262, 64263, 64264 };

    // 64258 NOx Tracking Engine Activity Lifetime Fuel Consumption Bins
    // 64259 NOx Tracking Engine Activity Lifetime Engine Run Time Bins
    // 64260 NOx Tracking Engine Activity Lifetime Vehicle Distance Bins
    // 64261 NOx Tracking Engine Activity Lifetime Engine Output Energy Bins NTEEEA
    public static final int[] NOx_LIFETIME_ACTIVITY_PGs = { 0, 0, 64261, 64258, 64259, 64260 };
    // PG Acronym NTFCA
    // NTEHA NTVMA NTEEA NTENA
    // NTSNA NTFCS NTEHS NTVMS
    // NTEES NTENS NTSNS
    // 64274 NOx Tracking Active 100 Hour Fuel Consumption Bins
    // 64275 NOx Tracking Active 100 Hour Engine Run Time Bins
    // 64276 NOx Tracking Active 100 Hour Vehicle Distance Bins
    // 64277 NOx Tracking Active 100 Hour Engine Output Energy Bins
    // 64278 NOx Tracking Active 100 Hour Engine Out NOx Mass Bins
    // 64279 NOx Tracking Active 100 Hour System Out NOx Mass Bins
    public static final int[] NOx_TRACKING_ACTIVE_100_HOURS_PGs = { 64279, 64278, 64277, 64274, 64275, 64276 };
    // 64268 NOx Tracking Stored 100 Hour
    // 64269 NOx Tracking Stored 100 Hour
    // 64270 NOx Tracking Stored 100 Hour
    // 64271 NOx Tracking Stored 100 Hour
    // 64272 NOx Tracking Stored 100 Hour
    // 64273 NOx Tracking Stored 100 Hour
    public static final int[] NOx_TRACKING_STORED_100_HOURS_PGs = { 64273, 64272, 64271, 64268, 64269, 64270 };

    public static final int[] NOx_ALL_PGNS = Stream.of(IntStream.of(NOx_TRACKING_ACTIVE_100_HOURS_PGs),
                                                       IntStream.of(NOx_TRACKING_STORED_100_HOURS_PGs),
                                                       IntStream.of(NOx_LIFETIME_PGs),
                                                       IntStream.of(NOx_LIFETIME_ACTIVITY_PGs))
                                                   .flatMapToInt(x -> x)
                                                   .filter(x -> x != 0)
                                                   .toArray();

    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0");
    private final static int columnWidth = 13;
    private final static int descriptionWidth = 25;

    public String format(List<GenericPacket> packets) {
        String moduleName = Lookup.getAddressName(packets.get(0).getSourceAddress());
        StringBuilder result = new StringBuilder("");
        boolean packetsContainActive = false;
        boolean packetsContainStored = false;
        boolean packetsContainLifetime = false;
        boolean packetsContainEngineLifetime = false;
        for (GenericPacket pkt : packets) {
            if (contains(NOx_TRACKING_ACTIVE_100_HOURS_PGs, pkt.getPgnDefinition().getId())) {
                packetsContainActive = true;
            }
            if (contains(NOx_TRACKING_STORED_100_HOURS_PGs, pkt.getPgnDefinition().getId())) {
                packetsContainStored = true;
            }
            if (contains(NOx_LIFETIME_PGs, pkt.getPgnDefinition().getId())) {
                packetsContainLifetime = true;
            }
            if (contains(NOx_LIFETIME_ACTIVITY_PGs, pkt.getPgnDefinition().getId())) {
                packetsContainEngineLifetime = true;
            }
        }
        if (packetsContainActive) {
            result.append(timeStamp())
                  .append(" NOx Binning Active 100-Hour Array from ")
                  .append(moduleName)
                  .append(NL)
                  .append(printTable(packets,
                                     NOx_TRACKING_ACTIVE_100_HOURS_PGs,
                                     6));
        }
        if (packetsContainStored) {
            result.append(timeStamp())
                  .append(" NOx Binning Stored 100-Hour Array from ")
                  .append(moduleName)
                  .append(NL)
                  .append(printTable(packets,
                                     NOx_TRACKING_STORED_100_HOURS_PGs,
                                     6));
        }
        if (packetsContainLifetime) {
            result.append(timeStamp() + " NOx Binning Lifetime Array from " + moduleName)
                  .append(NL)
                  .append(printTable(packets,
                                     NOx_LIFETIME_PGs,
                                     6));
        }
        if (packetsContainEngineLifetime) {
            result.append(timeStamp() + " NOx Binning Engine Activity Lifetime Array from " + moduleName)
                  .append(NL)
                  .append(printTable(packets,
                                     NOx_LIFETIME_ACTIVITY_PGs,
                                     4));
        }

        return result.toString();
    }

    private boolean contains(int[] pgns, int id) {
        return IntStream.of(pgns).anyMatch(x -> x == id);
    }

    private String timeStamp() {
        return dateTimeModule.getTime();
    }

    private String printTable(List<GenericPacket> packets, int[] pgns, int columnCount) {
        StringBuilder result = new StringBuilder();
        result.append(printSpacer(columnCount));
        result.append(printHeader(columnCount));
        result.append(printSpacer(columnCount));

        for (int bin = 1; bin <= 17; bin++) {
            result.append("| ").append(BIN_LABELS[bin]).append(" |");
            for (int i = 0; i < pgns.length; i++) {
                if (columnCount == 4 && (i == 0 || i == 1)) {
                    continue;
                }

                String value = printValue(packets, bin, pgns[i]);
                result.append(padLeft(value)).append(" |");
            }
            result.append(NL);
        }

        result.append(printSpacer(columnCount));

        result.append(NL);

        return result.toString();
    }

    private String printValue(List<GenericPacket> packets, int bin, int pgn) {
        var spns = packets.stream()
                          .filter(p -> p.getPacket().getPgn() == pgn)
                          .flatMap(p -> p.getSpns().stream())
                          .sorted(Comparator.comparingInt(Spn::getId))
                          .collect(Collectors.toList());

        if (spns.isEmpty()) {
            return "";
        } else {
            Spn spn = spns.get(bin - 1);
            return printSpn(spn);
        }
    }

    private String printHeader(int columnCount) {
        String header = "";
        header += printHeader(columnCount, HEADER_TOP);
        header += printHeader(columnCount, HEADER_BOTTOM);

        return header;
    }

    private String printHeader(int columnCount, String[] labels) {
        StringBuilder header = new StringBuilder();
        header.append("| ");
        for (int i = 0; i < labels.length; i++) {
            if (columnCount == 4 && (i == 1 || i == 2)) {
                continue;
            }
            header.append(center(labels[i])).append(" |");
        }
        header.append(NL);
        return header.toString();
    }

    private String printSpacer(int columns) {
        StringBuilder spacer = new StringBuilder();

        spacer.append("|-").append(padRight("").replace(' ', '-')).append("-+");
        for (int i = 1; i <= columns; i++) {
            spacer.append(padLeft("").replace(' ', '-'));
            if (i == columns) {
                spacer.append("-|");
            } else {
                spacer.append("-+");
            }
        }
        spacer.append(NL);

        return spacer.toString();
    }

    private String printSpn(Spn spn) {
        if (spn.isNotAvailable() || spn.isError()) {
            return spn.getStringValue();
        }

        double value = spn.getValue();

        String unit = spn.getSlot().getUnit();
        if ("s".equals(unit)) {
            value = value / 60; // Convert seconds to minutes
        } else if ("m".equals(unit)) {
            value = value / 1000; // Convert meters to kilometers
        }

        return decimalFormat.format(value);
    }

    private String padLeft(String input) {
        return StringUtils.padLeft(input, columnWidth);
    }

    @SuppressWarnings("SameParameterValue")
    private String padRight(String input) {
        return StringUtils.padRight(input, descriptionWidth);
    }

    private static String center(String string) {
        return StringUtils.center(string, columnWidth);
    }

}

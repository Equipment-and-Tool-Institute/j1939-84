package org.etools.j1939tools.modules;

import static org.etools.j1939tools.J1939tools.NL;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final int[] ACTIVE_100_HOUR_PGNS = new int[] { 0xFB17, 0xFB16, 0xFB15, 0xFB12, 0xFB13, 0xFB14 };

    private static final int[] STORED_100_HOUR_PGNS = new int[] { 0xFB11, 0xFB10, 0xFB0F, 0xFB0C, 0xFB0D, 0xFB0E };

    private static final int[] LIFETIME_PGNS = new int[] { 0xFB0B, 0xFB0A, 0xFB09, 0xFB06, 0xFB07, 0xFB08 };

    private static final int[] ENGINE_LIFETIME_PGNS = new int[] { 0, 0, 0xFB05, 0xFB02, 0xFB03, 0xFB04 };

    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0");
    private final static int columnWidth = 13;
    private final static int descriptionWidth = 25;

    public String format(List<GenericPacket> packets) {
        String moduleName = Lookup.getAddressName(packets.get(0).getSourceAddress());
        String result = "";

        result += timeStamp() + " NOx Binning Active 100-Hour Array from " + moduleName + NL;
        result += printTable(packets, ACTIVE_100_HOUR_PGNS, 6);

        result += timeStamp() + " NOx Binning Stored 100-Hour Array from " + moduleName + NL;
        result += printTable(packets, STORED_100_HOUR_PGNS, 6);

        result += timeStamp() + " NOx Binning Lifetime Array from " + moduleName + NL;
        result += printTable(packets, LIFETIME_PGNS, 6);

        result += timeStamp() + " NOx Binning Engine Activity Lifetime Array from " + moduleName + NL;
        result += printTable(packets, ENGINE_LIFETIME_PGNS, 4);

        return result;
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
            value = value / 60; //Convert seconds to minutes
        } else if ("m".equals(unit)) {
            value = value / 1000; //Convert meters to kilometers
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
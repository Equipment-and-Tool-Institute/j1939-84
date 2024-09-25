/*
 * Copyright (c) 2024. Equipment & Tool Institute
 */

package org.etools.j1939tools.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.utils.StringUtils;

public class CSERSModule {

    //64019 Cold Start Emissions Reduction Strategy Current Operating Cycle Data CSERSC
    public static final int CSERS_CURRENT_OP_CYCLE_PG = 64019;
    //64020 Cold Start Emissions Reduction Strategy Average Data CSERSA
    public static final int CSERS_AVERAGE_PG = 64020;

    private final DateTimeModule dateTimeModule;

    public CSERSModule(DateTimeModule dateTimeModule) {
        this.dateTimeModule = dateTimeModule;
    }

    public String format(List<GenericPacket> packets){
        String result = "";
        result += dateTimeModule.getTime() + " CSERS Average and Current Operating Cycle data from " + packets.get(0).getModuleName() + NL;
        result += printCSERSTable(packets);
        return result.toString();
    }

    private String printCSERSTable(List<GenericPacket> packets) {
        int headerRows = 42;
        int[] columnWidths = { 71, 12, 12 };
        boolean[] leftPad = { true, false, false, false };
        String[][] table = {
                { "|----------------------------------------------------------------------------+", "-------------+", "-------------|" },
                { "|                                                                            |", "   Current   |", "   Average   |" },
                { "|                                                                            |", "  Operating  |", "    Data     |"},
                { "|                                                                            |", "    Cycle    |", "             |"},
                { "|                                                                            |", "    Data     |", "             |"},
                { "|----------------------------------------------------------------------------+", "-------------+", "-------------|"},
                { "| Heat Energy Until FTP Cold Start Tracking Time,                         kJ |", "SPN_22227", "SPN_22237" },
                { "| Heat Energy Until FTP Engine Output Energy,                             kJ |", "SPN_22228", "SPN_22238" },
                { "| Heat Energy Until Catalyst Cold Start Tracking Temperature Threshold,   kJ |", "SPN_22229", "SPN_22239" },
                { "| Output Energy Until FTP Cold Start Tracking Time,                       kJ |", "SPN_22230", "SPN_22240" },
                { "| Output Energy Until Catalyst Cold Start Tracking Temperature Threshold, kJ |", "SPN_22231", "SPN_22241" },
                { "| EGR Mass Until FTP Cold Start Tracking Time,                            g  |", "SPN_22232", "SPN_22242" },
                { "| EGR Mass Until FTP Engine Output Energy,                                g  |", "SPN_22233", "SPN_22243" },
                { "| EGR Mass Until Cold Start Tracking Catalyst Temperature Threshold,      g  |", "SPN_22234", "SPN_22244" },
                { "| Time Until FTP Engine Output Energy,                                    s  |", "SPN_22235", "SPN_22245" },
                { "| Time Until Catalyst Cold Start Tracking Temperature Threshold,          s  |", "SPN_22236", "SPN_22246" },
                { "|----------------------------------------------------------------------------+", "-------------+", "-------------+" },
        };

        return printTable(packets, headerRows, columnWidths, leftPad, table);
    }

    private String printTable(List<GenericPacket> packets,
                              int headerRows,
                              int[] columnWidths,
                              boolean[] leftPad,
                              String[][] table) {
        StringBuilder sb = new StringBuilder();
        for (int rowIndex = 0; rowIndex < table.length; rowIndex++) {
            String[] row = table[rowIndex];
            for (int colIndex = 0; colIndex < row.length; colIndex++) {
                String cell = row[colIndex];
                int columnWidth = columnWidths[colIndex];
                if (cell.contains("SPN_")) {
                    int spnId = parseSpnId(cell);
                    String value = getSpnValue(packets, spnId);
                    sb.append(StringUtils.padLeft(value, columnWidth)).append(" |");
                } else {
                    if (rowIndex < headerRows) {
                        sb.append(StringUtils.center(cell, columnWidth));
                    } else {
                        if (leftPad[colIndex]) {
                            sb.append(StringUtils.padRight(cell, columnWidth));
                        } else {
                            sb.append(StringUtils.padLeft(cell, columnWidth));
                        }
                    }
                }
            }
            sb.append(NL);
        }

        String s = sb.toString();
        return s;
    }

    private int parseSpnId(String cell) {
        return Integer.parseInt(cell.replace("SPN_", "").replace(" ", ""));
    }

    private String getSpnValue(List<GenericPacket> packets, int spnId) {
        return packets.stream()
                .map(p -> p.getSpn(spnId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::printSpn)
                .map(v -> {
                    if ("Not Available".equals(v)) {
                        return "N/A";
                    } else {
                        return v;
                    }
                })
                .findFirst()
                .orElse("");
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

        return new DecimalFormat("#,##0").format(value);
    }
}

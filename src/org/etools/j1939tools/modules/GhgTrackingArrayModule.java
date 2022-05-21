package org.etools.j1939tools.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.utils.StringUtils;

public class GhgTrackingArrayModule {

    private final DateTimeModule dateTimeModule;

    public GhgTrackingArrayModule(DateTimeModule dateTimeModule) {
        this.dateTimeModule = dateTimeModule;
    }

    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0");

    public String format(List<GenericPacket> packets) {

        String moduleName = packets.get(0).getModuleName();

        String result = "";
        result += timeStamp() + " GHG Tracking Arrays from " + moduleName + NL;
        result += printTrackingArray(packets);

        return result;
    }

    public String formatXevTable(List<GenericPacket> packets) {

        String moduleName = packets.get(0).getModuleName();

        String result = "";
        result += timeStamp() + " GHG Tracking Arrays from " + moduleName + NL;
        result += printXevTrackingArray(packets);

        return result;
    }

    private String printXevTrackingArray(List<GenericPacket> packets) {
        int headerRows = 2;
        int[] columnWidths = { 25, 12, 12, 12 };
        boolean[] leftPad = { true, false, false, false };
        String[][] table = {
                { "|--------------------------------+", "-------------+", "-------------+", "-------------|" },
                { "|                                |", "    Active   |", "    Stored   |", "             |" },
                { "|                                |", "   100 Hour  |", "   100 Hour  |", "   Lifetime  |" },
                { "|--------------------------------+", "-------------+", "-------------+", "-------------|" },
                { "| Chg Depleting engine off,  km  |", "SPN_12767", "SPN_12775", "SPN_12783" },
                { "| Chg Depleting engine off,  km  |", "SPN_12768", "SPN_12776", "SPN_12784" },
                { "| Drv-Sel Inc Operation,     km  |", "SPN_12769", "SPN_12777", "SPN_12785" },
                { "| Fuel Consume: Chg Dep Op,  l   |", "SPN_12770", "SPN_12778", "SPN_12786" },
                { "| Fuel Consume: Drv-Sel In,  l   |", "SPN_12771", "SPN_12779", "SPN_12787" },
                { "| Grid: Chg Dep Op eng-off,  kWh |", "SPN_12772", "SPN_12780", "SPN_12788" },
                { "| Grid: Chg Dep Op eng-on,   kWh |", "SPN_12773", "SPN_12781", "SPN_12789" },
                { "| Grid: Energy into battery, kWh |", "SPN_12774", "SPN_12782", "SPN_12790" },
                { "| Prod: Prop system active,  s   |", "SPN_12791", "SPN_12794", "SPN_12797" },
                { "| Prod: Prop idle active,    s   |", "SPN_12792", "SPN_12795", "SPN_12798" },
                { "| Prod: Prop urban active,   s   |", "SPN_12793", "SPN_12796", "SPN_12799" },
                { "|--------------------------------+", "-------------+", "-------------+", "-------------|" },
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

    private String printTrackingArray(List<GenericPacket> packets) {

        int headerRows = 2;
        int[] columnWidths = {25, 12, 12, 12};
        boolean[] leftPad = {true, false, false, false};
        String[][] table = {
                {"|-------------------------+", "-------------+", "-------------+", "-------------|"},
                {"|                         |", "    Active   |", "    Stored   |", "             |"},
                {"|                         |", "   100 Hour  |", "   100 Hour  |", "   Lifetime  |"},
                {"|-------------------------+", "-------------+", "-------------+", "-------------|"},
                {"| Engine Run Time, s      |", "SPN_12700", "SPN_12715", "SPN_12730"},
                {"| Vehicle Dist., km       |", "SPN_12701", "SPN_12716", "SPN_12731"},
                {"| Vehicle Fuel, l         |", "SPN_12702", "SPN_12717", "SPN_12732"},
                {"| Engine Fuel, l          |", "SPN_12703", "SPN_12718", "SPN_12733"},
                {"| Eng.Out.Energy, kW-hr   |", "SPN_12704", "SPN_12719", "SPN_12734"},
                {"| PKE Numerator           |", "SPN_12705", "SPN_12720", "SPN_12735"},
                {"| Urban Speed Run Time, s |", "SPN_12706", "SPN_12721", "SPN_12736"},
                {"| Idle Run Time, s        |", "SPN_12707", "SPN_12722", "SPN_12737"},
                {"| Engine Idle Fuel, l     |", "SPN_12708", "SPN_12723", "SPN_12738"},
                {"| PTO Run Time, s         |", "SPN_12709", "SPN_12724", "SPN_12739"},
                {"| PTO Fuel Consumption, l |", "SPN_12710", "SPN_12725", "SPN_12740"},
                {"| AES Shutdown Count      |", "SPN_12711", "SPN_12726", "SPN_12741"},
                {"| Stop-Start Run Time, s  |", "SPN_12712", "SPN_12727", "SPN_12742"},
                {"|-------------------------+", "-------------+", "-------------+", "-------------|"},
        };

        return printTable(packets, headerRows, columnWidths, leftPad, table);
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

    private int parseSpnId(String cell) {
        return Integer.parseInt(cell.replace("SPN_", "").replace(" ", ""));
    }

    private String timeStamp() {
        return dateTimeModule.getTime();
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

}

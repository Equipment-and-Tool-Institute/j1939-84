package org.etools.j1939tools.modules;

import static org.etools.j1939tools.J1939tools.NL;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.etools.j1939tools.j1939.model.ActiveTechnology;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.GhgActiveTechnologyPacket;
import org.etools.j1939tools.utils.StringUtils;

public class GhgActiveTechnologyArrayModule {

    private final DateTimeModule dateTimeModule;

    public GhgActiveTechnologyArrayModule(DateTimeModule dateTimeModule) {
        this.dateTimeModule = dateTimeModule;
    }

    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0");
    private final static int columnWidth = 12;
    private final static int descriptionWidth = 35;

    public String format(List<GenericPacket> packets) {
        String moduleName = packets.get(0).getModuleName();

        String result = "";

        result += timeStamp() + " GHG Active Technology Arrays from " + moduleName + NL;
        result += printTechnologyArray(packets);

        return result;
    }

    private String printTechnologyArray(List<GenericPacket> packets) {
        var packetPgs = packets.stream().map(GenericPacket::getSpns).collect(Collectors.toList());
        if (packetPgs.contains(64256) || packetPgs.contains(64256) || packetPgs.contains(64256)) {
            return printTechnologyArray(packets, 64256, 64255, 64257);
        }
        if (packetPgs.contains(64253) || packetPgs.contains(64254)) {
            return printTechnologyArray(packets, 64254, 64253, 0);
        }
        return "";
    }

    private String printTechnologyArray(List<GenericPacket> packets,
                                        int activeArrayPg,
                                        int storedArrayPg,
                                        int lifetimeArrayPg) {
        String spacer1 = "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|";
        String header1 = "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |";
        String header2 = "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |";
        String header3 = "| Description                         |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |";

        StringBuilder sb = new StringBuilder();
        sb.append(spacer1).append(NL);
        sb.append(header1).append(NL);
        sb.append(header2).append(NL);
        sb.append(header3).append(NL);
        sb.append(spacer1).append(NL);

        for (int refIndex = 0; refIndex <= 250; refIndex++) {
            var active = getActiveTechnology(activeArrayPg, refIndex, packets);
            var stored = getActiveTechnology(storedArrayPg, refIndex, packets);
            var lifetime = getActiveTechnology(lifetimeArrayPg, refIndex, packets);

            String line = writeLine(active, stored, lifetime);
            if (line != null) {
                sb.append(line);
            }
        }
        sb.append(spacer1).append(NL);
        return sb.toString();
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

    private String writeLine(ActiveTechnology active, ActiveTechnology stored, ActiveTechnology lifetime) {
        String label = null;
        if (active != null) {
            label = active.getLabel();
        } else if (stored != null) {
            label = stored.getLabel();
        } else if (lifetime != null) {
            label = lifetime.getLabel();
        }

        if (label == null) {
            return null;
        }

        return "| " + StringUtils.padRight(label, descriptionWidth) + " |" +
               writeTechnology(active) +
               writeTechnology(stored) +
               writeTechnology(lifetime) +
               NL;
    }

    private String writeTechnology(ActiveTechnology technology) {
        if (technology != null) {
            return format(technology.getTimeSpn()) + " |" + format(technology.getDistanceSpn()) + " |";
        } else {
            return format("N/A") + " |" + format("N/A") + " |";
        }
    }

    private String format(String string) {
        return StringUtils.padLeft(string, columnWidth);
    }

    private String format(Spn spn) {
        double value = Double.parseDouble(spn.getStringValueNoUnit());

        String unit = spn.getSlot().getUnit();
        if ("s".equals(unit)) {
            value = value / 60; //Convert seconds to minutes
        } else if ("m".equals(unit)) {
            value = value / 1000; //Convert meters to kilometers
        }

        return format(decimalFormat.format(value));
    }

    private ActiveTechnology getActiveTechnology(int pgn, int index, List<GenericPacket> packets) {
        return packets.stream()
                      .filter(p -> p.getPacket().getPgn() == pgn)
                      .map(p -> new GhgActiveTechnologyPacket(p.getPacket()))
                      .flatMap(p -> p.getActiveTechnologies().stream())
                      .filter(t -> t.getIndex() == index)
                      .findFirst().orElse(null);
    }

    private String timeStamp() {
        return dateTimeModule.getTime();
    }

}

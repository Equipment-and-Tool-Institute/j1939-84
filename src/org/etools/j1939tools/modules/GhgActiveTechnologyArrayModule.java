package org.etools.j1939tools.modules;

import static org.etools.j1939_84.J1939_84.NL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.etools.j1939tools.j1939.model.ActiveTechnology;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.GhgActiveTechnologyPacket;
import org.etools.j1939tools.j1939.packets.GhgLifetimeActiveTechnologyPacket;
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
        var pgns = new ArrayList<>();
        for (GenericPacket packet : packets) {
            pgns.add(packet.getPgnDefinition().getId());
        }
        return printTechnologyArray(packets, 64256, 64255, 64257);
    }

    private String printTechnologyArray(List<GenericPacket> packets,
                                        int activeArrayPg,
                                        int storedArrayPg,
                                        int lifetimeArrayPg) {
        String spacer1 = "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|";
        String header1 = "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |";
        String header2 = "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |";
        String header3 = "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |";

        StringBuilder sb = new StringBuilder();
        sb.append(spacer1).append(NL);
        sb.append(header1).append(NL);
        sb.append(header2).append(NL);
        sb.append(header3).append(NL);
        sb.append(spacer1).append(NL);

        for (int refIndex = 0; refIndex <= 250; refIndex++) {
            var active = getActiveTechnology(activeArrayPg, refIndex, packets);
            var stored = getActiveTechnology(storedArrayPg, refIndex, packets);
            var lifetime = lifetimeArrayPg == 0 ? null : getActiveTechnology(lifetimeArrayPg, refIndex, packets);

            String line = writeLine(active, stored, lifetime);
            if (line != null) {
                sb.append(line);
            }
        }
        sb.append(spacer1).append(NL);
        return sb.toString();
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
            String message = "";
            if (!technology.getTimeSpn().hasValue()) {
                message += format("N/A") + " |";
            } else {
                message += format(technology.getTimeSpn()) + " |";
            }
            if (!technology.getDistanceSpn().hasValue()) {
                message += format("N/A") + " |";
            } else {
                message += format(technology.getDistanceSpn()) + " |";
            }
            return message;
        } else {
            return format("N/A") + " |" + format("N/A") + " |";
        }
    }

    private String format(String string) {
        return StringUtils.padLeft(string, columnWidth);
    }

    private String format(Spn spn) {
        Double value = Double.parseDouble(spn.getStringValueNoUnit());
        String unit = spn.getSlot().getUnit();
        if ("s".equals(unit)) {
            value = value / 60; // Convert seconds to minutes
        } else if ("m".equals(unit)) {
            value = value / 1000; // Convert meters to kilometers
        }

        return format(decimalFormat.format(value));
    }

    private ActiveTechnology getActiveTechnology(int pgn, int index, List<GenericPacket> packets) {
        return packets.stream()
                      .filter(p -> p.getPacket().getPgn() == pgn)
                      .map(p -> (GhgActiveTechnologyPacket) p)
                      .flatMap(p -> p.getActiveTechnologies().stream())
                      .filter(t -> t.getIndex() == index)
                      .findFirst()
                      .orElse(null);
    }

    private String timeStamp() {
        return dateTimeModule.getTime();
    }

}

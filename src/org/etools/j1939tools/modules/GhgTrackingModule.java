package org.etools.j1939tools.modules;

import static org.etools.j1939tools.J1939tools.NL;

import java.util.List;

import org.etools.j1939tools.j1939.packets.GenericPacket;

public class GhgTrackingModule {

    private final GhgTrackingArrayModule ghgTrackingArrayModule;
    private final GhgActiveTechnologyArrayModule ghgActiveTechnologyArrayModule;

    public GhgTrackingModule(DateTimeModule dateTimeModule) {
        this(new GhgTrackingArrayModule(dateTimeModule),
             new GhgActiveTechnologyArrayModule(dateTimeModule));
    }

    public GhgTrackingModule(GhgTrackingArrayModule ghgTrackingArrayModule,
                             GhgActiveTechnologyArrayModule ghgActiveTechnologyArrayModule) {
        this.ghgTrackingArrayModule = ghgTrackingArrayModule;
        this.ghgActiveTechnologyArrayModule = ghgActiveTechnologyArrayModule;
    }

    public String formatXevTable(List<GenericPacket> packets) {
        String result = "";
        result += ghgTrackingArrayModule.formatXevTable(packets);
        return result;
    }

    public String format(List<GenericPacket> packets) {
        String result = "";
        result += ghgTrackingArrayModule.format(packets);
        result += NL;
        result += ghgActiveTechnologyArrayModule.format(packets);
        return result;
    }

}

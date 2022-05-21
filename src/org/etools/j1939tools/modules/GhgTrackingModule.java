package org.etools.j1939tools.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.List;

import org.etools.j1939tools.j1939.packets.GenericPacket;

public class GhgTrackingModule {

    // Values below are defined by the requirements document in the following
    // places: 6.1.26, 6.2.17 and 6.12.12

    // 64252 GHG Tracking Lifetime Array Data - PG Acronym GHGTL
    public static final int GHG_TRACKING_LIFETIME_PG = 64252;
    // 64254 GHG Tracking Lifetime Array Data - PG Acronym GHGTA
    public static final int GHG_ACTIVE_100_HR = 64254;
    // 64253 GHG Tracking Lifetime Array Data - PG Acronym GHGTS
    public static final int GHG_STORED_100_HR = 64253;

    // 64241 PSA Times Lifetime Hours - PG Acronym PSATL
    public static final int GHG_TRACKING_LIFETIME_HYBRID_PG = 64241;
    // 64243 PSA Times Active 100 Hours - PG Acronym PSATA
    public static final int GHG_ACTIVE_HYBRID_100_HR = 64243;
    // 64242 PSA Times Stored 100 Hours - PG Acronym PSATS
    public static final int GHG_STORED_HYBRID_100_HR = 64242;

    // 64244 Hybrid Charge Depleting or Increasing Operation Lifetime Hours - PG Acronym HCDIOL
    public static final int GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG = 64244;
    // 64246 Hybrid Charge Depleting or Increasing Operation Stored 100 Hours - PG Acronym HCDIOA
    public static final int GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR = 64246;
    // 64245 Hybrid Charge Depleting or Increasing Operation Active 100 Hours - PG Acronym HCDIOS
    public static final int GHG_STORED_HYBRID_CHG_DEPLETING_100_HR = 64245;

    // 64257 Green House Gas Lifetime Active Technology Tracking - PG Acronym GHGTTL
    public static final int GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG = 64257;
    // 64256 Green House Gas Active 100 Hour Active Technology Tracking - PG Acronym GHGTTS
    public static final int GHG_ACTIVE_GREEN_HOUSE_100_HR = 64256;
    // 64255 Green House Gas Stored 100 Hour Active Technology Tracking - PG Acronym GHGTTA
    public static final int GHG_STORED_GREEN_HOUSE_100_HR = 64255;

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
        return ghgTrackingArrayModule.formatXevTable(packets);
    }

    public String format(List<GenericPacket> packets) {
        String result = "";
        result += ghgTrackingArrayModule.format(packets);
        result += NL;
        result += ghgActiveTechnologyArrayModule.format(packets);
        return result;
    }

    public String formatTrackingTable(List<GenericPacket> packets) {
        String result = "";
        result += ghgTrackingArrayModule.format(packets);
        return result;
    }

    public String formatTechTable(List<GenericPacket> packets) {
        String result = "";
        result += ghgActiveTechnologyArrayModule.format(packets);
        return result;
    }
}

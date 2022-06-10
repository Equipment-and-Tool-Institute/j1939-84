/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939tools.j1939.packets.ParsedPacket.NOT_AVAILABLE;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_GREEN_HOUSE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_HYBRID_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_GREEN_HOUSE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_HYBRID_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_HYBRID_CHG_DEPLETING_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_PG;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_ACTIVITY_SPs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_SPs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_ACTIVE_100_HOURS_SPs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_STORED_100_HOURS_SPs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.GhgTrackingModule;
import org.etools.j1939tools.modules.NOxBinningModule;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * 6.11.13 NOx Binning and GHG tracking for MY2022+ Engines and Vehicles
 */
public class Part11Step13Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final GhgTrackingModule ghgTrackingModule;
    private final NOxBinningModule nOxBinningModule;

    Part11Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new GhgTrackingModule(DateTimeModule.getInstance()),
             new NOxBinningModule(DateTimeModule.getInstance()));
    }

    Part11Step13Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           GhgTrackingModule ghgTrackingModule,
                           NOxBinningModule nOxBinningModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.ghgTrackingModule = ghgTrackingModule;
        this.nOxBinningModule = nOxBinningModule;
    }

    @Override
    protected void run() throws Throwable {
        // 6.11.13.1 Actions1 for MY2022+ Diesel Engines
        if (getEngineModelYear() >= 2022 && getFuelType().isCompressionIgnition()) {
            getDataRepository()
                               .getObdModules()
                               .forEach(module -> {
                                   if (module.supportsSpn(12675)) {
                                       // 6.11.13.1 - 6.11.13.4
                                       testSp12675(module);
                                   }
                                   if (module.supportsSpn(12730)) {
                                       // 6.11.13.5 - 6.11.13.8
                                       testSp12730(module);
                                   }
                                   if (module.supportsSpn(12691)) {
                                       // 6.11.13.9 - 6.11.13.12
                                       testSp12691(module);
                                   }
                                   if (module.supportsSpn(12797)) {
                                       // 6.11.13.13 - 6.11.13.16
                                       testSp12797(module);
                                   }
                                   if (module.supportsSpn(12783)) {
                                       // 6.11.13.13 - 6.11.13.20
                                       testSp12783(module);
                                   }
                               });

        }
    }

    private void testSp12783(OBDModuleInformation module) {
        // 6.11.13.17 Actions12 for MY2022+ Plug-in HEV DRIVES
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for
        // PG 64244 Hybrid Charge Depleting or Increasing Operation Lifetime Hours
        List<GenericPacket> ghgChgDepletingLifeTimePackets = requestPackets(module.getSourceAddress(),
                                                                            GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        GenericPacket packetForPg = haveResponseWithPg(ghgChgDepletingLifeTimePackets,
                                                       GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        if (packetForPg == null) {
            // 6.11.13.18.a. Fail PG query where no response was received
            addFailure("6.11.13.18.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        } else {
            var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
            packetForPg.getSpns()
                       .forEach(spn -> {
                           // 6.11.13.18.b - Fail PG query where any accumulator value
                           // received is greater than FAFFFFFFh.
                           if (spn.getRawValue() > 0xFAFFFFFFL) {
                               addFailure("6.11.13.18.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                       + module.getModuleName() + " for " + spn);
                           }
                           // 6.11.13.18.c Fail all values where the corresponding value received is part 2 is
                           // greater than the part 11 value
                           var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                           if (partTwoValue > spn.getValue()) {
                               addFailure("6.11.13.18.c - Value received from " + module.getModuleName()
                                                  + " for " + spn
                                                  + "  in part 2 was greater than part 11 value");
                           }

                       });
        }

        // 6.11.13.19 Actions13 for MY2022+ Plug-in HEV DRIVES
        // 6.11.13.19.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for Active 100hr Charge Depleting times and
        // Stored 100hr PSA Charge Depleting PGs:
        // PG PG Label
        // 64246 Hybrid Charge Depleting or Increasing Operation Active 100 Hours - PG Acronym HCDIOA
        // 64245 Hybrid Charge Depleting or Increasing Operation Stored 100 Hours - - PG Acronym HCDIOS
        var hybridChargeOpsPackets = new ArrayList<>(requestPackets(module.getSourceAddress(),
                                                                    GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR,
                                                                    GHG_STORED_HYBRID_CHG_DEPLETING_100_HR));

        // 6.11.13.19.b - List data received in a table using lifetime, stored 100 hr, active 100hr for columns, and
        // categories for rows.
        getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(ghgChgDepletingLifeTimePackets.stream(),
                                                                              hybridChargeOpsPackets.stream())
                                                                      .collect(Collectors.toList())));

        for (int pg : List.of(GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR,
                              GHG_STORED_HYBRID_CHG_DEPLETING_100_HR)) {
            GenericPacket hybridPacketForPg = haveResponseWithPg(hybridChargeOpsPackets,
                                                                 pg);
            if (hybridPacketForPg == null) {
                // 6.11.13.20.a - For MY2024+ Plug-in HEV DRIVES, Fail each PG query where
                // no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.11.13.20.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.11.13.20.b - For MY2022-23 Plug-in HEV DRIVES, Warn each PG query,
                // where no response was received
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.11.13.20.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                hybridPacketForPg.getSpns()
                                 .forEach(spn -> {
                                     // 6.11.13.20.c - Fail each PG query where any active technology label or
                                     // accumulator value received is greater than FAFFh, respectively.
                                     if (spn.getRawValue() > 0xFAFFL) {
                                         addFailure("6.11.13.20.c - Bin value received is greater than 0xFAFF(h) from "
                                                 + module.getModuleName() + " for "
                                                 + spn);
                                     }
                                     // 6.11.13.20.d -  Fail all values where the corresponding value received in part 2
                                     // is greater than the part 11 value. (Where supported)
                                     var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                                     if (partTwoValue > spn.getValue()) {
                                         addFailure("6.11.13.20.d - Value received from " + module.getModuleName()
                                                            + " for " + spn
                                                            + " in part 2 was greater than part 11 value");
                                     }

                                 });
            }
        }
    }

    private void testSp12797(OBDModuleInformation module) {
        // 6.11.13.13 Actions10 for MY2022+ HEV and BEV drives
        // 6.11.13.13.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for 64241 PSA Times
        // Lifetime Hours
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                GHG_TRACKING_LIFETIME_HYBRID_PG);

        GenericPacket packetForPg = haveResponseWithPg(ghgTrackingPackets, GHG_TRACKING_LIFETIME_HYBRID_PG);
        if (packetForPg == null) {
            // 6.11.13.14.a - Fail PG query where no response was received.
            addWarning("6.11.13.14.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_HYBRID_PG);
        } else {
            var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
            packetForPg.getSpns()
                       .forEach(spn -> {
                           // 6.11.13.14.b - Fail PG query where any accumulator value
                           // received is greater than FAFFFFFFh.
                           if (spn.getRawValue() > 0xFAFFFFFFL) {
                               addFailure("6.11.13.14.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                       + module.getModuleName() + " for " + spn);
                           }
                           // 6.11.13.14.c - Fail all values where the corresponding value received in part 2 is
                           // greater than the part 11 value.
                           var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                           if (partTwoValue > spn.getValue()) {
                               addFailure("6.11.13.14.c - Value received from " + module.getModuleName()
                                                  + " for " + spn
                                                  + " in part 2 was greater than part 11 value");
                           }

                       });
        }

        // 6.11.13.15 Actions11 for MY2022+ HEV and BEV drives
        // 6.11.13.15.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for Active 100hr
        // PSA Times and Stored 100hr PSA Times PGs:
        // PG PG Label
        // 64242 PSA Times Stored 100 Hours - PG Acronym PSATS
        // 64243 PSA Times Active 100 Hours - PG Acronym PSATA
        List<GenericPacket> ghgPackets = requestPackets(module.getSourceAddress(),
                                                        GHG_STORED_HYBRID_100_HR,
                                                        GHG_ACTIVE_HYBRID_100_HR);

        // 6.11.13.13.b - List data received in a table using lifetime, stored 100 hr, active 100hr for columns, and
        // categories for rows.
        // 6.11.13.15.b - List data received in a table using lifetime, stored 100 hr, active 100 hr for columns and
        // categories for rows.
        getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(ghgTrackingPackets.stream(),
                                                                              ghgPackets.stream())
                                                                      .collect(Collectors.toList())));

        for (int pg : List.of(GHG_STORED_HYBRID_100_HR, GHG_ACTIVE_HYBRID_100_HR)) {
            GenericPacket hybridPacketForPg = haveResponseWithPg(ghgPackets, pg);
            if (hybridPacketForPg == null) {
                // 6.11.13.16.a - For MY2024+ HEV and BEV drives, Fail each PG query where no
                // response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.11.13.16.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.11.13.16.b - For MY2022-23 HEV and BEV drives, Warn each PG query, where no
                // response was received.
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.11.13.16.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                hybridPacketForPg.getSpns()
                                 .forEach(spn -> {
                                     // 6.11.13.16.c - Fail each PG query where any accumulator
                                     // value received is greater than FAFFh.
                                     if (spn.getRawValue() > 0xFAFFL) {
                                         addFailure("6.11.13.16.c - Bin value received is greater than 0xFAFF(h) from "
                                                 + module.getModuleName() + " for " + spn);
                                     }
                                     // 6.11.13.16.d - Fail all values where the corresponding value received in part 2
                                     // is greater than the part 11 value. (Where supported)
                                     var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                                     if (partTwoValue > spn.getValue()) {
                                         addFailure("6.11.13.16.d - Value received from " + module.getModuleName()
                                                            + " for " + spn
                                                            + " in part 2 was greater than part 11 value");
                                     }
                                 });
            }
        }
    }

    private void testSp12730(OBDModuleInformation module) {
        // 6.11.13.5 Actions3 for all MY2022+ Engines
        // 6.11.13.5.a - DS request messages to ECU that indicated support in DM24 for upon request SP 12730 (GHG
        // Tracking Lifetime Engine Run
        // Time) for PG 64252 GHG Tracking Lifetime Array Data.
        var ghgTrackingLifetimePackets = requestPackets(module.getSourceAddress(),
                                                        GHG_TRACKING_LIFETIME_PG);

        GenericPacket packetForPg = haveResponseWithPg(ghgTrackingLifetimePackets, GHG_TRACKING_LIFETIME_PG);
        if (packetForPg == null) {
            // 6.11.13.6.a. Fail PG query where no response was received
            addFailure("6.11.13.6.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_PG);
        } else {
            var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
            packetForPg.getSpns()
                       .forEach(spn -> {
                           // 6.11.13.6.b. Fail PG query where any bin value received is greater than FAFFh.
                           if (spn.getRawValue() > 0xFAFFL) {
                               addFailure("6.11.13.6.b - Bin value received is greater than 0xFAFF(h) from "
                                       + module.getModuleName() + " for " + spn);
                           }
                           // 6.11.13.6.c - Fail all values where the corresponding value received in part 2 is
                           // greater than the part 11 value
                           var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                           if (partTwoValue > spn.getValue()) {
                               addFailure("6.11.13.6.c - Value received from " + module.getModuleName()
                                                  + " for " + spn
                                                  + " in part 2 was greater than part 11 value");
                           }
                           // 6.11.13.6.d - Fail if lifetime engine hours < 600 seconds
                           if (spn.getId() == 12730 && spn.getValue() < 600) {
                               addFailure("6.11.13.6.d - Lifetime engine hours received is < 600 seconds from "
                                       + module.getModuleName() + " for " + spn);
                           }
                       });
        }

        // 6.11.13.7 Actions4 for MY2022+ Engines
        // 6.11.13.7.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12730 (GHG Tracking Lifetime Engine Run Time) for each 100hr GHG tracking
        // PG
        // PG PG Label
        // 64254 GHG Tracking Active 100 Hour Array Data
        // 64253 GHG Tracking Stored 100 Hour Array Data
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                GHG_ACTIVE_100_HR,
                                                GHG_STORED_100_HR);

        // 6.11.13.7.b. List data received in a table using lifetime, stored 100 hr,
        // active 100hr for columns, and categories for rows.
        getListener().onResult(ghgTrackingModule.formatTrackingTable(Stream.concat(ghgTrackingLifetimePackets.stream(),
                                                                                   ghgTrackingPackets.stream())
                                                                           .collect(Collectors.toList())));

        for (int pg : List.of(GHG_ACTIVE_100_HR, GHG_STORED_100_HR)) {
            GenericPacket hybridPacketForPg = haveResponseWithPg(ghgTrackingPackets, pg);
            if (hybridPacketForPg == null) {
                // 6.11.13.8.a. For all MY2024+ engines, Fail each PG query where no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.11.13.8.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.11.13.8.b. For MY2022-23 engines, Warn each PG query, where no response was received
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.11.13.8.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                hybridPacketForPg.getSpns()
                                 .forEach(spn -> {
                                     if (spn.getRawValue() > 0xFAFFL) {
                                         // 6.11.13.8.c - Fail each PG query where any value received is greater than
                                         // FAFFh.
                                         addFailure("6.11.13.8.c - Bin value received is greater than 0xFAFF(h) from "
                                                 + module.getModuleName() + " for " + spn);
                                     }
                                     if (spn.getSlot().toValue(spn.getBytes()) > 0) {
                                         // 6.11.13.8.d - Fail each active 100 hr array value that is greater than zero
                                         addFailure("6.11.13.8.d - Active 100 hr array value received is greater than zero from "
                                                 + module.getModuleName() + " for " + spn);
                                     }
                                     // 6.11.13.8.e - Fail if active 100 hrs engine hours < 600 seconds. (where
                                     // supported)
                                     if (spn.getId() == 12700 ||
                                             spn.getId() == 12715 ||
                                             spn.getId() == 12730) {
                                         if (spn.getValue() < 600) {
                                             addWarning("6.11.13.8.e - Active Tech engine hours received is < 600 seconds from "
                                                     + module.getModuleName() + " for " + spn);
                                         }
                                     }
                                     // 6.11.13.8.f. Warn for all active 100 hr vehicle distance => 0.25 km. (Where
                                     // supported).
                                     if (spn.getId() == 12701 ||
                                             spn.getId() == 12716 ||
                                             spn.getId() == 12731) {
                                         if (spn.getValue() >= 0.25) {
                                             addWarning("6.11.13.8.f - Active Tech vehicle distance received is => 0.25km from "
                                                     + module.getModuleName() + " for " + spn);
                                         }
                                     }
                                     // 6.11.13.8.g. Warn for active 100 hr EOE <= 0.5 kW-hr (where supported)
                                     if (spn.getId() == 12704 ||
                                             spn.getId() == 12719 ||
                                             spn.getId() == 12734) {
                                         if (spn.getValue() <= 0.5) {
                                             addWarning("6.11.13.8.g - Active Tech EOE received is <= 0.5 kW-hr from "
                                                     + module.getModuleName() + " for " + spn);
                                         }
                                     }
                                 });
            }
        }
    }

    private void testSp12691(OBDModuleInformation module) {
        // 6.11.13.9 Actions8 for all MY2022+ Engines
        // 6.11.13.9.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for PG 64257 Green House Gas Lifetime Active
        // Technology Tracking.
        var ghgPackets = requestPackets(module.getSourceAddress(),
                                        GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG);

        GenericPacket packetForPg = haveResponseWithPg(ghgPackets, GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG);
        if (packetForPg == null) {
            // 6.11.13.10.a. Warn PG query where no response was received.
            addWarning("6.11.13.10.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG);
        } else {
            packetForPg.getSpns()
                       .forEach(spn -> {
                           // 6.11.13.10.b. Fail any accumulator value received that is greater
                           // than FAFFFFFFh.
                           if (spn.getRawValue() > 0xFAFFFFFFL) {
                               addFailure("6.11.13.10.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                       + module.getModuleName() + " for " + spn);
                           }
                           // 6.11.13.10.c. Fail PG query where any index value received is
                           // greater than FAh.
                           if (spn.getSlot().getId() == 12691 && spn.getRawValue() > 0xFA) {
                               addFailure("6.11.13.10.c - Index value received is greater than 0xFA(h) from "
                               + module.getModuleName() + " for " + spn);
                           }
                       });
        }

        // 6.11.13.11 Actions9 for MY2022+ Engines
        // SAE INTERNATIONAL J1939TM-84 Proposed Draft 24 March 2022 Page 36 of 140
        // 6.11.13.11.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for Active 100hr
        // Active technology PG, followed by DS request message to ECU for
        // Stored 100 hr Active Technology PG.
        // PG PG Label
        // 64256 Green House Gas Active 100 Hour Active Technology Tracking - PG Acronym GHGTTA
        // 64255 Green House Gas Stored 100 Hour Active Technology Tracking - PG Acronym GHGTTS
        var ghg100HrPackets = requestPackets(module.getSourceAddress(),
                                             GHG_ACTIVE_GREEN_HOUSE_100_HR,
                                             GHG_STORED_GREEN_HOUSE_100_HR);

        // 6.11.13.11.b. List data received in a table using lifetime, stored 100 hr,
        // active 100hr for columns, and categories for rows.
        getListener().onResult(ghgTrackingModule.formatTechTable(Stream.concat(ghgPackets.stream(),
                                                                               ghg100HrPackets.stream())
                                                                       .collect(Collectors.toList())));

        for (int pg : List.of(GHG_ACTIVE_GREEN_HOUSE_100_HR, GHG_STORED_GREEN_HOUSE_100_HR)) {
            GenericPacket greenHousePacketForPg = haveResponseWithPg(ghg100HrPackets, pg);
            if (greenHousePacketForPg == null) {
                if (getEngineModelYear() >= 2024) {
                    // 6.11.13.12.a. For all MY2024+ engines, Warn each PG query where no response was received.
                    addFailure("6.11.13.12.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    // 6.11.13.12.b. For MY2022-23 engines, Warn each PG query, where no response was received
                    addWarning("6.11.13.12.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                greenHousePacketForPg.getSpns()
                                     .forEach(spn -> {
                                         // FIXME:
                                         // @Joe values defined in email will update when I get that processed
                                         // 6.11.13.12.c. Fail each PG query where any active technology label or
                                         // accumulator value received is greater than FAh, or FAFFh, respectively.
                                         if (spn.getId() == 12691 || spn.getId() == 12694 || spn.getId() == 12697) {
                                             if (spn.getRawValue() > 0xFAL) {
                                                 addFailure("6.11.13.12.c - Active Technology value received is greater than 0xFA(h) from "
                                                         + module.getModuleName() + " for " + spn);
                                             }
                                         } else {
                                             if (spn.getRawValue() > 0xFAFFL) {
                                                 addFailure("6.11.13.12.c - Active Technology value received is greater than 0xFAFF(h) from "
                                                         + module.getModuleName() + " for " + spn);
                                             }
                                         }
                                         // FIXME:
                                         // @Joe values defined in email will update when I get that processed
                                         // 6.11.13.12.d. Fail each response where the number of labels received are not
                                         // the same as the number of labels received for the lifetime technology
                                         // response.

                                         // FIXME:
                                         // @Joe values defined in email will update when I get that processed
                                         // 6.11.13.12.e. Fail each response where the set of labels received is not a
                                         // subset of the set of labels received for the lifetime’ active technology
                                         // response.

                                         // 6.11.13.12.f. Fail all values where the corresponding value received in
                                         // part 2 is greater than the part 11 value. (Where supported)
                                         var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                                         if (partTwoValue > spn.getValue()) {
                                             addFailure("6.11.13.12.f - Value received from " + module.getModuleName()
                                                                + " for " + spn
                                                                + " in part 2 was greater than part 11 value");
                                         }

                                         // FIXME:
                                         // @Joe, will implement once dataRepo bug is fixed. (f-h)
                                         // 6.11.13.12.g. Warn if any stored 100 hrs active technology engine hours >
                                         // part 2 value + 600 seconds (where supported)
//                                         if(spn.getId() == 12698 || spn.getId() == 12696 || spn.getId() == 12693) {
//                                                 if( spn.getValue() > partTwoValue + 600) {
//                                                     addWarning("6.11.13.12.g - Active Tech time received is > part 2 value + 600 seconds");
//                                                 }
//                                         }
                                         // FIXME:
                                         // 6.11.13.12.h. Warn for any active 100 hr active technology vehicle distance
                                         // => 0.25 km. (Where supported).
//                                         if (spn.getId() == 12699 || spn.getId() == 12696 ||spn.getId() == 12693) {
//                                             if (spn.getValue() >= 0.25) {
//                                                 addWarning("6.11.13.12.h - Active Tech vehicle distance received is => 0.25km from "
//                                                         + module.getModuleName() + " for " + spn);
//                                             }
//                                         }
                                     });
            }
        }
    }

    private void testSp12675(OBDModuleInformation module) {
        int[] nOxLifeTimeSps = CollectionUtils.join(NOx_LIFETIME_SPs,
                                                    NOx_LIFETIME_ACTIVITY_SPs);
        // 6.11.13.1.a. DS request messages to ECU that indicated support in DM24 for upon request SP 12675 (NOx
        // Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total) for all lifetime NOx binning PGs, followed
        // by all Lifetime engine activity PGs
        var nOxPackets = requestPackets(module.getSourceAddress(),
                                        nOxLifeTimeSps);

        for (int pg : nOxLifeTimeSps) {
            GenericPacket packetForPg = haveResponseWithPg(nOxPackets, pg);
            if (packetForPg == null) {
                // 6.11.13.2.a. Fail each PG query where no response was received.
                addFailure("6.11.13.2.a - No response was received from "
                        + module.getModuleName() + " for PG "
                        + pg);
            } else {
                var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // 6.11.13.2.b. Fail each PG query where any bin value received
                               // is greater than FAFFFFFFh.
                               if (spn.getRawValue() > 0xFAFFFFFFL) {
                                   addFailure("6.11.13.2.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                           + module.getModuleName() + " for " + spn);
                               }
                               // 6.11.13.2.c Fail all values where the corresponding value received in part 2 is
                               // greater than the part 12 value.
                               var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                               if (partTwoValue > spn.getValue()) {
                                   addFailure("6.11.13.2.c - Value received from " + module.getModuleName()
                                                      + " for " + spn
                                                      + " in part 2 was greater than part 11 value");
                               }
                               //FIXME
                               // 6.11.13.2.d Fail if lifetime engine hours < part 2 value + 600 seconds
//                               if(spn.getId() == 12698 || spn.getId() == 12696 || spn.getId() == 12693) {
//                                   if( spn.getValue() > partTwoValue + 600) {
//                                       addWarning("6.11.13.2.d - Active Tech time received is > part 2 value + 600 seconds");
//                                   }
//                               }
                               //FIXME
                               // 6.11.13.2.e Fail if lifetime engine activity engine hours < part 2 value + 600 seconds
//                               if(spn.getId() == 12698 || spn.getId() == 12696 || spn.getId() == 12693) {
//                                   if( spn.getValue() > partTwoValue + 600) {
//                                       addWarning("6.11.13.12.g - Active Tech time received is > part 2 value + 600 seconds");
//                                   }
//                               }
                           });
            }
        }

        int[] nOx100HourSps = CollectionUtils.join(NOx_TRACKING_ACTIVE_100_HOURS_SPs,
                                                   NOx_TRACKING_STORED_100_HOURS_SPs);
        // 6.11.13.3.a - DS request message to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1
        // - Total) for each active 100hr NOx binning PG, followed by each Stored 100 hr PG
        // Label
        List<GenericPacket> nOx100HourPackets = requestPackets(module.getSourceAddress(),
                                                               nOx100HourSps);

        // 6.11.13.3.b - List data received in a table using bin numbers for rows.
        getListener().onResult(nOxBinningModule.format(Stream.concat(nOxPackets.stream(),
                                                                     nOx100HourPackets.stream())
                                                             .collect(Collectors.toList())));
        for (int pg : nOx100HourSps) {
            GenericPacket packetForPg = haveResponseWithPg(nOx100HourPackets, pg);
            if (packetForPg == null) {
                // 6.11.13.4.a. For all MY2024+ Diesel engines, Fail each PG query where no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.11.13.4.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.11.13.4.b. For all MY2022-23 Diesel engines, Warn each PG query where no response was received.
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.11.13.4.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                var partTwoPacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                packetForPg.getSpns().forEach(spn -> {
                    if (spn.getRawValue() > 0xFAFFFFFFL) {
                        // 6.11.13.4.c. Fail each PG query where any bin value received is greater than FAFFh. (Use
                        // FAFFFFFFh for NOx values)
                        addFailure("6.11.13.4.c - Bin value received is greater than 0xFAFFFFFF(h) from "
                                + module.getModuleName() + " for " + spn);

                    }
                    // 6.11.13.4.d. Fail all values where the corresponding value received in part 2 is greater than the
                    // part 11 value (where supported)
                    var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                    if (partTwoValue > spn.getValue()) {
                        addFailure("6.11.13.4.d - Value received from " + module.getModuleName()
                                           + " for " + spn
                                           + " in part 2 was greater than part 11 value");
                    }

                    // 6.11.13.4.e. Fail if active 100 hrs engine hours < 600 seconds (where supported)
                    if (spn.getId() == 12699 ||
                            spn.getId() == 12696 ||
                            spn.getId() == 12693) {
                        if (spn.getValue() < 600) {
                            addWarning("6.11.13.4.e - Active Tech engine hours received is < 600 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }

                    // 6.11.13.4.f. Info, if stored 100 hrs engine hours > 0 seconds (where supported)
                    if (pg == 64269 && spn.getValue() > 0) {
                        addFailure("6.11.13.4.f - Active Tech stored engine hours received is > 0 seconds from "
                                + module.getModuleName() + " for " + spn);
                    }

                    // FIXME:
                    // @Joe just need to add the logic once the dataRepo bug is fixed.
                    // 6.11.13.4.g. Warn for all active 100 hr bin 3 through bin 16 values that are greater than their
                    // respective values for the bins 3 through 16 in part 2 (where supported)

                    // 6.11.13.4.h. Warn for all active 100 hr vehicle distance bins => 0.25 km (where supported)
                    if (pg == 64276 ||
                            pg == 64270 ||
                            pg == 64264) {
                        if (spn.getValue() >= 0.25) {
                            addWarning("6.11.13.4.h - Active 100 hr vehicle distance bins received is => 0.25 km from "
                                    + module.getModuleName() + " for " + spn);
                        }

                    }

                    // 6.11.13.4.i. Warn for active 100 hr EOE bin 1 and bin 2 <= 0.5kW-hr (where supported
                    // 64271 - Bin 1 => 12457
                    // 64271 - Bin 2 => 12458
                    // 64265 - Bin 1 => 12559
                    // 64265 - Bin 2 => 12560
                    // 64261 - Bin 1 => 12627
                    // 64261 - Bin 2 => 12628
                    if (pg == 64271) {
                        var bin1Value = packetForPg.getSpn(12457).orElse(null);
                        var bin2Value = packetForPg.getSpn(12458).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() <= 0.5
                                && bin2Value.getValue() <= 0.5) {
                            addWarning("6.11.13.4.i - Active 100 hr EOE bin1 and bin2 received is <= 0.5kW-hr from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    if (pg == 64265) {
                        var bin1Value = packetForPg.getSpn(12559).orElse(null);
                        var bin2Value = packetForPg.getSpn(12560).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() <= 0.5
                                && bin2Value.getValue() <= 0.5) {
                            addWarning("6.11.13.4.i - Active 100 hr EOE bin1 and bin2 received is <= 0.5kW-hr from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    if (pg == 64261) {
                        var bin1Value = packetForPg.getSpn(12627).orElse(null);
                        var bin2Value = packetForPg.getSpn(12628).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() <= 0.5
                                && bin2Value.getValue() <= 0.5) {
                            addWarning("6.11.13.4.i - Active 100 hr EOE bin1 and bin2 received is <= 0.5kW-hr from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }

                    // 6.11.13.4.j. Warn for active 100 hr engine out NOx bin1 and bin 2 = 0 (where supported)
                    // 64278 - Bin 1 => 12338
                    // 64278 - Bin 2 => 12339
                    // 64272 - Bin 1 => 12440
                    // 64272 - Bin 2 => 12441
                    // 64266 - Bin 1 => 12542
                    // 64266 - Bin 2 => 12543
                    if (pg == 64278) {
                        var bin1Value = packetForPg.getSpn(12338).orElse(null);
                        var bin2Value = packetForPg.getSpn(12339).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() == 0
                                && bin2Value.getValue() == 0) {
                            addWarning("6.11.13.4.k - Active 100 hr system out Nox bin1 and bin2 received is = 0 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    if (pg == 64272) {
                        var bin1Value = packetForPg.getSpn(12440).orElse(null);
                        var bin2Value = packetForPg.getSpn(12441).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() == 0
                                && bin2Value.getValue() == 0) {
                            addWarning("6.11.13.4.k - Active 100 hr system out Nox bin1 and bin2 received is = 0 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    if (pg == 64266) {
                        var bin1Value = packetForPg.getSpn(12542).orElse(null);
                        var bin2Value = packetForPg.getSpn(12543).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() == 0
                                && bin2Value.getValue() == 0) {
                            addWarning("6.11.13.4.k - Active 100 hr system out Nox bin1 and bin2 received is = 0 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }

                    // 6.11.13.4.k. Warn for active 100 hr system out Nox bin 1 and bin 2 = 0 (where supported)
                    // 64279 - Bin 1 => 12321
                    // 64279 - Bin 2 => 12322
                    // 64273 - Bin 1 => 12423
                    // 64273 - Bin 2 => 12424
                    // 64267 - Bin 1 => 12525
                    // 64267 - Bin 2 => 12526
                    int id = spn.getId();
                    if (pg == 64279) {
                        var bin1Value = packetForPg.getSpn(12321).orElse(null);
                        var bin2Value = packetForPg.getSpn(12322).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() == 0
                                && bin2Value.getValue() == 0) {
                            addWarning("6.11.13.4.k - Active 100 hr system out Nox bin1 and bin2 received is = 0 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    if (pg == 64273) {
                        var bin1Value = packetForPg.getSpn(12423).orElse(null);
                        var bin2Value = packetForPg.getSpn(12424).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() == 0
                                && bin2Value.getValue() == 0) {
                            addWarning("6.11.13.4.k - Active 100 hr system out Nox bin1 and bin2 received is = 0 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    if (pg == 64267) {
                        var bin1Value = packetForPg.getSpn(12525).orElse(null);
                        var bin2Value = packetForPg.getSpn(12526).orElse(null);
                        if (bin1Value != null
                                && bin2Value != null
                                && bin1Value.getValue() == 0
                                && bin2Value.getValue() == 0) {
                            addWarning("6.11.13.4.k - Active 100 hr system out Nox bin1 and bin2 received is = 0 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                });
            }
        }
    }
}

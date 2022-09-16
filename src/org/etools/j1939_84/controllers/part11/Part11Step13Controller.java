/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static java.lang.Double.sum;
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
import org.etools.j1939tools.j1939.model.ActiveTechnology;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.GhgActiveTechnologyPacket;
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
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new GhgTrackingModule(DateTimeModule.getInstance()),
             new NOxBinningModule(DateTimeModule.getInstance()));
    }

    Part11Step13Controller(Executor executor,
                           BannerModule bannerModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           GhgTrackingModule ghgTrackingModule,
                           NOxBinningModule nOxBinningModule) {
        super(executor,
              bannerModule,
              DateTimeModule.getInstance(),
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
        List<GenericPacket> lifetimeHybridChgDepletingPkgs = requestPackets(module.getSourceAddress(),
                                                                            GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        if (lifetimeHybridChgDepletingPkgs.isEmpty()) {
            // 6.11.13.18.a. Fail PG query where no response was received
            addFailure("6.11.13.18.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        } else {
            lifetimeHybridChgDepletingPkgs.forEach(genericPacket -> {
                var partTwoPacket = get(genericPacket.getPgnDefinition().getId(),
                                        module.getSourceAddress(),
                                        2);
                genericPacket.getSpns().forEach(spn -> {
                    // 6.11.13.18.b - Fail PG query where any accumulator value
                    // received is greater than FAFFFFFFh.
                    if (spn.getRawValue() > 0xFAFFFFFFL) {
                        addFailure("6.11.13.18.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                + module.getModuleName() + " for " + spn);
                    }
                    // 6.11.13.18.c Fail all values where the corresponding value received
                    // is part 2 is
                    // greater than the part 11 value
                    var partTwoValue = partTwoPacket.getSpnValue(spn.getId())
                                                    .orElse(NOT_AVAILABLE);
                    if (partTwoValue > spn.getValue()) {
                        addFailure("6.11.13.18.c - Value received from "
                                + module.getModuleName()
                                + " for " + spn
                                + "  in part 2 was greater than part 11 value");
                    }
                });
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
        getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(lifetimeHybridChgDepletingPkgs.stream(),
                                                                              hybridChargeOpsPackets.stream())
                                                                      .collect(Collectors.toList())));

        if (hybridChargeOpsPackets.isEmpty()) {
            // 6.11.13.20.a - For MY2024+ Plug-in HEV DRIVES, Fail each PG query where
            // no response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.11.13.20.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.11.13.20.b - For MY2022-23 Plug-in HEV DRIVES, Warn each PG query,
            // where no response was received
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.11.13.20.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            hybridChargeOpsPackets.forEach(genericPacket -> {
                var partTwoPacket = get(genericPacket.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                genericPacket.getSpns().forEach(spn -> {
                    var lowerLimit = spn.getSlot().getByteLength() == 1 ? 0xFAL : 0xFAFFL;
                    var upperLimit = spn.getSlot().getByteLength() == 1 ? 0xFFL : 0xFFFFL;
                    var lowerLimitString = spn.getSlot().getByteLength() == 1 ? String.format("0x%02X", lowerLimit)
                            : String.format("0x%04X", lowerLimit);
                    var upperLimitString = spn.getSlot().getByteLength() == 1 ? String.format("0x%02X", upperLimit)
                            : String.format("0x%04X", upperLimit);
                    if (spn.getRawValue() > lowerLimit && spn.getRawValue() < upperLimit) {
                        // 6.11.13.20.c - Fail each PG query where any active technology label or accumulator value
                        // received is greater than FAFFh, respectively.
                        addFailure("6.11.13.8.c - Bin value received is greater than "
                                + lowerLimitString + "(h) and less than " + upperLimitString
                                + "(h) from " + module.getModuleName() + " for " + spn);
                    }
                    // 6.11.13.20.d - Fail all values where the corresponding value received in part 2
                    // is greater than the part 11 value. (Where supported)
                    var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                    if (spn.hasValue() && partTwoValue > spn.getValue()) {
                        addFailure("6.11.13.20.d - Value received from " + module.getModuleName()
                                + " for " + spn
                                + " in part 2 was greater than part 11 value");
                    }

                });
            });
        }
    }

    private void testSp12797(OBDModuleInformation module) {
        // 6.11.13.13 Actions10 for MY2022+ HEV and BEV drives
        // 6.11.13.13.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for 64241 PSA Times
        // Lifetime Hours
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                GHG_TRACKING_LIFETIME_HYBRID_PG);

        if (ghgTrackingPackets.isEmpty()) {
            // 6.11.13.14.a - Fail PG query where no response was received.
            addWarning("6.11.13.14.a - No response was received from "
                    + module.getModuleName());
        } else {
            var partTwoPacket = get(GHG_TRACKING_LIFETIME_HYBRID_PG, module.getSourceAddress(), 2);
            ghgTrackingPackets.stream()
                              .flatMap(genericPacket -> {
                                  return genericPacket.getSpns().stream();
                              })
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
                                  if (spn.hasValue() && partTwoValue > spn.getValue()) {
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

        if (ghgPackets.isEmpty()) {
            // 6.11.13.16.a - For MY2024+ HEV and BEV drives, Fail each PG query where no
            // response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.11.13.16.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.11.13.16.b - For MY2022-23 HEV and BEV drives, Warn each PG query, where no
            // response was received.
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.11.13.16.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            ghgPackets.forEach(genericPacket -> {
                var partTwoPacket = get(genericPacket.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                genericPacket.getSpns()
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
                                 if (spn.hasValue() && partTwoValue > spn.getValue()) {
                                     addFailure("6.11.13.16.d - Value received from " + module.getModuleName()
                                             + " for " + spn
                                             + " in part 2 was greater than part 11 value");
                                 }
                             });
            });
        }
    }

    private void testSp12730(OBDModuleInformation module) {
        // 6.11.13.5 Actions3 for all MY2022+ Engines
        // 6.11.13.5.a - DS request messages to ECU that indicated support in DM24 for upon request SP 12730 (GHG
        // Tracking Lifetime Engine Run
        // Time) for PG 64252 GHG Tracking Lifetime Array Data.
        var ghgTrackingLifetimePackets = requestPackets(module.getSourceAddress(),
                                                        GHG_TRACKING_LIFETIME_PG);

        if (ghgTrackingLifetimePackets.isEmpty()) {
            // 6.11.13.6.a. Fail PG query where no response was received
            addFailure("6.11.13.6.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_PG);
        } else {
            var partTwoPacket = get(GHG_TRACKING_LIFETIME_PG, module.getSourceAddress(), 2);

            ghgTrackingLifetimePackets.stream().map(GenericPacket::getSpns).forEach(spns -> {
                spns.forEach(spn -> {
                    // 6.11.13.6.b. Fail PG query where any bin value received is greater than FAFFFFFFh and less than
                    // FFFFFFFFh.
                    if (spn.getRawValue() > 0xFAFFFFFFL && spn.getRawValue() < 0xFFFFFFFFL) {
                        addFailure("6.11.13.6.b - Bin value received is greater than 0xFAFFFFFF(h) and less than 0xFFFFFFFF(h)from "
                                + module.getModuleName() + " for " + spn);
                    }
                    // 6.11.13.6.c - Fail all values where the corresponding value received in part 2 is
                    // greater than the part 11 value
                    var partTwoValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                    if (spn.hasValue() && partTwoValue > spn.getValue()) {
                        addFailure("6.11.13.6.c - Value received from " + module.getModuleName()
                                + " for " + spn + " in part 2 was greater than part 11 value");
                    }
                    // 6.11.13.6.d - Fail if lifetime engine hours SPN 12730 < 600 seconds. (Where supported)
                    if (spn.hasValue() && spn.getId() == 12730 && spn.getValue() < 600) {
                        addFailure("6.11.13.6.d - Lifetime engine hours SPN " + spn.getId()
                                + " received is < 600 seconds from " + module.getModuleName());
                    }
                });
            });
        }

        // 6.11.13.7 Actions4 for MY2022+ Engines
        // 6.11.13.7.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12730 (GHG Tracking Lifetime Engine Run Time) for each 100hr GHG tracking PG
        // PG Label
        // 64254 GHG Tracking Active 100 Hour Array Data
        // 64253 GHG Tracking Stored 100 Hour Array Data
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                GHG_ACTIVE_100_HR,
                                                GHG_STORED_100_HR);

        // 6.11.13.7.b. List data received in a table using lifetime, stored 100 hr,
        // active 100hr for columns, and categories for rows.

        if (ghgTrackingPackets.isEmpty()) {
            // 6.11.13.8.a. For all MY2024+ engines, Fail each PG query where no response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.11.13.8.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.11.13.8.b. For MY2022-23 engines, Warn each PG query, where no response was received
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.11.13.8.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            if(!ghgTrackingLifetimePackets.isEmpty() || !ghgTrackingPackets.isEmpty()) {
                getListener().onResult(ghgTrackingModule.formatTrackingTable(Stream.concat(ghgTrackingLifetimePackets.stream(),
                                                                                           ghgTrackingPackets.stream())
                                                                                     .collect(Collectors.toList())));
            }
            ghgTrackingPackets.forEach(packet -> {
                var partTwoPacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 2);

                packet.getSpns().forEach(spn -> {
                    var lowerLimit = spn.getSlot().getByteLength() == 2 ? 0xFAFFL : 0xFAFFFFFFL;
                    var upperLimit = spn.getSlot().getByteLength() == 2 ? 0xFFFFL : 0xFFFFFFFFL;
                    var lowerLimitString = spn.getSlot().getByteLength() == 2 ? String.format("0x%04X", lowerLimit)
                            : String.format("0x%08X", lowerLimit);
                    var upperLimitString = spn.getSlot().getByteLength() == 2 ? String.format("0x%04X", upperLimit)
                            : String.format("0x%08X", upperLimit);
                    if (spn.getRawValue() > lowerLimit && spn.getRawValue() < upperLimit) {
                        // 6.11.13.8.c - Fail each PG query where any bin value received is greater
                        // than FAFFh and less than FFFFh (Use FAFFFFFFh and less than FFFFFFFFh for
                        // 32-bit SPNs 12705 and 12720).
                        addFailure("6.11.13.8.c - Bin value received is greater than "
                                + lowerLimitString + "(h) and less than " + upperLimitString
                                + "(h) from " + module.getModuleName() + " for " + spn);
                    }
                    var partTwoSpn = partTwoPacket.getSpn(spn.getId())
                                                  .orElse(Spn.create(module.getSourceAddress(), NOT_AVAILABLE));
                    if (spn.getRawValue() < partTwoSpn.getRawValue()) {
                        // 6.11.13.8.d - Fail all values where the corresponding value received in part 2 is greater
                        // than the part 11 value. (Where supported)
                        addFailure("6.11.13.8.d - Value received from " + module.getModuleName()
                                + " for " + spn
                                + " in part 2 was greater than part 11 value");
                    }
                    // 6.11.13.8.e - Fail if active 100 hrs engine hours SPN 12700 < 600 seconds. (where supported)
                    if (spn.getId() == 12700) {
                        if (spn.hasValue() && spn.getValue() < 600) {
                            addWarning("6.11.13.8.e - Active 100 hrs engine hours SPN " + spn.getId()
                                    + " received is < 600 seconds from "
                                    + module.getModuleName() + " for " + spn.getLabel());
                        }
                    }
                    // 6.11.13.8.f. Warn for all active 100 hr vehicle distance SPN 12701 => 0.25 km. (Where supported).
                    if (spn.getId() == 12701) {
                        if (spn.hasValue() && spn.getValue() >= 0.25) {
                            addWarning("6.11.13.8.f - Active Tech vehicle distance received is => 0.25km from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    // 6.11.13.8.g. Info for active 100 hr EOE SPN 12704 greater than 1.0 kW-hr (where supported)
                    if (spn.getId() == 12704) {
                        if (spn.hasValue() && spn.getValue() > 1.0) {
                            addInfo("6.11.13.8.g - Active Tech EOE received is > 1.0 kW-hr from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                });
            });
        }
    }

    private void testSp12691(OBDModuleInformation module) {
        // 6.11.13.9 Actions8 for all MY2022+ Engines
        // 6.11.13.9.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for PG 64257 Green House Gas Lifetime Active
        // Technology Tracking.
        var lifetimeGhgPackets = requestPackets(module.getSourceAddress(),
                                                GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG)
                                                                                     .stream()
                                                                                     .map(GenericPacket::getPacket)
                                                                                     .map(GhgActiveTechnologyPacket::new)
                                                                                     .collect(Collectors.toList());

        if (lifetimeGhgPackets.isEmpty()) {
            // 6.11.13.10.a. Warn PG query where no response was received.
            addWarning("6.11.13.10.a - No response was received from "
                    + module.getModuleName());
        } else {
            lifetimeGhgPackets.stream().map(GenericPacket::getSpns).forEach(spns -> {
                spns.forEach(spn -> {
                    // 6.11.13.10.b. Fail any accumulator value received that is greater
                    // than FAFFFFFFh.
                    if (spn.getRawValue() > 0xFAFFFFFFL) {
                        addFailure("6.11.13.10.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                + module.getModuleName() + " for " + spn);
                    }
                    // 6.11.13.10.c. Fail PG query where any index value received is
                    // greater than FAh.
                    if (spn.getId() == 12691 && spn.getRawValue() > 0xFAL) {
                        addFailure("6.11.13.10.c - Index value received is greater than 0xFA(h) from "
                                + module.getModuleName() + " for " + spn);
                    }
                });
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
                                             GHG_STORED_GREEN_HOUSE_100_HR)
                                                                           .stream()
                                                                           .map(GenericPacket::getPacket)
                                                                           .map(GhgActiveTechnologyPacket::new)
                                                                           .collect(Collectors.toList());

        if (ghg100HrPackets.isEmpty()) {
            if (getEngineModelYear() >= 2024) {
                // 6.11.13.12.a. For all MY2024+ engines, FAIL each PG query where no response was received.
                addFailure("6.11.13.12.a - No response was received from "
                        + module.getModuleName());
            }
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                // 6.11.13.12.b. For MY2022-23 engines, Warn each PG query, where no response was received
                addWarning("6.11.13.12.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            // 6.11.13.11.b. List data received in a table using lifetime, stored 100 hr,
            // active 100hr for columns, and categories for rows.
            getListener().onResult(ghgTrackingModule.formatTechTable(Stream.concat(lifetimeGhgPackets.stream(),
                                                                                   ghg100HrPackets.stream())
                                                                           .collect(Collectors.toList())));

            ghg100HrPackets.forEach(packet -> {
                var partTwoPacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                packet.getSpns().forEach(spn -> {
                    // 6.11.13.12.c. Fail each PG query where any 100-hr active technology label value
                    // (SPNs 12694 and 12697) received is greater than FAh
                    if (spn.getId() == 12694 || spn.getId() == 12697) {
                        if (spn.getRawValue() > 0xFAL) {
                            addFailure("6.11.13.12.c - Active Technology value received is greater than 0xFA(h) from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    // 6.11.13.12.d Fail each PG query where any 100-hr active technology accumulator value (SPNs 12698,
                    // 12699, 12695, 12696) received is greater than FAFFh, and less than FFFFh.
                    if (spn.getId() == 12695 || spn.getId() == 12696 || spn.getId() == 12698 || spn.getId() == 12699) {
                        if (spn.getRawValue() > 0xFAFFL && spn.getRawValue() < 0xFFFFL) {
                            addFailure(
                                       "6.11.13.12.d - Active Technology value received is greater than 0xFAFF(h) and less than 0xFFFF(h) from "
                                               + module.getModuleName() + " for " + spn);
                        }
                    }
                    var partTwoValue = partTwoPacket.getSpn(spn.getId())
                                                    .map(Spn::getValue)
                                                    .orElse(NOT_AVAILABLE);
                    // 6.11.13.12.g. - Fail all values where the corresponding value received in part 2 is greater
                    // than the part 11 value. (Where supported)
                    if (spn.hasValue() && (partTwoValue > spn.getValue())) {
                        addWarning("6.11.13.12.g - Active Tech time received is greater than part 2 value from "
                                + module.getModuleName() + " for " + spn);
                    }

                    // 6.11.13.12.h. Warn if any active 100 hrs active technology time SPN 12695 > part 2
                    // value + 600 seconds (where supported)
                    if (spn.getId() == 12695) {
                        if (spn.hasValue() && spn.getValue() >= (partTwoValue + 600)) {
                            addWarning("6.11.13.12.h - Active Tech time received is > part 2 value + 600 seconds from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }
                    // 6.11.13.12.i. Warn for active 100 hr active technology vehicle distance SPN 12696 => 0.25 km.
                    // (Where supported).
                    if (spn.getId() == 12696) {
                        if (spn.hasValue() && spn.getValue() >= 0.25) {
                            addWarning("6.11.13.12.i - Active Tech vehicle distance received is => 0.25km from "
                                    + module.getModuleName() + " for " + spn);
                        }
                    }

                });
            });
        }

        var lifetimeIndexes = lifetimeGhgPackets.stream()
                                                .flatMap(ghgPacket -> ghgPacket.getActiveTechnologies().stream())
                                                .map(ActiveTechnology::getIndex)
                                                .collect(Collectors.toList());

        var activeIndexes = ghg100HrPackets.stream()
                                           .filter(p -> {
                                               return p.getPacket().getPgn() == GHG_ACTIVE_GREEN_HOUSE_100_HR;
                                           })
                                           .flatMap(ghgPacket -> ghgPacket.getActiveTechnologies().stream())
                                           .map(ActiveTechnology::getIndex)
                                           .collect(Collectors.toList());
        var storedIndexes = ghg100HrPackets.stream()
                                           .filter(genericPacket -> {
                                               return genericPacket.getPgnDefinition()
                                                                   .getId() == GHG_STORED_GREEN_HOUSE_100_HR;
                                           })
                                           .flatMap(ghgPacket -> ghgPacket.getActiveTechnologies().stream())
                                           .map(ActiveTechnology::getIndex)
                                           .collect(Collectors.toList());

        // 6.11.13.12.e. Fail each response where the number of labels received are not
        // the same as the number of labels received for the lifetime technology response.
        if (lifetimeIndexes.size() != activeIndexes.size()) {
            addFailure("6.11.13.12.e - Number of active labels received differs from the number of lifetime labels");
        }
        if (lifetimeIndexes.size() != storedIndexes.size()) {
            addFailure("6.11.13.12.e - Number of stored labels received differs from the number of lifetime labels");
        }

        // 6.11.13.12.f. Fail each response where the set of labels received is not a
        // subset of the set of labels received for the lifetime’ active technology
        // response.
        if (!lifetimeIndexes.containsAll(activeIndexes)) {
            addFailure("6.11.13.12.f - Active labels received is not a subset of lifetime labels");
        }
        if (!lifetimeIndexes.containsAll(storedIndexes)) {
            addFailure("6.11.13.12.f - Stored labels received is not a subset of lifetime labels");
        }
    }

    private void testSp12675(OBDModuleInformation module) {
        // 6.11.13.1.a. DS request messages to ECU that indicated support in DM24 for upon request SP 12675 (NOx
        // Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total) for all lifetime NOx binning PGs, followed
        // by all Lifetime engine activity PGs
        var nOxPackets = requestPackets(module.getSourceAddress(),
                                        CollectionUtils.join(NOx_LIFETIME_SPs, NOx_LIFETIME_ACTIVITY_SPs));

        if (nOxPackets.isEmpty()) {
            // 6.11.13.2.a. Fail each PG query where no response was received.
            addFailure("6.11.13.2.a - No response was received from "
                    + module.getModuleName());
        } else {
            // 6.11.13.1.b - List data received in a table using bin numbers for rows.
            getListener().onResult(nOxBinningModule.format(nOxPackets));
            nOxPackets.forEach(genericPacket -> {
                var partTwoPacket = get(genericPacket.getPgnDefinition().getId(), module.getSourceAddress(), 2);
                genericPacket.getSpns()
                             .forEach(spn -> {
                                 // 6.11.13.2.b. Fail each PG query where any bin value received
                                 // is greater than FAFFFFFFh.
                                 if (spn.getRawValue() > 0xFAFFFFFFL) {
                                     addFailure("6.11.13.2.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                             + module.getModuleName() + " for " + spn);
                                 }
                                 // 6.11.13.2.c Fail all values where the corresponding value received in part 2 is
                                 // greater than the part 12 value.
                                 var partTwoValue = partTwoPacket.getSpn(spn.getId())
                                                                 .map(Spn::getValue)
                                                                 .orElse(NOT_AVAILABLE);
                                 if (spn.getValue() != null && partTwoValue > spn.getValue()) {
                                     addFailure("6.11.13.2.c - Value received from " + module.getModuleName()
                                             + " for " + spn
                                             + " in part 2 was greater than part 11 value");
                                 }
                                 // 6.11.13.2.d Info if lifetime engine hours bin 1 (total) SPN 12593 < part 2 value +
                                 // 60 seconds.
                                 if (spn.getId() == 12593) {
                                     double expectedValue = sum(partTwoValue, 60.0);
                                     if (spn.getValue() < expectedValue) {
                                         addInfo("6.11.13.2.d - Lifetime engine hours bin 1 (total) SP " + spn.getId()
                                                 + " value is < part 2 value + 60 seconds");
                                     }
                                 }
                                 // 6.11.13.2.e Fail if lifetime engine activity engine hours bin 1 (total) SPN 12659 <
                                 // part 2 value + 600 seconds
                                 if (spn.getId() == 12659) {
                                     if (spn.getValue() < partTwoValue + 600) {
                                         addFailure("6.11.13.2.e - Lifetime engine activity engine hours bin 1 (total) SP "
                                                 + spn.getId() + " is < part 2 value + 600 seconds");
                                     }
                                 }
                             });
            });
        }

        int[] nOx100HourSps = CollectionUtils.join(NOx_TRACKING_ACTIVE_100_HOURS_SPs,
                                                   NOx_TRACKING_STORED_100_HOURS_SPs);
        // 6.11.13.3.a - DS request message to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1
        // - Total) for each active 100hr NOx binning PG, followed by each Stored 100 hr PG
        // Label
        List<GenericPacket> nOx100HourPackets = requestPackets(module.getSourceAddress(),
                                                               nOx100HourSps);

        if (nOx100HourPackets.isEmpty()) {
            // 6.11.13.4.a. For all MY2024+ Diesel engines, Fail each PG query where no response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.11.13.4.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.11.13.4.b. For all MY2022-23 Diesel engines, Warn each PG query where no response was received.
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.11.13.4.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            // 6.11.13.3.b - List data received in a table using bin numbers for rows.
            getListener().onResult(nOxBinningModule.format(nOx100HourPackets));

            nOx100HourPackets.stream().forEach(genericPacket -> {
                var partTwoPacket = get(genericPacket.getPgnDefinition().getId(), module.getSourceAddress(), 2);

                genericPacket.getSpns().forEach(spn -> {
                    var checkValue = spn.getSlot().getByteLength() == 2 ? 0xFAFFL : 0xFAFFFFFFL;
                    var checkValueString = spn.getSlot().getByteLength() == 2 ? String.format("0x%04X", checkValue)
                            : String.format("0x%08X", checkValue);
                    if (spn.getRawValue() > checkValue) {
                        // 6.11.13.4.c. Fail each PG query where any bin value received is greater than FAFFh. (Use
                        // FAFFFFFFh for NOx values)
                        addFailure(
                                   "6.11.13.4.c - Bin value received is greater than " + checkValueString + "(h) from "
                                           + module.getModuleName() + " for " + spn);

                    }
                    // 6.11.13.4.d. Fail all values where the corresponding value received in part 2 is greater than the
                    // part 11 value (where supported)
                    var partTwoValue = partTwoPacket.getSpnValue(spn.getId())
                                                    .orElse(NOT_AVAILABLE);

                    // 6.11.13.4.e. Info if active 100 hrs engine hours bin 1 SP 12389 < 60 seconds (where supported)
                    if (spn.getId() == 12389) {
                        if (spn.hasValue() && spn.getValue() < 60) {
                            addInfo("6.11.13.4.e - Active 100 hrs engine hours bin 1 SP " + spn.getId()
                                    + " value received is < 60 seconds from "
                                    + module.getModuleName() + " for " + spn.getLabel());
                        }
                    }

                    // 6.11.13.4.f. Info, if stored 100 hrs engine hours > 0 seconds (where supported)
                    if (genericPacket.getPgnDefinition().getId() == 64269 && spn.hasValue() && spn.getValue() > 0) {
                        addInfo("6.11.13.4.f - Active Tech stored engine hours received is > 0 seconds from "
                                + module.getModuleName() + " for " + spn.getLabel());
                    }

                    // 6.11.13.4.g. Warn for all active 100 hr bin 3 through bin 16 values that are less than their
                    // respective values for the bins 3 through 16 in part 2 (where supported)
                    // @formatter:off
                    List<Integer> bins3Thr16Spns = List.of(// PG 64274 bins 3 through 16
                                                           12408, 12409, 12410, 12411, 12412, 12413, 12414,
                                                           12415, 12416, 12417, 12418, 12419, 12420, 12421,
                                                           // PG 64275 bins 3 through 16
                                                           12391, 12392, 12393, 12394, 12395, 12396, 12397,
                                                           12398, 12399, 12400, 12401, 12402, 12403, 12404,
                                                           // PG 64276 bins 3 through 16
                                                           12374, 12375, 12376, 12377, 12378, 12379, 12380,
                                                           12381, 12382, 12383, 12384, 12385, 12386, 12387,
                                                           // PG 64277 bins 3 through 16
                                                           12357, 12358, 12359, 12360, 12361, 12370, 12371,
                                                           12372, 12373, 12374, 12375, 12376, 12377, 12378,
                                                           // PG 64278 bin 3 through 16
                                                           12340, 12341, 12342, 12343, 12344, 12345, 12346,
                                                           12347, 12348, 12349, 12350, 12351, 12352, 12353,
                                                           // PG 64279 bins 3 through 16
                                                           12323, 12324, 12325, 12326, 12327, 12328, 12329,
                                                           12330, 12331, 12332, 12333, 12334, 12335, 12336);
                    // @formatter:on
                    if (bins3Thr16Spns.contains(spn.getId())) {
                        if (spn.getValue() != null && spn.getValue() < partTwoValue) {
                            addWarning("6.11.13.4.g - Value received from " + module.getModuleName()
                                    + " for " + spn + " in part 11 was less than part 2 value");
                        }
                    } else {
                        if (spn.getValue() != null && partTwoValue > spn.getValue()) {
                            addFailure("6.11.13.4.d - Value received from " + module.getModuleName()
                                    + " for " + spn
                                    + " in part 2 was greater than part 11 value");
                        }
                    }

                    // 6.11.13.4.h. Warn for all active 100 hr vehicle distance bins => 0.25 km. (Where supported)
                    if (genericPacket.getPgnDefinition().getId() == 64276) {
                        if (spn.getValue() != null && spn.getValue() >= 0.25) {
                            addWarning("6.11.13.4.h - Active 100 hr vehicle distance bins received is => 0.25 km from "
                                    + module.getModuleName() + " for " + spn);
                        }

                    }

                    // 6.11.13.4.i. Info for active 100 hr EOE bin 1 SPN 12355 <= 0.5 or bin 2 SPN 12356 <= 0.5 kW-hr
                    // (Where supported)
                    if (spn.getId() == 12355 || spn.getId() == 12356) {
                        if (spn.getValue() != null && spn.getValue() <= 0.5) {
                            addInfo("6.11.13.4.i - Active 100 hr EOE " + spn.getId() + " received is <= 0.5kW-hr from "
                                    + module.getModuleName() + " for " + spn.getLabel());
                        }
                    }

                    // 6.11.13.4.j. Info for active 100hr engine out NOx bin 1 SPN 12338 = 0 or bin 2 SPN 12339 = 0.
                    // (Where supported).
                    if (spn.getId() == 12338 || spn.getId() == 12339) {
                        if (spn.getValue() != null && spn.getValue() == 0) {
                            addInfo("6.11.13.4.k - Active 100 hr system out Nox " + spn.getId()
                                    + " received is = 0 seconds from " + module.getModuleName()
                                    + " for " + spn.getLabel());
                        }
                    }

                    // 6.11.13.4.k. Info for active 100hr system out NOx bin 1 SPN 12321 = 0 or bin 2 SPN 12322 = 0.
                    // (Where supported)
                    if (spn.getId() == 12321 || spn.getId() == 12322) {
                        if (spn.getValue() != null && spn.getValue() == 0) {
                            addInfo("6.11.13.4.k - Active 100 hr system out Nox " + spn.getId()
                                    + " received is = 0 seconds from " + module.getModuleName()
                                    + " for " + spn.getLabel());
                        }
                    }

                });
            });
        }
    }
}

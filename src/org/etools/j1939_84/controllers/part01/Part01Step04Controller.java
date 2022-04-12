/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.model.FuelType.BATT_ELEC;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.4 DM24: SPN support
 */
public class Part01Step04Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    private final SupportedSpnModule supportedSpnModule;

    Part01Step04Controller(DataRepository dataRepository) {
        this(dataRepository, DateTimeModule.getInstance());
    }

    Part01Step04Controller(DataRepository dataRepository, DateTimeModule dateTimeModule) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SupportedSpnModule(),
             dataRepository,
             dateTimeModule);
    }

    Part01Step04Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           SupportedSpnModule supportedSpnModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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
        this.supportedSpnModule = supportedSpnModule;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.4.1.a. Destination Specific (DS) DM24 (send Request (PGN 59904) for PGN
        // 64950 (SPNs 3297, 4100-4103)) to each OBD ECU.6
        var responses = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM24(getListener(), a))
                                           .map(BusResult::requestResult)
                                           .collect(Collectors.toList());

        // 6.1.4.1.b. If no response (transport protocol RTS or NACK(Busy) in 220 ms),
        // then retry DS DM24 request to the OBD ECU.
        // [Do not attempt retry for NACKs that indicate not supported].
        List<DM24SPNSupportPacket> dm24s = filterRequestResultPackets(responses);

        var responseAddresses = dm24s
                                     .stream()
                                     .map(ParsedPacket::getSourceAddress)
                                     .collect(Collectors.toSet());

        var nackAddresses = filterRequestResultAcks(responses)
                                                              .stream()
                                                              .filter(r -> r.getResponse() == NACK)
                                                              .map(ParsedPacket::getSourceAddress)
                                                              .collect(Collectors.toSet());

        List<Integer> missingAddresses = getDataRepository().getObdModuleAddresses();
        missingAddresses.removeAll(responseAddresses);
        missingAddresses.removeAll(nackAddresses);

        missingAddresses.stream()
                        .map(address -> getCommunicationsModule().requestDM24(getListener(), address))
                        .map(BusResult::requestResult)
                        .forEach(responses::add);

        // 6.1.4.2.a. Fail if retry was required to obtain DM24 response.
        missingAddresses.stream()
                        .map(Lookup::getAddressName)
                        .forEach(moduleName -> {
                            addFailure("6.1.4.2.a - Retry was required to obtain DM24 response from " + moduleName);
                        });

        // 6.1.4.1.c Create vehicle list of supported SPNs for data stream
        // 6.1.4.1.d. Create ECU specific list of supported SPNs for test results.
        // 6.1.4.1.e. Create ECU specific list of supported freeze frame SPNs.
        dm24s.forEach(this::save);

        // 6.1.4.2.b. Fail if one or more minimum expected SPNs for data stream
        // not supported per section A.1, Minimum Support Table, from the OBD ECU(s).
        List<Integer> dataStreamSPNs = getDataRepository().getObdModules()
                                                          .stream()
                                                          .map(OBDModuleInformation::getDataStreamSPNs)
                                                          .flatMap(Collection::stream)
                                                          .map(SupportedSPN::getSpn)
                                                          .distinct()
                                                          .sorted()
                                                          .collect(Collectors.toList());

        boolean dataStreamOk = supportedSpnModule.validateDataStreamSpns(getListener(), dataStreamSPNs, getFuelType());
        if (!dataStreamOk) {
            addFailure("6.1.4.2.b - N.2 One or more SPNs for data stream is not supported");
        }

        // 6.1.4.2.c. Fail if one or more minimum expected SPNs for freeze frame not
        // supported per section A.2, Criteria for Freeze Frame Evaluation, from the OBD ECU(s).
        List<Integer> freezeFrameSPNs = getDataRepository().getObdModules()
                                                           .stream()
                                                           .map(OBDModuleInformation::getFreezeFrameSPNs)
                                                           .flatMap(Collection::stream)
                                                           .map(SupportedSPN::getSpn)
                                                           .distinct()
                                                           .sorted()
                                                           .collect(Collectors.toList());

        boolean freezeFrameOk = supportedSpnModule.validateFreezeFrameSpns(getListener(), freezeFrameSPNs);
        if (!freezeFrameOk) {
            addFailure("6.1.4.2.c - One or more SPNs for freeze frame are not supported");
        }

        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {
            // For MY2022+ diesel engines
            boolean modelYearIs2022Plus = getVehicleInformationModule().getEngineModelYear() >= 2022;
            List<GenericPacket> packets = new ArrayList<>();
            int address = obdModule.getSourceAddress();
            FuelType fuelType = getFuelType();

            // 6.1.4.2.d. For MY2022+ diesel engines, Fail if SP 12675 (NOx Tracking Engine Activity Lifetime Fuel
            // Consumption Bin 1 - Total) is not included in DM24 response
            if (modelYearIs2022Plus && fuelType.isCompressionIgnition()
                    && !obdModule.supportsSpn(12675)) {
                    addFailure(
                               "6.1.4.2.d - For MY2022+ diesel engines, Fail if SP 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total)"
                                       + NL + "            is not included in DM24 response from "
                                       + Lookup.getAddressName(obdModule.getSourceAddress()));
            }
            // 6.1.4.2.e. For all MY2022+ engines, Fail if SP 12730 (GHG Tracking Engine Run Time) is not included
            // in DM24 response
            if (modelYearIs2022Plus && !obdModule.supportsSpn(12730)) {
                addWarning(
                           "6.1.4.2.e. - For all MY2022+ engines, Fail if SP 12730 (GHG Tracking Engine Run Time) is not included in DM24 response from "
                                   + Lookup.getAddressName(obdModule.getSourceAddress()));
            }

            // 6.1.4.2.f. For all MY2022+ engines, Warn if SP 12691 (GHG Tracking Lifetime Active Technology Index)
            // is not included in DM24 response
            if (modelYearIs2022Plus && !obdModule.supportsSpn(12691)) {
                addWarning(
                           "6.1.4.2.f - For all MY2022+ engines, Warn if SP 12691 (GHG Tracking Lifetime Active Technology Index) is not included in DM24 response from "
                                   + Lookup.getAddressName(obdModule.getSourceAddress()));
            }

            // 6.1.4.2.g. For all MY2022+ HEV and BEV drives, Fail if SP 12797 (Hybrid Lifetime Propulsion System
            // Active Time), is not included in DM24 response. (SP 12797 is Lifetime EV Tracking Byte 1 SP)
            if (modelYearIs2022Plus && (fuelType.isHybrid() || fuelType.isElectric())
                    && !obdModule.supportsSpn(12797)) {
                addFailure("6.1.4.2.g - For all MY2022+ HEV and BEV drives, Fail if SP 12797 (Hybrid Lifetime Propulsion System Active Time),"
                                   + NL
                                   + " is not included in DM24 response (SP 12797 is Lifetime EV Tracking Byte 1 SP) from "
                        + Lookup.getAddressName(obdModule.getSourceAddress()));
            }

            // 6.1.4.2.h. For all MY2022+ Plug-in HEV drives, Fail if SP 12783 (Hybrid Lifetime Distance Traveled in
            // Charge Depleting Operation with Engine off), is not included in DM24 response
            if (modelYearIs2022Plus && fuelType == BATT_ELEC && !obdModule.supportsSpn(12783)) {
                addFailure(
                           "6.1.4.2.h - For all MY2022+ Plug-in HEV drives, Fail if SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with Engine off),"
                                   + NL + " is not included in DM24 response from "
                                   + Lookup.getAddressName(obdModule.getSourceAddress()));
            }
        }
    }
}

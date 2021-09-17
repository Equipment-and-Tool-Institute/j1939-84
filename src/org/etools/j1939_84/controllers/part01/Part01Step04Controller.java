/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.1.4 DM24: SPN support
 */
public class Part01Step04Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    private final SupportedSpnModule supportedSpnModule;

    Part01Step04Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new SupportedSpnModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step04Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           SupportedSpnModule supportedSpnModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
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
                                           .map(a -> getDiagnosticMessageModule().requestDM24(getListener(), a))
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
                        .map(address -> getDiagnosticMessageModule().requestDM24(getListener(), address))
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

    }
}

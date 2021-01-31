/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 * <p>
 * The controller for DM24: SPN support
 */
public class Part01Step04Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;
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
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.supportedSpnModule = supportedSpnModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        List<DM24SPNSupportPacket> destinationSpecificPackets = new ArrayList<>();

        // 6.1.4.1 Actions:
        //
        // a. Destination Specific (DS) DM24 (send Request (PGN 59904) for PGN
        // 64950 (SPNs 3297, 4100-4103)) to each OBD ECU.6
        //
        // b. If no response (transport protocol RTS or NACK(Busy) in 220 ms),
        // then retry DS DM24 request to the OBD ECU.
        //
        // [Do not attempt retry for NACKs that indicate not supported].
        dataRepository.getObdModules()
                .stream()
                .mapToInt(OBDModuleInformation::getSourceAddress)
                .forEach(sourceAddress -> {
                    BusResult<DM24SPNSupportPacket> result = getDiagnosticMessageModule().requestDM24(getListener(),
                                                                                                      sourceAddress);
                    result.getPacket().flatMap(packet -> packet.left).ifPresent(destinationSpecificPackets::add);

                    // 6.1.4.2.a. Fail if retry was required to obtain DM24 response.
                    if (result.isRetryUsed()) {
                        String moduleName = Lookup.getAddressName(sourceAddress);
                        addFailure("6.1.4.2.a - Retry was required to obtain DM24 response from " + moduleName);
                    }
                });

        // 6.1.4.1.c Create vehicle list of supported SPNs for data stream
        destinationSpecificPackets.forEach(p -> {
            OBDModuleInformation info = dataRepository.getObdModule(p.getSourceAddress());
            if (info != null) {
                // 6.1.4.1.d. Create ECU specific list of supported SPNs for test results.
                info.setSupportedSpns(p.getSupportedSpns());
                dataRepository.putObdModule(info);
            }
        });

        // 6.1.4.1.d. Create ECU specific list of supported SPNs for test results.
        List<Integer> dataStreamSpns = dataRepository.getObdModules().stream()
                .map(OBDModuleInformation::getDataStreamSpns)
                .flatMap(Collection::stream)
                .map(SupportedSPN::getSpn)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        FuelType fuelType = dataRepository.getVehicleInformation().getFuelType();
        boolean dataStreamOk = supportedSpnModule.validateDataStreamSpns(getListener(), dataStreamSpns, fuelType);
        if (!dataStreamOk) {
            // 6.1.4.2.b. Fail if one or more minimum expected SPNs for data stream
            // not supported per section A.1, Minimum Support Table, from the OBD ECU(s).
            addFailure("6.1.4.2.b - N.2 One or more SPNs for data stream is not supported");
        }

        // 6.1.4.1.e. Create ECU specific list of supported freeze frame SPNs.
        List<Integer> freezeFrameSpns = dataRepository.getObdModules().stream()
                .map(OBDModuleInformation::getFreezeFrameSpns)
                .flatMap(Collection::stream)
                .map(SupportedSPN::getSpn)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        boolean freezeFrameOk = supportedSpnModule.validateFreezeFrameSpns(getListener(), freezeFrameSpns);
        if (!freezeFrameOk) {
            // 6.1.4.2.c. Fail if one or more minimum expected SPNs for freeze frame not
            // supported per section A.2, Criteria for Freeze Frame Evaluation, from the OBD ECU(s).
            addFailure("6.1.4.2.c - One or more SPNs for freeze frame are not supported");
        }

    }
}

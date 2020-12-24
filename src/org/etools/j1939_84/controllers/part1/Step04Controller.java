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
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 * The controller for DM24: SPN support
 */
public class Step04Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;
    private final OBDTestsModule obdTestsModule;
    private final SupportedSpnModule supportedSpnModule;

    Step04Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new OBDTestsModule(),
             new SupportedSpnModule(),
             dataRepository);
    }

    Step04Controller(Executor executor,
                     EngineSpeedModule engineSpeedModule,
                     BannerModule bannerModule,
                     VehicleInformationModule vehicleInformationModule,
                     OBDTestsModule obdTestsModule,
                     SupportedSpnModule supportedSpnModule,
                     DataRepository dataRepository) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.obdTestsModule = obdTestsModule;
        this.supportedSpnModule = supportedSpnModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        obdTestsModule.setJ1939(getJ1939());

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
        dataRepository.getObdModules().forEach(module -> {
            BusResult<DM24SPNSupportPacket> result = obdTestsModule.requestDM24(getListener(),
                                                                                module.getSourceAddress());
            result.getPacket().flatMap(packet -> packet.left).ifPresent(destinationSpecificPackets::add);
            // 6.1.4.2.a. Fail if retry was required to obtain DM24 response.
            if (result.isRetryUsed()) {
                addFailure("6.1.4.2.a - Retry was required to obtain DM24 response");
            }
        });
        // 6.1.4.1.c Create vehicle list of supported SPNs for data stream
        destinationSpecificPackets.forEach(p -> {
            OBDModuleInformation info = dataRepository.getObdModule(p.getSourceAddress());
            if (info != null) {
                // d. Create ECU specific list of supported SPNs for test results.
                info.setSupportedSpns(p.getSupportedSpns());
                dataRepository.putObdModule(p.getSourceAddress(), info);
            }
        });

        // 6.1.4.1.e. Create ECU specific list of supported freeze frame SPNs.
        List<Integer> freezeFrameSpns = dataRepository.getObdModules().stream()
                .map(OBDModuleInformation::getFreezeFrameSpns)
                .flatMap(Collection::stream)
                .map(SupportedSPN::getSpn)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        boolean freezeFrameOk = supportedSpnModule.validateFreezeFrameSpns(getListener(),
                                                                           freezeFrameSpns);
        // c. Fail if one or more minimum expected SPNs for freeze frame not
        // supported per section A.2, Criteria for Freeze Frame Evaluation, from the OBD ECU(s).
        if (!freezeFrameOk) {
            addFailure("6.1.4.2.c - One or more SPNs for freeze frame are not supported");
        }

        // 6.1.4.1.d. Create ECU specific list of supported SPNs for test results.
        List<Integer> dataStreamSpns = dataRepository.getObdModules().stream()
                .map(OBDModuleInformation::getDataStreamSpns)
                .flatMap(Collection::stream)
                .map(SupportedSPN::getSpn)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        boolean dataStreamOk = supportedSpnModule
                .validateDataStreamSpns(getListener(),
                                        dataStreamSpns,
                                        dataRepository.getVehicleInformation().getFuelType());
        // 6.1.4.2.b. Fail if one or more minimum expected SPNs for data stream
        // not supported per section A.1, Minimum Support Table, from the OBD ECU(s).
        if (!dataStreamOk) {
            addFailure("6.1.4.2.b - One or more SPNs for data stream is not supported");
        }
    }
}

/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.*;
import org.etools.j1939_84.utils.CollectionUtils;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.25 DM20: Monitor performance ratio
 */

public class Step25Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 25;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Step25Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new DiagnosticReadinessModule(),
                dataRepository,
                DateTimeModule.getInstance());
    }

    Step25Controller(Executor executor,
            EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule,
            DiagnosticReadinessModule diagnosticReadinessModule,
            DataRepository dataRepository,
                     DateTimeModule dateTimeModule) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dateTimeModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        diagnosticReadinessModule.setJ1939(getJ1939());
        // 6.1.25.1.a. DS DM20 (send Request (PGN 59904) for PGN 49664 to each OBD ECU.
        dataRepository.getObdModules().forEach(module -> {
            // Request DM20 from the module
            BusResult<DM20MonitorPerformanceRatioPacket> dm20BusResult = diagnosticReadinessModule
                    .requestDM20(getListener(), true, module.getSourceAddress());
            // 6.1.25.2.a. Fail if retry was required to obtain DM20 response.
            if (dm20BusResult.isRetryUsed()) {
                addFailure("6.1.25.2.a - Retry was required to obtain DM20 response:" + NL
                        + getAddressName(module.getSourceAddress())
                        + " required a retry when requesting its destination specific DM20");
            }
            // 6.1.25.1.b. If no response (transport protocol RTS or NACK(Busy) in 220
            // ms), then retry DS DM20 request to the OBD ECU. [Do not attempt
            // retry for NACKs that indicate not supported] - handled at the
            // j1939 request layer

            Optional<Either<DM20MonitorPerformanceRatioPacket, AcknowledgmentPacket>> packet = dm20BusResult.getPacket();
            if (packet.isPresent()) {
                Either<DM20MonitorPerformanceRatioPacket, AcknowledgmentPacket> ratio = packet.get();
                if (ratio.left.isPresent()) {
                    //6.1.25.1.a.i. Store ignition cycle counter value (SPN 3048) for later use.
                    module.setIgnitionCycleCounterValue(ratio.left.get().getIgnitionCycles());
                    // 6.1.25.2.b. Fail if any difference compared to data received during global request earlier
                    if (!CollectionUtils.areTwoCollectionsEqual(module.getPerformanceRatios(),
                            ratio.left.get().getRatios())) {
                        String failureMessage = "6.1.25.2.b - Difference compared to data received during global request earlier" + NL;
                        failureMessage += getAddressName(module.getSourceAddress());
                        failureMessage += " had a difference between stored performance ratios and destination specific requested DM20 response ratios";
                        addFailure(failureMessage);
                    }
                } else if (ratio.right.isEmpty() || ratio.right.get().getResponse() != Response.NACK) {
                    // 6.1.25.2.c. Fail if NACK not received from OBD ECUs that did not respond to global query in test 1.8.
                    addFailure("6.1.25.2.c - NACK not received from OBD ECUs that did not respond to global query");
                }
            } else {
                addWarning(getAddressName(module.getSourceAddress()) + " did not response to the DS20 request");
            }
        });
    }
}

/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.CollectionUtils;

/**
 * 6.1.25 DM20: Monitor performance ratio
 */
public class Part01Step25Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 25;
    private static final int TOTAL_STEPS = 0;

    Part01Step25Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step25Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
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
    }

    @Override
    protected void run() throws Throwable {
        // 6.1.25.1.a. DS DM20 (send Request (PGN 59904) for PGN 49664 to each OBD ECU.
        getDataRepository().getObdModules().forEach(module -> {
            // Request DM20 from the module
            var dm20BusResult = getCommunicationsModule().requestDM20(getListener(), module.getSourceAddress());

            // 6.1.25.2.a. Fail if retry was required to obtain DM20 response.
            String moduleName = module.getModuleName();
            if (dm20BusResult.isRetryUsed()) {
                addFailure("6.1.25.2.a - Retry was required to obtain DM20 response:" + NL
                        + moduleName + " required a retry when DS requesting DM20");
            }

            // 6.1.25.1.b. If no response, then retry DS DM20 request to the OBD ECU. [Do not attempt
            // retry for NACKs that indicate not supported] - handled at the j1939 request layer
            Optional<Either<DM20MonitorPerformanceRatioPacket, AcknowledgmentPacket>> optionalEither = dm20BusResult.getPacket();
            if (optionalEither.isPresent()) {
                Either<DM20MonitorPerformanceRatioPacket, AcknowledgmentPacket> result = optionalEither.get();
                Optional<DM20MonitorPerformanceRatioPacket> optionalLeft = result.left;
                if (optionalLeft.isPresent()) {
                    DM20MonitorPerformanceRatioPacket dm20 = optionalLeft.get();

                    // 6.1.25.2.b. Fail if any difference compared to data received during global request earlier
                    if (!CollectionUtils.areTwoCollectionsEqual(getRatios(module), dm20.getRatios())) {
                        String failureMessage = "6.1.25.2.b - Difference compared to data received during global request earlier"
                                + NL;
                        failureMessage += moduleName;
                        failureMessage += " had a difference between stored performance ratios and DS requested DM20 response ratios";
                        addFailure(failureMessage);
                    }

                    // 6.1.25.1.a.i. Store ignition cycle counter value (SPN 3048) for later use.
                    module.set(dm20, PART_NUMBER);
                    getDataRepository().putObdModule(module);
                } else {
                    Optional<AcknowledgmentPacket> optionalRight = result.right;
                    if (optionalRight.isEmpty() || optionalRight.get().getResponse() != Response.NACK) {
                        // 6.1.25.2.c. Fail if NACK not received from OBD ECUs that did not respond to global query in
                        // test 1.8.
                        addFailure("6.1.25.2.c - NACK not received from OBD ECUs that did not respond to global query");
                    }
                }
            } else {
                addWarning(moduleName + " did not respond to the DS DM20 request");
            }
        });
    }

    private static Collection<PerformanceRatio> getRatios(OBDModuleInformation module) {
        var dm20 = module.get(DM20MonitorPerformanceRatioPacket.class, 1);
        return dm20 == null ? List.of() : dm20.getRatios();
    }
}

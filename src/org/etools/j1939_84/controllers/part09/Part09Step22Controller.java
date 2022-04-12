/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static java.lang.String.format;
import static org.etools.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;;

/**
 * 6.9.22 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part09Step22Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 22;
    private static final int TOTAL_STEPS = 0;

    Part09Step22Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step22Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
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
        // 6.9.22.1.a. Global DM2 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM2(getListener())
                                                        .getPackets()
                                                        .stream()
                                                        .peek(this::save)
                                                        .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                                                        .peek(p -> {
                                                            // 6.9.22.2.a. (if supported) Fail if any ECU does not
                                                            // report MIL off or MIL not supported.
                                                            // See Section A.8 for allowed values.
                                                            if (isNotOff(p.getMalfunctionIndicatorLampStatus()) &&
                                                                    p.getMalfunctionIndicatorLampStatus() != NOT_SUPPORTED) {
                                                                addFailure(format("6.9.22.2.a - ECU %s reported MIL status of %s",
                                                                                  p.getModuleName(),
                                                                                  p.getMalfunctionIndicatorLampStatus()));
                                                            }
                                                            // 6.9.22.2.b. (if supported) Fail if any OBD ECU reports a
                                                            // previously active DTC. DM2 DTC are only previously active
                                                            // DTCs
                                                            if (p.hasDTCs()) {
                                                                addFailure(format("6.9.22.2.b - ECU %s reported a previously active DTC",
                                                                                  p.getModuleName()));

                                                            }
                                                        })
                                                        .collect(Collectors.toList());

        // 6.9.22.3.a. DS DM2 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(address -> getCommunicationsModule().requestDM2(getListener(),
                                                                                                address))
                                           .collect(Collectors.toList());

        // 6.9.22.4.a. (if supported) Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.9.22.4.a");
        // 6.9.22.4.b. (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.9.22.4.b");
    }
}

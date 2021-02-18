/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.12 DM24: SPNs Supported
 */
public class Part03Step12Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part03Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step12Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
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
    }

    @Override
    protected void run() throws Throwable {

        // 6.3.12.1.a. DS DM24 (send Request (PGN 59904) for PGN 64950 (SPNs 3297, 4100-4103)) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                .stream().map(address -> getDiagnosticMessageModule().requestDM24(getListener(), address))
                .collect(Collectors.toList());

        // 6.3.12.1.b. Compare response with responses received in part 1 test 4 for each OBD ECU.
        // 6.3.12.2.a. Fail if the message data received differs from that provided in part 1.
        filterPackets(dsResults).forEach(packet -> {
            int sourceAddress = packet.getSourceAddress();
            OBDModuleInformation info = getDataRepository().getObdModule(sourceAddress);
            if (!info.getSupportedSPNs().equals(packet.getSupportedSpns())) {
                addFailure("6.3.12.2.a - Message data received from " + Lookup.getAddressName(sourceAddress) + " differs from that provided in part 6.1.4");
            }
        });

        // 6.3.12.2.b. Fail if NACK not received from OBD ECUs that did not provide DM24
        checkForNACKsFromObdModules(filterPackets(dsResults), filterAcks(dsResults), "6.3.12.2.b");
    }

}

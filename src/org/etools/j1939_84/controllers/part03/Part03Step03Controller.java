/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.3.3 DM27: All pending DTCs
 */
public class Part03Step03Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part03Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step03Controller(Executor executor,
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
        // 6.3.3.1.a Global DM27 (send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 3038, 1706)).
        var dm27s = getCommunicationsModule().requestDM27(getListener()).getPackets();

        // 6.3.3.2.a Fail if (if supported) no ECU reports the same DTC observed in step 6.3.2.1 in a positive DM27
        // response.
        dm27s.forEach(packet -> {
            List<DiagnosticTroubleCode> dm27DTCs = packet.getDtcs();
            List<DiagnosticTroubleCode> dm6DTCs = getDTCs(packet.getSourceAddress());

            if (isNotSubset(dm6DTCs, dm27DTCs)) {
                addFailure("6.3.3.2.a - OBD ECU " + packet.getModuleName() +
                        " reported different DTC than observed in Step 6.3.2.1");
            }
        });

        dm27s.forEach(packet -> {
            List<DiagnosticTroubleCode> dm27DTCs = packet.getDtcs();
            List<DiagnosticTroubleCode> dm6DTCs = getDTCs(packet.getSourceAddress());

            // 6.3.3.3.a. Warn if (if supported) any ECU additional DTCs are provided than the DTC observed in step
            // 6.3.2.1 in a positive DM27 response.
            if (isNotSubset(dm27DTCs, dm6DTCs)) {
                addWarning("6.3.3.3.a - OBD ECU " + packet.getModuleName() +
                        " reported " + dm6DTCs.size() + " DTCs in response to DM6 in 6.3.2.1 and " +
                        dm27DTCs.size() + " DTCs when responding to DM27");
            }
        });

        dm27s.forEach(this::save);

        // 6.3.3.4.a DS DM27 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM27(getListener(), a))
                                           .map(BusResult::requestResult)
                                           .collect(Collectors.toList());

        // 6.3.3.5.a Fail if (if supported) any difference compared to data received with global request.
        compareRequestPackets(dm27s, filterRequestResultPackets(dsResults), "6.3.3.5.a");

        // 6.3.3.5.b Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(dm27s, filterRequestResultAcks(dsResults), "6.3.3.5.b");
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM6PendingEmissionDTCPacket.class, address, 3);
    }

}

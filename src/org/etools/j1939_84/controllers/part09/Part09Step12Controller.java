/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9.12 DM28: Permanent DTCs
 */
public class Part09Step12Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part09Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step12Controller(Executor executor,
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
        // 6.9.12.1.a. DS DM28 [(send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 1706, and 3038)]) to each OBD
        // ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM28(getListener(), a))
                                           .map(BusResult::requestResult)
                                           .collect(Collectors.toList());

        var dsPackets = filterRequestResultPackets(dsResults);

        dsPackets.forEach(this::save);

        // 6.9.12.2.a. Fail if no ECU reports a permanent DTC present.
        boolean noDTCs = dsPackets.stream().allMatch(p -> p.getDtcs().isEmpty());
        if (noDTCs) {
            addFailure("6.9.12.2.a - No OBD ECU reported a permanent DTC");
        }

        // 6.9.12.2.b. Fail if any ECU does not report MIL off. See Section A.8 for more information.
        dsPackets.stream()
                 .filter(p -> isNotOff(p.getMalfunctionIndicatorLampStatus()))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.9.12.2.b - " + moduleName + " did not report MIL 'off'");
                 });

        // 6.9.12.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM28 message.
        checkForNACKsDS(dsPackets, filterRequestResultAcks(dsResults), "6.9.12.2.c");

        // 6.9.12.3.a. Warn if permanent DTC is different than DM12 DTC earlier in this part.
        dsPackets.stream()
                 .filter(p -> p.getMalfunctionIndicatorLampStatus() != getMIL(p.getSourceAddress()))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.9.12.3.a - " + moduleName
                             + " reported different MIL status than DM12 response earlier in test 6.9.2.1.b");
                 });

    }

    private LampStatus getMIL(int moduleAddress) {
        var dm12 = get(DM12MILOnEmissionDTCPacket.class, moduleAddress, 9);
        return dm12 == null ? null : dm12.getMalfunctionIndicatorLampStatus();
    }
}

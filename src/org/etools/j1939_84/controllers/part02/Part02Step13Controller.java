/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.2.13 DM31: DTC to Lamp Association
 */
public class Part02Step13Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    Part02Step13Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new DiagnosticMessageModule());
    }

    Part02Step13Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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
        // 6.2.13.1.a. DS DM31 (send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117)) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM31(getListener(), a))
                                           .collect(Collectors.toList());

        List<DM31DtcToLampAssociation> dsPackets = filterRequestResultPackets(dsResults);

        // 6.2.13.2.a (if supported) Fail if any ECU does not report MIL off. See Section A.8 for allowed values.
        dsPackets.stream()
                 .filter(this::isMilNotOffAndNotAltOff)
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.2.13.2.a - " + moduleName + " did not report MIL 'off'");
                 });

        // b. Fail if NACK not received from OBD ECUs that did not provide DM31.
        checkForNACKsGlobal(dsPackets, filterRequestResultAcks(dsResults), "6.2.13.2.b");
    }

    private boolean isMilNotOffAndNotAltOff(DM31DtcToLampAssociation packet) {
        return packet.getDtcLampStatuses()
                     .stream()
                     .map(DTCLampStatus::getMalfunctionIndicatorLampStatus)
                     .anyMatch(this::isNotOff);
    }
}

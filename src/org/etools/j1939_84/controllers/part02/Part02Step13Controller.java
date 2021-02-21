/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

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
        // 6.2.13 DM31: DTC to Lamp Association
        // 6.2.13.1 Actions:
        // a. DS DM31 (send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117)) to each OBD ECU.
        List<DM31DtcToLampAssociation> dsPackets = new ArrayList<>();
        List<AcknowledgmentPacket> ackPackets = new ArrayList<>();
        getDataRepository().getObdModules().forEach(module -> {
            RequestResult<DM31DtcToLampAssociation> requestResult = getDiagnosticMessageModule()
                                                                                                .requestDM31(getListener(),
                                                                                                             module.getSourceAddress());
            if (!requestResult.getPackets().isEmpty()) {
                dsPackets.addAll(requestResult.getPackets());
            }
            if (!requestResult.getAcks().isEmpty()) {
                ackPackets.addAll(requestResult.getAcks());
            }
        });

        // 6.2.13.2 Fail criteria (if supported):
        dsPackets.stream()
                 .filter(packet -> isMilNotOffAndNotAltOff(packet))
                 .forEach(packet -> {
                     addFailure("6.2.13.2.a - ECU " + Lookup.getAddressName(packet.getSourceAddress())
                             + " reported MIL not off/alt-off");
                 });
        // b. Fail if NACK not received from OBD ECUs that did not provide DM31.
        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();
        checkForNACKs(dsPackets, ackPackets, obdModuleAddresses, "6.2.13.2.b");
    }

    private boolean isMilNotOffAndNotAltOff(DM31DtcToLampAssociation packet) {
        return packet.getDtcLampStatuses()
                     .stream()
                     .map(DTCLampStatus::getMalfunctionIndicatorLampStatus)
                     .anyMatch(lampStatus -> lampStatus != LampStatus.OFF
                             && lampStatus != LampStatus.ALTERNATE_OFF);
    }
}

/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.23 DM31: DTC to Lamp Association
 */

public class Part01Step23Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 23;
    private static final int TOTAL_STEPS = 0;

    Part01Step23Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(), DateTimeModule.getInstance());
    }

    Part01Step23Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
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
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.23.1.a. Global DM31 (send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117)).
        RequestResult<DM31DtcToLampAssociation> globalResponse = getDiagnosticMessageModule().requestDM31(getListener());

        globalResponse.getPackets().stream().filter(packet -> isMilNotOff(packet)).forEach(packet -> {
            // 6.1.23.2.a. Fail if any received ECU response does not report MIL off.
            addFailure("6.1.23.2.a - ECU " + Lookup.getAddressName(packet.getSourceAddress())
                               + " reported MIL light not off");
        });
    }
    private boolean isMilNotOff(DM31DtcToLampAssociation packet) {
        return packet.getDtcLampStatuses().stream()
                .map(DTCLampStatus::getMalfunctionIndicatorLampStatus)
                .anyMatch(lampStatus -> lampStatus != LampStatus.OFF);
    }
}

/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.15 DM21: Diagnostic readiness 2
 */
public class Part03Step15Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part03Step15Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step15Controller(Executor executor,
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
        // 6.3.15.1.a. DS DM21 (send Request (PGN 59904) for PGN 49408 (SPNs 3069, 3295)) to each OBD ECU.
        getDataRepository()
                .getObdModuleAddresses()
                .stream()
                .sorted()
                .forEach(address -> getDiagnosticMessageModule().requestDM21(getListener(), address)
                        .getPacket()
                        .ifPresentOrElse(packet -> {
                            if (packet.left.isPresent()) {
                                DM21DiagnosticReadinessPacket dm21 = packet.left.get();
                                if (dm21.getKmWhileMILIsActivated() > 0 || dm21.getMinutesWhileMILIsActivated() > 0) {
                                    // 6.3.15.2.a. Fail if any ECU reports distance (SPN 3069) or time (SPN 3295) with MIL on > 0.
                                    addFailure("6.3.15.2.a - OBD module " + dm21.getModuleName() + " reported active time or distance > 0");
                                }
                            } else if(packet.right.isPresent()){
                                AcknowledgmentPacket ackPacket = packet.right.get();
                                if(ackPacket.getResponse() != NACK){
                                    addFailure("6.3.15.2.b - OBD module " + getAddressName(address) + " did not respond to DS DM21 request");
                                }
                            }
                        }, () -> {
                            // 6.3.15.2.b. Fail if NACK not received from OBD ECUs that did not provide DM21 response to DS query.
                            addFailure("6.3.15.2.b - OBD module " + getAddressName(address) + " did not respond to DS DM21 request");
                        }));
    }

}

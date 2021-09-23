/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
 */
public class Part01Step10Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part01Step10Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             DateTimeModule.getInstance(),
             dataRepository);
    }

    protected Part01Step10Controller(Executor executor,
                                     EngineSpeedModule engineSpeedModule,
                                     BannerModule bannerModule,
                                     VehicleInformationModule vehicleInformationModule,
                                     CommunicationsModule communicationsModule,
                                     DateTimeModule dateTimeModule,
                                     DataRepository dataRepository) {
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

        // 6.1.10.1.a. Global DM11 (send Request (PGN 59904) for PGN 65235).
        // 6.1.10.1.b. Record all ACK/NACK/BUSY/Access Denied responses (for PGN 65235) in the log.
        // 6.1.10.1.c. Allow 5 s to elapse before proceeding with test step 6.1.10.2.
        long timeOut = 5;
        List<AcknowledgmentPacket> packets = getDiagnosticMessageModule().requestDM11(getListener(), timeOut, SECONDS)
                                                                         .stream()
                                                                         .filter(p -> isObdModule(p.getSourceAddress()))
                                                                         .collect(Collectors.toList());

        // 6.1.10.2.a. Fail if NACK received from any HD OBD ECU
        packets.stream()
               .filter(p -> p.getResponse() == NACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.1.10.2.a - The request for DM11 was NACK'ed by " + moduleName);
               });

        // 6.1.10.3.a. Warn if ACK received from any HD OBD ECU.
        packets.stream()
               .filter(p -> p.getResponse() == ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.1.10.3.a - The request for DM11 was ACK'ed by " + moduleName);
               });

    }

}

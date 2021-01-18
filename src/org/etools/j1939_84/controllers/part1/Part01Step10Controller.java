/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * 6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
 */
public class Part01Step10Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    private final DTCModule dtcModule;
    private final DataRepository dataRepository;
    private final DateTimeModule dateTimeModule;

    Part01Step10Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new DTCModule(),
                DateTimeModule.getInstance(),
                dataRepository);
    }

    protected Part01Step10Controller(Executor executor,
                                     EngineSpeedModule engineSpeedModule,
                                     BannerModule bannerModule,
                                     VehicleInformationModule vehicleInformationModule,
                                     DTCModule dtcModule,
                                     DateTimeModule dateTimeModule,
                                     DataRepository dataRepository) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dateTimeModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
        this.dtcModule = dtcModule;
        this.dateTimeModule = dateTimeModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        dtcModule.setJ1939(getJ1939());

        // 6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
        List<AcknowledgmentPacket> globalDM11Packets = dtcModule.requestDM11(getListener())
                .getAcks()
                .stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .collect(Collectors.toList());

        // c. Allow 5 s to elapse before proceeding with test step 6.1.10.2.
        long stopTime = dateTimeModule.getTimeAsLong() + (5L * 1000L);
        long secondsToGo;
        while (true) {
            secondsToGo = (stopTime - dateTimeModule.getTimeAsLong()) / 1000;
            if (secondsToGo > 0) {
                getListener().onProgress(String.format("Waiting for %1$d seconds", secondsToGo));
                dateTimeModule.pauseFor(1000);
            } else {
                break;
            }
        }

        // 6.1.10.2.a. Fail if NACK received from any HD OBD ECU
        boolean nacked = globalDM11Packets.stream().anyMatch(packet -> packet.getResponse() == Response.NACK);
        if (nacked) {
            addWarning(1, 10, "6.1.10.3.a - The request for DM11 was NACK'ed");
        }

        // 6.1.10.3.a. Warn if ACK received from any HD OBD ECU.16
        boolean acked = globalDM11Packets.stream().anyMatch(packet -> packet.getResponse() == Response.ACK);
        if (acked) {
            addWarning(1, 10, "6.1.10.3.a - The request for DM11 was ACK'ed");
        }
    }

}

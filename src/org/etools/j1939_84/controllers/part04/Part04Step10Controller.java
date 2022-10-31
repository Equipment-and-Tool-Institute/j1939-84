/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.4.10 DM25: Expanded Freeze Frame
 */
public class Part04Step10Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part04Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step10Controller(Executor executor,
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

        // 6.4.10.1.a a. DS DM25 [(send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214-1215)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM25(getListener(),
                                                                                           a,
                                                                                           get(DM24SPNSupportPacket.class,
                                                                                               a,
                                                                                               1)))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        packets.forEach(this::save);

        // 6.4.10.2.a Fail if no ECU reports freeze frame data.
        boolean hasFreezeFrame = packets.stream()
                                        .map(DM25ExpandedFreezeFrame::getFreezeFrames)
                                        .anyMatch(f -> !f.isEmpty());
        if (!hasFreezeFrame) {
            addFailure("6.4.10.2.a - No ECU reported freeze frame data");
        }

        // 6.4.10.2.b. Fail if DTC in freeze frame data does not include the DTC reported in DM12 earlier in this part.
        packets.forEach(p -> {
            List<DiagnosticTroubleCode> ffDTCs = p.getFreezeFrames()
                  .stream()
                  .map(f -> f.getDtc())
                  .collect(Collectors.toList());
            if (!ffDTCs.containsAll(getDTCs(p.getSourceAddress()))) {
                addFailure("6.4.10.2.b - " + p.getModuleName()
                        + " did not report DTC in freeze frame data which included the DTC reported in DM12 earlier in this part");
            }
        });

        // 6.4.10.2.c. Fail if NACK not received from OBD ECUs that did not provide DM25 response.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.4.10.2.c");
    }

    private List<DiagnosticTroubleCode> getDTCs(int moduleAddress) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, moduleAddress, 4);
    }

}

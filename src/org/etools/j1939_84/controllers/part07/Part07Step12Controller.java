/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.12 DM25: Expanded Freeze Frame
 */
public class Part07Step12Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part07Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step12Controller(Executor executor,
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
        // 6.7.12.1.a. DS DM25 ([send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214-1215)]) to each OBD ECU.
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

        // 6.7.12.2.a. Fail if no ECU reports Freeze Frame data.
        boolean hasFreezeFrame = packets.stream()
                                        .map(DM25ExpandedFreezeFrame::getFreezeFrames)
                                        .flatMap(Collection::stream)
                                        .findAny()
                                        .isPresent();
        if (!hasFreezeFrame) {
            addFailure("6.7.12.2.a - No ECU reported Freeze Frame data");
        }

        // 6.7.12.2.b. Fail if DTC in reported Freeze Frame data does not include the DTC provided by DM23 earlier in
        // this part.
        for (DM25ExpandedFreezeFrame dm25 : packets) {
            List<DiagnosticTroubleCode> ffDTCs = dm25.getFreezeFrames()
                                                     .stream()
                                                     .map(ff -> ff.getDtc())
                                                     .collect(Collectors.toList());
            if (!ffDTCs.containsAll(getDTCs(dm25.getSourceAddress()))) {
                addFailure("6.7.12.2.b - " + dm25.getModuleName()
                        + " did not reported DTC in Freeze Frame data which included the DTC provided by DM23 earlier in this part");
            }
        }

        // 6.7.12.2.c. Fail if NACK not received from OBD ECUs that did not provide DM25 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.7.12.2.c");

        // 6.7.12.3.a. Warn if more than one Freeze Frame is provided
        packets.stream()
               .filter(p -> p.getFreezeFrames().size() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.7.12.3.a - " + moduleName + " reported more than one Freeze Frame");
               });
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class, address, 7);
    }

}

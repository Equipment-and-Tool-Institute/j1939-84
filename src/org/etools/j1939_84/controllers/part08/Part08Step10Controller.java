/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import java.util.ArrayList;
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
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.8.10 DM25: Expanded Freeze Frame
 */
public class Part08Step10Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part08Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step10Controller(Executor executor,
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
        // 6.8.10.1.a. DS DM25 [(send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214, 1215)]) to each OBD ECU.
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

        // 6.8.10.2.a. Fail if DTC(s) reported in the freeze frame does not include either the DTC reported in DM12 or
        // the DTC reported in DM23 earlier in this part
        packets.forEach(pp -> {
            var ffDTCs = pp.getFreezeFrames().stream().map(ff -> ff.getDtc()).collect(Collectors.toList());
            if (!ffDTCs.containsAll(getDM12DTCs(pp.getSourceAddress()))
                    || !ffDTCs.containsAll(getDM23DTCs(pp.getSourceAddress()))) {
                addFailure("6.8.10.2.a - DTC(s) reported in the freeze frame by " + pp.getModuleName()
                        + " did not include either the DTC reported in DM12 or DM23 earlier in this part");
            }
        });

        // 6.8.10.2.b. Fail if no ECU provides freeze frame data
        boolean noFreezeFrames = packets.stream().allMatch(p -> p.getFreezeFrames().isEmpty());
        if (noFreezeFrames) {
            addFailure("6.8.10.2.b - No ECU provided freeze frame data");
        }

        // 6.8.10.2.c. Fail if NACK not received from OBD that did not provide an DM25 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.8.10.2.c");

        // 6.8.10.3.a. Warn if DTC reported by DM23 earlier in this part is not present in the freeze frame data.
        packets.forEach(p -> {
            List<DiagnosticTroubleCode> ffDTCs = p.getFreezeFrames()
                                                  .stream()
                                                  .map(ff -> ff.getDtc())
                                                  .collect(Collectors.toList());
            if (!ffDTCs.containsAll(getDM23DTCs(p.getSourceAddress())))
                addWarning("6.8.10.3.a - DTC(s) reported by DM23 earlier in this part is/are not present in the freeze frame data from "
                        + p.getModuleName());
        });
    }

    private List<DiagnosticTroubleCode> getDM23DTCs(int address) {
        return getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class, address, 8);
    }

    private List<DiagnosticTroubleCode> getDM12DTCs(int address) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, address, 8);
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        List<DiagnosticTroubleCode> dtcs = new ArrayList<>();
        dtcs.addAll(getDTCs(DM12MILOnEmissionDTCPacket.class, address, 8));
        dtcs.addAll(getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class, address, 8));
        return dtcs;
    }
}

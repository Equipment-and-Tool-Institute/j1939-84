/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.6.9 DM31: DTC to Lamp Association
 */
public class Part06Step09Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part06Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part06Step09Controller(Executor executor,
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
        // 6.6.9.1.a DS DM31 [(send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117)])
        // to ECU(s) reporting DM12 MIL on DTC active.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM31(getListener(), a))
                                           .collect(Collectors.toList());

        List<DM31DtcToLampAssociation> packets = filterRequestResultPackets(dsResults);

        // 6.6.9.2.a (if supported) Fail if any ECU response does not report same DTC as its own DM12 response.
        for (DM31DtcToLampAssociation packet : packets) {
            for (DiagnosticTroubleCode dtc : getDTCs(packet.getSourceAddress())) {
                if (packet.findLampStatusForDTC(dtc) == null) {
                    addFailure("6.6.9.2.a - " + packet.getModuleName()
                            + " did not report same DTC as its own DM12 response");
                    break;
                }
            }
        }

        // 6.6.9.2.b (if supported) Fail if any ECU response does not report MIL on for its own DM12 DTC.
        for (DM31DtcToLampAssociation packet : packets) {
            for (DiagnosticTroubleCode dtc : getDTCs(packet.getSourceAddress())) {
                DTCLampStatus lampStatus = packet.findLampStatusForDTC(dtc);
                if (lampStatus != null && lampStatus.getMalfunctionIndicatorLampStatus() != ON) {
                    addFailure("6.6.9.2.b - " + packet.getModuleName()
                            + " did not report MIL on for its own DM12 DTC");
                    break;
                }
            }
        }

        // 6.6.9.2.c (if supported) Fail if NACK not received from OBD ECUs that did not provide a DM31 message.
        checkForNACKsDS(packets, filterRequestResultAcks(dsResults), "6.6.9.2.c");
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, address, 6);
    }

}

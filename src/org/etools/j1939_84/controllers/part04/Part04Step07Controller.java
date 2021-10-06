/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.solidDesign.j1939.packets.LampStatus.ON;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.solidDesign.j1939.packets.DiagnosticTroubleCode;
import net.solidDesign.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.7 DM31: DTC to Lamp Association
 */
public class Part04Step07Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part04Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step07Controller(Executor executor,
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

        for (OBDModuleInformation obdModuleInformation : getDataRepository().getObdModules()) {
            var moduleAddress = obdModuleInformation.getSourceAddress();
            String moduleName = obdModuleInformation.getModuleName();

            // 6.4.7.1.a DS DM31 [(send Request (PGN 59904) for PGN 47128 (SPN 1214-1215, 4113, 4117)]) to each ECU
            // supporting DM12.
            if (getDTCPacket(moduleAddress) == null) {
                continue;
            }

            var response = getCommunicationsModule().requestDM31(getListener(), moduleAddress);

            var packets = response.getPackets();
            packets.forEach(this::save);

            if (packets.isEmpty()) {
                boolean isNacked = response.getAcks()
                                           .stream()
                                           .map(AcknowledgmentPacket::getResponse)
                                           .anyMatch(r -> r == NACK);
                if (!isNacked) {
                    // 6.4.7.2.b Fail if NACK not received from OBD ECU that did not provide DM31 response.
                    addFailure("6.4.7.2.b - OBD ECU " + moduleName + " did not provide a NACK for the DS query");
                }
            } else {
                // 6.4.7.2.a Fail if an OBD ECU does not include the same SPN and FMI from its DM12 response earlier in
                // this part
                // and report MIL on Status for that SPN and FMI in its DM31 response (if DM31 is supported).
                packets.forEach(dm31 -> {
                    for (DiagnosticTroubleCode dtc : getDTCs(moduleAddress)) {
                        var lampStatus = dm31.findLampStatusForDTC(dtc);
                        if (lampStatus == null) {
                            addFailure("6.4.7.2.a - " + moduleName
                                    + " did not include the same SPN and FMI from its DM12 response earlier in this part");
                        } else if (lampStatus.getMalfunctionIndicatorLampStatus() != ON) {
                            addFailure("6.4.7.2.a - " + moduleName
                                    + " did not report the same MIL on Status as the SPN and FMI from its DM12 response earlier in this part");
                        }
                    }
                });
            }

        }
    }

    private List<DiagnosticTroubleCode> getDTCs(int moduleAddress) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, moduleAddress, 4);
    }

    private DiagnosticTroubleCodePacket getDTCPacket(int moduleAddress) {
        return get(DM12MILOnEmissionDTCPacket.class, moduleAddress, 4);
    }

}

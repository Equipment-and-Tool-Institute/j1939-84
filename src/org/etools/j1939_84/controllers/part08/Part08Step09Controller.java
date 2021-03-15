/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.8.9 DM31: DTC to Lamp Association
 */
public class Part08Step09Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part08Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step09Controller(Executor executor,
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
        // 6.8.9.1.a. Global DM31 [(send Request (PGN 59904) for PGN 41728 (SPNs 1214, 1215, 4113, 4117)]).
        var packets = getDiagnosticMessageModule().requestDM31(getListener()).getPackets();

        packets.forEach(this::save);

        // 6.8.9.2.a. (if supported) Fail if no ECU reports same DTC as MIL on for as was reported in DM12 earlier
        // in this part.
        boolean noDM12Match = true;
        for (DM31DtcToLampAssociation dm31 : packets) {
            var dtcs = getDM12DTCs(dm31.getSourceAddress());
            for (DiagnosticTroubleCode dtc : dtcs) {
                var dtcLampStatus = dm31.findLampStatusForDTC(dtc);
                if (dtcLampStatus != null && dtcLampStatus.getMalfunctionIndicatorLampStatus() == ON) {
                    noDM12Match = false;
                }
            }
        }
        if (noDM12Match) {
            addFailure("6.8.9.2.a - No ECU reported same DTC as MIL on as reported in DM12 earlier in this part");
        }

        // 6.8.9.2.b. (if supported) Fail if any ECU reports additional or fewer DTCs than those reported in DM12 and
        // DM23 responses earlier in this part.
        packets.stream()
               .filter(p -> p.getDtcLampStatuses()
                             .size() != (getDM12DTCs(p.getSourceAddress()).size()
                                     + getDM23DTCs(p.getSourceAddress()).size()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.9.2.b - " + moduleName
                           + " reported additional or fewer DTCs than those reported in DM12 and DM23 responses earlier in this part");
               });

        // 6.8.9.2.c. (if supported) Fail if no ECU reports the same DTC as MIL off for the previous active DTC reported
        // in DM23 earlier in this part.
        boolean noDM23Match = true;
        for (DM31DtcToLampAssociation dm31 : packets) {
            var dtcs = getDM23DTCs(dm31.getSourceAddress());
            for (DiagnosticTroubleCode dtc : dtcs) {
                var dtcLampStatus = dm31.findLampStatusForDTC(dtc);
                if (dtcLampStatus != null && dtcLampStatus.getMalfunctionIndicatorLampStatus() == OFF) {
                    noDM23Match = false;
                }
            }
        }
        if (noDM23Match) {
            addFailure("6.8.9.2.c - No ECU reported same DTC as MIL off as reported in DM23 earlier in this part");
        }
    }

    private List<DiagnosticTroubleCode> getDM12DTCs(int address) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, address, 8);
    }

    private List<DiagnosticTroubleCode> getDM23DTCs(int address) {
        return getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class, address, 8);
    }

}

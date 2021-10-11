/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ON;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.soliddesign.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import net.soliddesign.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCode;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.6.4 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part06Step04Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part06Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part06Step04Controller(Executor executor,
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
        // 6.6.4.1.a Receive broadcast DM1 [(PGN 65226 (SPNs 1213-1215, 3038, 1706)]).
        List<DM1ActiveDTCsPacket> packets = getCommunicationsModule().readDM1(getListener())
                                                                        .stream()
                                                                        .filter(p -> isObdModule(p.getSourceAddress()))
                                                                        .collect(Collectors.toList());

        // 6.6.4.2.a Fail if no OBD ECU reports MIL on.
        boolean noMil = packets.stream()
                               .map(DiagnosticTroubleCodePacket::getMalfunctionIndicatorLampStatus)
                               .noneMatch(mil -> mil == ON);
        if (noMil) {
            addFailure("6.6.4.2.a - No OBD ECU reported MIL on");
        }

        // 6.6.4.2.b Fail the DTC provided by the OBD ECU in DM12 is not included in its DM1 display.
        packets.forEach(p -> {
            List<DiagnosticTroubleCode> dm12DTCs = getDTCs(p.getSourceAddress());
            List<DiagnosticTroubleCode> dm1DTCs = p.getDtcs();

            for (DiagnosticTroubleCode dtc : dm12DTCs) {
                if (!dm1DTCs.contains(dtc)) {
                    int spn = dtc.getSuspectParameterNumber();
                    int fmi = dtc.getFailureModeIndicator();
                    addFailure("6.6.4.2.b - The DTC (" + spn + ":" + fmi + ") provided by " + p.getModuleName()
                            + " in DM12 is not included in its DM1 display");
                }
            }
        });

        // 6.6.4.2.c Fail if any OBD ECU reports a different number of active DTCs than what that ECU reported in DM5
        // for number of active DTCs.
        packets.stream()
               .filter(p -> p.getDtcs().size() != getDM5ActiveCount(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.6.4.2.c - " + moduleName
                       + " reported a different number of active DTCs than what it reported in DM5 for number of active DTCs"));
    }

    private int getDM5ActiveCount(int moduleAddress) {
        var dm5 = get(DM5DiagnosticReadinessPacket.class, moduleAddress, 6);
        return dm5 == null ? -1 : dm5.getActiveCodeCount();
    }

    private List<DiagnosticTroubleCode> getDTCs(int moduleAddress) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, moduleAddress, 6);
    }

}

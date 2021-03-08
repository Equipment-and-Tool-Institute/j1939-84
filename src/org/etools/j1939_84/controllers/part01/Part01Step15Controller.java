/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.1.15 DM1: Active diagnostic trouble codes (DTCs)
 */
public class Part01Step15Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part01Step15Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step15Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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

        // 6.1.15.1.a. Gather broadcast DM1 data from all ECUs (PGN 65226)
        List<DM1ActiveDTCsPacket> packets = getDiagnosticMessageModule().readDM1(getListener());

        // 6.1.15.2.a. Fail if any OBD ECU reports an active DTC.
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(DiagnosticTroubleCodePacket::hasDTCs)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.1.15.2.a - OBD Module " + moduleName + " reported an active DTC");
               });

        // 6.1.15.2.b. Fail if any OBD ECU does not report MIL off. See section A.8
        // for allowed values
        // 6.1.15.3.a. Warn if any ECU reports the non-preferred MIL off format.
        // See section A.8 for description of (0b00, 0b00).
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> isNotOff(p.getMalfunctionIndicatorLampStatus())) // This warns for ALT_OFF
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.1.15.2.b - OBD Module " + moduleName + " did not report MIL 'off'");
               });

        // 6.1.15.2.c. Fail if any non-OBD ECU does not report MIL off or not
        // supported/ MIL status (per SAE J1939-73 Table 5).
        packets.stream()
               .filter(p -> !isObdModule(p.getSourceAddress()))
               .filter(p -> {
                   LampStatus mil = p.getMalfunctionIndicatorLampStatus();
                   return mil != OFF && mil != NOT_SUPPORTED;
               })
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.1.15.2.c - Non-OBD Module " + moduleName + " did not report MIL off or not supported");
               });

        // 6.1.15.2.d. Fail if any OBD ECU reports SPN conversion method (SPN 1706) equal to binary 1.
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(Part01Step15Controller::isConversionMethod1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.1.15.2.d - OBD Module " + moduleName
                           + " reported SPN conversion method (SPN 1706) equal to binary 1");
               });

        // 6.1.15.3.b. Warn if any non-OBD ECU reports SPN conversion method (SPN 1706) equal to 1.
        packets.stream()
               .filter(p -> !isObdModule(p.getSourceAddress()))
               .filter(Part01Step15Controller::isConversionMethod1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.1.15.3.b - Non-OBD Module " + moduleName
                           + " reported SPN conversion method (SPN 1706) equal to 1");
               });

        // 6.1.15.2.e Fail if no OBD ECU provides DM1
        boolean foundObdPacket = packets.stream().anyMatch(p -> isObdModule(p.getSourceAddress()));
        if (!foundObdPacket) {
            addFailure("6.1.15.2 - No OBD ECU provided a DM1");
        }
    }

    private static boolean isConversionMethod1(DM1ActiveDTCsPacket dm1) {
        return dm1.getDtcs().stream().anyMatch(p -> p.getConversionMethod() == 1);
    }
}

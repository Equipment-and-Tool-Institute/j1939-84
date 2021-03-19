/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
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
 * 6.4.5 DM23: Emission Related Previously Active DTCs
 */
public class Part04Step05Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part04Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step05Controller(Executor executor,
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
        // 6.4.5.1.a DS DM23 [send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 1706, and 3038)] to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM23(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        packets.forEach(this::save);

        // 6.4.5.2.a Fail if any ECU reports > 0 previously active DTC.
        packets.stream()
               .filter(DiagnosticTroubleCodePacket::hasDTCs)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.4.5.2.a - OBD ECU " + moduleName + " reported > 0 previously active DTC");
               });

        // 6.4.5.2.b Fail if any ECU reports a different MIL status than it did in DM12 response earlier in this part.
        packets.stream()
               .filter(p -> p.getMalfunctionIndicatorLampStatus() != getMILStatus(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.4.5.2.b - OBD ECU " + moduleName
                           + " reported a MIL status different from the DM12 response earlier in this part");
               });

        // 6.4.5.2.c Fail if NACK not received from OBD ECUs that did not provide DM23 response.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.4.5.2.c");

        // 6.4.5.2.d Fail if no OBD ECU provides DM23.
        if (packets.isEmpty()) {
            addFailure("6.4.5.2.d - No OBD ECU provided a DM23");
        }
    }

    private LampStatus getMILStatus(int address) {
        var dm12 = get(DM12MILOnEmissionDTCPacket.class, address, 4);
        return dm12 == null ? null : dm12.getMalfunctionIndicatorLampStatus();
    }
}

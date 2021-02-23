/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
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
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *         <p>
 *         The controller for 6.1.21 DM27: All Pending DTCs
 */

public class Part01Step21Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 21;
    private static final int TOTAL_STEPS = 0;

    Part01Step21Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step21Controller(Executor executor,
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

        // 6.1.21.1.a. Global DM27 (send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 3038, 1706)).
        List<DM27AllPendingDTCsPacket> globalPackets = getDiagnosticMessageModule().requestDM27(getListener())
                                                                                   .getPackets();

        // Save the packet for later use
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .forEach(p -> {
                         var obdModule = getDataRepository().getObdModule(p.getSourceAddress());
                         obdModule.set(p);
                         getDataRepository().putObdModule(obdModule);
                     });

        // 6.1.21.2.a. Fail if any OBD ECU reports an all pending DTC.
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getSourceAddress)
                     .map(Lookup::getAddressName)
                     .forEach(moduleName -> addFailure("6.1.21.2.a - " + moduleName + " reported an all pending DTC"));

        // 6.1.21.2.b. Fail if any ECU does not report MIL off
        globalPackets.stream()
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != LampStatus.OFF)
                     .map(ParsedPacket::getSourceAddress)
                     .map(Lookup::getAddressName)
                     .forEach(moduleName -> addFailure("6.1.21.2.b - " + moduleName + " did not report MIL off"));

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.1.21.3.a. DS DM28 to each OBD ECU.
        List<BusResult<DM27AllPendingDTCsPacket>> dsResults = obdModuleAddresses.stream()
                                                                                .map(address -> getDiagnosticMessageModule().requestDM27(getListener(),
                                                                                                                                         address))
                                                                                .collect(Collectors.toList());

        // 6.1.20.4.a. Fail if any difference compared to data received during global request.
        List<DM27AllPendingDTCsPacket> dsPackets = filterPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.1.21.4.a");

        // 6.1.20.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        List<AcknowledgmentPacket> dsAcks = filterAcks(dsResults);
        checkForNACKs(globalPackets, dsAcks, "6.1.21.4.b");
    }
}

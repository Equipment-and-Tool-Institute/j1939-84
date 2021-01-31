/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Garrison Garland {garrison@soliddesign.net)
 */
public class Part01Step16Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    Part01Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    protected Part01Step16Controller(Executor executor,
                                     EngineSpeedModule engineSpeedModule,
                                     BannerModule bannerModule,
                                     VehicleInformationModule vehicleInformationModule,
                                     DiagnosticMessageModule diagnosticMessageModule,
                                     DataRepository dataRepository,
                                     DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);

        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.16.1.a. Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706))
        RequestResult<DM2PreviouslyActiveDTC> globalResults = getDiagnosticMessageModule().requestDM2(getListener());

        // Get DM2PreviouslyActiveDTC so we can get DTCs and report accordingly
        List<DM2PreviouslyActiveDTC> globalPackets = globalResults.getPackets();

        // 6.1.16.2.a Fail if any OBD ECU reports a previously active DTC.
        globalPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> !p.getDtcs().isEmpty())
                .forEach(p -> {
                    String moduleName = Lookup.getAddressName(p.getSourceAddress());
                    addFailure("6.1.16.2.a - OBD ECU " + moduleName + " reported a previously active DTC");
                });

        // 6.1.16.2.b Fail if any OBD ECU does not report MIL (Malfunction Indicator Lamp) off
        globalPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                .forEach(p -> {
                    String moduleName = Lookup.getAddressName(p.getSourceAddress());
                    addFailure("6.1.16.2.b - OBD ECU " + moduleName + " did not report MIL off");
                });

        // 6.1.16.2.c Fail if any non-OBD ECU does not report MIL off or not supported - LampStatus of OTHER
        globalPackets.stream()
                .filter(p -> !dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> {
                    LampStatus milStatus = p.getMalfunctionIndicatorLampStatus();
                    return milStatus != OFF && milStatus != NOT_SUPPORTED;
                }).forEach(p -> {
            String moduleName = Lookup.getAddressName(p.getSourceAddress());
            addFailure("6.1.16.2.c - Non-OBD ECU " + moduleName + " did not report MIL off or not supported");
        });

        List<Integer> obdAddresses = dataRepository.getObdModuleAddresses();
        // 6.1.16.3.a DS DM2 to each OBD ECU
        List<BusResult<DM2PreviouslyActiveDTC>> dsResult = obdAddresses.stream()
                .map(address -> getDiagnosticMessageModule().requestDM2(getListener(), address))
                .collect(Collectors.toList());

        // 6.1.16.4.a Fail if any responses differ from global responses
        List<DM2PreviouslyActiveDTC> dsDM2s = filterPackets(dsResult);
        compareRequestPackets(globalPackets, dsDM2s, "6.1.16.4.a");

        // 6.1.16.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        List<AcknowledgmentPacket> dsAcks = filterAcks(dsResult);
        checkForNACKs(globalPackets, dsAcks, obdAddresses, "6.1.16.4.b");
    }
}
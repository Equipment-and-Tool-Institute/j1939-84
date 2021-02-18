/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.4 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part04Step04Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part04Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step04Controller(Executor executor,
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
        // 6.4.4.1.a Global DM2 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getDiagnosticMessageModule().requestDM2(getListener()).getPackets();

        // 6.4.4.2.a (if supported) Fail if any OBD ECU reports > 0 previously active DTCs.
        globalPackets.stream()
                .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                .filter(p -> p.getDtcs().size() > 0)
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.4.4.2.a - OBD ECU " + moduleName + " reported > 0 previously active DTCs"));

        // 6.4.4.2.b (if supported) Fail if any OBD ECU reports a different MIL status (e.g., on and flashing, or off) than it did in DM12 response earlier in this part.
        globalPackets.stream()
                .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                .filter(p -> getDataRepository().getObdModule(p.getSourceAddress())
                        .get(DM12MILOnEmissionDTCPacket.class) != null)
                .filter(p -> p.getMalfunctionIndicatorLampStatus() !=
                        getDataRepository().getObdModule(p.getSourceAddress())
                                .get(DM12MILOnEmissionDTCPacket.class)
                                .getMalfunctionIndicatorLampStatus())
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.4.4.2.b - OBD ECU " + moduleName + " reported a MIL status differing from DM12 response earlier in this part"));

        List<Integer> obdAddresses = getDataRepository().getObdModuleAddresses();

        // 6.4.4.3.a DS DM2 to each OBD ECU.
        var dsResult = obdAddresses.stream()
                .map(address -> getDiagnosticMessageModule().requestDM2(getListener(), address))
                .collect(Collectors.toList());

        // 6.4.4.4.a (if supported) Fail if any difference compared to data received from global request.
        List<DM2PreviouslyActiveDTC> dsPackets = filterPackets(dsResult);
        compareRequestPackets(globalPackets, dsPackets, "6.4.4.4.a");

        // 6.4.4.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
        List<AcknowledgmentPacket> dsAcks = filterAcks(dsResult);
        checkForNACKs(globalPackets, dsAcks, obdAddresses, "6.4.4.4.b");
    }

}
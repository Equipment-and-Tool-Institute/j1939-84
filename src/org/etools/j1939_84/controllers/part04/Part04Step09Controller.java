/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.4.9 DM27: All Pending DTCs
 */
public class Part04Step09Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part04Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step09Controller(Executor executor,
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

        var globalPackets = getCommunicationsModule().requestDM27(getListener()).getPackets();

        // 6.4.9.2.a (if supported) Fail if any ECU reports a pending DTC.
        globalPackets.stream()
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.4.9.2.a - " + moduleName + " reported a pending DTC");
                     });

        // 6.4.9.2.b (if supported) Fail if any ([OBD)] ECU reports a different MIL status than it did for DM12 response
        // earlier in this part.
        globalPackets.stream()
                     .filter(p -> getLampStatus(p.getSourceAddress()) != null)
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != getLampStatus(p.getSourceAddress()))
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.4.9.2.b - " + moduleName
                                 + " reported a different MIL status that it did for DM12 response earlier in this part");
                     });

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.4.9.3.a DS DM27 to each OBD ECU.
        var dsResults = obdModuleAddresses
                                          .stream()
                                          .map(a -> getCommunicationsModule().requestDM27(getListener(), a))
                                          .collect(Collectors.toList());

        // 6.4.9.4.a (if supported) Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.4.9.4.a");

        // 6.4.9.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.4.9.4.b");
    }

    private LampStatus getLampStatus(int moduleAddress) {
        var packet = (DiagnosticTroubleCodePacket) get(DM12MILOnEmissionDTCPacket.class, moduleAddress, 4);
        return packet == null ? null : packet.getMalfunctionIndicatorLampStatus();
    }
}

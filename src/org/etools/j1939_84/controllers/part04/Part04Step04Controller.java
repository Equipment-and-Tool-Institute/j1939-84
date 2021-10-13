/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.soliddesign.j1939tools.j1939.packets.LampStatus;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

;

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
             new CommunicationsModule());
    }

    Part04Step04Controller(Executor executor,
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
        // 6.4.4.1.a Global DM2 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM2(getListener()).getPackets();

        // 6.4.4.2.a (if supported) Fail if any OBD ECU reports > 0 previously active DTCs.
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getDtcs().size() > 0)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.4.4.2.a - OBD ECU " + moduleName + " reported > 0 previously active DTCs");
                     });

        // 6.4.4.2.b (if supported) Fail if any OBD ECU reports a different MIL status (e.g., on and flashing, or off)
        // than it did in DM12 response earlier in this part.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> getDM12(p.getSourceAddress()) != null)
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != getMILStatus(p.getSourceAddress()))
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.4.4.2.b - OBD ECU " + moduleName
                                 + " reported a MIL status differing from DM12 response earlier in this part");
                     });

        // 6.4.4.3.a DS DM2 to each OBD ECU.
        var dsResult = getDataRepository().getObdModuleAddresses()
                                          .stream()
                                          .map(a -> getCommunicationsModule().requestDM2(getListener(), a))
                                          .collect(Collectors.toList());

        // 6.4.4.4.a (if supported) Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterPackets(dsResult), "6.4.4.4.a");

        // 6.4.4.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResult), "6.4.4.4.b");
    }

    private LampStatus getMILStatus(int sourceAddress) {
        var dm12 = getDM12(sourceAddress);
        return dm12 == null ? null : dm12.getMalfunctionIndicatorLampStatus();
    }

    private DM12MILOnEmissionDTCPacket getDM12(int address) {
        return get(DM12MILOnEmissionDTCPacket.class, address, 4);
    }

}

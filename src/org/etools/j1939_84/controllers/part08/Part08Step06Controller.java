/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.8.6 DM5: Diagnostic Readiness 1
 */
public class Part08Step06Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;
    private static final byte NA = (byte) 0xFF;

    Part08Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step06Controller(Executor executor,
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
        // 6.8.6.1.a Global DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1218-1223)]).
        var obdPackets = getCommunicationsModule().requestDM5(getListener())
                                                     .getPackets()
                                                     .stream()
                                                     .filter(p -> isObdModule(p.getSourceAddress()))
                                                     .collect(Collectors.toList());

        // 6.8.6.2.a Fail if any OBD ECU reports different number of DTCs than corresponding DM1 response earlier
        // this part.
        obdPackets.stream()
                  .filter(p -> p.getActiveCodeCount() != dm1Count(p.getSourceAddress()))
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      addFailure("6.8.6.2.a - " + moduleName
                              + " reported different number of DTCs than correspond DM1 response earlier in this part");
                  });

        // 6.8.6.2.a Fail if any OBD ECU reports different number of DTCs than corresponding DM2 response earlier
        // this part. [Ignore previously active count when DM2 is not supported.]
        obdPackets.stream()
                  .filter(p -> dm2Count(p.getSourceAddress()) != NA)
                  .filter(p -> p.getPreviouslyActiveCodeCount() != dm2Count(p.getSourceAddress()))
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      addFailure("6.8.6.2.a - " + moduleName
                              + " reported different number of DTCs than correspond DM2 response earlier in this part");
                  });

        // 6.8.6.3.a DS DM5 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM5(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.8.6.4.a Fail if any difference in data compared to global response.
        compareRequestPackets(obdPackets, filterPackets(dsResults), "6.8.6.4.a");
    }

    private byte dm1Count(int address) {
        return getDTCCount(DM1ActiveDTCsPacket.class, address);
    }

    private byte dm2Count(int address) {
        return getDTCCount(DM2PreviouslyActiveDTC.class, address);
    }

    private byte getDTCCount(Class<? extends DiagnosticTroubleCodePacket> clazz, int address) {
        DiagnosticTroubleCodePacket packet = get(clazz, address, 8);
        return packet == null ? NA : (byte) packet.getDtcs().size();
    }

}

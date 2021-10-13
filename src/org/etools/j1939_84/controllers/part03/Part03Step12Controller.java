/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.12 DM24: SPNs Supported
 */
public class Part03Step12Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part03Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step12Controller(Executor executor,
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

        // 6.3.12.1.a. DS DM24 (send Request (PGN 59904) for PGN 64950 (SPNs 3297, 4100-4103)) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM24(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);

        // Print the Freeze Frame SPNs in order to aid in debugging next Test
        packets.stream()
               .filter(p -> !p.getFreezeFrameSPNsInOrder().isEmpty())
               .forEach(packet -> {
                   getListener().onResult(packet.printFreezeFrameSPNsInOrder());
               });

        // 6.3.12.1.b. Compare response with responses received in part 1 test 4 for each OBD ECU.
        // 6.3.12.2.a. Fail if the message data received differs from that provided in part 1.
        packets.stream()
               .filter(p -> !p.getSupportedSpns().equals(getPart1SPNs(p.getSourceAddress())))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.3.12.2.a - Message data received from " + moduleName
                           + " differs from that provided in part 6.1.4");
               });

        // 6.3.12.2.b. Fail if NACK not received from OBD ECUs that did not provide DM24
        checkForNACKsDS(packets, filterAcks(dsResults), "6.3.12.2.b");
    }

    private List<SupportedSPN> getPart1SPNs(int sourceAddress) {
        OBDModuleInformation info = getDataRepository().getObdModule(sourceAddress);
        return info == null ? List.of() : info.getSupportedSPNs();
    }

}

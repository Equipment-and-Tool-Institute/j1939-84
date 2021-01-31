/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 * <p>
 * The controller for DM24: SPN support
 */
public class Part02Step03Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;
    private final DiagnosticMessageModule diagnosticMessageModule;

    Part02Step03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step03Controller(Executor executor,
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
              new DiagnosticMessageModule(), dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.diagnosticMessageModule = diagnosticMessageModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        diagnosticMessageModule.setJ1939(getJ1939());

        // 6.2.3.1.a. DS DM24 (send Request (PGN 59904) for PGN 64950 (SPNs 3297, 4100-4103)) to each OBD ECU.
        List<DM24SPNSupportPacket> packets = dataRepository.getObdModuleAddresses()
                .stream()
                .sorted()
                .map(address -> diagnosticMessageModule.requestDM24(getListener(), address))
                .map(BusResult::getPacket)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(p -> p.left)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // 6.2.3.2.a. Fail if the message data received differs from that provided in part 6.1.4
        packets.forEach(packet -> {
            int sourceAddress = packet.getSourceAddress();
            OBDModuleInformation info = dataRepository.getObdModule(sourceAddress);
            if (info != null) {
                List<SupportedSPN> part1SPNs = info.getSupportedSpns();
                List<SupportedSPN> part2SPNs = packet.getSupportedSpns();
                if (!part1SPNs.equals(part2SPNs)) {
                    addFailure("6.2.3.2.a - Message data received from " + Lookup.getAddressName(sourceAddress) + " differs from that provided in part 6.1.4");
                }
            }
        });

    }
}

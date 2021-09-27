/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.solidDesign.j1939.packets.DM20MonitorPerformanceRatioPacket;
import net.solidDesign.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9.11 DM20: Monitor Performance Ratio
 */
public class Part09Step11Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part09Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step11Controller(Executor executor,
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
        // 6.9.11.1.a. DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPNs 3048-3049, 3066-3068)]) to ECUs that
        // responded earlier in this part with DM20 data.
        List<Integer> addresses = getDataRepository().getObdModules()
                                                     .stream()
                                                     .map(OBDModuleInformation::getSourceAddress)
                                                     .filter(a -> getDM20(a) != null)
                                                     .collect(Collectors.toList());

        var dsResults = addresses.stream()
                                 .map(a -> getCommunicationsModule().requestDM20(getListener(), a))
                                 .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        var acks = filterAcks(dsResults);

        // 6.9.11.2.a. Fail if ignition cycle is not equal to the value that it was earlier in Step 6.9.4.1.b
        // (before DM11).
        packets.stream()
               .filter(p -> getDM20(p.getSourceAddress()) != null)
               .filter(p -> getDM20(p.getSourceAddress()).getIgnitionCycles() != p.getIgnitionCycles())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.11.2.a - " + moduleName
                           + " reported value for ignition cycles is not equal to the value from Step 6.9.4.1.b");
               });

        // 6.9.11.2.a. Fail if numerator or denominator is not equal to the value that it was earlier in Step 6.9.4.1.b
        // (before DM11).
        packets.stream()
               .filter(p -> getDM20(p.getSourceAddress()) != null)
               .filter(p -> !getDM20(p.getSourceAddress()).getRatios().equals(p.getRatios()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.11.2.a - " + moduleName
                           + " reported values for performance ratios not equal to the values from Step 6.9.4.1.b");
               });

        // 6.9.11.2.b. Fail if any ECU now NACKs DM20 requests after previously providing data in 6.9.4.1.
        acks.stream()
            .filter(a -> a.getResponse() == NACK)
            .filter(a -> getDM20(a.getSourceAddress()) != null)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addFailure("6.9.11.2.b - " + moduleName
                        + " now NACK'd DM20 request after previously providing data in 6.9.4.1");
            });

        // 6.9.11.2.c. Fail if any NACK not received from an OBD ECU that did not provide a DM20 message.
        checkForNACKsDS(packets, acks, "6.9.11.2.c", addresses);
    }

    private DM20MonitorPerformanceRatioPacket getDM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 9);
    }

}

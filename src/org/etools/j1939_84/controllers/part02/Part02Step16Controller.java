/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.NOT_AVAILABLE;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.OUTSIDE;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DM34NTEStatus;
import org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.16 DM34: NTE status
 */
public class Part02Step16Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    Part02Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    Part02Step16Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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
        // 6.2.16.1.a. Global DM34 (send Request (PGN 59904) for PGN 40960 (SPNs 4127-4132)).
        var globalPackets = getCommunicationsModule().requestDM34(getListener()).getPackets();

        // 6.2.16.2.a. Fail if no ECU responds, unless the user selected SI technology.
        if (globalPackets.isEmpty() && !isSparkIgnition()) {
            addFailure("6.2.16.2.a - No ECU responded to the global request");
        }

        // 6.2.16.2.b. Fail if any ECU response is not = 0b00 (Outside Control Area) for NOx control area
        globalPackets.stream()
                     .filter(p -> p.getNoxNTEControlAreaStatus() != OUTSIDE)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.b - " + moduleName + " reported NOx control area != 0b00");
                     });

        // 6.2.16.2.b. Fail if any ECU response is not = 0b00 (Outside Control Area) for PM control area
        globalPackets.stream()
                     .filter(p -> p.getPmNTEControlAreaStatus() != OUTSIDE)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.b - " + moduleName + " reported PM control area != 0b00");
                     });

        // 6.2.16.2.c. Fail if any ECU response is not = 0b00 (Outside Area) or 0b11 (not available) for NOx carve-out
        // area.
        globalPackets.stream()
                     .filter(p -> {
                         AreaStatus area = p.getNoxNTECarveOutAreaStatus();
                         return area != OUTSIDE && area != NOT_AVAILABLE;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.c - " + moduleName + " reported NOx carve-out area != 0b00 or 0b11");
                     });

        // 6.2.16.2.c. Fail if any ECU response is not = 0b00 (Outside Area) or 0b11 (not available) for NOx deficiency
        // area.
        globalPackets.stream()
                     .filter(p -> {
                         AreaStatus area = p.getNoxNTEDeficiencyAreaStatus();
                         return area != OUTSIDE && area != NOT_AVAILABLE;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.c - " + moduleName + " reported NOx deficiency area != 0b00 or 0b11");
                     });

        // 6.2.16.2.c. Fail if any ECU response is not = 0b00 (Outside Area) or 0b11 (not available) for PM carve-out
        // area
        globalPackets.stream()
                     .filter(p -> {
                         AreaStatus area = p.getPmNTECarveOutAreaStatus();
                         return area != OUTSIDE && area != NOT_AVAILABLE;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.c - " + moduleName + " reported PM carve-out area != 0b00 or 0b11");
                     });

        // 6.2.16.2.c. Fail if any ECU response is not = 0b00 (Outside Area) or 0b11 (not available) for PM deficiency
        // area
        globalPackets.stream()
                     .filter(p -> {
                         AreaStatus area = p.getPmNTEDeficiencyAreaStatus();
                         return area != OUTSIDE && area != NOT_AVAILABLE;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.c - " + moduleName + " reported PM deficiency area != 0b00 or 0b11");
                     });

        // 6.2.16.2.d. Fail if any ECU response is not = 0b11 for byte 1 bits 1-2 and for byte 2 bits 1-2.
        globalPackets.stream()
                     .filter(p -> (p.getPacket().get(0) & 0x03) != 0x03)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.d - " + moduleName + " reported byte 1 bit 1-2 != 0b11");
                     });

        // 6.2.16.2.d. Fail if any ECU response is not = 0b11 for byte 2 bits 1-2.
        globalPackets.stream()
                     .filter(p -> (p.getPacket().get(1) & 0x03) != 0x03)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.d - " + moduleName + " reported byte 2 bit 1-2 != 0b11");
                     });

        // 6.2.16.2.e. Fail if any reserved bytes 3-8 are not = 0xFF.
        globalPackets.stream()
                     .map(ParsedPacket::getPacket)
                     .filter(p -> Arrays.stream(p.getData(2, 7)).anyMatch(d -> d != 0xFF))
                     .map(Packet::getSource)
                     .map(Lookup::getAddressName)
                     .forEach(moduleName -> {
                         addFailure("6.2.16.2.d - " + moduleName + " reported reserve bytes 3-8 != 0xFF");
                     });

        var obdAddresses = getDataRepository().getObdModules()
                                              .stream()
                                              .filter(m -> m.get(DM34NTEStatus.class, 1) != null)
                                              .map(OBDModuleInformation::getSourceAddress)
                                              .collect(Collectors.toList());

        // 6.2.16.3.a. DS DM34 to each OBD ECU which responded to the DM34 global request in step 1.
        var dsResponses = obdAddresses
                                      .stream()
                                      .map(a -> getCommunicationsModule().requestDM34(getListener(), a))
                                      .collect(Collectors.toList());

        // 6.2.16.4.a. Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterRequestResultPackets(dsResponses), "6.2.16.4.a");

        // 6.2.16.4.b. Fail if NACK received from OBD ECUs that responded to the global query in part 1.
        checkForNACKsDS(filterRequestResultPackets(dsResponses),
                        filterRequestResultAcks(dsResponses),
                        "6.2.16.4.b",
                        obdAddresses);
    }
}

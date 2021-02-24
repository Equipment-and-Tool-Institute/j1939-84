/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.2.12 DM29: Regulated DTC counts
 */
public class Part02Step12Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part02Step12Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new DiagnosticMessageModule());
    }

    Part02Step12Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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
        // 6.2.12.1.a. Global DM29 (send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)).
        List<DM29DtcCounts> globalPackets = getDiagnosticMessageModule().requestDM29(getListener()).getPackets();

        // 6.2.12.2.a. For OBD ECUs that did support DM27 in step 6.2.10, fail if any ECU does not report
        // pending/all pending/MIL on/previously MIL on/permanent = 0/0/0/0/0.
        globalPackets.stream()
                     .filter(p -> p.hasNonZeroCounts(true))
                     .map(ParsedPacket::getSourceAddress)
                     .filter(this::supportsDM27)
                     .map(Lookup::getAddressName)
                     .forEach(moduleName -> addFailure("6.2.12.2.a - " + moduleName
                             + " did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0"));

        // 6.2.12.2.b. For OBD ECUs that did not support DM27 in step 6.2.10, fail if any ECU does not report
        // pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0.
        globalPackets.stream()
                     .filter(p -> p.hasNonZeroCounts(false))
                     .map(ParsedPacket::getSourceAddress)
                     .filter(address -> !supportsDM27(address))
                     .map(Lookup::getAddressName)
                     .forEach(moduleName -> addFailure("6.2.12.2.b - " + moduleName
                             + " did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0"));

        // 6.2.12.2.c. For non-OBD ECUs, fail if any ECU reports any pending, MIL-on, previously MIL-on,
        // or permanent DTC count that is greater than 0
        globalPackets.stream()
                     .filter(p -> p.hasNonZeroCounts(null))
                     .map(ParsedPacket::getSourceAddress)
                     .filter(address -> !getDataRepository().isObdModule(address))
                     .map(Lookup::getAddressName)
                     .forEach(moduleName -> addFailure("6.2.12.2.c - A non-OBD ECU " + moduleName
                             + " reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0"));

        // 6.2.12.2.d. Fail if no OBD ECU provides DM29.
        boolean noObdResponses = globalPackets
                                              .stream()
                                              .map(ParsedPacket::getSourceAddress)
                                              .filter(getDataRepository()::isObdModule)
                                              .findAny()
                                              .isEmpty();
        if (noObdResponses) {
            addFailure("6.2.12.2.d - No OBD ECU provided DM29");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.2.12.3.a. DS DM29 to each OBD ECU.
        List<BusResult<DM29DtcCounts>> dsResults = obdModuleAddresses.stream()
                                                                     .map(address -> getDiagnosticMessageModule().requestDM29(getListener(),
                                                                                                                              address))
                                                                     .collect(Collectors.toList());

        // 6.2.12.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.2.12.4.a");

        // 6.2.12.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.2.12.4.b");
    }

}

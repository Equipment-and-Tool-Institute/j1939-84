/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9.13 DM31: DTC to Lamp Association
 */
public class Part09Step13Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    Part09Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step13Controller(Executor executor,
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
        // 6.9.13.1.a. DS DM31 [(send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117)]) to each ECU(s)
        // that has any DM28 permanent DTCs.
        var addresses = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .filter(a -> !getDTCs(DM28PermanentEmissionDTCPacket.class, a, 9).isEmpty())
                                           .collect(Collectors.toList());

        var results = addresses.stream()
                               .map(a -> getDiagnosticMessageModule().requestDM31(getListener(), a))
                               .collect(Collectors.toList());

        var packets = filterRequestResultPackets(results);

        // 6.9.13.2.a. (if supported) Fail if MIL is not reported off for all reported DTCs.
        for (int address : addresses) {
            packets.stream()
                   .filter(p -> p.getSourceAddress() == address)
                   .map(DM31DtcToLampAssociation::getDtcLampStatuses)
                   .flatMap(Collection::stream)
                   .filter(s -> s.getMalfunctionIndicatorLampStatus() != OFF)
                   .map(s -> Lookup.getAddressName(address))
                   .forEach(moduleName -> {
                       addFailure("6.9.13.2.a - " + moduleName + " reported MIL not off for all reported DTCs");
                   });
        }

        // 6.9.13.2.b. (if supported) Fail if NACK not received from OBD ECUs that did not provide a DM31 message.
        checkForNACKsDS(packets, filterRequestResultAcks(results), "6.9.13.2.b", addresses);
    }

}

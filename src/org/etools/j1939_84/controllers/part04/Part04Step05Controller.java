/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.5 DM23: Emission Related Previously Active DTCs
 */
public class Part04Step05Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part04Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step05Controller(Executor executor,
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
        // 6.4.5.1.a DS DM23 [(send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 1706, and 3038)]) to each OBD ECU.
        List<BusResult<DM23PreviouslyMILOnEmissionDTCPacket>> dsResults = new ArrayList<>();
        getDataRepository().getObdModuleAddresses().forEach(address -> {
            BusResult<DM23PreviouslyMILOnEmissionDTCPacket> result = getDiagnosticMessageModule().requestDM23(
                    getListener(),
                    address);
            result.getPacket().ifPresentOrElse(p -> {
                dsResults.add(result);
                if(p.left.isPresent()){
                    DM23PreviouslyMILOnEmissionDTCPacket dm23 = p.left.get();
                    // 6.4.5.2.a Fail if any ECU reports > 0 previously active DTC.
                    if (dm23.getDtcs().size() > 0) {
                        addFailure("6.4.5.2.a - OBD module " + dm23.getModuleName() + " reported active distance > 0");
                    }
                }
                if(p.right.isPresent()){
                    AcknowledgmentPacket ackPacket = p.right.get();
                    if (ackPacket.getResponse() != NACK) {
                        addFailure("6.4.5.2.c - NACK not received from  " + getAddressName(ackPacket.getSourceAddress()) + " and did not provide a response to DS DM21 query");
                    }
                }
            }, () -> {
                // 6.4.5.2.c Fail if NACK not received from OBD ECUs that did not provide DM23 response.
                addFailure("6.4.5.2.c - NACK not received from  " + getAddressName(address) + " and did not provide a response to DS DM21 query");
            });
        });
        // 6.4.5.2.d Fail if no OBD ECU provides DM23.
        if (dsResults.isEmpty()) {
            addFailure("6.4.5.2.d - No OBD module provided a DM23");
        }
    }
}
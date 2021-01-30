/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.19 DM23: Emission Related Previously Active DTCs
 */

public class Part01Step19Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 19;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Part01Step19Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step19Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DTCModule dtcModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }


    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());

        // 6.1.19.1.a. Global DM23 (send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 3038, 1706)).
        RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> globalResponse = dtcModule.requestDM23(getListener());
        // 6.1.19.1.c. Fail if no OBD ECU provides DM23.
        List<DM23PreviouslyMILOnEmissionDTCPacket> globalPackets = globalResponse.getPackets();
        if (globalPackets.isEmpty()) {
            addFailure("6.1.19.2.c - No OBD ECU provided DM23");
        } else {
            for (DM23PreviouslyMILOnEmissionDTCPacket packet : globalPackets) {
                // 6.1.19.2.a. Fail if any ECU reports previously active DTCs.
                if (!packet.getDtcs().isEmpty()) {
                    addFailure("6.1.19.2.a - An ECU reported active DTCs");
                    break;
                }
            }
            for (DM23PreviouslyMILOnEmissionDTCPacket packet : globalPackets) {
                // 6.1.19.2.b. Fail if any ECU does not report MIL off.
                if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                    addFailure("6.1.19.2.b - An ECU did not report MIL off");
                    break;
                }
            }
        }

        // 6.1.19.3.a. DS DM23 to each OBD ECU.
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        List<BusResult<DM23PreviouslyMILOnEmissionDTCPacket>> dsResults = obdModuleAddresses.stream()
                .map(address -> dtcModule.requestDM23(getListener(), address))
                .collect(Collectors.toList());

        // 6.1.19.4.a. Fail if any difference compared to data received during global request.
        List<DM23PreviouslyMILOnEmissionDTCPacket> dsPackets = filterPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.1.19.4.a");

        // 6.1.19.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        List<AcknowledgmentPacket> dsAcks = filterAcks(dsResults);
        checkForNACKs(globalPackets, dsAcks, obdModuleAddresses, "6.1.19.4.b");
    }
}

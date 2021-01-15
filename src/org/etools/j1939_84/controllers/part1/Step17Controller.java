/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
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
 * The controller for 6.1.17 DM6: Emission related pending DTCs
 */

public class Step17Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 17;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step17Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Step17Controller(Executor executor,
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

        // 6.1.17.1.a. Global DM6 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706)).
        RequestResult<DM6PendingEmissionDTCPacket> globalResponse = dtcModule.requestDM6(getListener());

        List<DM6PendingEmissionDTCPacket> globalPackets = globalResponse.getPackets();
        // 6.1.17.2.c. Fail if no OBD ECU provides DM6.
        if (globalPackets.isEmpty()) {
            addFailure("6.1.17.2.c - No OBD ECU provided DM6");
        } else {
            for (DM6PendingEmissionDTCPacket packet : globalPackets) {
                // 6.1.17.2.a. Fail if any ECU reports pending DTCs
                if (!packet.getDtcs().isEmpty()) {
                    addFailure("6.1.17.2.a - An ECU reported pending DTCs");
                    break;
                }
            }
            for (DM6PendingEmissionDTCPacket packet : globalPackets) {
                // 6.1.17.2.b. Fail if any ECU does not report MIL off.
                if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                    addFailure("6.1.17.2.b - An ECU did not report MIL off");
                    break;
                }
            }
        }

        // 6.1.17.3.a. DS DM6 to each OBD ECU.
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        List<RequestResult<DM6PendingEmissionDTCPacket>> dsResults = obdModuleAddresses.stream()
                .map(address -> dtcModule.requestDM6(getListener(), address))
                .collect(Collectors.toList());

        // 6.1.17.4.a. Fail if any difference compared to data received during global request.
        List<DM6PendingEmissionDTCPacket> dsPackets = filterRequestResultPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.1.17.4.a");

        // 6.1.17.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        List<AcknowledgmentPacket> dsAcks = filterRequestResultAcks(dsResults);
        checkForNACKs(globalPackets, dsAcks, obdModuleAddresses, "6.1.17.4.b");
    }
}

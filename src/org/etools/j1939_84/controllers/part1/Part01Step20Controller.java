/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
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
 * The controller for 6.1.20 DM28: Permanent DTCs DTCs
 */

public class Part01Step20Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 20;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Part01Step20Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new DTCModule(),
                dataRepository,
                DateTimeModule.getInstance());
    }

    Part01Step20Controller(Executor executor,
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

        // 6.1.20.1.a. Global DM28 for PGN 64896
        RequestResult<DM28PermanentEmissionDTCPacket> globalResponse = dtcModule.requestDM28(getListener());

        List<DM28PermanentEmissionDTCPacket> globalPackets = globalResponse.getPackets();

        // 6.1.20.2.c. Fail if no OBD ECU provides DM28
        if (globalPackets.isEmpty()) {
            addFailure("6.1.20.2.c - No OBD ECU provided a DM28");
        } else {
            for (DM28PermanentEmissionDTCPacket packet : globalPackets) {
                // 6.1.20.2.a. Fail if any ECU reports a permanent DTC
                if (!packet.getDtcs().isEmpty()) {
                    addFailure("6.1.20.2.a - An ECU reported permanent DTCs");
                    break;
                }
            }
            for (DM28PermanentEmissionDTCPacket packet : globalPackets) {
                // 6.1.20.2.b. Fail if any ECU does not report MIL off
                if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                    addFailure("6.1.20.2.b - An ECU did not report MIL off");
                    break;
                }
            }
        }

        // 6.1.20.3.a. DS DM28 to each OBD ECU.
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        List<BusResult<DM28PermanentEmissionDTCPacket>> dsResults = obdModuleAddresses.stream()
                .map(address -> dtcModule.requestDM28(getListener(), address))
                .collect(Collectors.toList());

        // 6.1.20.4.a. Fail if any difference compared to data received during global request.
        List<DM28PermanentEmissionDTCPacket> dsPackets = filterPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.1.20.4.a");

        // 6.1.20.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        List<AcknowledgmentPacket> dsAcks = filterAcks(dsResults);
        checkForNACKs(globalPackets, dsAcks, obdModuleAddresses, "6.1.20.4.b");
    }
}

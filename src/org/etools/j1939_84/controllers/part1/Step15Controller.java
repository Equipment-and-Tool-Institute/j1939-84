/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The controller for 6.1.15 DM1: Active diagnostic trouble codes (DTCs)
 */

public class Step15Controller extends StepController {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 15;

    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step15Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(),
                new DTCModule(), dataRepository);
    }

    Step15Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DTCModule dtcModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory,
                PART_NUMBER, STEP_NUMBER, TOTAL_STEPS);
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());

        // 6.1.15.1 Actions:
        // a. Gather broadcast DM1 data from all ECUs (PGN 65226 (SPNs
        // 1213-1215, 1706, and 3038)).
        RequestResult<DM1ActiveDTCsPacket> results = dtcModule.readDM1(getListener(), true);

        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        // 6.1.15.2 Fail criteria:
        results.getPackets().forEach(packet -> {
            boolean isObdModule = obdModuleAddresses.contains(packet.getSourceAddress());
            // a. Fail if any OBD ECU reports an active DTC.
            if (isObdModule && !packet.getDtcs().isEmpty()) {
                addFailure("6.1.15.2.a - Fail if any OBD ECU reports an active DTC");
            }
            // b. Fail if any OBD ECU does not report MIL off. See section A.8
            // for allowed values
            LampStatus malfunctionIndicatorLampStatus = packet.getMalfunctionIndicatorLampStatus();
            if (isObdModule && malfunctionIndicatorLampStatus != OFF) {
                addFailure("6.1.15.2.b - Fail if any OBD ECU does not report MIL off per Section A.8 allowed values");

                // 6.1.15.3 Warn criteria:
                // a. Warn if any ECU reports the non-preferred MIL off format.
                // See section A.8 for description of (0b00, 0b00).
                if (malfunctionIndicatorLampStatus == LampStatus.ALTERNATE_OFF) {
                    addWarning("6.1.15.3.a - any ECU reports the non-preferred MIL off format per Section A.8");
                }
            }
            // c. Fail if any non-OBD ECU does not report MIL off or not
            // supported/ MIL status (per SAE J1939-73 Table 5).
            System.out.println("LampStatus is: " + malfunctionIndicatorLampStatus);
            if (!isObdModule && malfunctionIndicatorLampStatus != OFF
                    && malfunctionIndicatorLampStatus != NOT_SUPPORTED) {
                addFailure("6.1.15.2.c - Fail if any non-OBD ECU does not report MIL off or not supported");
            }
            packet.getDtcs().forEach(dtc -> {
                if (dtc.getConversionMethod() == 1) {
                    if (isObdModule) {
                        // d. Fail if any OBD ECU reports SPN conversion method
                        // (SPN 1706) equal to binary 1.
                        addFailure(
                                "6.1.15.2.d - Fail if any OBD ECU reports SPN conversion method (SPN 1706) equal to binary 1");
                    } else {
                        // 6.1.15.3
                        // b. Warn if any non-OBD ECU reports SPN conversion
                        // method (SPN 1706) equal to 1.
                        addWarning(
                                "6.1.15.3.b - Warn if any non-OBD ECU reports SPN conversion method (SPN 1706) equal to 1");
                    }
                }
            });

        });
        // 6.1.15.2.e Fail if no OBD ECU provides DM1 - (DM1 do not use 'Ack or
        // N'ack. It is a periodically published message.)
        if (results.getPackets().isEmpty()) {
            addFailure("6.1.15.2 - Fail if no OBD ECU provides DM1");
        }
    }
}

/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.15 DM1: Active diagnostic trouble codes (DTCs)
 */
public class Part01Step15Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part01Step15Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step15Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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

        // 6.1.15.1.a. Gather broadcast DM1 data from all ECUs (PGN 65226)
        RequestResult<DM1ActiveDTCsPacket> results = getDiagnosticMessageModule().readDM1(getListener());
        List<DM1ActiveDTCsPacket> packets = results.getPackets();

        boolean foundObdPacket = false;

        for (DM1ActiveDTCsPacket dm1 : packets) {
            int sourceAddress = dm1.getSourceAddress();
            boolean isObdModule = getDataRepository().isObdModule(sourceAddress);
            String moduleName = Lookup.getAddressName(sourceAddress);
            foundObdPacket |= isObdModule;

            Packet dm1Packet = dm1.getPacket();
            getListener().onResult(NL + getDateTimeModule().format(dm1Packet.getTimestamp()) + " " + dm1Packet);
            getListener().onResult(dm1.toString());

            // 6.1.15.2.a. Fail if any OBD ECU reports an active DTC.
            if (isObdModule && !dm1.getDtcs().isEmpty()) {
                addFailure("6.1.15.2.a - OBD Module " + moduleName + " reported an active DTC");
            }

            // 6.1.15.2.b. Fail if any OBD ECU does not report MIL off. See section A.8
            // for allowed values
            LampStatus milStatus = dm1.getMalfunctionIndicatorLampStatus();
            if (isObdModule && milStatus != OFF) {
                addFailure("6.1.15.2.b - OBD Module " + moduleName + " did not report MIL off per Section A.8 allowed values");

                // 6.1.15.3.a. Warn if any ECU reports the non-preferred MIL off format.
                // See section A.8 for description of (0b00, 0b00).
                if (milStatus == LampStatus.ALTERNATE_OFF) {
                    addWarning("6.1.15.3.a - OBD Module " + moduleName + " reported the non-preferred MIL off format per Section A.8");
                }
            }
            // 6.1.15.2.c. Fail if any non-OBD ECU does not report MIL off or not
            // supported/ MIL status (per SAE J1939-73 Table 5).
            if (!isObdModule && milStatus != OFF && milStatus != NOT_SUPPORTED) {
                addFailure("6.1.15.2.c - Non-OBD Module " + moduleName + " did not report MIL off or not supported");
            }

            dm1.getDtcs().forEach(dtc -> {
                if (dtc.getConversionMethod() == 1) {
                    if (isObdModule) {
                        // 6.1.15.2.d. Fail if any OBD ECU reports SPN conversion method (SPN 1706) equal to binary 1.
                        addFailure("6.1.15.2.d - OBD Module " + moduleName + " reported SPN conversion method (SPN 1706) equal to binary 1");
                    } else {
                        // 6.1.15.3.b. Warn if any non-OBD ECU reports SPN conversion method (SPN 1706) equal to 1.
                        addWarning("6.1.15.3.b - Non-OBD Module " + moduleName + " reported SPN conversion method (SPN 1706) equal to 1");
                    }
                }
            });
        }

        if (!foundObdPacket) {
            // 6.1.15.2.e Fail if no OBD ECU provides DM1 - (DM1 do not use 'Ack or
            // N'ack. It is a periodically published message.)
            addFailure("6.1.15.2 - No OBD ECU provided a DM1");
        }
    }
}

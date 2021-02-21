/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.8.12 DM22: Individual Clear/Reset of Active and Previously Active DTC
 */
public class Part08Step12Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part08Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step12Controller(Executor executor,
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
        // 6.8.12.1.a DS DM22 (PGN 49920) to OBD ECU(s) without a DM12 MIL on DTC stored
        // using the MIL On DTC SPN and FMI and control byte = 17, Request to Clear/Reset Active DTC.
        // 6.8.12.2.a. Fail if the ECU provides CLR_PA_ACK or CLR_ACT_ACK (as described in SAE J1939-73 paragraph
        // 5.7.22).
        // 6.8.12.2.b. Fail if the ECU provides J1939-21 ACK for PGN 49920.
        // 6.8.12.2.c. Fail if the ECU provides CLR_ACT_NACK or CLR_PA_NACK with an acknowledgement code greater than
        // 0.46
        // 6.8.12.3.a. Info: if DM22 (PGN 49920) CA_PA_NACK or CA_ACT_NACK is not received with an acknowledgement code
        // of 0.47
        // 6.8.12.3.b. Info: if J1939-21 NACK for PGN 49920 is received.
        // 6.8.12.4.a. DS DM22 to OBD ECU with a DM12 MIL on DTC stored using the DM12 MIL On DTC SPN and FMI and
        // control byte = 1, Request to Clear/Reset Previously Active DTC.
        // 6.8.12.5.a. Fail if the ECU provides DM22 with CLR_PA_ACK or CLR_ACT_ACK.
        // 6.8.12.5.b. Fail if the ECU provides J1939-21 ACK for PGN 49920.
        // 6.8.12.5.c. Fail if the ECU provides CLR_ACT_NACK with an acknowledgement code greater than 0.
        // 6.8.12.6.a. Warn if DM22 (PGN 49920) CA_PA_NACK or CA_ACT_NACK is not received with an acknowledgement code
        // of 0.
        // 6.8.12.6.b. Warn if J1939-21 NACK for PGN 49920 is received.
        // 6.8.12.7.a. Global DM22 using DM12 MIL On DTC SPN and FMI with control byte = 1, Request to Clear/Reset
        // Previously Active DTC.
        // 6.8.12.8.a. Fail if any ECU provides DM22 with CLR_PA_ACK or CLR_ACT_ACK.
        // 6.8.12.8.b. Fail if any ECU provides J1939-21 ACK for PGN 49920.
        // 6.8.12.8.c. Fail if any ECU provides CLR_ACT_NACK or CLR_PA_NACK with an acknowledgement code greater than 0.
        // 6.8.12.9.a. Global DM22 using DM12 MIL On DTC SPN and FMI with control byte = 17, Request to Clear/Reset
        // Active DTC.
        // 6.8.12.10.a. Fail if any ECU provides CLR_PA_ACK or CLR_ACT_ACK.
        // 6.8.12.10.b. Fail if any ECU provides J1939-21 ACK for PGN 49920.
        // 6.8.12.10.c. Fail if any ECU provides CLR_ACT_NACK or CLR_PA_NACK with an acknowledgement code greater than
        // 0.
        // 6.8.12.10.d. Fail if any OBD ECU erases any diagnostic information. See Section A.5 for more information.48
    }

}

/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

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
 * 6.9.3 DM22: Individual Clear/Reset of Active and Previously Active DTC
 */
public class Part09Step03Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part09Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step03Controller(Executor executor,
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
        // 6.9.3.1.a DS DM22 (PGN 49920) to OBD ECU(s) without a DM12 MIL on DTC stored using
        //  the MIL On DTC SPN and FMI and control byte = 17, Request to Clear/Reset Active DTC.
        // 6.9.3.2.a (if supported) Fail if the ECU provides CLR_PA_ACK or CLR_ACT_ACK (as described in SAE J1939-73 paragraph 5.7.22).
        // 6.9.3.2.b (if supported) Fail if the ECU provides J1939-21 ACK for PGN 49920.
        // 6.9.3.2.c (if supported) Fail if the ECU provides CLR_ACT_NACK or CLR_PA_NACK with an acknowledgement code greater than 0.50
        // 6.9.3.3.a Warn if DM22 (PGN 49920) CA_PA_NACK or CA_ACT_NACK is not received with an acknowledgement code of 0.
        // 6.9.3.3.b Warn if J1939-21 NACK for PGN 49920 is received.
        // 6.9.3.4.a DS DM22 to OBD ECU with a DM12 MIL on DTC stored using the DM12 MIL On DTC SPN and FMI and control byte = 1, Request to Clear/Reset Previously Active DTC.
        // 6.9.3.5.a Fail if the ECU provides DM22 with CLR_PA_ACK or CLR_ACT_ACK.
        // 6.9.3.5.b Fail if the ECU provides J1939-21 ACK for PGN 49920.
        // 6.9.3.5.c Fail if the ECU provides CLR_ACT_NACK with an acknowledgement code greater than 0.
        // 6.9.3.6.a Warn if DM22 (PGN 49920) CA_PA_NACK or CA_ACT_NACK is not received with an acknowledgement code of 0.
        // 6.9.3.6.b Warn if J1939-21 NACK for PGN 49920 is received.
        // 6.9.3.7.a Global DM22 using DM12 MIL On DTC SPN and FMI with control byte = 1, Request to Clear/Reset Previously Active DTC.
        // 6.9.3.8.a Fail if any ECU provides DM22 with CLR_PA_ACK or CLR_ACT_ACK.
        // 6.9.3.8.b Fail if any ECU provides J1939-21 ACK for PGN 49920.
        // 6.9.3.8.c Fail if any ECU provides CLR_ACT_NACK or CLR_PA_NACK with an acknowledgement code greater than 0.
        // 6.9.3.9.a Global DM22 using DM12 MIL On DTC SPN and FMI with control byte = 17, Request to Clear/Reset Active DTC
        // 6.9.3.10.a Fail if any ECU provides CLR_PA_ACK or CLR_ACT_ACK.
        // 6.9.3.10.b Fail if any ECU provides J1939-21 ACK for PGN 49920.
        // 6.9.3.10.c Fail if any ECU provides CLR_ACT_NACK or CLR_PA_NACK with an acknowledgement code greater than > 0.
        // 6.9.3.10.d Fail if any OBD ECU erases any diagnostic information. See Section A.5 for more information.51
    }

}
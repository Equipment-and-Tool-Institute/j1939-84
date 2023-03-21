/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.Outcome.FAIL;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.DM5Heartbeat;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.27 Part 1 to Part 2 Transition
 */
public class Part01Step27Controller extends StepController {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 27;

    private static final int TOTAL_STEPS = 3;

    Part01Step27Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance());
    }

    Part01Step27Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        try (var dm5 = DM5Heartbeat.run(getJ1939(), getListener())) {
            // 6.1.27.1 Actions:
            // a. Testing may be stopped for vehicles with failed tests and for
            // vehicles with the MIL on or a non-emissions related fault displayed
            // in DM1. Vehicles with the MIL on will fail subsequent tests.
            displayQuestionMessage();

            // b. The transition from part 1 to part 2 shall be as provided below.
            // i. The engine shall be started without turning the key off.
            // ii. Or, an electric drive or hybrid drive system shall be placed in the operating
            // mode used to provide power to the drive system without moving the vehicle, if not
            // automatically provided during the initial key off to key on operation.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.1.27.1.b");

            // iii. The engine shall be allowed to idle one minute
            pause("Step 6.1.27.b.iii - Allowing engine to idle for %1$d seconds", 60L);
        }
    }

    /**
     * This method determines if there was a failure in the previous tests.
     * Then, if there has been a failure; the method asks the user if they
     * would like to continue and responds accordingly.
     */
    private void displayQuestionMessage() {
        // Only display question if there was a failure otherwise assume continuing
        // First of all, let's figure out if we have a failure
        boolean hasFailure = getPartResult(PART_NUMBER).getStepResults()
                                                       .stream()
                                                       .anyMatch(s -> s.getOutcome() == FAIL);
        if (hasFailure) {
            // a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on
            // or a non-emissions related fault displayed in DM1. Vehicles with the MIL on will fail subsequent tests.
            String message = "";
            message += "Testing may be stopped for vehicles with failed tests " + NL;
            message += "and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL;
            message += "Vehicles with the MIL on will fail subsequent tests." + NL;
            message += NL;
            message += "This vehicle has had failures and will likely fail subsequent tests." + NL;
            message += NL;
            message += "Would you like to continue?" + NL;
            displayInstructionAndWait(message, "Step 6.1.27.1.a", QUESTION);
        }
    }
}

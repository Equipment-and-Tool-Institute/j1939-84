/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.isTesting;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939_84.model.Outcome.FAIL;

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
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.27 Part 1 to Part 2 Transition
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
             new DiagnosticMessageModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance());
    }

    Part01Step27Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository) {
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
        incrementProgress("Part 1, Step 27 - Part 1 to Part 2 Transition");
        //  6.1.27.1 Actions:
        //  a. Testing may be stopped for vehicles with failed tests and for
        //  vehicles with the MIL on or a non-emissions related fault displayed
        //  in DM1. Vehicles with the MIL on will fail subsequent tests.
        if (!isTesting()) {
            displayQuestionMessage();
        }

        //  b. The transition from part 1 to part 2 shall be as provided below.
        //        i. The engine shall be started without turning the key off.
        //       ii. Or, an electric drive or hybrid drive system shall be placed in the operating
        //           mode used to provide power to the drive system without moving the vehicle, if not
        //           automatically provided during the initial key off to key on operation.
        incrementProgress("Part 1, Step 27 b.i - Ensuring Key On, Engine On");
        if (!isTesting()) {
            ensureKeyOnEngineOn();
        }

        //      iii. The engine shall be allowed to idle one minute
        incrementProgress("Part 1, Step 27 b.iii - Allowing engine to idle one minute");
        if (!isTesting()) {
            pause("Allowing engine to idle for %1$d seconds", 60L);
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
            //  a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on
            //  or a non-emissions related fault displayed in DM1. Vehicles with the MIL on will fail subsequent tests.
            String message = "Ready to transition from Part 1 to Part 2 of the test" + NL;
            message += "a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL;
            message += "   Vehicles with the MIL on will fail subsequent tests." + NL + NL;
            message += "This vehicle has had failures and will likely fail subsequent tests.  Would you still like to continue?" + NL;
            displayInstructionAndWait(message, "Start Part 2", QUESTION);
        }
    }
}

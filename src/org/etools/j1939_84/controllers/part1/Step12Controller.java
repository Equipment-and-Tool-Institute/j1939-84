/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.12 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results
 */
public class Step12Controller extends StepController {
    //formatter:off
    private static final Set<Integer> VALID_SLOTS = new HashSet<>(Arrays.asList(5, 8, 9, 10, 12, 13, 14, 16, 17, 18, 19,
            22, 23, 27, 28, 29, 30, 32, 37, 39, 42, 43, 50, 51, 52, 55, 57, 64, 68, 69, 70, 71, 72, 76, 77, 78, 80, 82,
            85, 96, 98, 104, 106, 107, 112, 113, 114, 115, 125, 127, 130, 131, 132, 136, 138, 143, 144, 145, 146, 151,
            162, 206, 208, 211, 219, 221, 222, 223, 224, 226, 227, 231, 235, 236, 237, 238, 242, 243, 249, 250, 251,
            256, 261, 262, 264, 270, 272, 277, 285, 288, 290, 295, 301, 302, 303, 305, 306, 307, 317, 318, 319, 320,
            323, 324, 333, 334, 336, 337, 345, 346, 346, 347, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359,
            360, 361, 362, 363, 364, 365, 366, 367, 369, 370, 372, 373, 375, 377, 378, 379, 380, 383, 384, 385, 386,
            387, 388, 389, 390, 393, 394, 396, 397, 398, 399, 400, 401, 403, 414, 415, 416, 429, 430, 431, 433, 434,
            436, 437, 438, 440, 441, 442, 443, 444, 445, 446, 450, 451, 452, 453, 456, 459, 460, 462, 463, 464, 474,
            475, 476, 479));
    //formatter:on

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;
    private final OBDTestsModule obdTestsModule;
    private final TableA7Validator tableA7Validator;

    Step12Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                dataRepository,
                new VehicleInformationModule(),
                new OBDTestsModule(),
                new TableA7Validator());
    }

    Step12Controller(Executor executor,
            EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            DataRepository dataRepository,
            VehicleInformationModule vehicleInformationModule,
            OBDTestsModule obdTestsModule,
            TableA7Validator tableA7Validator) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
        this.dataRepository = dataRepository;
        this.obdTestsModule = obdTestsModule;
        this.tableA7Validator = tableA7Validator;
    }

    @Override
    protected void run() throws Throwable {
        // 6.1.12 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results
        obdTestsModule.setJ1939(getJ1939());

        // 6.1.12.1 Actions:
        // Get all the obdModuleAddresses then send DM7 to each address we have and get supported spns
        List<ScaledTestResult> vehicleTestResults = new ArrayList<>();

        // Record it the DM30 for each module
        for (OBDModuleInformation obdModule : dataRepository.getObdModules()) {
            List<ScaledTestResult> moduleTestResults = new ArrayList<>();
            for (SupportedSPN spn : obdModule.getTestResultSpns()) {
                List<ScaledTestResult> testResults =
                        obdTestsModule.getDM30Packets(getListener(), obdModule.getSourceAddress(), spn)
                                .stream()
                                .peek(p -> verifyDM30PacketSupported(p, spn))
                                .flatMap(p -> p.getTestResults().stream())
                                .collect(Collectors.toList());
                moduleTestResults.addAll(testResults);
            }
            obdModule.setScaledTestResults(moduleTestResults);
            vehicleTestResults.addAll(moduleTestResults);
        }

        // 6.1.12.2 Fail/warn criteria:
        //
        // Create list of ECU address+SPN+FMI supported test results.
        // a. Fail/warn per section A.7 Criteria for Test Results Evaluation.
        FuelType fuelType = dataRepository.getVehicleInformation().getFuelType();

        if (fuelType.isCompressionIgnition()) {
            tableA7Validator.validateForCompressionIgnition(vehicleTestResults, getListener());
        } else if (fuelType.isSparkIgnition()) {
            tableA7Validator.validateForSparkIgnition(vehicleTestResults, getListener());
        }

        // d. Warn if any ECU reports more than one set of test results for the same SPN+FMI.
        Collection<ScaledTestResult> reportedDuplicates = tableA7Validator.findDuplicates(vehicleTestResults);
        reportedDuplicates.forEach(dup -> {
            String failureMessage = "6.1.12.1.d SPN " + dup.getSpn() + " FMI " + dup.getFmi() + " returned duplicates";
            addWarning(PART_NUMBER, STEP_NUMBER, failureMessage);
        });
    }

    private void verifyDM30PacketSupported(DM30ScaledTestResultsPacket packet, SupportedSPN spn) {

        // a. Fail if no test result (comprised of a SPN+FMI with a test result
        // and a min and max test limit) for an SPN indicated as supported is
        // actually reported from the ECU/device that indicated support.
        List<ScaledTestResult> testResults = packet.getTestResults().stream()
                .filter(p -> p.getSpn() == spn.getSpn())
                .collect(Collectors.toList());

        if (testResults.isEmpty()) {
            addFailure(PART_NUMBER,
                    STEP_NUMBER,
                    "6.1.12.1.a No test result for an SPN indicated as supported is actually reported from the ECU that indicated support");
        }

        // b. Fail if any test result does not report the test result/min test
        // limit/max test limit as initialized (after code clear) values (either
        // 0xFB00/0xFFFF/0xFFFF or 0x0000/0x0000/0x0000).
        for (ScaledTestResult result : testResults) {
            boolean isMaximum = result.getTestValue() == 0xFB00 &&
                    result.getTestMinimum() == 0xFFFF && result.getTestMaximum() == 0xFFFF;
            boolean isMinimum = result.getTestValue() == 0x0000 &&
                    result.getTestMinimum() == 0x0000 && result.getTestMaximum() == 0x0000;
            if (!isMaximum && !isMinimum) {
                addFailure(PART_NUMBER,
                        STEP_NUMBER,
                        "6.1.12.1.b Test result does not report the test result/min test limit/max test limit initialized properly");
            }
            // c. Fail if the SLOT identifier for any test results is an
            // undefined or a not valid SLOT in Appendix A of J1939-71. See
            // Table A-7-2 3 for a list of the valid, SLOTs known to be
            // appropriate for use in test results.
            if (result.getSlot() != null) {
                int slotIdentifier = result.getSlot().getId();

                if (!VALID_SLOTS.contains(slotIdentifier)) {
                    addFailure(PART_NUMBER,
                            STEP_NUMBER,
                            "6.1.12.1.c #" + slotIdentifier + " SLOT identifier is invalid");
                }
            } else {
                addFailure(PART_NUMBER, STEP_NUMBER, "6.1.12.1.c #" + result.getSlotNumber() + " SLOT identifier is undefined");
            }
        }
    }

}

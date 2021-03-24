/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.Slot;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 6.1.12 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results
 */
public class Part01Step12Controller extends StepController {
    //@formatter:off
    private static final Set<Integer> VALID_SLOTS = new HashSet<>(Arrays.asList(5, 8, 9, 10, 12, 13, 14, 16, 17, 18, 19,
            22, 23, 27, 28, 29, 30, 32, 37, 39, 42, 43, 50, 51, 52, 55, 57, 64, 68, 69, 70, 71, 72, 76, 77, 78, 80, 82,
            85, 96, 98, 104, 106, 107, 112, 113, 114, 115, 125, 127, 130, 131, 132, 136, 138, 143, 144, 145, 146, 151,
            162, 206, 208, 211, 219, 221, 222, 223, 224, 226, 227, 231, 235, 236, 237, 238, 242, 243, 249, 250, 251,
            256, 261, 262, 264, 270, 272, 277, 285, 288, 290, 295, 301, 302, 303, 305, 306, 307, 317, 318, 319, 320,
            323, 324, 333, 334, 336, 337, 345, 346, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358,
            359, 360, 361, 362, 363, 364, 365, 366, 367, 369, 370, 372, 373, 375, 377, 378, 379, 380, 383, 384, 385,
            386, 387, 388, 389, 390, 393, 394, 396, 397, 398, 399, 400, 401, 403, 414, 415, 416, 429, 430, 431, 433,
            434, 436, 437, 438, 440, 441, 442, 443, 444, 445, 446, 450, 451, 452, 453, 456, 459, 460, 462, 463, 464,
            474, 475, 476, 479));
    //@formatter:on

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    private final TableA7Validator tableA7Validator;

    Part01Step12Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             dataRepository,
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new TableA7Validator(),
             DateTimeModule.getInstance());
    }

    Part01Step12Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           DataRepository dataRepository,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           TableA7Validator tableA7Validator,
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
        this.tableA7Validator = tableA7Validator;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.12.1.a. DS DM7 with TID 247 using FMI 31 for each SPN identified as providing test results in a
        // DM24 response in step 6.1.4.1 to the SPN’s respective OBD ECU.

        // Create list of ECU address+SPN+FMI supported test results.
        // A.K.A Get all the obdModuleAddresses then send DM7 to each address we have and get supported SPNs
        List<ScaledTestResult> vehicleTestResults = new ArrayList<>();

        // Record the DM30 for each module
        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {
            List<ScaledTestResult> moduleTestResults = new ArrayList<>();
            int sourceAddress = obdModule.getSourceAddress();
            String moduleName = obdModule.getModuleName();

            obdModule.getTestResultSPNs()
                     .stream()
                     .mapToInt(SupportedSPN::getSpn)
                     .forEachOrdered(spnId -> {
                         var dm30Packets = getDiagnosticMessageModule().requestTestResults(getListener(),
                                                                                           sourceAddress,
                                                                                           247,
                                                                                           spnId,
                                                                                           31);
                         if (dm30Packets.isEmpty()) {
                             addFailure("6.1.12.1.a - No test result for Supported SPN " + spnId + " from "
                                     + moduleName);
                         } else {
                             var testResults = dm30Packets
                                                          .stream()
                                                          .peek(p -> verifyDM30PacketSupported(p, spnId))
                                                          .flatMap(p -> p.getTestResults().stream())
                                                          .collect(Collectors.toList());

                             // 6.1.12.1.d. Warn if any ECU reports more than one set of test results for the same
                             // SPN+FMI.
                             tableA7Validator.findDuplicates(testResults)
                                             .forEach(dup -> {
                                                 addWarning("6.1.12.2.a (A7.2.b) - " + moduleName
                                                         + " returned duplicate test results for SPN " + dup.getSpn()
                                                         + " FMI " + dup.getFmi());
                                             });

                             moduleTestResults.addAll(testResults);
                         }
                         getListener().onResult("");
                     });

            if (!moduleTestResults.isEmpty()) {
                getListener().onResult(moduleName + " Test Results:");
                getListener().onResult(moduleTestResults.stream()
                                                        .map(ScaledTestResult::toString)
                                                        .collect(Collectors.toList()));

                obdModule.setScaledTestResults(moduleTestResults);
                getDataRepository().putObdModule(obdModule);
                vehicleTestResults.addAll(moduleTestResults);
            }
        }

        // Create list of ECU address+SPN+FMI supported test results.
        // 6.1.12.2.a. Fail/warn per section A.7 Criteria for Test Results Evaluation.
        if (getFuelType().isCompressionIgnition()) {
            tableA7Validator.validateForCompressionIgnition(vehicleTestResults, getListener());
        } else if (getFuelType().isSparkIgnition()) {
            tableA7Validator.validateForSparkIgnition(vehicleTestResults, getListener());
        }
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "There are no null checks back up the line")
    private void verifyDM30PacketSupported(DM30ScaledTestResultsPacket packet, int spnId) {

        String moduleName = Lookup.getAddressName(packet.getSourceAddress());

        // 6.1.12.1.a. Fail if no test result (comprised of a SPN+FMI with a test result
        // and a min and max test limit) for an SPN indicated as supported is
        // actually reported from the ECU/device that indicated support.

        List<ScaledTestResult> testResults = packet.getTestResults()
                                                   .stream()
                                                   .filter(p -> p.getSpn() == spnId)
                                                   .collect(Collectors.toList());

        if (testResults.isEmpty()) {
            addFailure("6.1.12.2.a (A7.1.a) - No test result for supported SPN " + spnId + " from " + moduleName);
        }

        // 6.1.12.1.b. Fail if any test result does not report the test result/min test
        // limit/max test limit as initialized (after code clear) values (either
        // 0xFB00/0xFFFF/0xFFFF or 0x0000/0x0000/0x0000).
        for (ScaledTestResult result : testResults) {
            if (!result.isInitialized()) {
                addFailure("6.1.12.2.a (A7.1.b) - Test result for SPN " + spnId + " FMI " + result.getFmi() + " from "
                        + moduleName
                        + " does not report the test result/min test limit/max test limit initialized properly");
            }

            // 6.1.12.1.c. Fail if the SLOT identifier for any test results is an
            // undefined or a not valid SLOT in Appendix A of J1939-71. See
            // Table A-7-2 3 for a list of the valid, SLOTs known to be
            // appropriate for use in test results.
            Slot slot = result.getSlot();
            int slotIdentifier = slot == null ? -1 : slot.getId();
            if (!VALID_SLOTS.contains(slotIdentifier)) {
                addFailure("6.1.12.2.a (A7.1.c) - #" + slotIdentifier + " SLOT identifier for SPN " + spnId + " from "
                        + moduleName + " is invalid");
            }
        }
    }

}

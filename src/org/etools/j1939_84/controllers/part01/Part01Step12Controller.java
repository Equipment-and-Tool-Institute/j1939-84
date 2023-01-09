/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

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
             new CommunicationsModule(),
             new TableA7Validator(),
             DateTimeModule.getInstance());
    }

    Part01Step12Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           DataRepository dataRepository,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           TableA7Validator tableA7Validator,
                           DateTimeModule dateTimeModule) {
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
        this.tableA7Validator = tableA7Validator;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.12.1.a. DS DM7 with TID 247 using FMI 31 for each SP identified as providing test results in a
        // DM24 response in step 6.1.4.1 to the SP’s respective OBD ECU.

        // Create list of ECU address+SP+FMI supported test results.
        // A.K.A Get all the obdModuleAddresses then send DM7 to each address we have and get supported SPs
        List<ScaledTestResult> vehicleTestResults = new ArrayList<>();
        Map<Integer, Collection<ScaledTestResult>> testResultsByModuleMap = new HashMap<>();

        // Record the DM30 for each module
        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {
            List<ScaledTestResult> moduleTestResults = new ArrayList<>();
            int sourceAddress = obdModule.getSourceAddress();
            String moduleName = obdModule.getModuleName();

            obdModule.getTestResultSPNs()
                     .stream()
                     .mapToInt(SupportedSPN::getSpn)
                     .forEachOrdered(spId -> {
                         var dm30Packets = getCommunicationsModule().requestTestResults(getListener(),
                                                                                        sourceAddress,
                                                                                        247,
                                                                                        spId,
                                                                                        31);
                         // 6.1.12.2.a Table A.7.2.a Fail if no test result is received for any of the SPN+FMI
                         // combinations listed in Table A5A.
                         if (dm30Packets.isEmpty()) {
                             addFailure("6.1.12.2.a (A.7.2.a) - No test result for Supported SP " + spId + " from "
                                     + moduleName);
                         } else {
                             var testResults = dm30Packets
                                                          .stream()
                                                          .peek(p -> verifyDM30PacketSupported(p, spId))
                                                          .flatMap(p -> p.getTestResults().stream())
                                                          .collect(Collectors.toList());

                             // 6.1.12.1.d. Warn if any ECU reports more than one set of test results for the same
                             // SP+FMI.
                             testResultsByModuleMap.put(obdModule.getSourceAddress(),
                                                        new HashSet<>(testResults));
                             // 6.1.12.2.a Table A.7.1.d Warn if any ECU reports more than one set of test results for
                             // the same SPN+FMI.
                             tableA7Validator.findDuplicates(testResults)
                                             .stream()
                                             .distinct() // let's log it just once
                                             .forEach(dup -> {
                                                 addWarning("6.1.12.2.a (A.7.1.d) - " + moduleName
                                                         + " returned duplicate test results for SP " + dup.getSpn()
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

        Collection<ScaledTestResult> scaledTestResults = testResultsByModuleMap.values()
                                                                               .stream()
                                                                               .flatMap(Collection::stream)
                                                                               .collect(Collectors.toList());

        // 6.1.12.2.a. Table A.7.2.b Warn if more than one ECU responds with test results for the same SPN+FMI
        // combination
        tableA7Validator.findDuplicates(scaledTestResults)
                        .stream()
                        .distinct() // let's log it just once
                        .forEach(dup -> {
                            addWarning("6.1.12.2.a (A.7.2.b) - More than one ECU responded with test results for SP "
                                    + dup.getSpn() + " + FMI " + dup.getFmi() + " combination");
                        });

        // Create list of ECU address+SP+FMI supported test results.
        // 6.1.12.2.a. Fail/warn per section A.7 Criteria for Test Results Evaluation.
        if (getFuelType().isCompressionIgnition()) {
            tableA7Validator.validateForCompressionIgnition(vehicleTestResults, getListener());
        } else if (getFuelType().isSparkIgnition()) {
            tableA7Validator.validateForSparkIgnition(vehicleTestResults, getListener());
        }

        // 6.1.12.3 Actions2: // 6.1.12.3 was omitted in error.
        // 6.1.12.3.a. DS DM7 with TID 245 (for DM58) using FMI 31 for each SP identified as supporting DM58 in a DM24
        // response In step 6.1.4.1 to the SP’s respective OBD ECU.
        // Display the scaled engineering value for the requested SP.
        if (getEngineModelYear() >= 2022) {
            getDataRepository().getObdModules().forEach(module -> {
                module.getSupportedSPNs()
                      .stream()
                      .filter(SupportedSPN::supportsRationalityFaultData)
                      .forEach(spn -> {
                          getCommunicationsModule().requestDM58(getListener(),
                                                                module.getSourceAddress(),
                                                                spn.getSpn())
                                                   .requestResult()
                                                   .getEither()
                                                   .stream()
                                                   .findFirst()
                                                   .ifPresentOrElse(response -> {
                                                       // 6.1.12.4 Fail/Warn criteria2:
                                                       // 6.1.12.4.a. Fail if NACK received for DM7 PG from OBD ECU
                                                       // 6.1.12.4.b. Fail, if DM58 not received (after allowed retries)
                                                       if (response.right.isPresent()) {
                                                           addFailure("6.1.12.4.a - NACK received for DM7 PG from OBD ECU from "
                                                                   + module.getModuleName() + " for SP " + spn);
                                                       } else if (response.left.isPresent()) {
                                                           var dm58 = response.left;

                                                           DM58RationalityFaultSpData packet = dm58.get();
                                                           // 6.1.12.4.c Fail, if expected unused bytes in DM58 are
                                                           // not padded with FFh
                                                           if (!areUnusedBytesPaddedWithFFh(packet)) {
                                                               addFailure(
                                                                          "6.1.12.4.c - Unused bytes in DM58 are not padded with FFh in the response from "
                                                                                  + module.getModuleName()
                                                                                  + " for SP " + spn);
                                                           }
                                                           // 6.1.12.4.d. Fail, if data returned is greater than FBh
                                                           // (for 1
                                                           // byte SP), FBFFh (for 2 byte SP), or FBFFFFFFh (for 4
                                                           // byte SP).
                                                           if (isGreaterThanFb(packet)) {
                                                               addFailure(
                                                                          "6.1.12.4.d - Data returned is greater than 0xFB... threshold from "
                                                                                  + module.getModuleName()
                                                                                  + " for " + spn);

                                                           }
                                                       }
                                                   },
                                                                    () -> {
                                                                        // 6.1.12.4.b. Fail, if DM58 not received (after
                                                                        // allowed retries)
                                                                        addFailure("6.1.12.4.b. DM58 not received from "
                                                                                + module.getModuleName() + " for SP "
                                                                                + spn);
                                                                    });
                      });
            });
            // 6.1.12.5 - 6.1.12.6
            getDm58AndVerifyData();
        }
    }

    private void getDm58AndVerifyData() {

        // 6.1.12.5 Actions3:
        // 6.1.12.5.a. DS DM7 with TID 245 (for DM58) using FMI 31 for first SP identified as not supporting DM58 in a
        // DM24
        // response In step 6.1.4.1 to the SP’s respective OBD ECU. (Use of an SP that supports test results is
        // preferred when available).
        var obdModules = getDataRepository().getObdModules()
                                            .stream()
                                            .filter(module -> {
                                                var supportedSPNs = module.getSupportedSPNs()
                                                                          .stream()
                                                                          .filter(supportedSPN -> !supportedSPN.supportsRationalityFaultData())
                                                                          .collect(Collectors.toList());
                                                return !supportedSPNs.isEmpty();
                                            })
                                            .collect(Collectors.toList());

        var nonRatFaultSps = getDataRepository().getObdModules()
                                                .stream()
                                                .flatMap(m -> m.getSupportedSPNs().stream())
                                                .filter(supported -> !supported.supportsRationalityFaultData())
                                                .collect(Collectors.toList());
        if (nonRatFaultSps.isEmpty() || obdModules.isEmpty()) {
            getListener().onResult("6.1.12.5.a - No SPs found that do NOT indicate support for DM58 in the DM24 response");
        } else {
            int requestSpn = nonRatFaultSps.stream()
                                           .filter(SupportedSPN::supportsScaledTestResults)
                                           .findFirst()
                                           .orElseGet(() -> nonRatFaultSps.get(0))
                                           .getSpn();

            int sourceAddress = obdModules.get(0).getSourceAddress();
            var packet = getCommunicationsModule().requestDM58(getListener(), sourceAddress, requestSpn)
                                                  .requestResult()
                                                  .getAcks()
                                                  .stream()
                                                  .findFirst()
                                                  .orElse(null);

            // 6.1.12.6 Fail/Warn criteria3:
            // 6.1.12.6.a. Fail if a NACK is not received
            if (packet == null || packet.getResponse() != AcknowledgmentPacket.Response.NACK) {
                addFailure("6.1.12.6.a - NACK not received for DM7 PG from OBD ECU "
                        + Lookup.getAddressName(sourceAddress) + " for SPN " + requestSpn);
            }
        }
    }

    private void verifyDM30PacketSupported(DM30ScaledTestResultsPacket packet, int spId) {

        String moduleName = Lookup.getAddressName(packet.getSourceAddress());

        // 6.1.12.2.a. Table A.7.1.a Fail if no test result (comprised of a SP+FMI with a test result
        // and a min and max test limit) for an SP indicated as supported is
        // actually reported from the ECU/device that indicated support.

        List<ScaledTestResult> testResults = packet.getTestResults()
                                                   .stream()
                                                   .filter(p -> p.getSpn() == spId)
                                                   .collect(Collectors.toList());

        if (testResults.isEmpty()) {
            addFailure("6.1.12.2.a (A.7.1.a) - No test result for supported SP " + spId + " from " + moduleName);
        }

        // 6.1.12.2.a. Table A.7.1.b Fail if any test result does not report the test result/min test
        // limit/max test limit as initialized (after code clear) values (either
        // 0xFB00/0xFFFF/0xFFFF or 0x0000/0x0000/0x0000).
        for (ScaledTestResult result : testResults) {
            if (!result.isInitialized()) {
                addFailure("6.1.12.2.a (A.7.1.b) - Test result for SP " + spId + " FMI " + result.getFmi() + " from "
                        + moduleName
                        + " does not report the test result/min test limit/max test limit initialized properly");
            }

            // 6.1.12.2.a. Table A.7.1.c Fail if the SLOT identifier for any test results is an
            // undefined or a not valid SLOT in Appendix A of J1939-71. See
            // Table A-7-2 3 for a list of the valid, SLOTs known to be
            // appropriate for use in test results.
            int slotIdentifier = result.getSlot().getId();
            if (!VALID_SLOTS.contains(slotIdentifier)) {
                addFailure("6.1.12.2.a (A.7.1.c) - #" + slotIdentifier + " SLOT identifier for SP " + spId
                        + " FMI " + result.getFmi() + " from " + moduleName + " is invalid");
            }
        }
    }

}

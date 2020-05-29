/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The controller for 6.1.12 DM7/DM30: Command Non-continuously
 *         Monitored Test/Scaled Test Results
 */
public class Step12Controller extends Controller {

    private final DataRepository dataRepository;
    private final OBDTestsModule obdTestsModule;

    Step12Controller(DataRepository dataRepository, OBDTestsModule obdTestsModule) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), dataRepository, new VehicleInformationModule(), obdTestsModule,
                new PartResultFactory());
    }

    protected Step12Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, DataRepository dataRepository,
            VehicleInformationModule vehicleInformationModule,
            OBDTestsModule obdTestsModule, PartResultFactory partResultFactory) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.dataRepository = dataRepository;
        this.obdTestsModule = obdTestsModule;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 12";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    private void processTestResultsEvaluation(ScaledTestResult scaledTestResult) {
        // A.7 CRITERIA FOR TEST RESULTS EVALUATION
        // Section A.7 describes the evaluation of test results, including the
        // definition of a minimum set for SPNs and FMIs to be supported as test
        // results, for the HD OBD monitoring requirements given.
        // 1. Each ECU/device that indicates one or more SPNs are supported for test
        // results with a DM24 response shall be evaluated separately. The request (DM7)
        // and response (DM30) for test results shall:
        // a. Fail if no test result (comprised of a SPN+FMI with a test result and a
        // min and max test limit) for an SPN indicated as supported is actually
        // reported from the ECU/device that indicated support.

        // b. Fail if any test result does not report the test result/min test limit/max
        // test limit as initialized (after code clear) values (either
        // 0xFB00/0xFFFF/0xFFFF or 0x0000/0x0000/0x0000).

        // c. Fail if the SLOT identifier for any test results is an undefined or a not
        // valid SLOT in Appendix A of J1939-71. See Table A-7-2 3 for a list of the
        // valid, SLOTs known to be appropriate for use in test results.

        // d. Warn if any ECU reports more than one set of test results for the same
        // SPN+FMI.

        // 2. All DM30 responses shall be combined using appropriate ‘AND’ logic to
        // create a composite vehicle test results list (i.e., a list of each ECU
        // address +SPN+FMI combination that test results were received for). Composite
        // vehicle test results shall be evaluated and:

        // a. Fail if no test result is received for any of the SPN+FMI combinations
        // listed in Table A-7-1 Composite Vehicle Test Results.

        System.out.println("Working hard!");

    }

    // So we need to go back to step 4
    // get from repository all the supported spns by module address
    // then send DM7 to each module address asking each supported spn
    // then we need to save to dataRepository the spn and fmi by address

    @Override
    protected void run() throws Throwable {
        // 6.1.12 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results
        obdTestsModule.setJ1939(getJ1939());
        //
        // 6.1.12.1 Actions:
        // Get all the obdModuleAddresses
        // then send DM7 to each address we have and get supported spns

        List<ScaledTestResult> vehicleTestResults = new ArrayList<>();

        // Record it the DM30 for each module
        for (OBDModuleInformation obdModule : dataRepository.getObdModules()) {
            List<ScaledTestResult> moduleTestResults = new ArrayList<>();
            for (SupportedSPN spn : obdModule.getSupportedSpns()) {
                List<DM30ScaledTestResultsPacket> dm30Packets = obdTestsModule
                        .getDM30Packets(getListener(), obdModule.getSourceAddress(), spn);
                // TODO ensure the module actually supports the SPN it claims to support
                dm30Packets.forEach(packet -> {
                    verifyDM30PacketSupported(packet, spn);
                });
                List<ScaledTestResult> testResults = dm30Packets
                        .stream()
                        .flatMap(p -> p.getTestResults().stream())
                        .collect(Collectors.toList());

                // TODO check the "quality" of the TestResults
                moduleTestResults.addAll(testResults);
            }
            obdModule.setScaledTestResults(moduleTestResults);
            vehicleTestResults.addAll(moduleTestResults);
        }

        FuelType fuelType = dataRepository.getVehicleInformation().getFuelType();

        if (fuelType.isCompressionIgnition()) {

        } else if (fuelType.isSparkIgnition()) {

        } else {
            // TODO
        }

        // 6.1.12.2 Fail/warn criteria:
        //
        // Create list of ECU address+SPN+FMI
        // supported test results.19
        // a. Fail/warn per section A.7 Criteria for Test Results Evaluation.
        // Evaluate each result.

    }

    private boolean verifyDM30PacketSupported(DM30ScaledTestResultsPacket packet, SupportedSPN spn) {
        boolean verified = false;

        // a. Fail if no test result (comprised of a SPN+FMI with a test result and a
        // min and max test limit) for an SPN indicated as supported is actually
        // reported from the ECU/device that indicated support.
        List<ScaledTestResult> testResults = packet.getTestResults().stream()
                .filter(p -> p.getSpn() == spn.getSpn())
                .collect(Collectors.toList());

        if (testResults == null || testResults.isEmpty()) {
            addFailure(1,
                    12,
                    "Fail if no test result (comprised of a SPN+FMI with a test result and a min and max test limit) for an SPN indicated as supported is actually reported from the ECU/device that indicated support.");

        }

        // b. Fail if any test result does not report the test result/min test limit/max
        // test limit as initialized (after code clear) values (either
        // 0xFB00/0xFFFF/0xFFFF or 0x0000/0x0000/0x0000).
        for (ScaledTestResult result : testResults) {
            if (result.getScaledTestMaximum() == 0xFB00 &&
                    result.getScaledTestMaximum() == 0xFFFF) {
                addFailure(1,
                        12,
                        "Fail if any test result does not report the test result max test limit initialized one of the following values 0xFB00/0xFFFF/0xFFFF ");

            }
            if (result.getScaledTestMinimum() == 0x0000) {
                addFailure(1,
                        12,
                        "Fail if any test result does not report the test result min test limit initialized one of the following values 0x0000/0x0000/0x0000");

            }
            // c. Fail if the SLOT identifier for any test results is an undefined or a not
            // valid SLOT in Appendix A of J1939-71. See Table A-7-2 3 for a list of the
            // valid, SLOTs known to be appropriate for use in test results.
            verifyValidSlotIdentifier(result.getSlot().getId());

        }

        // d. Warn if any ECU reports more than one set of test results for the same
        // SPN+FMI.
        // SomeClass validator = new SomeClass();
        // validator.reportDuplicates()

        return verified;
    }

    private void verifyValidSlotIdentifier(int slotIdentifier) {

        Set<Integer> validSlots = new HashSet<>() {
            {
                add(5);
                add(8);
                add(9);
                add(10);
                add(12);
                add(13);
                add(14);
                add(16);
                add(17);
                add(18);
                add(19);
                add(22);
                add(23);
                add(27);
                add(28);
                add(29);
                add(30);
                add(32);
                add(37);
                add(39);
                add(42);
                add(43);
                add(50);
                add(51);
                add(52);
                add(55);
                add(57);
                add(64);
                add(68);
                add(69);
                add(70);
                add(71);
                add(72);
                add(76);
                add(77);
                add(78);
                add(80);
                add(82);
                add(85);
                add(96);
                add(98);
                add(104);
                add(106);
                add(107);
                add(112);
                add(113);
                add(114);
                add(115);
                add(125);
                add(127);
                add(130);
                add(131);
                add(132);
                add(136);
                add(138);
                add(143);
                add(144);
                add(145);
                add(146);
                add(151);
                add(162);
                add(206);
                add(208);
                add(211);
                add(219);
                add(221);
                add(222);
                add(223);
                add(224);
                add(226);
                add(227);
                add(231);
                add(235);
                add(236);
                add(237);
                add(238);
                add(242);
                add(243);
                add(249);
                add(250);
                add(251);
                add(256);
                add(261);
                add(262);
                add(264);
                add(270);
                add(272);
                add(277);
                add(285);
                add(288);
                add(290);
                add(295);
                add(301);
                add(302);
                add(303);
                add(305);
                add(306);
                add(307);
                add(317);
                add(318);
                add(319);
                add(320);
                add(323);
                add(324);
                add(333);
                add(334);
                add(336);
                add(337);
                add(345);
                add(346);
                add(346);
                add(347);
                add(349);
                add(350);
                add(351);
                add(352);
                add(353);
                add(354);
                add(355);
                add(356);
                add(357);
                add(358);
                add(359);
                add(360);
                add(361);
                add(362);
                add(363);
                add(364);
                add(365);
                add(366);
                add(367);
                add(369);
                add(370);
                add(372);
                add(373);
                add(375);
                add(377);
                add(378);
                add(379);
                add(380);
                add(383);
                add(384);
                add(385);
                add(386);
                add(387);
                add(388);
                add(389);
                add(390);
                add(393);
                add(394);
                add(396);
                add(397);
                add(398);
                add(399);
                add(400);
                add(401);
                add(403);
                add(414);
                add(415);
                add(416);
                add(429);
                add(430);
                add(431);
                add(433);
                add(434);
                add(436);
                add(437);
                add(438);
                add(440);
                add(441);
                add(442);
                add(443);
                add(444);
                add(445);
                add(446);
                add(450);
                add(451);
                add(452);
                add(453);
                add(456);
                add(459);
                add(460);
                add(462);
                add(463);
                add(464);
                add(474);
                add(475);
                add(476);
                add(479);
            }
        };
        if (!validSlots.contains(slotIdentifier)) {
            addFailure(1,
                    12,
                    slotIdentifier + " SLOT identifier is an undefined or invalid");
        }
    }

}

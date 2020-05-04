/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step08Controller extends Controller {

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Step08Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), dataRepository);
    }

    Step08Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DiagnosticReadinessModule diagnosticReadinessModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 8";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    /**
     *
     * 6.1.8 DM20: Monitor Performance Ratio
     *
     * 6.1.8.1 Actions:
     *
     * a. Global DM20 (send Request (PGN 59904) for PGN 49664 (SPNs 3048, 3049,
     * 3066-3068).
     *
     * i. Create list by ECU address of all data for use later in the test.
     *
     * 6.1.8.2 Fail criteria:
     *
     * a. Fail if minimum expected SPNs are not supported (in the aggregate response
     * for the vehicle) per section A.4, Criteria for Monitor Performance Ratio
     * Evaluation.
     *
     * /**
     * <ul>
     * <li>A.4 CRITERIAFORMONITORPERFORMANCERATIOEVALUATION
     * Monitor performance ratio reporting identifies how frequently key emissions
     * control subsystems are completely evaluated against a specific operating
     * cycle definition (e.g., minimum 600 s in duration). In SAE J1939-73 this data
     * is reported using DM20. Tests for DM20 shall consider the following factors
     * <li>
     * <ul>
     * <li>1. Each response from a responding ECU/device shall be evaluated
     * separately:
     * <li>
     * <ul>
     * <li>a. Fail if no response received from any OBD ECU (i.e., ECUs that
     * indicates
     * 0x13, 0x14, 0x22, or 0x23 for OBD compliance in DM5).
     * <li>b. Warn if any response from non-OBD ECU received.
     * <li>c. Fail if any response does not report an ignition cycle counter and
     * general
     * denominator (bytes 1-4).
     * <li>d. Fail if any response does not correctly pad the unused bits (the 5
     * most
     * significant bits set to one) of any of the SPNs for which it is reporting
     * monitoring data (e.g., bytes 5-7, SPN 3066).
     * <li>d. Fail if any response does not correctly pad the unused bits (the 5
     * most
     * significant bits set to one) of any of the SPNs for which it is reporting
     * monitoring data (e.g., bytes 5-7, SPN 3066).
     * <li>e. Warn, if any SPN is reported but indicated as unsupported by reporting
     * 0xFFFF for both the numerator (bytes 8-9, SPN 3067) and for the denominator
     * (bytes 10-11, SPN 3068).
     * <li>f. Fail if any denominator (SPN 3068) for a reported SPN (SPN 3066) is
     * greater than the general denominator (SPN 3049) reported by that ECU.
     * </ul>
     * <li>2. All responses received from all responding ECUs/devices shall be
     * combined
     * with appropriate ‘AND/OR’ logic to create a composite vehicle monitor
     * performance response (i.e., if one or more responses indicates a particular
     * SPN as supported with numerator and denominator data, then the composite
     * vehicle monitor performance shall indicate that SPN is supported for monitor
     * performance data else it shall indicate unsupported for that SPN). Composite
     * vehicle monitor performance support shall be evaluated:
     * <li>
     * <ul>
     * <li>a. Fail if it does not indicate support for any of the SPNs in Table A-3
     * Composite Vehicle Monitor Performance Support.
     * <table border="1">
     * <tr>
     * <td><b>Monitored System</b></td>
     * <td><b>SPN</b></td>
     * <td><b>SPN Name</b></td>
     * </tr>
     * <tr>
     * <td>NMHC Converting Catalyst</td>
     * <td>5322</td>
     * <td>cell 23</td>
     * </tr>
     * <tr>
     * <td>Exhaust Gas Sensor</td>
     * <td>5318</td>
     * <td>cell 33</td>
     * </tr>
     * <tr>
     * <td>EGR System</td>
     * <td>3058</td>
     * <td>cell 43</td>
     * </tr>
     * <tr>
     * <td>PM Filter</td>
     * <td>3064</td>
     * <td>cell 53</td>
     * </tr>
     * <tr>
     * <td>Boost Pressure Control System</td>
     * <td>5321</td>
     * <td>cell 63</td>
     * </tr>
     * <tr>
     * <td>Fuel system</td>
     * <td>3055</td>
     * <td>cell 73</td>
     * </tr>
     * <tr>
     * <td>NOx Catalyst Bank 1 or NOx Absorber</td>
     * <td>
     * <tr>
     * 4364
     * </tr>
     * <tr>
     * 4792
     * </tr>
     * <tr>
     * 5308
     * </tr>
     * </td>
     * <td>
     * <tr>
     * SPN 4364, Aftertreatment 1 SCR Conversion Efficiency (Warning)
     * </tr>
     * <tr>
     * SPN 4792, Aftertreatment 1 SCR Catalyst System Monitor
     * </tr>
     * <tr>
     * SPN 5308, Aftertreatment 1 NOx Adsorbing Catalyst System Monitor
     * </tr>
     * </td>
     * </tr>
     * </table>
     * </ul>
     *
     * </ul>
     *
     * A.4 CRITERIAFORMONITORPERFORMANCERATIOEVALUATION
     * Monitor performance ratio reporting identifies how frequently key emissions
     * control subsystems are completely evaluated against a specific operating
     * cycle definition (e.g., minimum 600 s in duration). In SAE J1939-73 this data
     * is reported using DM20. Tests for DM20 shall consider the following factors
     * during the evaluation of responses:
     * 1. Each response from a responding ECU/device shall be evaluated separately:
     * a. Fail if no response received from any OBD ECU (i.e., ECUs that indicates
     * 0x13, 0x14, 0x22, or 0x23 for OBD compliance in DM5).
     * b. Warn if any response from non-OBD ECU received.
     * c. Fail if any response does not report an ignition cycle counter and general
     * denominator (bytes 1-4).
     * d. Fail if any response does not correctly pad the unused bits (the 5 most
     * significant bits set to one) of any of the SPNs for which it is reporting
     * monitoring data (e.g., bytes 5-7, SPN 3066).
     * e. Warn, if any SPN is reported but indicated as unsupported by reporting
     * 0xFFFF for both the numerator (bytes 8-9, SPN 3067) and for the denominator
     * (bytes 10-11, SPN 3068).
     * f. Fail if any denominator (SPN 3068) for a reported SPN (SPN 3066) is
     * greater than the general denominator (SPN 3049) reported by that ECU.
     * 2. All responses received from all responding ECUs/devices shall be combined
     * with appropriate ‘AND/OR’ logic to create a composite vehicle monitor
     * performance response (i.e., if one or more responses indicates a particular
     * SPN as supported with numerator and denominator data, then the composite
     * vehicle monitor performance shall indicate that SPN is supported for monitor
     * performance data else it shall indicate unsupported for that SPN). Composite
     * vehicle monitor performance support shall be evaluated:
     * a. Fail if it does not indicate support for any of the SPNs in Table A-3
     * Composite Vehicle Monitor Performance Support.
     * Table A-3-1 - Composite vehicle monitor support (Diesel)
     * Monitored System
     * NMHC Converting Catalyst
     * Exhaust Gas Sensor
     * EGR System
     * PM Filter
     * Boost Pressure Control System
     * Fuel system
     * NOx Catalyst Bank 1 or NOx Adsorber
     * SPN
     * 5322
     * 5318
     * 3058
     * 3064
     * 5321
     * 3055 4792, 5308, or 4364,
     * SPN Name
     * Aftertreatment NMHC Converting Catalyst System Monitor Aftertreatment Exhaust
     * Gas Sensor System Monitor
     * EGR System Monitor
     * Aftertreatment Diesel Particulate Filter System Monitor Engine Intake
     * Manifold Pressure System Monitor
     * Engine Fuel System Monitor
     * SPN 4792, Aftertreatment 1 SCR Catalyst System Monitor
     * SPN 5308, Aftertreatment 1 NOx Adsorbing Catalyst System Monitor
     * SPN 4364, Aftertreatment 1 SCR Conversion Efficiency (Warning)
     */

    @Override
    protected void run() throws Throwable {
        // 6.1.8 DM20: Monitor Performance Ratio
        diagnosticReadinessModule.setJ1939(getJ1939());

        // 6.1.8.1.a. Global DM20 (send Request (PGN 59904) for PGN 49664
        List<DM20MonitorPerformanceRatioPacket> globalDM20s = diagnosticReadinessModule.getDM20Packets(getListener(),
                true);

        /*
         * TODO Get PerformanceRatios off Packets and Save in OBDModuleInformation by
         * Source Address (the below loop is wrong)
         */
        // 6.1.8.1 Actions:
        // 6.1.8.1.a.i. Create list of ECU address
        for (DM20MonitorPerformanceRatioPacket packet : globalDM20s) {
            int sourceAddress = packet.getSourceAddress();
            // Save performance ratio on the obdModule for each ECU
            OBDModuleInformation obdModule = dataRepository.getObdModule(sourceAddress);
            if (obdModule == null) {
                obdModule = new OBDModuleInformation(sourceAddress);
            }
            obdModule.setPerformanceRatios(packet.getRatios());
        }

        // Gather all the spn of performance ratio from the vehicle
        Set<Integer> dm20Spns = globalDM20s.stream()
                .flatMap(dm20 -> dm20.getRatios().stream())
                .map(ratio -> ratio.getSpn())
                .collect(Collectors.toSet());

        dm20Spns.forEach(dm20Spn -> {
            // verifyMinimumExpectedSpnSupported(dm20Spn);
            System.out.println(dm20Spn);
        });

        verifyMinimumExpectedSpnSupported(dm20Spns);
    }

    /**
     * This method verifies that the minimum expected SPNs are supported per the
     * "Criteria for Monitor Performance Ratio Evaluation" section A.4 of J1939_84
     *
     * @param dm20Spns
     *
     */
    private void verifyMinimumExpectedSpnSupported(Set<Integer> dm20Spns) {
        VehicleInformation vehicleInfo = dataRepository.getVehicleInformation();
        FuelType fuelType = vehicleInfo.getFuelType();

        if (fuelType.isCompressionIgnition()) {

            // List of SPNs per Fuel type that is checked with the SPNs Stored in stream
            // Last three SPNs for the NOx Catalyst Bank 1 or NOx Absorber Don't all have to
            // be in there. It can be any combination of the three.
            int SPNa[] = { 5322, 5318, 3058, 3064, 5321, 3055 };
            int SPNn[] = { 4792, 5308, 4364 };

            if (!IntStream.of(SPNa).allMatch(spn -> dm20Spns.contains(spn))
                    || !IntStream.of(SPNn).anyMatch(spn -> dm20Spns.contains(spn))) {
                addFailure(1, 8, "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");
            }
        } else if (fuelType.isSparkIgnition()) {
            // TODO Add the Outlet Oxygen Sensor Banks in Table A-3-2 (pg.111) with
            // non-integer variables i.e. New1, New2 at the end of the table.
            int SPNsi[] = { 3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057 };
            if (!IntStream.of(SPNsi).allMatch(spn -> dm20Spns.contains(spn))) {
                addFailure(1, 8, "6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");
            }
        } else {
            // TODO Check this
            getLogger().info("Ignition Type not supported in Monitor Performance Ratio Evaluation.");
        }
    }
}
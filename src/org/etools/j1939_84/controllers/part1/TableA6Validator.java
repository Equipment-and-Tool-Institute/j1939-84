/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.Outcome;

/**
 * The validator for Table A.6.1 Composite vehicle readiness - Diesel Engines &
 * Table A.6.2 Composite vehicle readiness - Spark Ignition Engines
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class TableA6Validator {

    private static final String TABLE_NAME = "TableA6";

    private static Collection<MonitoredSystemStatus> findStatus(boolean isSupported, boolean isComplete) {
        List<MonitoredSystemStatus> statuses = new ArrayList<>();
        statuses.add(MonitoredSystemStatus.findStatus(false, isSupported, isComplete));
        statuses.add(MonitoredSystemStatus.findStatus(true, isSupported, isComplete));
        return statuses;
    }

    private static boolean recordProgress(MonitoredSystem system, ResultsListener listener,
            List<MonitoredSystemStatus> acceptableStatuses) {
        boolean passed = true;
        String name = system.getName().trim();
        if (acceptableStatuses.isEmpty()) {
            listener.onProgress(
                    "INFO: " + TABLE_NAME + " " + name + " verification" + NL
                            + "This test is only valid for compression or spark ignition");

        } else if (acceptableStatuses.contains(system.getStatus())) {
            listener.onProgress("PASS: " + TABLE_NAME + " " + name + " verification");
        } else {
            listener.onProgress("FAIL: " + TABLE_NAME + " " + name + " verification");
            passed = false;
        }
        return passed;
    }

    private final DataRepository dataRepository;

    public TableA6Validator(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * @param system
     * @param listener
     */
    private void addOutcome(MonitoredSystem system, ResultsListener listener, Outcome outcome, int partNumber,
            int stepNumber) {
        listener.addOutcome(partNumber, stepNumber, outcome,
                TABLE_NAME + " " + system.getName().trim() + " is " + system.getStatus().toString());
    }

    private FuelType getFuelType() {
        return dataRepository.getVehicleInformation().getFuelType();
    }

    private int getModelYear() {
        return dataRepository.getVehicleInformation().getEngineModelYear();
    }

    private boolean isAfterCodeClear() {
        return dataRepository.isAfterCodeClear();
    }

    private boolean validateAcSystemRefrigerant(MonitoredSystem system, ResultsListener listener) {

        // Only state allowed for either compression or spark is
        // not-enabled/not-complete
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(false, false));
            }
        };
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateBoostPressureControlSystem(MonitoredSystem system, ResultsListener listener) {

        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();

        // Check if code clear has happened
        if (!isAfterCodeClear()) {
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for both compression and spark
            acceptableStatuses.addAll(findStatus(true, false));
        }
        // Compression ignition only allows it state of enabled
        // and completed
        if (fuelType.isCompressionIgnition()) {
            acceptableStatuses.addAll(findStatus(true, true));
        } else if (fuelType.isSparkIgnition()) {
            // Spark ignition allows two states of not-enabled/not-complete
            // and enabled/complete
            acceptableStatuses.addAll(findStatus(false, false));
            acceptableStatuses.addAll(findStatus(true, true));

        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateCatalyst(MonitoredSystem system, ResultsListener listener) {

        FuelType fuelType = getFuelType();

        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();

        if (fuelType.isCompressionIgnition()) {
            // Compression engines shall not have catalyst system supported
            // and not complete
            acceptableStatuses.addAll(findStatus(false, false));
        } else if (fuelType.isSparkIgnition()) {
            // Spark engines need to have the catalyst system supported
            // and complete
            acceptableStatuses.addAll(findStatus(true, true));
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateColdStartAidSystem(MonitoredSystem system, ResultsListener listener, int partNumber,
            int stepNumber) {

        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(true, true));
                addAll(findStatus(false, false));
            }
        };
        // Warning is required if spark ignition and the system isn't enabled
        if (fuelType.isSparkIgnition() && !system.getStatus().isEnabled()) {
            addOutcome(system, listener, WARN, partNumber, stepNumber);
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateComprehensiveComponent(MonitoredSystem system, ResultsListener listener) {

        // Comprehesive Component must be supported and complete for
        // compression and spark ignition
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(true, true));
            }
        };
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateDieselParticulateFilter(MonitoredSystem system, ResultsListener listener) {
        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();

        if (fuelType.isCompressionIgnition()) {
            // Check if code clear has happened
            if (!isAfterCodeClear()) {
                // If a code clear hasn't happened the additional
                // state of enable but not complete is allowed
                // for compression ignition
                acceptableStatuses.addAll(findStatus(true, false));
            }
            // Compression ignition only allows for enabled/complete
            // unless a code clear hasn't happened
            acceptableStatuses.addAll(findStatus(true, true));
        } else if (fuelType.isSparkIgnition()) {
            // Spark ignition only allows for not-enabled/not-complete
            acceptableStatuses.addAll(findStatus(false, false));
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateEgrVvtSystem(MonitoredSystem system, ResultsListener listener, int partNumber,
            int stepNumber) {

        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(true, true));
            }
        };
        if (!isAfterCodeClear()) {
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for both compression and spark ignition
            acceptableStatuses.addAll(findStatus(true, false));
        }
        if (fuelType.isSparkIgnition()) {
            // Warning is required if spark ignition and the system isn't
            // enabled
            if (!system.getStatus().isEnabled()) {
                addOutcome(system, listener, WARN, partNumber, stepNumber);
            }
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateEvaporativeSystem(MonitoredSystem system, ResultsListener listener) {
        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();

        if (fuelType.isCompressionIgnition()) {
            // Compression ignition only allows for not-enabled/not-complete
            acceptableStatuses.addAll(findStatus(false, false));
        } else if (fuelType.isSparkIgnition()) {
            // Spark ignition only allows for enabled/complete
            acceptableStatuses.addAll(findStatus(true, true));
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateExhaustGasSensor(MonitoredSystem system, ResultsListener listener) {

        // Sensor should be enabled and complete for both compression
        // and spark ignition
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(true, true));
            }
        };
        if (!isAfterCodeClear()) {
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for both compression and spark ignition
            acceptableStatuses.addAll(findStatus(true, false));
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateExhaustGasSensorHeater(MonitoredSystem system, ResultsListener listener, int partNumber,
            int stepNumber) {

        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(true, true));
                addAll(findStatus(false, false));
            }
        };
        if (!isAfterCodeClear()) {
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for both compression and spark ignition
            acceptableStatuses.addAll(findStatus(true, false));
        }

        // Warning is required if the engine model year is 2013
        // and the system is not-enabled for compression ignition only
        if (fuelType.isCompressionIgnition() && !system.getStatus().isEnabled() && getModelYear() == 2013) {
            addOutcome(system, listener, WARN, partNumber, stepNumber);
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateFuelSystem(MonitoredSystem system, ResultsListener listener) {

        // Compression and spark ignition shall have the system enabled
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(true, true));
            }
        };
        // If a code clear hasn't happened the additional
        // state of enable but not complete is allowed
        // for both compression and spark ignition
        if (!isAfterCodeClear()) {
            acceptableStatuses.addAll(findStatus(true, false));
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateHeatedCatalyst(MonitoredSystem system, ResultsListener listener) {

        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        if (fuelType.isCompressionIgnition()) {
            acceptableStatuses.addAll(findStatus(false, false));
        } else if (fuelType.isSparkIgnition()) {
            acceptableStatuses.addAll(findStatus(true, true));
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for spark ignition
            if (!isAfterCodeClear()) {
                acceptableStatuses.addAll(findStatus(true, false));
            }
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateMisfire(MonitoredSystem system, ResultsListener listener, int partNumber, int stepNumber) {

        FuelType fuelType = getFuelType();
        // Compression and spark ignition allows for enabled/complete OR
        // not-enabled/not-complete
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(true, true));
                addAll(findStatus(false, false));
            }
        };
        if (!isAfterCodeClear() || fuelType.isSparkIgnition()) {
            // If a code clear hasn't happened for compression ignition add
            // additional state OR this is spark ignition which has only the
            // state of enable but not complete
            acceptableStatuses.addAll(findStatus(true, false));

        }
        // Warning is required if the system is not-complete for compression
        // ignition only after a code clear has happened
        if (fuelType.isCompressionIgnition()
                && !system.getStatus().isComplete()
                && isAfterCodeClear()) {
            addOutcome(system, listener, WARN, partNumber, stepNumber);
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateNmhcConvertingCatalyst(MonitoredSystem system, ResultsListener listener) {

        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        if (fuelType.isCompressionIgnition()) {
            // Compression ignition shall have the system enabled/complete
            acceptableStatuses.addAll(findStatus(true, true));
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for compression ignition
            if (!isAfterCodeClear()) {
                acceptableStatuses.addAll(findStatus(true, false));
            }
        } else if (fuelType.isSparkIgnition()) {
            // Spark ignition shall have the system not-enabled/not-complete
            acceptableStatuses.addAll(findStatus(false, false));
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateNoxCatalystAbsorber(MonitoredSystem system, ResultsListener listener) {

        FuelType fuelType = getFuelType();
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        if (fuelType.isCompressionIgnition()) {
            // Compression ignition shall have the system enabled/complete
            acceptableStatuses.addAll(findStatus(true, true));
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for compression ignition
            if (!isAfterCodeClear()) {
                acceptableStatuses.addAll(findStatus(true, false));
            }
        } else {
            // Spark ignition shall have the system not-enabled/not-complete
            acceptableStatuses.addAll(findStatus(false, false));
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateSecondaryAirSystem(MonitoredSystem system, ResultsListener listener) {

        // Compression and spark ignition both allow for
        // not-enabled/not-complete
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>() {
            {
                addAll(findStatus(false, false));
            }
        };
        if (getFuelType().isSparkIgnition()) {
            // Spark ignition also allows for enabled/complete
            acceptableStatuses.addAll(findStatus(true, true));
            // If a code clear hasn't happened the additional
            // state of enable but not complete is allowed
            // for spark ignition
            if (!isAfterCodeClear()) {
                acceptableStatuses.addAll(findStatus(true, false));
            }
        }
        return recordProgress(system, listener, acceptableStatuses);
    }

    private boolean validateSystem(MonitoredSystem system, ResultsListener listener, int partNumber, int stepNumber) {

        switch (system.getId()) {
        case AC_SYSTEM_REFRIGERANT:
            return validateAcSystemRefrigerant(system, listener);
        case BOOST_PRESSURE_CONTROL_SYS:
            return validateBoostPressureControlSystem(system, listener);
        case CATALYST:
            return validateCatalyst(system, listener);
        case COLD_START_AID_SYSTEM:
            return validateColdStartAidSystem(system, listener, partNumber, stepNumber);
        case COMPREHENSIVE_COMPONENT:
            return validateComprehensiveComponent(system, listener);
        case DIESEL_PARTICULATE_FILTER:
            return validateDieselParticulateFilter(system, listener);
        case EGR_VVT_SYSTEM:
            return validateEgrVvtSystem(system, listener, partNumber, stepNumber);
        case EVAPORATIVE_SYSTEM:
            return validateEvaporativeSystem(system, listener);
        case EXHAUST_GAS_SENSOR:
            return validateExhaustGasSensor(system, listener);
        case EXHAUST_GAS_SENSOR_HEATER:
            return validateExhaustGasSensorHeater(system, listener, partNumber, stepNumber);
        case FUEL_SYSTEM:
            return validateFuelSystem(system, listener);
        case HEATED_CATALYST:
            return validateHeatedCatalyst(system, listener);
        case MISFIRE:
            return validateMisfire(system, listener, partNumber, stepNumber);
        case NMHC_CONVERTING_CATALYST:
            return validateNmhcConvertingCatalyst(system, listener);
        case NOX_CATALYST_ABSORBER:
            return validateNoxCatalystAbsorber(system, listener);
        case SECONDARY_AIR_SYSTEM:
            return validateSecondaryAirSystem(system, listener);
        default:
            return false;
        }
    }

    public boolean verify(ResultsListener listener, DiagnosticReadinessPacket packet, int partNumber, int stepNumber) {

        boolean[] passed = { true };

        List<MonitoredSystem> systems = packet.getMonitoredSystems().stream().collect(Collectors.toList());

        Collections.sort(systems);
        systems.forEach(system -> {
            if (!validateSystem(system, listener, partNumber, stepNumber)) {
                passed[0] = false;
            }
        });

        return passed[0];
    }

}
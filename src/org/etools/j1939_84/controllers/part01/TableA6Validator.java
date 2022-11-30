/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.MonitoredSystemStatus;

/**
 * The validator for Table A.6.1 Composite vehicle readiness - Diesel Engines &
 * Table A.6.2 Composite vehicle readiness - Spark Ignition Engines
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class TableA6Validator {

    private final DataRepository dataRepository;
    private final int partNumber;
    private final int stepNumber;

    public TableA6Validator(DataRepository dataRepository, int partNumber, int stepNumber) {
        this.dataRepository = dataRepository;
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
    }

    public void verify(ResultsListener listener,
                       List<CompositeMonitoredSystem> systems,
                       String section,
                       boolean engineHasRun) {
        systems.forEach(system -> validateSystem(system, listener, section, engineHasRun));
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean
            validateSystem(MonitoredSystem system, ResultsListener listener, String section, boolean engineHasRun) {

        switch (system.getId()) {
            case AC_SYSTEM_REFRIGERANT:
                return validateAcSystemRefrigerant(system, listener, section);
            case BOOST_PRESSURE_CONTROL_SYS:
                return validateBoostPressureControlSystem(system, listener, section);
            case CATALYST:
                return validateCatalyst(system, listener, section);
            case COLD_START_AID_SYSTEM:
                return validateColdStartAidSystem(system, listener, section);
            case COMPREHENSIVE_COMPONENT:
                return validateComprehensiveComponent(system, listener, section);
            case DIESEL_PARTICULATE_FILTER:
                return validateDieselParticulateFilter(system, listener, section);
            case EGR_VVT_SYSTEM:
                return validateEgrVvtSystem(system, listener, section);
            case EVAPORATIVE_SYSTEM:
                return validateEvaporativeSystem(system, listener, section);
            case EXHAUST_GAS_SENSOR:
                return validateExhaustGasSensor(system, listener, section);
            case EXHAUST_GAS_SENSOR_HEATER:
                return validateExhaustGasSensorHeater(system, listener, section);
            case FUEL_SYSTEM:
                return validateFuelSystem(system, listener, section, engineHasRun);
            case HEATED_CATALYST:
                return validateHeatedCatalyst(system, listener, section);
            case MISFIRE:
                return validateMisfire(system, listener, section, engineHasRun);
            case NMHC_CONVERTING_CATALYST:
                return validateNmhcConvertingCatalyst(system, listener, section);
            case NOX_CATALYST_ABSORBER:
                return validateNoxCatalystAbsorber(system, listener, section);
            case SECONDARY_AIR_SYSTEM:
                return validateSecondaryAirSystem(system, listener, section);
            default:
                return false;
        }
    }

    private boolean validateAcSystemRefrigerant(MonitoredSystem system, ResultsListener listener, String section) {
        return validate(system, listener, findStatus(0, 0), section);
    }

    private boolean validateBoostPressureControlSystem(MonitoredSystem system,
                                                       ResultsListener listener,
                                                       String section) {

        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();

        // Compression ignition only allows it state of enabled and completed
        if (isCompressionIgnition()) {
            acceptableStatuses.addAll(findStatus(1, 1));
        } else if (isSparkIgnition()) {
            // Spark ignition allows two states of not-enabled/not-complete and enabled/complete
            acceptableStatuses.addAll(findStatus(0, 0));
            acceptableStatuses.addAll(findStatus(1, 1));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateCatalyst(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        if (isCompressionIgnition()) {
            // Compression engines shall not have catalyst system supported and not complete
            acceptableStatuses.addAll(findStatus(0, 0));
        } else if (isSparkIgnition()) {
            // Spark engines need to have the catalyst system supported and complete
            acceptableStatuses.addAll(findStatus(1, 1));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateColdStartAidSystem(MonitoredSystem system, ResultsListener listener, String section) {

        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        acceptableStatuses.addAll(findStatus(0, 0));
        acceptableStatuses.addAll(findStatus(1, 1));

        // Warning is required if spark ignition and the system isn't enabled
        if (isSparkIgnition() && !system.getStatus().isEnabled()) {
            addWarning(system, listener, section);
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateComprehensiveComponent(MonitoredSystem system, ResultsListener listener, String section) {
        // Comprehensive Component must be supported and complete for compression and spark ignition
        return validate(system, listener, findStatus(1, 0), section);
    }

    private boolean validateDieselParticulateFilter(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();

        if (isCompressionIgnition()) {
            // Compression ignition only allows for enabled/complete
            acceptableStatuses.addAll(findStatus(1, 1));
        } else if (isSparkIgnition()) {
            // Spark ignition only allows for not-enabled/not-complete
            acceptableStatuses.addAll(findStatus(0, 0));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateEgrVvtSystem(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>(findStatus(1, 1));
        if (isSparkIgnition()) {
            // Warning is required if spark ignition and the system isn't enabled
            if (!system.getStatus().isEnabled()) {
                addWarning(system, listener, section);
            }
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateEvaporativeSystem(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();

        if (isCompressionIgnition()) {
            // Compression ignition only allows for not-enabled/not-complete
            acceptableStatuses.addAll(findStatus(0, 0));
        } else if (isSparkIgnition()) {
            // Spark ignition only allows for enabled/complete
            acceptableStatuses.addAll(findStatus(1, 1));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateExhaustGasSensor(MonitoredSystem system, ResultsListener listener, String section) {
        // Sensor should be enabled and complete for both compression and spark ignition
        return validate(system, listener, findStatus(1, 1), section);
    }

    private boolean validateExhaustGasSensorHeater(MonitoredSystem system, ResultsListener listener, String section) {

        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>(findStatus(1, 1));

        // Warning is required if the engine model year is 2013
        // and the system is not-enabled for compression ignition only
        if (isCompressionIgnition() && !system.getStatus().isEnabled()
                && (getModelYear() >= 2013 && getModelYear() <= 2015)) {
            acceptableStatuses.addAll(findStatus(0, 0));
            addWarning(system, listener, section);
        }

        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean
            validateFuelSystem(MonitoredSystem system, ResultsListener listener, String section, boolean engineHasRun) {
        // Compression and spark ignition shall have the system enabled
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        acceptableStatuses.addAll(findStatus(1, 1));
        if (isCompressionIgnition() && engineHasRun && system.getStatus().isComplete()) {
            acceptableStatuses.addAll(findStatus(1, 0));
            addOutcome(system, listener, section, INFO);
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateHeatedCatalyst(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        acceptableStatuses.addAll(findStatus(0, 0));
        if (isSparkIgnition() && system.getStatus().isEnabled()) {
            acceptableStatuses.addAll(findStatus(1, 1));
            addWarning(system, listener, section);
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean
            validateMisfire(MonitoredSystem system, ResultsListener listener, String section, boolean engineHasRun) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        if (isCompressionIgnition()) {
            acceptableStatuses.addAll(findStatus(1, 1));
            if (system.getStatus().isComplete()) {
                if (getModelYear() >= 2019) {
                    addOutcome(system, listener, section, WARN);
                    acceptableStatuses.addAll(findStatus(1, 0));
                } else if (engineHasRun && getModelYear() < 2013) {
                    addOutcome(system, listener, section, INFO);
                    acceptableStatuses.addAll(findStatus(1, 0));
                }
            }
        } else if (isSparkIgnition()) {
            acceptableStatuses.addAll(findStatus(1, 0));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateNmhcConvertingCatalyst(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        if (isCompressionIgnition()) {
            acceptableStatuses.addAll(findStatus(1, 1));
        } else if (isSparkIgnition()) {
            acceptableStatuses.addAll(findStatus(0, 0));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateNoxCatalystAbsorber(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>();
        if (isCompressionIgnition()) {
            acceptableStatuses.addAll(findStatus(1, 1));
        } else {
            acceptableStatuses.addAll(findStatus(0, 0));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validateSecondaryAirSystem(MonitoredSystem system, ResultsListener listener, String section) {
        List<MonitoredSystemStatus> acceptableStatuses = new ArrayList<>(findStatus(0, 0));
        if (isSparkIgnition()) {
            acceptableStatuses.addAll(findStatus(1, 1));
        }
        return validate(system, listener, acceptableStatuses, section);
    }

    private boolean validate(MonitoredSystem system,
                             ResultsListener listener,
                             List<MonitoredSystemStatus> acceptableStatuses,
                             String section) {
        if (!acceptableStatuses.contains(system.getStatus())) {
            listener.addOutcome(partNumber,
                                stepNumber,
                                FAIL,
                                section + " - Composite vehicle readiness for " + system.getName().trim()
                                        + " did not meet the criteria of Table A4");
            return false;
        }
        return true;
    }

    private void addOutcome(MonitoredSystem system,
                            ResultsListener listener,
                            String section,
                            Outcome outcome) {

        String status = system.getStatus().toString().replaceAll(" {4}", "");
        listener.addOutcome(partNumber,
                            stepNumber,
                            outcome,
                            section + " - " + system.getName().trim() + " is " + status);
    }

    private void addWarning(MonitoredSystem system,
                            ResultsListener listener,
                            String section) {
        addOutcome(system, listener, section, WARN);
    }

    private boolean isCompressionIgnition() {
        return getFuelType().isCompressionIgnition();
    }

    private boolean isSparkIgnition() {
        return getFuelType().isSparkIgnition();
    }

    private FuelType getFuelType() {
        return dataRepository.getVehicleInformation().getFuelType();
    }

    private int getModelYear() {
        return dataRepository.getVehicleInformation().getEngineModelYear();
    }

    private static List<MonitoredSystemStatus> findStatus(int support, int status) {
        return List.of(MonitoredSystemStatus.findStatus(true, support == 1, status == 0));
    }

}

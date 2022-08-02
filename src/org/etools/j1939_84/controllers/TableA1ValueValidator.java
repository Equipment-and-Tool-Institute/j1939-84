/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import org.etools.j1939tools.j1939.model.FuelType;

public class TableA1ValueValidator {

    private final DataRepository dataRepository;

    TableA1ValueValidator() {
        this(DataRepository.getInstance());
    }

    TableA1ValueValidator(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * Returns true if the supplied value is not valid (implausible) given the conditions according to Table A1
     */
    public boolean isImplausible(int spn, Double value, boolean isEngineRunning) {

        if (value == null) {
            return false;
        }

        switch (spn) {
            case 92:
            case 513:
                if (isEngineRunning) {
                    return value >= 50;
                } else {
                    return value > 0;
                }
            case 512:
            case 91:
                return value > 0;
            case 514:
                if (isEngineRunning) {
                    return value <= 0;
                } else {
                    return value > 0;
                }
            case 2978:
                if (isEngineRunning) {
                    return value > 8 || value < 0;
                } else {
                    return value > 0;
                }
            case 539:
            case 540:
                return value < 10;
            case 541:
                if (isEngineRunning) {
                    return value < 30;
                } else {
                    return value < 20;
                }
            case 542:
                if (isEngineRunning) {
                    return value < 20;
                } else {
                    return value < 15;
                }
            case 543:
                return value > 100;
            case 544:
                if (isEngineRunning) {
                    return value < 200 || value > 4000 || value != dataRepository.getKoeoEngineReferenceTorque();
                } else {
                    dataRepository.setKoeoEngineReferenceTorque(value);
                    return value < 200 || value > 4000;
                }
            case 110:
            case 175:
            case 1637:
            case 4076:
            case 4193:
            case 3031:
            case 3515:
                return value < -7 || value > 110;
            case 190:
            case 4201:
            case 723:
            case 4202:
                if (isEngineRunning) {
                    return value < 250;
                } else {
                    return value > 0;
                }
            case 84:
                return value != 0;
            case 108:
                return !isEngineRunning && (value < 25 || value > 110);
            case 158:
            case 168:
                return isEngineRunning && value < 6;
            case 3700:
                return !isEngineRunning && value == 1; // Active
            case 5837:
                if (getFuelType().isCompressionIgnition()) {
                    return value != 4; // Diesel
                }
                if (getFuelType().isSparkIgnition()) {
                    return value == 4; // Diesel
                }
                break;
            case 183:
            case 1600:
                if (isEngineRunning) {
                    return value == 0 || value > 4;
                } else {
                    return value > 0;
                }
            case 6895:
                if (isEngineRunning) {
                    return value < 15;
                } else {
                    return value > 0;
                }
            case 7333:
            case 2659:
            case 12750:
            case 12751:
            case 6894:
            case 4331:
            case 6595:
            case 12752:
            case 4348:
            case 6593:
            case 3481:
            case 5503:
            case 12743:
            case 3479:
            case 5444:
                return !isEngineRunning && value > 0;
            case 3609:
            case 3610:
            case 3251:
                return !isEngineRunning && value > 3;
            case 3226:
                return !isEngineRunning && value != 3012.8 && value > 500;
            case 132:
                if (isEngineRunning) {
                    return value < 0;
                } else {
                    return value > 0.20;
                }
            case 6393:
                if (isEngineRunning) {
                    return value < 0;
                } else {
                    return value > 0.04;
                }
            case 102:
            case 1127:
                if (isEngineRunning) {
                    return value > 21;
                } else {
                    return value > 4;
                }
            case 106:
            case 3563:
                if (isEngineRunning) {
                    return value > 121;
                } else {
                    return value > 105;
                }
            case 5829:
                if (isEngineRunning) {
                    return value > 50;
                } else {
                    return value > 5;
                }
            case 4360:
            case 4363:
                return !isEngineRunning && value < 7;

        }
        return false;
    }

    private FuelType getFuelType() {
        return dataRepository.getVehicleInformation().getFuelType();
    }

}

/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import org.etools.j1939_84.model.FuelType;

public class TableA1ValueValidator {

    private final DataRepository dataRepository;

    TableA1ValueValidator(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * Returns true if the supplied value is not valid (implausible) given the conditions according to Table A1
     */
    public boolean isImplausible(int spn, Double value, boolean isEngineOn, FuelType fuelType) {
        if (value == null) {
            return false;
        }

        switch (spn) {
        case 92:
        case 513:
            if (isEngineOn) {
                return value >= 50;
            } else {
                return value > 0;
            }
        case 512:
        case 91:
            return value > 0;
        case 514:
        case 2978:
            if (isEngineOn) {
                return value <= 0;
            } else {
                return value > 0;
            }
        case 539:
        case 540:
            return value < 10;
        case 541:
            if (isEngineOn) {
                return value < 30;
            } else {
                return value < 20;
            }
        case 542:
            if (isEngineOn) {
                return value < 20;
            } else {
                return value < 15;
            }
        case 543:
            return value >= 100;
        case 544:
            if (isEngineOn) {
                return value < 200 || value > 4000 || value != dataRepository.getKoeoEngineReferenceTorque();
            } else {
                dataRepository.setKoeoEngineReferenceTorque(value);
                return value < 200 || value > 4000;
            }
        case 110:
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
            if (isEngineOn) {
                return value < 250;
            } else {
                return value > 0;
            }
        case 84:
            return value != 0;
        case 108:
            return !isEngineOn && (value < 25 || value > 110);
        case 158:
        case 168:
            return isEngineOn && value < 6;
        case 3700:
            return !isEngineOn && value == 1; //Active
        case 5837:
            if (fuelType.isCompressionIgnition()) {
                return value != 4; //Diesel
            }
            if (fuelType.isSparkIgnition()) {
                return value == 4; //Diesel
            }
            break;
        case 183:
        case 1600:
            if (isEngineOn) {
                return value == 0 || value > 4;
            } else {
                return value > 0;
            }
        case 6895:
            if (isEngineOn) {
                return value < 15;
            } else {
                return value > 0;
            }
        case 7333:
        case 3609:
        case 3610:
        case 3251:
            return !isEngineOn && value > 0;
        case 3226:
            return !isEngineOn && value != 3012.8 && value > 500;
        case 132:
            if (isEngineOn) {
                return value < 0;
            } else {
                return value > 0.20;
            }
        case 6393:
            if (isEngineOn) {
                return value < 0;
            } else {
                return value > 0.04;
            }
        case 102:
        case 1127:
            if (isEngineOn) {
                return value > 10;
            } else {
                return value > 2;
            }
        case 106:
        case 3563:
            if (isEngineOn) {
                return value > 111;
            } else {
                return value > 104;
            }
        case 5829:
            if (isEngineOn) {
                return value > 50;
            } else {
                return value > 5;
            }
        }
        return false;
    }

}

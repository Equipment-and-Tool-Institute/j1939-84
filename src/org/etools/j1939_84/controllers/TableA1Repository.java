/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.PASS;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.model.Outcome;
import org.etools.j1939tools.j1939.model.FuelType;

public class TableA1Repository {

    private static TableA1Repository instance;

    public static TableA1Repository getInstance() {
        if (instance == null) {
            instance = new TableA1Repository();
        }
        return instance;
    }

    private List<TableA1Row> rows;

    public TableA1Repository() {
    }

    private List<TableA1Row> getRows() {
        if (rows == null) {
            rows = new ArrayList<>(175);
            rows.add(new TableA1Row(92, 2013, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(512, 2013, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(513, 2013, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(514, 2016, true, true, false, false, null, FAIL, FAIL));
            rows.add(new TableA1Row(2978, 2016, true, true, false, false, null, FAIL, FAIL));
            rows.add(new TableA1Row(539, 2013, true, true, false, false, null, FAIL, FAIL));
            rows.add(new TableA1Row(540, 2013, true, true, false, false, null, FAIL, FAIL));
            rows.add(new TableA1Row(541, 2013, true, true, false, false, null, FAIL, FAIL));
            rows.add(new TableA1Row(542, 2013, true, true, false, false, null, FAIL, FAIL));
            rows.add(new TableA1Row(543, 2013, true, true, false, false, null, FAIL, FAIL));
            rows.add(new TableA1Row(188, 2013, true, true, false, false, null, FAIL, null));
            rows.add(new TableA1Row(544, 2013, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(110, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(1637, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(4076, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(4193, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(190, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(4201, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(723, 2013, true, true, false, false, FAIL, FAIL, WARN));

            rows.add(new TableA1Row(4202, 2013, true, true, false, false, FAIL, FAIL, null));
            rows.add(new TableA1Row(84, 2013, true, true, false, false, INFO, INFO, WARN));
            rows.add(new TableA1Row(91, 2013, true, true, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(108, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(158, 2013, true, true, false, false, null, INFO, INFO));
            rows.add(new TableA1Row(168, 2013, true, true, false, false, null, INFO, INFO));
            rows.add(new TableA1Row(5466, 2016, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(3719, 2013, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3700, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(5827, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(5454, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(235, 2013, true, true, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(247, 2013, true, true, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(248, 2013, true, true, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(5837, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(183, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(1600, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(1413, 2013, true, true, false, false, FAIL, FAIL, WARN));

            rows.add(new TableA1Row(1433, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(1436, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(132, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(6393, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(3609, 2013, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(3610, 2013, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(3251, 2013, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(102, 2013, true, true, false, false, WARN, WARN, WARN));
            rows.add(new TableA1Row(106, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(1127, 2013, true, true, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(3563, 2013, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(2791, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(5829, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(27, 2013, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(94, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(157, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(164, 2013, true, false, false, false, FAIL, FAIL, WARN));

            rows.add(new TableA1Row(5313, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(5314, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(5578, 2013, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(3226, 2013, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(175, 2013, true, true, false, false, FAIL, WARN, INFO));
            rows.add(new TableA1Row(6895, 2016, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(7333, 2016, true, true, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(3516, 2016, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(3518, 2016, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(7346, 2016, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(3031, 2016, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(3515, 2016, true, false, false, false, FAIL, WARN, WARN));
            rows.add(new TableA1Row(96, 2016, true, true, false, false, INFO, INFO, INFO));
            rows.add(new TableA1Row(38, 2016, true, true, false, false, INFO, INFO, INFO));
            rows.add(new TableA1Row(2848, 2016, true, true, false, false, null, null, null));
            rows.add(new TableA1Row(51, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3464, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(4236, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(4237, 2013, false, true, false, false, FAIL, FAIL, FAIL));

            rows.add(new TableA1Row(4240, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3249, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3245, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3241, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3217, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3227, 2013, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(166, 2022, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(12744, 2024, false, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(12750, 2022, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(12751, 2022, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(4360, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(6894, 2024, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(4331, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(6595, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(12752, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(12753, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(245, 2024, true, true, false, false, INFO, INFO, INFO));
            rows.add(new TableA1Row(917, 2024, true, true, false, false, INFO, INFO, INFO));

            rows.add(new TableA1Row(4348, 2024, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(6593, 2024, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(12749, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(4363, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(2659, 2024, true, true, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(4331, 2024, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(6595, 2024, true, false, false, false, FAIL, FAIL, WARN));
            rows.add(new TableA1Row(3230, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3220, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3481, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(5503, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(12743, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3479, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(3480, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(4334, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(2630, 2024, true, false, false, false, FAIL, FAIL, FAIL));

            rows.add(new TableA1Row(12758, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(101, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(5444, 2024, true, false, false, false, FAIL, FAIL, FAIL));
            rows.add(new TableA1Row(74, 2024, true, false, false, false, WARN, INFO, INFO));
            rows.add(new TableA1Row(7315, 2024, false, false, true, true, FAIL, WARN, WARN));
            rows.add(new TableA1Row(7896, 2024, false, false, false, true, FAIL, WARN, WARN));
            rows.add(new TableA1Row(8086, 2024, false, false, true, true, FAIL, WARN, WARN));
            rows.add(new TableA1Row(5919, 2024, false, false, true, true, FAIL, WARN, WARN));
            rows.add(new TableA1Row(5920, 2024, false, false, true, true, FAIL, WARN, WARN));
        }
        return rows;
    }

    public List<Integer> getSPsForOutcomeOfProvidedAndNotSupported(Outcome outcome,
                                                                   FuelType fuelType,
                                                                   int engineModelYear) {
        return getRows(fuelType, engineModelYear)
                                                 .filter(r -> r.getOutcomeIfProvidedAndNotSupported() == outcome)
                                                 .map(TableA1Row::getSpn)
                                                 .collect(Collectors.toList());
    }

    public Outcome getOutcomeForNonObdModuleProvidingSpn(int spn, FuelType fuelType, int engineModelYear) {
        return getRows(fuelType, engineModelYear)
                                                 .filter(r -> r.getSpn() == spn)
                                                 .map(TableA1Row::getOutcomeIfProvidedByNonObd)
                                                 .filter(Objects::nonNull)
                                                 .findFirst()
                                                 .orElse(PASS);
    }

    public Outcome getOutcomeForDuplicateSpn(int spn,
                                             FuelType fuelType,
                                             int engineModelYear) {
        return getRows(fuelType, engineModelYear)
                                                 .filter(r -> r.getSpn() == spn)
                                                 .map(TableA1Row::getOutcomeIfTwoResponses)
                                                 .filter(Objects::nonNull)
                                                 .findFirst()
                                                 .orElse(PASS);
    }

    private boolean supportsEngineModelYear(TableA1Row r, int engineModelYear) {
        return engineModelYear >= r.getMinimumModelYear();
    }

    public boolean supportsFuelType(TableA1Row row, FuelType fuelType) {
        return (fuelType.isSparkIgnition() && row.isSparkIgnition())
                || (fuelType.isCompressionIgnition() && row.isCompressionIgnition())
                || (fuelType.isElectric() && row.isElectric())
                || (fuelType.isHybrid() && row.isHybrid());
    }

    private Stream<TableA1Row> getRows(FuelType fuelType, int engineModelYear) {
        return getRows().stream()
                        .filter(r -> supportsFuelType(r, fuelType))
                        .filter(r -> supportsEngineModelYear(r, engineModelYear));
    }

}

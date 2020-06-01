/**
 *
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.ExpectedTestResult;
import org.etools.j1939_84.model.Outcome;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TableA7Validator {

    private class Row {

        private final Collection<ExpectedTestResult> expectedTestResults;
        private final int minimumContains;
        private final String monitorName;

        public Row(String monitorName, int minimumContains, ExpectedTestResult... expectedTestResults) {
            this.monitorName = monitorName;
            this.minimumContains = minimumContains;
            this.expectedTestResults = Arrays.asList(expectedTestResults);
        }

        public void validate(Collection<ScaledTestResult> actualTestResults, ResultsListener listener) {
            boolean isValid = rowValidator.isValid(actualTestResults, expectedTestResults, minimumContains);
            if (!isValid) {
                listener.addOutcome(1, 12, Outcome.FAIL, monitorName + " is missing required Test Result");
            }
        }
    }

    private static ExpectedTestResult etr(int spn, int fmi) {
        return new ExpectedTestResult(spn, fmi);
    }

    private final TableA7RowValidator rowValidator;

    public TableA7Validator() {
        this(new TableA7RowValidator());
    }

    TableA7Validator(TableA7RowValidator rowValidator) {
        this.rowValidator = rowValidator;
    }

    private Collection<Row> getCompressionIgnitionRows() {
        Collection<Row> rows = new ArrayList<>(20);
        rows.add(new Row("Fuel system pressure control low", 1, etr(157, 18), etr(164, 18), etr(3055, 18)));
        rows.add(new Row("Fuel system pressure control high", 1, etr(157, 16), etr(164, 16), etr(3055, 16)));
        rows.add(new Row("Injector Quantity (High Flow) or Injector Timing",
                1,
                etr(651, 7),
                etr(651, 16),
                etr(5358, 16),
                etr(1413, 16)));
        rows.add(new Row("Injector Quantity (Low Flow) or Injector Timing",
                1,
                etr(651, 7),
                etr(651, 18),
                etr(5358, 16),
                etr(1413, 16)));
        rows.add(new Row("% of misfire", 1, etr(1323, 31)));
        rows.add(new Row("% of misfire", 1, etr(1324, 31)));
        rows.add(new Row("% of misfire", 1, etr(1325, 31)));
        rows.add(new Row("% of misfire", 1, etr(1326, 31)));
        rows.add(new Row("Low Flow", 1, etr(3058, 18), etr(2659, 18), etr(411, 18)));
        rows.add(new Row("High Flow", 1, etr(3058, 16), etr(2659, 0), etr(2659, 16), etr(411, 16)));
        rows.add(new Row("Cooler performance", 1, etr(4752, 1), etr(4752, 18)));
        rows.add(new Row("Under Boost", 1, etr(102, 17), etr(102, 18), etr(1127, 18), etr(3563, 18), etr(4817, 18)));
        rows.add(new Row("Over Boost", 1, etr(102, 16), etr(1127, 16), etr(3563, 16), etr(4817, 16)));
        rows.add(new Row("Charge Air Undercooling", 1, etr(2630, 16), etr(105, 16), etr(1636, 16), etr(5285, 18)));
        rows.add(new Row(
                "Conversion Efficiency, or Aftertreatment Assistance: Exotherm to assist PM reg., or Aftertreatment Assistance: Feedgas to assist SCR",
                1,
                etr(5018, 18),
                etr(5298, 18),
                etr(5300, 31)));
        rows.add(new Row("Conversion Efficiency", 1, etr(4364, 17), etr(4364, 18), etr(4364, 31)));
        rows.add(new Row("SCR or Other Reductant delivery performance",
                1,
                etr(3361, 7),
                etr(4331, 15),
                etr(4331, 18),
                etr(4334, 18)));
        rows.add(new Row("Filtering Performance", 1, etr(3251, 2), etr(3936, 2), etr(3936, 16), etr(3639, 18)));
        rows.add(new Row("Incomplete regeneration", 1, etr(3713, 31), etr(5319, 7), etr(5319, 31)));
        rows.add(new Row("NOx Sensor Performance Monitoring Capability", 1, etr(3226, 16), etr(3226, 20)));

        return rows;
    }

    private Collection<Row> getSparkIgnitionRows() {
        Collection<Row> rows = new ArrayList<>(12);
        rows.add(new Row("% of misfire", 1, etr(1323, 16), etr(1323, 31)));
        rows.add(new Row("% of misfire", 1, etr(1324, 16), etr(1324, 31)));
        rows.add(new Row("% of misfire", 1, etr(1325, 16), etr(1325, 31)));
        rows.add(new Row("% of misfire", 1, etr(1326, 16), etr(1326, 31)));
        rows.add(new Row("Conversion Efficiency", 1, etr(3050, 18), etr(6652, 18)));
        {
            ExpectedTestResult[] results = new ExpectedTestResult[32];
            for (int i = 0; i < 32; i++) {
                results[i] = etr(3217, i);
            }
            rows.add(new Row("Engine Exhaust Sensor", 2, results));
        }
        {
            ExpectedTestResult[] results = new ExpectedTestResult[32];
            for (int i = 0; i < 32; i++) {
                results[i] = etr(3227, i);
            }
            rows.add(new Row("Engine Exhaust Sensor", 2, results));
        }
        rows.add(new Row("Engine Exhaust Sensor Heater", 1, etr(3222, 1), etr(3222, 2)));
        rows.add(new Row("Engine Exhaust Sensor Heater", 1, etr(3232, 1), etr(3232, 2)));
        rows.add(new Row("Conversion Efficiency", 1, etr(4364, 17), etr(4364, 18), etr(4364, 31)));
        rows.add(new Row("Fuel System Monitor", 1, etr(651, 3), etr(651, 4), etr(651, 5), etr(6575, 2)));
        rows.add(new Row("EVAP Monitor", 1, etr(7835, 7), etr(7835, 20), etr(7835, 21)));
        return rows;
    }

    public Collection<ScaledTestResult> hasDuplicates(Collection<ScaledTestResult> testResults) {

        List<ExpectedTestResult> expectedTestResults = testResults.stream()
                .map(r -> new ExpectedTestResult(r.getSpn(), r.getFmi())).collect(Collectors.toList());

        Set<ExpectedTestResult> duplicates = expectedTestResults.stream()
                .filter(r -> Collections.frequency(expectedTestResults, r) > 1).collect(Collectors.toSet());

        Set<ScaledTestResult> results = new HashSet<>();
        for (ExpectedTestResult etr : duplicates) {
            for (ScaledTestResult str : testResults) {
                if (str.getSpn() == etr.getSpn() && str.getFmi() == etr.getFmi()) {
                    results.add(str);
                    break;
                }
            }
        }

        return results;
    }

    public void validateForCompressionIgnition(Collection<ScaledTestResult> testResults, ResultsListener listener) {
        getCompressionIgnitionRows().forEach(r -> r.validate(testResults, listener));
    }

    public void validateForSparkIgnition(Collection<ScaledTestResult> testResults, ResultsListener listener) {
        getSparkIgnitionRows().forEach(r -> r.validate(testResults, listener));
    }
}

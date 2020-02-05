/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.IResult;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class SummaryModule {

    private static final int LINE_LENGTH = 72;

    private static String dots(int length) {
        char[] charArray = new char[length];
        Arrays.fill(charArray, '.');
        return new String(charArray);
    }

    public static void main(String... strings) throws InterruptedException {
        SummaryModule module = new SummaryModule();
        String summary = module.generateSummary();
        System.out.println(summary);
    }

    private final List<PartResult> partResults = new ArrayList<>();

    /**
     *
     */
    public SummaryModule() {
    }

    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
        StepResult stepResult = getStepResult(partNumber, stepNumber);
        stepResult.addResult(new ActionOutcome(outcome, message));
    }

    /**
     * @param partResult {@link PartResult} to be added to partResults
     */
    public void beginPart(PartResult partResult) {
        partResults.add(partResult);
    }

    public void endStep(StepResult stepResult) {
        if (stepResult.getOutcome() == Outcome.INCOMPLETE) {
            stepResult.addResult(new ActionOutcome(Outcome.PASS, null));
        }
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        for (PartResult partResult : partResults) {
            sb.append(println(partResult));
            sb.append(NL);
            for (StepResult stepResult : partResult.getStepResults()) {
                sb.append(println(stepResult));
            }
            sb.append(NL);
        }
        return sb.toString();
    }

    private StepResult getStepResult(int partNumber, int stepNumber) {
        for (PartResult partResult : partResults) {
            if (partResult.getPartNumber() == partNumber) {
                return partResult.getStepResult(stepNumber);
            }
        }
        return null;
    }

    private String println(IResult iResult) {
        String name = iResult.toString();

        String result = "";
        Outcome outcome = iResult.getOutcome();
        if (outcome != Outcome.PASS) {
            result = "(" + outcome + ")";
        }

        if (!result.isEmpty()) {
            int totalLength = name.length() + result.length() + 9;

            if (totalLength > LINE_LENGTH) {
                int overrun = totalLength - LINE_LENGTH + 3;
                name = name.substring(0, name.length() - overrun);
            }

            int dots = LINE_LENGTH - (name.length() + result.length() + 3);

            return name + dots(dots) + result + NL;
        } else {
            return name + NL;
        }
    }

}

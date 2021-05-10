/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.List;

import org.etools.j1939_84.controllers.PartResultRepository;
import org.etools.j1939_84.model.IResult;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class SummaryModule {

    private static final int LINE_LENGTH = 80;
    private final PartResultRepository partResultRepository;

    public SummaryModule() {
        this(PartResultRepository.getInstance());
    }

    public SummaryModule(PartResultRepository partResultRepository) {
        this.partResultRepository = partResultRepository;
    }

    private static String dots(int length) {
        char[] charArray = new char[length];
        Arrays.fill(charArray, '.');
        return new String(charArray);
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        for (PartResult partResult : getPartResults()) {
            sb.append(println(partResult));
            sb.append(NL);
            for (StepResult stepResult : partResult.getStepResults()) {
                sb.append(println(stepResult));
            }
            sb.append(NL);
        }
        return sb.toString();
    }

    private List<PartResult> getPartResults() {
        return partResultRepository.getPartResults();
    }

    public long getOutcomeCount(Outcome outcome) {
        return getPartResults().stream()
                               .flatMap(p -> p.getStepResults().stream())
                               .flatMap(s -> s.getOutcomes().stream())
                               .filter(o -> o.getOutcome() == outcome)
                               .count();
    }

    private static String println(IResult iResult) {
        String name = iResult.toString();

        Outcome outcome = iResult.getOutcome();
        String result = "(" + outcome + ")";

        // Name[...](Result) with min 3 dots
        int totalLength = name.length() + result.length() + 3;

        if (totalLength > LINE_LENGTH) {
            int overrun = totalLength - LINE_LENGTH + 3;
            name = name.substring(0, name.length() - overrun);
        }

        int dots = LINE_LENGTH - (name.length() + result.length() + 3);

        return name + dots(dots) + result + NL;
    }

}

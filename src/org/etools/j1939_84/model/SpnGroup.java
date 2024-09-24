/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class SpnGroup implements Comparable<SpnGroup> {
    public final List<Integer> spns;

    public SpnGroup(Integer... spns) {
        this.spns = Arrays.asList(spns);
    }

    public boolean isSatisfied(Collection<Integer> supportedSpnValues) {
        return supportedSpnValues.stream().anyMatch(spns::contains);
    }

    public boolean containsMultiple(Collection<Integer> supportedSpnValues) {
        if (spns.isEmpty()) {
            return false;
        } else {
            return supportedSpnValues.stream().filter(spns::contains).collect(Collectors.toList()).size() > 1;
        }
    }

    @Override
    public String toString() {
        return "SPNs: " + spns.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    @SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS", justification = "Not using .equals() on purpose.")
    @Override
    public int compareTo(SpnGroup spnGroup) {
        int thisSum = spns.stream().mapToInt(s -> s).sum();
        int thatSum = spnGroup.spns.stream().mapToInt(s -> s).sum();
        return thisSum - thatSum;
    }
}

/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class IndexGenerator {

    private static IndexGenerator instance;

    public static IndexGenerator instance() {
        if (instance == null) {
            instance = new IndexGenerator(new Random());
        }
        return instance;
    }

    private final Random random;

    private final Set<String> usedIndexes = new HashSet<>();

    /**
     *
     */
    private IndexGenerator(Random random) {
        this.random = random;
    }

    public String index() {
        String next = random();
        while (usedIndexes.contains(next)) {
            next = random();
        }
        usedIndexes.add(next);
        return next;
    }

    private String random() {
        // FIXME - This shouldn't require a substring cut.
        return String.format("%08X", random.nextLong()).substring(0, 8);
    }

}

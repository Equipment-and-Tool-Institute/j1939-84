/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.resources.J193984Resources;

import com.opencsv.CSVReader;

public class PartLookup {

    private static final List<Map<Integer, String>> steps = new ArrayList<>();

    /**
     * The Map that holds the values for the Test Parts
     */
    private static final Map<Integer, String> parts = loadMap("parts.csv");

    static {
        steps.add(loadMap("part01Steps.csv"));
        steps.add(loadMap("part02Steps.csv"));
        steps.add(loadMap("part03Steps.csv"));
        steps.add(loadMap("part04Steps.csv"));
        steps.add(loadMap("part05Steps.csv"));
        steps.add(loadMap("part06Steps.csv"));
        steps.add(loadMap("part07Steps.csv"));
        steps.add(loadMap("part08Steps.csv"));
        steps.add(loadMap("part09Steps.csv"));
        steps.add(loadMap("part10Steps.csv"));
        steps.add(loadMap("part11Steps.csv"));
        steps.add(loadMap("part12Steps.csv"));
    }

    /**
     * Helper method to find a value in the given map
     *
     * @param  map
     *                 the map that contains the values
     * @param  key
     *                 the key to find in the map
     * @return     the value from the map or "Unknown" if the key does not have a
     *             value in the map
     */
    private static String find(Map<Integer, String> map, int key) {
        String name = map != null ? map.get(key) : null;
        return name != null ? name : "Unknown";
    }

    /**
     * Returns the Name of the given Test Part
     *
     * @param  partNumber
     *                        the test part number
     * @return            the name of the Test Part or "Unknown" if not defined
     */
    public static String getPartName(int partNumber) {
        return find(parts, partNumber);
    }

    public static String getStepName(int partNumber, int stepNumber) {
        return find(getStepMap(partNumber), stepNumber);
    }

    private static Map<Integer, String> getStepMap(int partNumber) {
        if (steps.size() >= partNumber && partNumber > 0) {
            return steps.get(partNumber - 1);
        }
        return null;
    }

    /**
     * Reads the given file and returns a map populated the values. It's assumed
     * the file is a Comma Separated Values file with the first column being an
     * integer (key) and the second column being the String (value)
     *
     * @param  fileName
     *                      the name of the file to read
     * @return          a Map of Integers to Strings
     */
    private static Map<Integer, String> loadMap(String fileName) {
        Map<Integer, String> map = new HashMap<>();
        String[] values;

        InputStream is = J193984Resources.class.getResourceAsStream(fileName);
        if (is != null) {
            InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
            try (CSVReader reader = new CSVReader(isReader)) {
                while ((values = reader.readNext()) != null) {
                    map.put(Integer.valueOf(values[0]), values[1]);
                }
            } catch (Exception e) {
                J1939_84.getLogger().log(Level.SEVERE, "Error loading map from " + fileName, e);
            }
        }
        return map;
    }

}

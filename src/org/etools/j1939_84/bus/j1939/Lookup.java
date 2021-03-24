/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.resources.Resources;

import com.opencsv.CSVReader;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Class that converts the datalink values into descriptions for Source
 * Addresses, Suspect Parameter Numbers, and Failure Mode Indicators
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Lookup {

    /**
     * The Map that holds the values for the Source Addresses
     */
    private static final Map<Integer, String> addresses = loadMap("addresses.csv");

    /**
     * The Map that holds the values for the Failure Mode Indicators
     */
    private static final Map<Integer, String> fmis = loadMap("fmis.csv");

    /**
     * The Map that holds the values for the Manufacturers
     */
    private static final Map<Integer, String> manufacturers = loadMap("manufacturers.csv");

    /**
     * The Map that holds the values for the Test Parts
     */
    private static final Map<Integer, String> parts = loadMap("parts.csv");

    private static final List<Map<Integer, String>> steps = new ArrayList<>();

    private static final Map<Integer, String> duplicateSpnOutcomes = loadMap("outcomeForDuplicateSpns.csv");

    private static final Map<Integer, String> nonObdSpnOutcomes = loadMap("outcomeForNonObd.csv");

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
     * Not used. Use as a static
     */
    private Lookup() {

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
     * Translates the given sourceAddress into a name as defined by SAE
     *
     * @param  sourceAddress
     *                           the sourceAddress of the module that sent the packet
     * @return               The name as defined by SAE or "Unknown" if it's not defined
     */
    public static String getAddressName(int sourceAddress) {
        return find(addresses, sourceAddress) + " (" + sourceAddress + ")";
    }

    /**
     * Translates the given fmi into a description as defined by SAE
     *
     * @param  fmi
     *                 the failure mode indicator from the Diagnostic Trouble Code
     * @return     The description as defined by SAE or "Unknown" if it's not
     *             defined
     */
    public static String getFmiDescription(int fmi) {
        return find(fmis, fmi);
    }

    /**
     * Translates the given manufacturerId into a manufacturer name as defined
     * by SAE
     *
     * @param  manufacturerId
     *                            the ID of the manufacturer
     * @return                the manufacturer as defined by SAE or "Unknown" if it's not
     *                        defined
     */
    public static String getManufacturer(int manufacturerId) {
        return find(manufacturers, manufacturerId);
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

    /**
     * Translates the given spn into a name as defined by SAE
     *
     * @param  spn
     *                 the suspect parameter number from the Diagnostic Trouble Code
     * @return     The name as defined by SAE or "Unknown" if it's not defined
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Null check needs to happen here")

    public static String getSpnName(int spn) {
        SpnDefinition spnDef = J1939DaRepository.getInstance().findSpnDefinition(spn);
        return spnDef == null ? "Unknown" : spnDef.getLabel();
    }

    private static Map<Integer, String> getStepMap(int partNumber) {
        if (steps.size() >= partNumber && partNumber > 0) {
            return steps.get(partNumber - 1);
        }
        return null;
    }

    public static String getStepName(int partNumber, int stepNumber) {
        return find(getStepMap(partNumber), stepNumber);
    }

    public static Outcome getOutcomeForDuplicateSpn(int spn) {
        try {
            return Outcome.valueOf(find(duplicateSpnOutcomes, spn));
        } catch (Exception e) {
            return Outcome.PASS;
        }
    }

    public static Outcome getOutcomeForNonObdModuleProvidingSpn(int spn) {
        try {
            return Outcome.valueOf(find(nonObdSpnOutcomes, spn));
        } catch (Exception e) {
            return Outcome.PASS;
        }
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

        InputStream is = Resources.class.getResourceAsStream(fileName);
        InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
        try (CSVReader reader = new CSVReader(isReader)) {
            while ((values = reader.readNext()) != null) {
                map.put(Integer.valueOf(values[0]), values[1]);
            }
        } catch (Exception e) {
            J1939_84.getLogger().log(Level.SEVERE, "Error loading map from " + fileName, e);
        }
        return map;
    }
}

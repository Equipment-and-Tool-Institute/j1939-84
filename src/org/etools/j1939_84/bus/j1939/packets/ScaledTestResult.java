/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.ParsedPacket.toInts;
import static org.etools.j1939_84.utils.CollectionUtils.join;

import java.util.Arrays;

import org.etools.j1939_84.NumberFormatter;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * Represents a Scaled Test Result from a {@link DM30ScaledTestResultsPacket}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class ScaledTestResult {

    /**
     * The Possible Outcomes of the Test
     *
     * @author Matt Gumbel (matt@soliddesign.net)
     */
    public enum TestResult {

        CANNOT_BE_PERFORMED("Test Cannot Be Performed"),
        FAILED("Test Failed"),
        NOT_COMPLETE("Test Not Complete"),
        PASSED("Test Passed");

        private final String string;

        TestResult(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public static ScaledTestResult create(int testIdentifier,
                                          int spn,
                                          int fmi,
                                          int slotNumber,
                                          int testValue,
                                          int testMaximum,
                                          int testMinimum) {

        int[] data = new int[4];
        data[0] = (testIdentifier & 0xFF);
        data[1] = (byte) (spn & 0xFF);
        data[2] = (byte) ((spn >> 8) & 0xFF);
        data[3] = (byte) (((spn >> 16 & 0xE0)) + (fmi & 0x1F));

        data = join(data, toInts(slotNumber));
        data = join(data, toInts(testValue));
        data = join(data, toInts(testMaximum));
        data = join(data, toInts(testMinimum));

        return new ScaledTestResult(data);
    }

    private final int[] data;
    private final int fmi;
    private Slot slot;
    private final int slotNumber;
    private final int spn;
    private final int testIdentifier;
    private final int testMaximum;
    private final int testMinimum;
    private final int testValue;

    /**
     * Constructor
     *
     * @param data
     *            the data that contains the {@link ScaledTestResult}
     */
    public ScaledTestResult(int[] data) {
        this.data = data;
        testIdentifier = data[0];
        spn = SupportedSPN.parseSPN(Arrays.copyOfRange(data, 1, 4));
        fmi = data[3] & 0x1F;
        slotNumber = ((data[5] << 8) | data[4]) & 0xFFFF;
        testValue = ((data[7] << 8) | data[6]) & 0xFFFF;
        testMaximum = ((data[9] << 8) | data[8]) & 0xFFFF;
        testMinimum = ((data[11] << 8) | data[10]) & 0xFFFF;
    }

    public int[] getData() {
        return data;
    }

    /**
     * Returns the Failure Mode Indicator
     *
     * @return int
     */
    public int getFmi() {
        return fmi;
    }

    /**
     * Returns the Test Maximum scaled via the {@link Slot}
     *
     * @return double
     */
    public double getScaledTestMaximum() {
        return getSlot() != null ? getSlot().scale(getTestMaximum()) : getTestMaximum();
    }

    /**
     * Returns the Test Minimum scaled via the {@link Slot}
     *
     * @return double
     */
    public double getScaledTestMinimum() {
        return getSlot() != null ? getSlot().scale(getTestMinimum()) : getTestMinimum();
    }

    /**
     * Returns the Test Value scaled via the {@link Slot}
     *
     * @return double
     */
    public double getScaledTestValue() {
        return getSlot() != null ? getSlot().scale(getTestValue()) : getTestValue();
    }

    /**
     * Returns the {@link Slot} used to scale the values
     *
     * @return {@link Slot}
     */
    public Slot getSlot() {
        if (slot == null) {
            slot = J1939DaRepository.findSlot(slotNumber, spn);
        }
        return slot;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    /**
     * Returns the Suspect Parameter Number
     *
     * @return int
     */
    public int getSpn() {
        return spn;
    }

    /**
     * Returns the Identifier for the Test
     *
     * @return int
     */
    public int getTestIdentifier() {
        return testIdentifier;
    }

    /**
     * Returns the non-scaled Test Maximum
     *
     * @return int
     */
    public int getTestMaximum() {
        return testMaximum;
    }

    /**
     * Returns the non-scaled Test Minimum
     *
     * @return int
     */
    public int getTestMinimum() {
        return testMinimum;
    }

    /**
     * Returns the Result of the Test based upon the TestValue, TestMaximum and
     * TestMinimum
     *
     * @return {@link TestResult}
     */
    public TestResult getTestResult() {
        if (getTestValue() == 0xFB00) {
            return TestResult.NOT_COMPLETE;
        } else if (getTestValue() == 0xFB01) {
            return TestResult.CANNOT_BE_PERFORMED;
        } else if (hasMinimum() && getTestValue() < getTestMinimum()) {
            return TestResult.FAILED;
        } else if (hasMaximum() && getTestValue() > getTestMaximum()) {
            return TestResult.FAILED;
        } else {
            return TestResult.PASSED;
        }
    }

    /**
     * Returns the non-scaled Test Value
     *
     * @return int
     */
    public int getTestValue() {
        return testValue;
    }

    /**
     * Return true if the test has a maximum value
     *
     * @return boolean
     */
    private boolean hasMaximum() {
        return getTestMaximum() != 0xFFFF;
    }

    /**
     * Returns true if the test has a minimum value
     *
     * @return boolean
     */
    private boolean hasMinimum() {
        return getTestMinimum() != 0xFFFF;
    }

    public boolean isInitialized() {
        boolean initHigh = getTestValue() == 0xFB00 && getTestMinimum() == 0xFFFF && getTestMaximum() == 0xFFFF;
        boolean initLow = getTestValue() == 0x0000 && getTestMinimum() == 0x0000 && getTestMaximum() == 0x0000;
        return initHigh || initLow;
    }

    @Override
    public String toString() {
        String result = "SPN " + getSpn() + " FMI " + getFmi() + " (SLOT " + slotNumber + ") ";
        final TestResult testResult = getTestResult();
        result += "Result: " + testResult + ".";
        if (testResult == TestResult.PASSED || testResult == TestResult.FAILED) {
            String unit = getSlot() != null ? getSlot().getUnit() : null;
            unit = unit != null && !unit.trim().isEmpty() ? " " + unit : "";
            result += " Min: " + (hasMinimum() ? NumberFormatter.format(getScaledTestMinimum()) : "N/A") + ",";
            result += " Value: " + NumberFormatter.format(getScaledTestValue()) + ",";
            result += " Max: " + (hasMaximum() ? NumberFormatter.format(getScaledTestMaximum()) : "N/A") + unit + "";
        }
        return result;
    }

}

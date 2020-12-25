/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.Objects;

import org.etools.j1939_84.NumberFormatter;
import org.etools.j1939_84.bus.j1939.Lookup;

/**
 * Represents a single Performance Ratio from a
 * {@link DM20MonitorPerformanceRatioPacket}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class PerformanceRatio {

    private final int denominator;
    private String name;
    private final int numerator;
    private String source;
    private final int sourceAddress;
    private final int spn;

    /**
     * Constructor
     *
     * @param spn
     *            the Suspect Parameter Number
     * @param numerator
     *            the value of the numerator
     * @param denominator
     *            the value of the denominator
     * @param sourceAddress
     *            the source address of the module this ratio is from
     */
    public PerformanceRatio(int spn, int numerator, int denominator, int sourceAddress) {
        this.spn = spn;
        this.numerator = numerator;
        this.denominator = denominator;
        this.sourceAddress = sourceAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PerformanceRatio)) {
            return false;
        }
        PerformanceRatio that = (PerformanceRatio) obj;
        return getSpn() == that.getSpn()
                && getNumerator() == that.getNumerator()
                && getDenominator() == that.getDenominator()
                && sourceAddress == that.sourceAddress;
    }

    /**
     * Returns the Denominator of the Ratio
     *
     * @return int
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * Returns a unique id for this ratio. This value will be same for ratios
     * from a given source address
     *
     * @return int
     */
    public int getId() {
        return spn << 8 | sourceAddress;
    }

    /**
     * Returns the Name of this Ratio
     *
     * @return {@link String}
     */
    public String getName() {
        if (name == null) {
            name = "SPN " + String.format("%1$4s", getSpn()) + " " + Lookup.getSpnName(getSpn());
        }
        return name;
    }

    /**
     * Returns the Numerator of the Ratio
     *
     * @return int
     */
    public int getNumerator() {
        return numerator;
    }

    /**
     * Returns the formatted name of the Source module for this ratio
     *
     * @return String
     */
    public String getSource() {
        if (source == null) {
            source = Lookup.getAddressName(sourceAddress);
        }
        return source;
    }

    /**
     * Returns the decimal source address for the module that sent this ratio
     *
     * @return int
     */
    public int getSourceAddress() {
        return sourceAddress;
    }

    /**
     * Returns the Suspect Parameter Number
     *
     * @return int
     */
    public int getSpn() {
        return spn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSpn(), getNumerator(), getDenominator(), sourceAddress);
    }

    @Override
    public String toString() {
        return getName() + ": " + NumberFormatter.format(getNumerator()) + " / "
                + NumberFormatter.format(getDenominator());
    }

}

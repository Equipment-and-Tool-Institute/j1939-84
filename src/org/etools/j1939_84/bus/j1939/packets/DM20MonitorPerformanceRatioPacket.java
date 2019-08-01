/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.NumberFormatter;
import org.etools.j1939_84.bus.Packet;

/**
 * The Parsed DM20 {@link Packet}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM20MonitorPerformanceRatioPacket extends ParsedPacket {

    public static final int PGN = 49664;

    private List<PerformanceRatio> ratios;

    public DM20MonitorPerformanceRatioPacket(Packet packet) {
        super(packet);
    }

    /**
     * Returns the Number of Ignition Cycles
     *
     * @return int
     */
    public int getIgnitionCycles() {
        return getPacket().get16(0);
    }

    /**
     * Helper method to find the longest {@link PerformanceRatio} name
     *
     * @param ratios
     *            all the {@link PerformanceRatio}s
     * @return the length of the longest name
     */
    private int getLongestName(List<PerformanceRatio> ratios) {
        return ratios.stream().mapToInt(t -> t.getName().length()).max().orElse(50);
    }

    @Override
    public String getName() {
        return "DM20";
    }

    /**
     * Returns the number of times that the vehicle has been operated in the
     * specified OBD monitoring conditions
     *
     * @return int
     */
    public int getOBDConditionsCount() {
        return getPacket().get16(2);
    }

    /**
     * Returns the {@link PerformanceRatio}s that were sent
     *
     * @return {@link List}
     */
    public List<PerformanceRatio> getRatios() {
        if (ratios == null) {
            ratios = parsePacket();
        }
        return ratios;
    }

    /**
     * Pads the String with spaces on the left
     *
     * @param string
     *            the String to pad
     * @param length
     *            the maximum number of spaces
     * @return the padded string
     */
    private String padLeft(String string, int length) {
        return String.format("%1$" + length + "s", string);
    }

    /**
     * Pads the String with spaces on the right
     *
     * @param string
     *            the String to pad
     * @param length
     *            the maximum number of spaces
     * @return the padded string
     */
    private String padRight(String string, int length) {
        return String.format("%1$-" + length + "s", string);
    }

    private List<PerformanceRatio> parsePacket() {
        List<PerformanceRatio> results = new ArrayList<>();
        final int length = getPacket().getLength();
        for (int i = 4; i + 6 < length; i = i + 7) {
            results.add(parseRatio(i));
        }
        return results;
    }

    private PerformanceRatio parseRatio(int index) {
        int spn = getPacket().get24(index) & 0x7FFFF;
        int numerator = getPacket().get16(index + 3);
        int denominator = getPacket().get16(index + 5);
        return new PerformanceRatio(spn, numerator, denominator, getSourceAddress());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix()).append(" [" + NL);
        int max = getLongestName(getRatios()) + 1;
        sb.append(padRight(" ", max)).append("  Num'r /  Den'r").append(NL);
        sb.append(padRight("Ignition Cycles", max)).append(padLeft(NumberFormatter.format(getIgnitionCycles()), 16))
                .append(NL);
        sb.append(padRight("OBD Monitoring Conditions Encountered", max))
                .append(padLeft(NumberFormatter.format(getOBDConditionsCount()), 16)).append(NL);

        for (PerformanceRatio ratio : getRatios()) {
            sb.append(padRight(ratio.getName(), max));
            sb.append(" ");
            sb.append(padLeft(NumberFormatter.format(ratio.getNumerator()), 6));
            sb.append(" / ");
            sb.append(padLeft(NumberFormatter.format(ratio.getDenominator()), 6));
            sb.append(NL);
        }
        sb.append("]");
        return sb.toString();
    }

}

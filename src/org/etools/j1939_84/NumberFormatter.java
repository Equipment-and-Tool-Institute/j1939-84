/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class used to format numbers for display
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class NumberFormatter {

    /**
     * Formats the given number
     *
     * @param  number
     *                    the number to format
     * @return        {@link String}
     */
    public static final String format(Number number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    /**
     * Use the static format method
     */
    private NumberFormatter() {

    }
}

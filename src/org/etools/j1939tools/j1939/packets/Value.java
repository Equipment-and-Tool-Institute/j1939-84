package org.etools.j1939tools.j1939.packets;

/**
 * Represents a value that may be smaller than a SPN, but is normally an SPN. This is the minimum needed for reporting.
 */
public interface Value {
    default String getLabel() {
        return "";
    }

    default String getValue() {
        return "";
    }

    default String getUnit() {
        return "";
    }
}

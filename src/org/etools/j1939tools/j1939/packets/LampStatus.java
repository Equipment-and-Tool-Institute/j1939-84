/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

/**
 * The Status of Diagnostic Trouble Code Lamps
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public enum LampStatus {
    ALTERNATE_OFF("alternate off", false),
    FAST_FLASH("fast flash", true),
    NOT_SUPPORTED("not supported", false),
    OFF("off", false),
    ON("on", true),
    OTHER("other", false),
    SLOW_FLASH("slow flash", true);

    private final String name;
    private final boolean active;

    LampStatus(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    /**
     * Returns a {@link LampStatus} based upon the onOff and flash values as
     * defined by SAE. If an undefined {@link LampStatus} is found, OTHER will
     * be returned
     *
     * @param  onOff
     *                   the On/Off Value
     * @param  flash
     *                   the Flash Value
     * @return       {@link LampStatus}
     */
    public static LampStatus getStatus(int onOff, int flash) {
        boolean off = (onOff & 0x03) == 0x00;
        boolean on = (onOff & 0x03) == 0x01;
        boolean notSupported = (onOff & 0x03) == 0x03;

        boolean slowFlash = (flash & 0x03) == 0x00;
        boolean fastFlash = (flash & 0x03) == 0x01;
        boolean dontFlash = (flash & 0x03) == 0x03;

        if (off) {
            if (slowFlash) {
                return LampStatus.ALTERNATE_OFF;
            }
            return LampStatus.OFF;
        }
        if (on) {
            if (slowFlash) {
                return LampStatus.SLOW_FLASH;
            }
            if (fastFlash) {
                return LampStatus.FAST_FLASH;
            }
            if (dontFlash) {
                return LampStatus.ON;
            }
        }
        if (notSupported && dontFlash) {
            return LampStatus.NOT_SUPPORTED;
        }
        return LampStatus.OTHER;
    }

    /*
     * FOR TESTING PURPOSES ONLY!
     * Can only be used with the values encoded as follows:
     * ALTERNATE_OFF {0, 0}
     * FAST_FLASH {1, 1}
     * NOT_SUPPORTED {3, 3}
     * OFF {0, 3}
     * ON {1, 0}
     * OTHER {3, 2}
     * SLOW_FLASH {1, 3}
     *
     * Any packets encoded with any of the other values will
     * fail comparisions as the additional values can not be
     * created.
     */
    public static int[] getBytes(LampStatus lampStatus) {
        int[] data;
        switch (lampStatus) {
            case ALTERNATE_OFF:
                data = new int[] { 0, 0 };
                break;
            case FAST_FLASH:
                data = new int[] { 1, 1 };
                break;
            case NOT_SUPPORTED:
                data = new int[] { 3, 3 };
                break;
            case OFF:
                data = new int[] { 0, 3 };
                break;
            case ON:
                data = new int[] { 1, 3 };
                break;
            case OTHER:
            default:
                data = new int[] { 3, 2 };
                break;
            case SLOW_FLASH:
                data = new int[] { 1, 0 };
                break;
        }
        return data;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}

/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

/**
 * The Status of Diagnostic Trouble Code Lamps
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public enum LampStatus {
    ALTERNATE_OFF("alternate off"), FAST_FLASH("fast flash"), NOT_SUPPORTED("not supported"), OFF("off"), ON("on"),
    OTHER("other"), SLOW_FLASH("slow flash");

    /**
     * Returns a {@link LampStatus} based upon the onOff and flash values as
     * defined by SAE. If an undefined {@link LampStatus} is found, OTHER will
     * be returned
     *
     * @param onOff
     *            the On/Off Value
     * @param flash
     *            the Flash Value
     * @return {@link LampStatus}
     */
    public static LampStatus getStatus(int onOff, int flash) {
        System.out.println("onOff is: " + onOff);
        System.out.println("flash is: " + flash);
        System.out.println("onOff & 0x03 is: " + (onOff & 0x03));
        System.out.println("flash & 0x03 is: " + (flash & 0x03));

        boolean off = (onOff & 0x03) == 0x00;
        boolean on = (onOff & 0x03) == 0x01;
        boolean notSupported = (onOff & 0x03) == 0x03;

        boolean slowFlash = (flash & 0x03) == 0x00;
        boolean fastFlash = (flash & 0x03) == 0x01;
        boolean dontFlash = (flash & 0x03) == 0x03;

        if (off) {
            System.out.println("slow flash is: " + slowFlash);
            if (slowFlash) {
                return LampStatus.ALTERNATE_OFF;

            }
            System.out.println("returning the correct value");
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

    private final String name;

    private LampStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}

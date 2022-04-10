/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

/**
 * The Exception used by the {@link Bus} interface
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BusException extends Exception {
    private static final long serialVersionUID = 6985964191306994194L;

    public BusException(String m) {
        super(m);
    }

    public BusException(String string, Throwable e) {
        super(string, e);
    }

}

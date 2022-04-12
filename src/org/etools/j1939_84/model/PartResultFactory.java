/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import org.etools.j1939_84.controllers.PartLookup;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class PartResultFactory {

    private static String name(int partNumber) {
        return PartLookup.getPartName(partNumber);
    }

    public PartResult create(int partNumber) {
        return new PartResult(partNumber, name(partNumber));
    }

}

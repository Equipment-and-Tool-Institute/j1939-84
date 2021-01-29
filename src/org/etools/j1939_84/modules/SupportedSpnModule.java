/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.SpnGroup;

/**
 * Module used to validate Supported SPNs
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class SupportedSpnModule {

    /*
     * The Data Stream SPNs that are required to be supported by the vehicle regardless of engine fuel type
     */
    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS = Arrays.asList(
            new SpnGroup(110, 1637, 4076, 4193),
            new SpnGroup(190, 4201, 723, 4202),
            new SpnGroup(158, 168),
            new SpnGroup(5827, 5454),
            new SpnGroup(183, 1600),
            new SpnGroup(1413, 1433, 1436),
            new SpnGroup(102, 106, 1127, 3563),
            new SpnGroup(92),
            new SpnGroup(512),
            new SpnGroup(513),
            new SpnGroup(514),
            new SpnGroup(2978),
            new SpnGroup(539),
            new SpnGroup(540),
            new SpnGroup(541),
            new SpnGroup(542),
            new SpnGroup(543),
            new SpnGroup(544),
            new SpnGroup(84),
            new SpnGroup(91),
            new SpnGroup(108),
            new SpnGroup(5466),
            new SpnGroup(3700),
            new SpnGroup(235),
            new SpnGroup(247),
            new SpnGroup(248),
            new SpnGroup(5837),
            new SpnGroup(2791),
            new SpnGroup(5829),
            new SpnGroup(27));

    /*
     * The Data Stream SPNs that are required to be supported by the vehicle with a
     * Compression Ignition Engine
     */
    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_CI = Arrays.asList(
            new SpnGroup(3609, 3610, 3251),
            new SpnGroup(94, 157, 164, 5313, 5314, 5578),
            new SpnGroup(3516, 3518, 7346),
            new SpnGroup(3031, 3515),
            new SpnGroup(6895),
            new SpnGroup(7333),
            new SpnGroup(3226));

    /*
     * The Data Stream SPNs that are required to be supported by the vehicle with a
     * Spark Ignition Engine
     */
    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_SI = Arrays.asList(
            new SpnGroup(94, 157, 5313, 5578),
            new SpnGroup(51),
            new SpnGroup(3464),
            new SpnGroup(4236),
            new SpnGroup(4237),
            new SpnGroup(4240),
            new SpnGroup(3249),
            new SpnGroup(3241),
            new SpnGroup(3217),
            new SpnGroup(3227));

    /*
     * The Freeze Frame SPNs that are required to be supported by the vehicle
     */
    private static final Collection<SpnGroup> REQUIRED_FREEZE_FRAME_SPNS = Arrays.asList(
            new SpnGroup(92),
            new SpnGroup(110, 1637, 4076, 4193),
            new SpnGroup(190, 4201, 723, 4202),
            new SpnGroup(512),
            new SpnGroup(513),
            new SpnGroup(3301));

    /**
     * Helper method to get the required Data Stream SPNs based upon the
     * {@link FuelType} of the Engine
     *
     * @param fuelType
     *         the {@link FuelType} of the Engine
     * @return a {@link Collection} of {@link SpnGroup}
     */
    private static Collection<SpnGroup> getRequiredDataStreamSpns(FuelType fuelType) {
        Collection<SpnGroup> requiredSpns = new HashSet<>(REQUIRED_DATA_STREAM_SPNS);

        if (fuelType.isCompressionIgnition()) {
            requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_CI);
        } else if (fuelType.isSparkIgnition()) {
            requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_SI);
        }
        return requiredSpns;
    }

    public boolean validateDataStreamSpns(ResultsListener listener, Collection<Integer> spns, FuelType fuelType) {

        boolean result = true;
        for (SpnGroup spnGroup : getRequiredDataStreamSpns(fuelType)) {
            if (!spnGroup.isSatisfied(spns)) {
                listener.onResult("Required Data Stream SPNs are not supported: " + spnGroup);
                result = false;
            }
        }
        return result;
    }

    public boolean validateFreezeFrameSpns(ResultsListener listener, Collection<Integer> spns) {

        boolean result = true;
        for (SpnGroup spnGroup : REQUIRED_FREEZE_FRAME_SPNS) {
            if (!spnGroup.isSatisfied(spns)) {
                listener.onResult("Required Freeze Frame SPNs are not supported: " + spnGroup);
                result = false;
            }
        }
        return result;
    }

}

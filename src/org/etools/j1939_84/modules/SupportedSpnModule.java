/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.SpnGroup;
import org.etools.j1939tools.j1939.model.FuelType;

/**
 * Module used to validate Supported SPNs
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class SupportedSpnModule {

    /*
     * The Data Stream SPNs that are not (causes INFO) to be supported by the vehicle regardless of engine fuel type
     */
    private static final Collection<SpnGroup> NOTICED_NON_DUP_DATA_STREAM_SPNS = List.of(new SpnGroup(110,
                                                                                                      1637,
                                                                                                      4076,
                                                                                                      4193),
                                                                                         new SpnGroup(190,
                                                                                                      4201,
                                                                                                      723,
                                                                                                      4202));

    /*
     * The Data Stream SPNs that are not (causes INFO) to be supported by the vehicle regardless of engine fuel type
     */
    private static final Collection<SpnGroup> NOTICED_NON_DUP_2024_DATA_STREAM_SPNS = List.of(new SpnGroup(4348,
                                                                                                           6593));

    /*
     * The Data Stream SPNs that are not (causes WARN) to be supported by the vehicle regardless of engine fuel type
     */
    private static final Collection<SpnGroup> DESIRED_NON_DUP_DATA_STREAM_SPNS = List.of(new SpnGroup(132,
                                                                                                      6393));

    /*
     * The Data Stream SPNs that are desired (cause INFO) to be supported by the vehicle regardless of engine fuel type
     */
    private static final Collection<SpnGroup> NOTICED_DATA_STREAM_SPNS = List.of(new SpnGroup(110));

    /*
     * The Data Stream SPNs that are desired (cause INFO) to be supported by the vehicle compression ignition engine
     * type
     * for vehicles manufactured 2024 or later
     */
    private static final Collection<SpnGroup> NOTICED_2024_CI_DATA_STREAM_SPNS = List.of(new SpnGroup(101),
                                                                                         new SpnGroup(74));
    /*
     * The Data Stream SPNs that are desired (cause WARN) to be supported by the vehicle regardless of engine fuel type
     */
    private static final Collection<SpnGroup> DESIRED_DATA_STREAM_SPNS = List.of(new SpnGroup(190),
                                                                                 new SpnGroup(5827),
                                                                                 new SpnGroup(132),
                                                                                 new SpnGroup(157),
                                                                                 new SpnGroup(5313),
                                                                                 new SpnGroup(175));

    /*
     * The Data Stream SPNs that are desired (cause WARN) to be supported by the vehicle regardless of engine fuel type
     * for vehicle manufactured 2016 or later
     */
    private static final Collection<SpnGroup> DESIRED_2016_DATA_STREAM_SPNS = List.of(new SpnGroup(3516,
                                                                                                   3518,
                                                                                                   7346),
                                                                                      new SpnGroup(96));

    /*
     * The Data Stream SPNs that are desired (cause WARN) to be supported by the vehicle regardless of engine fuel type
     * for vehicle manufactured 2022 or later
     */
    private static final Collection<SpnGroup> DESIRED_2022_DATA_STREAM_SPNS = List.of(new SpnGroup(166),
                                                                                      new SpnGroup(12750),
                                                                                      new SpnGroup(12751));

    /*
     * The Data Stream SPNs that are desired (cause WARN) to be supported by the vehicle regardless of engine fuel type
     * for vehicle manufactured 2024 or later
     */
    private static final Collection<SpnGroup> DESIRED_2024_DATA_STREAM_SPNS = List.of(new SpnGroup(4360),
                                                                                      new SpnGroup(4331,
                                                                                                   6595),
                                                                                      new SpnGroup(12752),
                                                                                      new SpnGroup(12753),
                                                                                      new SpnGroup(245,
                                                                                                   917),
                                                                                      new SpnGroup(2659));

    /*
     * The Data Stream SPNs that are desired (cause WARN) to be supported by the vehicle with compression ignition
     * engine
     * fuel type for vehicle manufactured 2024 or later
     */
    private static final Collection<SpnGroup> DESIRED_2024_CI_DATA_STREAM_SPNS = List.of(new SpnGroup(4348,
                                                                                                      6593),
                                                                                         new SpnGroup(12749),
                                                                                         new SpnGroup(4363),
                                                                                         new SpnGroup(3230),
                                                                                         new SpnGroup(3220),
                                                                                         new SpnGroup(3481,
                                                                                                      5503),
                                                                                         new SpnGroup(12743,
                                                                                                      3479),
                                                                                         new SpnGroup(3480),
                                                                                         new SpnGroup(4334),
                                                                                         new SpnGroup(2630),
                                                                                         new SpnGroup(12758),
                                                                                         new SpnGroup(5444));

    /*
     * The Data Stream SPNs that are desired (cause WARN) to be supported by the vehicle with hybrid electric vehicle
     * manufactured 2024 or later
     */
    private static final Collection<SpnGroup> DESIRED_2024_HEV_DATA_STREAM_SPNS = List.of(new SpnGroup(7896));

    /*
     * The Data Stream SPNs that are desired (cause WARN) to be supported by the vehicle with electric vehicle
     * manufactured 2024 or later
     */
    private static final Collection<SpnGroup> DESIRED_2024_XEV_DATA_STREAM_SPNS = List.of(new SpnGroup(7315),
                                                                                          new SpnGroup(8086,
                                                                                                       5919),
                                                                                          new SpnGroup(5920));

    /*
     * The Data Stream SPNs that are required to be supported by the vehicle regardless of engine fuel type
     */
    private static final Collection<SpnGroup> REQUIRED_2013_DATA_STREAM_SPNS = List.of(new SpnGroup(110,
                                                                                                    1637,
                                                                                                    4076,
                                                                                                    4193),
                                                                                       new SpnGroup(190,
                                                                                                    4201,
                                                                                                    723,
                                                                                                    4202),
                                                                                       new SpnGroup(158, 168),
                                                                                       new SpnGroup(102,
                                                                                                    106,
                                                                                                    1127,
                                                                                                    3563),
                                                                                       new SpnGroup(92),
                                                                                       new SpnGroup(512),
                                                                                       new SpnGroup(513),
                                                                                       new SpnGroup(539),
                                                                                       new SpnGroup(540),
                                                                                       new SpnGroup(541),
                                                                                       new SpnGroup(542),
                                                                                       new SpnGroup(543),
                                                                                       new SpnGroup(544),
                                                                                       new SpnGroup(84),
                                                                                       new SpnGroup(91),
                                                                                       new SpnGroup(108),
                                                                                       new SpnGroup(235),
                                                                                       new SpnGroup(247),
                                                                                       new SpnGroup(248),
                                                                                       new SpnGroup(5837),
                                                                                       new SpnGroup(183, 1600),
                                                                                       new SpnGroup(1413,
                                                                                                    1433,
                                                                                                    1436),
                                                                                       new SpnGroup(2791),
                                                                                       new SpnGroup(5829),
                                                                                       new SpnGroup(27));

    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_2016 = List.of(new SpnGroup(514),
                                                                                       new SpnGroup(2978),
                                                                                       new SpnGroup(6895),
                                                                                       new SpnGroup(7333));

    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_2022 = List.of(new SpnGroup(166));

    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_2016_CI = List.of(new SpnGroup(3031, 3515));

    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_2024 = List.of(new SpnGroup(6894));
    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_2024_CI = List.of(new SpnGroup(12748));

    /*
     * The Data Stream SPNs that are required to be supported by the vehicle with a
     * Compression Ignition Engine
     */
    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_CI = List.of(new SpnGroup(5466),
                                                                                     new SpnGroup(3700),
                                                                                     new SpnGroup(5827, 5454),
                                                                                     new SpnGroup(3609,
                                                                                                  3610,
                                                                                                  3251),
                                                                                     new SpnGroup(94,
                                                                                                  157,
                                                                                                  164,
                                                                                                  5313,
                                                                                                  5314,
                                                                                                  5578),
                                                                                     new SpnGroup(3516,
                                                                                                  3518,
                                                                                                  7346),
                                                                                     new SpnGroup(3226));

    /*
     * The Data Stream SPNs that are required to be supported by the vehicle with a
     * Spark Ignition Engine
     */
    private static final Collection<SpnGroup> REQUIRED_DATA_STREAM_SPNS_SI = List.of(new SpnGroup(94,
                                                                                                  157,
                                                                                                  5313,
                                                                                                  5578),
                                                                                     new SpnGroup(51),
                                                                                     new SpnGroup(3464),
                                                                                     new SpnGroup(4236),
                                                                                     new SpnGroup(4237),
                                                                                     new SpnGroup(4240),
                                                                                     new SpnGroup(3249),
                                                                                     new SpnGroup(3241),
                                                                                     new SpnGroup(3217),
                                                                                     new SpnGroup(3227),
                                                                                     new SpnGroup(12744));

    /*
     * The Freeze Frame SPNs that are required to be supported by the vehicle
     */
    private static final Collection<SpnGroup> REQUIRED_FREEZE_FRAME_SPNS = List.of(new SpnGroup(92),
                                                                                   new SpnGroup(110,
                                                                                                1637,
                                                                                                4076,
                                                                                                4193),
                                                                                   new SpnGroup(190,
                                                                                                4201,
                                                                                                723,
                                                                                                4202),
                                                                                   new SpnGroup(512),
                                                                                   new SpnGroup(513),
                                                                                   new SpnGroup(3301));

    private static Collection<SpnGroup> getRequiredDataStreamSpns(FuelType fuelType, int engineModelYear) {
        Collection<SpnGroup> requiredSpns = new HashSet<>(REQUIRED_2013_DATA_STREAM_SPNS);

        if (fuelType.isCompressionIgnition()) {
            requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_CI);
            if (engineModelYear >= 2016) {
                requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_2016_CI);
            }
            if (engineModelYear >= 2024) {
                requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_2024_CI);
            }
        } else if (fuelType.isSparkIgnition()) {
            requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_SI);
        }

        if (engineModelYear >= 2016) {
            requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_2016);
        }
        if (engineModelYear >= 2022) {
            requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_2022);
        }
        if (engineModelYear >= 2024) {
            requiredSpns.addAll(REQUIRED_DATA_STREAM_SPNS_2024);
        }

        return requiredSpns.stream().sorted().collect(Collectors.toList());
    }

    public boolean validateRequiredDataStreamSpns(ResultsListener listener,
                                                  Collection<Integer> spns,
                                                  FuelType fuelType,
                                                  int engineModelYear) {

        boolean result = true;
        for (SpnGroup spnGroup : getRequiredDataStreamSpns(fuelType, engineModelYear)) {
            if (!spnGroup.isSatisfied(spns)) {
                listener.onResult("Required Data Stream SPNs are not supported. " + spnGroup);
                result = false;
            }
        }
        return result;
    }

    private static Collection<SpnGroup> getDesiredDataStreamSpns(FuelType fuelType, int engineModelYear) {
        Collection<SpnGroup> requiredSpns = new HashSet<>(DESIRED_DATA_STREAM_SPNS);

        if (fuelType.isCompressionIgnition()) {
            if (engineModelYear >= 2024) {
                requiredSpns.addAll(DESIRED_2024_CI_DATA_STREAM_SPNS);
            }
        }
        if (fuelType.isElectric()) {
            if (engineModelYear >= 2024) {
                requiredSpns.addAll(DESIRED_2024_XEV_DATA_STREAM_SPNS);
            }
        }
        if (fuelType.isHybrid()) {
            if (engineModelYear >= 2024) {
                requiredSpns.addAll(DESIRED_2024_HEV_DATA_STREAM_SPNS);
            }
        }
        if (engineModelYear >= 2016) {
            requiredSpns.addAll(DESIRED_2016_DATA_STREAM_SPNS);
        }
        if (engineModelYear >= 2022) {
            requiredSpns.addAll(DESIRED_2022_DATA_STREAM_SPNS);
        }
        if (engineModelYear >= 2024) {
            requiredSpns.addAll(DESIRED_2024_DATA_STREAM_SPNS);
        }

        return requiredSpns.stream().sorted().collect(Collectors.toList());
    }

    public boolean validateDesiredDataStreamSpns(ResultsListener listener,
                                                 Collection<Integer> spns,
                                                 FuelType fuelType,
                                                 int engineModelYear) {

        boolean result = true;
        for (SpnGroup spnGroup : getDesiredDataStreamSpns(fuelType, engineModelYear)) {
            if (!spnGroup.isSatisfied(spns)) {
                listener.onResult("Required Data Stream SPNs are not supported. " + spnGroup);
                result = false;
            }
        }
        return result;
    }

    private static Collection<SpnGroup> getNoticedDataStreamSpns(FuelType fuelType, int engineModelYear) {
        Collection<SpnGroup> requiredSpns = new HashSet<>(NOTICED_DATA_STREAM_SPNS);

        if (fuelType.isCompressionIgnition()) {
            if (engineModelYear >= 2024) {
                requiredSpns.addAll(NOTICED_2024_CI_DATA_STREAM_SPNS);
            }
        }
        return requiredSpns.stream().sorted().collect(Collectors.toList());
    }

    public boolean validateNoticedDataStreamSpns(ResultsListener listener,
                                                 Collection<Integer> spns,
                                                 FuelType fuelType,
                                                 int engineModelYear) {

        boolean result = true;
        for (SpnGroup spnGroup : getNoticedDataStreamSpns(fuelType, engineModelYear)) {
            if (!spnGroup.isSatisfied(spns)) {
                listener.onResult("Required Data Stream SPNs are not supported. " + spnGroup);
                result = false;
            }
        }
        return result;
    }

    public boolean validateFreezeFrameSpns(ResultsListener listener, Collection<Integer> spns) {

        boolean result = true;
        for (SpnGroup spnGroup : REQUIRED_FREEZE_FRAME_SPNS) {
            if (!spnGroup.isSatisfied(spns)) {
                listener.onResult("Required Freeze Frame SPNs are not supported. " + spnGroup);
                result = false;
            }
        }
        return result;
    }

    private static Collection<SpnGroup> getNoticedNoDupDataStreamSpns(FuelType fuelType, int engineModelYear) {
        Collection<SpnGroup> requiredSpns = new HashSet<>(NOTICED_NON_DUP_DATA_STREAM_SPNS);

        if (engineModelYear >= 2024) {
            requiredSpns.addAll(NOTICED_NON_DUP_2024_DATA_STREAM_SPNS);
        }

        return requiredSpns.stream().sorted().collect(Collectors.toList());
    }

    private static Collection<SpnGroup> getDesiredNoDupDataStreamSpns(FuelType fuelType, int engineModelYear) {
        Collection<SpnGroup> requiredSpns = new HashSet<>(DESIRED_NON_DUP_DATA_STREAM_SPNS);

        return requiredSpns.stream().sorted().collect(Collectors.toList());
    }

    public boolean isMoreThanOneSpnReportedInfo(ResultsListener listener,
                                                Collection<Integer> spns,
                                                FuelType fuelType,
                                                int engineModelYear) {
        boolean result = false;
        for (SpnGroup spnGroup : getNoticedNoDupDataStreamSpns(fuelType, engineModelYear)) {
            if (spnGroup.containsMultiple(spns)) {
                listener.onResult("More than one of the SPNs are reported as supported. " + spnGroup);
                result = true;
            }
        }
        return result;
    }

    public boolean isMoreThanOneSpnReportedWarning(ResultsListener listener,
                                                   Collection<Integer> spns,
                                                   FuelType fuelType,
                                                   int engineModelYear) {
        boolean result = false;
        for (SpnGroup spnGroup : getDesiredNoDupDataStreamSpns(fuelType, engineModelYear)) {
            if (spnGroup.containsMultiple(spns)) {
                listener.onResult("More than one of the SPNs are reported as supported. " + spnGroup);
                result = true;
            }
        }
        return result;
    }

}

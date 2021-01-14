/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.etools.j1939_84.NumberFormatter;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * {@link FunctionalModule} that requests DM5, DM20, DM21, and DM26 messages
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DiagnosticReadinessModule extends FunctionalModule {

    /**
     * Condenses the input string by using abbreviations and removing
     * unnecessary words
     *
     * @param input
     *         the input string
     * @param maxLen
     *         the maximum length of the string
     * @return a shorter version of the input string
     */
    private static String condense(String input, int maxLen) {
        input = input.replace("Aftertreatment", "AFT");
        input = input.replace("Engine ", "");
        input = input.replace("System", "Sys");
        input = input.replace("Diesel Particulate Filter ", "DPF ");
        input = input.replace("Selective Catalytic Reduction", "SCR");
        input = input.replace("Exhaust Gas Recirculation", "EGR");
        input = input.replace("Oxygen (or Exhaust Gas) Sensor Bank", "N/O2 Exh Gas Snsr");
        input = input.replace("Cold Start Emission Reduction Strategy", "Cold Start Strategy");
        input = input.replace("Catalyst Bank", "Catalyst");
        input = input.replace("Secondary Air", "2ndary Air");
        input = input.replace("Monitor", "Mon");
        input = input.replace("Evaporative", "Evap");
        input = input.replace("Positive Crankcase Ventilation", "+Crankcase Vent");
        input = input.replace("Pressure", "Press");
        input = input.replace("Exhaust", "Exh");
        input = input.replace("Sensor", "Snsr");
        input = input.replace("SPN ", "");

        if (input.length() > maxLen) {
            input = input.substring(0, maxLen - 1) + ".";
        }
        return input;
    }

    /**
     * Helper method to gather the given monitored systems into a vehicle
     * composite
     *
     * @param monitoredSystems
     *         the {@link MonitoredSystem}s to gather
     * @param isDM5
     *         indicates if these systems are from a DM5 message
     * @return a List of {@link MonitoredSystem}
     */
    public static List<CompositeMonitoredSystem> getCompositeSystems(Collection<MonitoredSystem> monitoredSystems,
                                                                     boolean isDM5) {
        Map<CompositeSystem, CompositeMonitoredSystem> map = new HashMap<>();
        for (MonitoredSystem system : monitoredSystems) {
            CompositeSystem key = system.getId();
            CompositeMonitoredSystem existingSystem = map.get(key);
            if (existingSystem == null) {
                map.put(key, new CompositeMonitoredSystem(system, isDM5));
            } else {
                existingSystem.addMonitoredSystems(system);
            }
        }
        List<CompositeMonitoredSystem> systems = new ArrayList<>(map.values());
        Collections.sort(systems);
        return systems;
    }

    /**
     * Helper method to extract all the {@link MonitoredSystem}s given a
     * {@link List} of {@link DiagnosticReadinessPacket}s
     *
     * @param packets
     *         the Packets to parse
     * @param isDM5
     *         true to indicate the packets are DM5s
     * @return a {@link List} of {@link MonitoredSystem}s
     */
    public static List<CompositeMonitoredSystem> getCompositeSystems(List<? extends DiagnosticReadinessPacket> packets,
                                                                     boolean isDM5) {
        return getCompositeSystems(packets.stream()
                                           .flatMap(p -> p.getMonitoredSystems().stream())
                                           .collect(Collectors.toSet()), isDM5);
    }

    /**
     * Helper method to get the Number of Ignition Cycles from the packets. The
     * maximum value is returned.
     *
     * @param packets
     *         the {@link Collection} of
     *         {@link DM20MonitorPerformanceRatioPacket}
     * @return int
     */
    public static int getIgnitionCycles(Collection<DM20MonitorPerformanceRatioPacket> packets) {
        return packets.stream()
                .mapToInt(DM20MonitorPerformanceRatioPacket::getIgnitionCycles)
                .filter(v -> v <= 0xFAFF)
                .max()
                .orElse(-1);
    }

    /**
     * Helper method to get the maximum number of OBD Monitoring Conditions
     * Encountered from the packets. The maximum value is returned.
     *
     * @param packets
     *         the {@link Collection} of
     *         {@link DM20MonitorPerformanceRatioPacket}
     * @return int
     */
    public static int getOBDCounts(Collection<DM20MonitorPerformanceRatioPacket> packets) {
        return packets.stream()
                .mapToInt(DM20MonitorPerformanceRatioPacket::getOBDConditionsCount)
                .filter(v -> v <= 0xFAFF)
                .max()
                .orElse(-1);
    }

    /**
     * Helper method to get the {@link Set} of {@link PerformanceRatio} from the
     * packets
     *
     * @param packets
     *         the {@link Collection} of
     *         {@link DM20MonitorPerformanceRatioPacket}
     * @return {@link Set} of {@link PerformanceRatio}
     */
    public static Set<PerformanceRatio> getRatios(Collection<DM20MonitorPerformanceRatioPacket> packets) {
        return packets.stream().flatMap(t -> t.getRatios().stream()).collect(Collectors.toSet());
    }

    /**
     * Helper method to get the {@link Set} of {@link MonitoredSystem} from
     * {@link DiagnosticReadinessPacket}
     *
     * @param packets
     *         the {@link Collection} of {@link DiagnosticReadinessPacket}
     * @return {@link Set} of {@link MonitoredSystem}
     */
    public static Set<MonitoredSystem> getSystems(Collection<? extends DiagnosticReadinessPacket> packets) {
        return packets.stream().flatMap(t -> t.getMonitoredSystems().stream()).collect(Collectors.toSet());
    }

    /**
     * Sends a global request for DM20 Packets. The request and results will be
     * returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM20MonitorPerformanceRatioPacket}s
     */
    public List<DM20MonitorPerformanceRatioPacket> getDM20Packets(ResultsListener listener,
                                                                  boolean fullString) {
        return getPacketsFromGlobal("Global DM20 Request",
                                    DM20MonitorPerformanceRatioPacket.PGN,
                                    DM20MonitorPerformanceRatioPacket.class,
                                    listener,
                                    fullString).getPackets();
    }

    /**
     * Sends an address specific request for DM21 Packets. The request and
     * results will be returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @param obdModuleAddress
     *         the address to which the destination specific request will be
     *         sent
     * @return the {@link List} of {@link DM21DiagnosticReadinessPacket}s
     */
    public BusResult<DM21DiagnosticReadinessPacket> getDM21Packets(ResultsListener listener, boolean fullString,
                                                                   int obdModuleAddress) {
        return getPacketDS("Destination Specific DM21 Request",
                           DM21DiagnosticReadinessPacket.PGN,
                           DM21DiagnosticReadinessPacket.class,
                           listener,
                           fullString,
                           obdModuleAddress);
    }

    /**
     * Sends a global request for DM26 Packets. The request and results will be
     * returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM26TripDiagnosticReadinessPacket}s
     */
    public List<DM26TripDiagnosticReadinessPacket> getDM26Packets(ResultsListener listener,
                                                                  boolean fullString) {

        return getPacketsFromGlobal("Global DM26 Request",
                                    DM26TripDiagnosticReadinessPacket.PGN,
                                    DM26TripDiagnosticReadinessPacket.class,
                                    listener,
                                    fullString).getPackets();

    }

    /**
     * Sends a global request for DM5 Packets. The request and results will be
     * returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM5DiagnosticReadinessPacket}s
     */
    public List<DM5DiagnosticReadinessPacket> getDM5Packets(ResultsListener listener, boolean fullString) {
        return getPacketsFromGlobal("Global DM5 Request",
                                    DM5DiagnosticReadinessPacket.PGN,
                                    DM5DiagnosticReadinessPacket.class,
                                    listener,
                                    fullString).getPackets();
    }

    /**
     * Sends the DM5 to determine which modules support HD-OBD. It returns a
     * {@link List} of source addresses of the modules that do support HD-OBD.
     *
     * @param listener
     *         the {@link ResultsListener} that is notified of the
     *         communications
     * @return List of source addresses
     */
    public List<Integer> getOBDModules(ResultsListener listener) {
        List<DM5DiagnosticReadinessPacket> packets = getDM5Packets(listener, false);
        List<Integer> addresses = packets.stream().filter(DM5DiagnosticReadinessPacket::isHdObd)
                .map(ParsedPacket::getSourceAddress)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        if (addresses.isEmpty()) {
            listener.onResult("No modules report as HD-OBD compliant - stopping.");
        } else {
            for (int i : addresses) {
                listener.onResult(getAddressName(i) + " reported as an HD-OBD Module.");
            }
        }
        return addresses;
    }

    /**
     * Helper method to return the {@link MonitoredSystem} padded with extra
     * space on the right if necessary
     *
     * @param system
     *         the {@link MonitoredSystem} to pad
     * @return String with extra space on the right
     */
    private String getPaddedStatus(MonitoredSystem system) {
        String status;
        if (!system.getStatus().isEnabled()) {
            status = "Unsupported";
        } else {
            status = (system.getStatus().isComplete() ? "   " : "Not") + " Complete";
        }
        return padRight(" " + status, 14);
    }

    /**
     * Pads the String with spaces on the left
     *
     * @param string
     *         the String to pad
     * @param length
     *         the maximum number of spaces
     * @return the padded string
     */
    private String padLeft(String string, int length) {
        return String.format("%1$" + length + "s", string);
    }

    /**
     * Pads the String with spaces on the right
     *
     * @param string
     *         the String to pad
     * @param length
     *         the maximum number of spaces
     * @return the padded string
     */
    private String padRight(String string, int length) {
        return String.format("%1$-" + length + "s", string);
    }

    /**
     * Requests all DM20s from all vehicle modules. The results are reported
     * back to the supplied listener
     *
     * @param listener
     *         the {@link ResultsListener} that will be notified of results
     * @return true if packets were received
     */
    public boolean reportDM20(ResultsListener listener) {
        List<DM20MonitorPerformanceRatioPacket> packets = getDM20Packets(listener, true);
        return !packets.isEmpty();
    }

    /**
     * Requests all DM26 from all vehicle modules. The compiles the
     * {@link MonitoredSystem} to include a vehicle composite of those systems
     * for the report. The results are reported back to the supplied listener
     *
     * @param listener
     *         the {@link ResultsListener} that will be notified of results
     * @return true if packets were received
     */
    public boolean reportDM26(ResultsListener listener) {
        List<DM26TripDiagnosticReadinessPacket> packets = getDM26Packets(listener, true);
        if (!packets.isEmpty()) {
            listener.onResult("");
            listener.onResult("Vehicle Composite of DM26:");
            List<CompositeMonitoredSystem> systems = getCompositeSystems(packets, false);
            listener.onResult(systems.stream().map(MonitoredSystem::toString).collect(Collectors.toList()));
        }
        return !packets.isEmpty();
    }

    /**
     * Requests all DM5 from all vehicle modules. The compiles the
     * {@link MonitoredSystem} to include a vehicle composite of those systems
     * for the report. The results are reported back to the supplied listener
     *
     * @param listener
     *         the {@link ResultsListener} that will be notified of results
     * @return true if packets were received
     */
    public boolean reportDM5(ResultsListener listener) {
        List<DM5DiagnosticReadinessPacket> packets = getDM5Packets(listener, true);
        if (!packets.isEmpty()) {
            listener.onResult("");
            listener.onResult("Vehicle Composite of DM5:");
            List<CompositeMonitoredSystem> systems = getCompositeSystems(packets, true);
            listener.onResult(systems.stream().map(MonitoredSystem::toString).collect(Collectors.toList()));
        }
        return !packets.isEmpty();
    }

    /**
     * Adds a report of the difference between the initial values and the final
     * values. The results are returned to the listener
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the table
     * @param initialValues
     *         the {@link Collection} of {@link MonitoredSystem} that were
     *         gathered when the process started
     * @param finalValues
     *         the {@link Collection} of {@link MonitoredSystem} that were
     *         last gathered in the process
     * @param initialTime
     *         the formatted time when the process started
     * @param finalTime
     *         the formatted time when the process ended
     */
    public void reportMonitoredSystems(ResultsListener listener,
                                       Collection<MonitoredSystem> initialValues,
                                       Collection<MonitoredSystem> finalValues,
                                       String initialTime,
                                       String finalTime) {
        // By design the total number will always be the same. If they are not,
        // cash in your chips because you can't trust anything

        // These are sorted by Name
        List<CompositeMonitoredSystem> startSystems = getCompositeSystems(initialValues, true);
        List<CompositeMonitoredSystem> endSystems = getCompositeSystems(finalValues, true);

        String[] initialDateTime = initialTime.split("T");
        String[] finalDateTime = finalTime.split("T");

        listener.onResult(getTime() + " Vehicle Composite Results of DM5:");

        String separator = "+----------------------------+----------------+----------------+";

        listener.onResult(separator);
        listener.onResult("| " + padLeft(padRight("Monitor", 16), 26) + " | Initial Status |  Last Status   |");
        listener.onResult("| " + padRight("", 26) + " | " + padRight("  " + initialDateTime[0], 14) + " | "
                                  + padRight("  " + finalDateTime[0], 14) + " |");
        listener.onResult("| " + padRight("", 26) + " | " + padRight(" " + initialDateTime[1], 14) + " | "
                                  + padRight(" " + finalDateTime[1], 14) + " |");
        listener.onResult(separator);

        for (int i = 0; i < startSystems.size(); i++) {
            MonitoredSystem startSystem = startSystems.get(i);
            MonitoredSystem endSystem = endSystems.get(i);
            boolean diff = endSystem.getStatus() != startSystem.getStatus();
            listener.onResult("|" + (diff ? "*" : " ") + startSystem.getName() + " | " + getPaddedStatus(startSystem)
                                      + " | " + getPaddedStatus(endSystem) + (diff ? "*" : " ") + "|");
        }
        listener.onResult(separator);
    }

    /**
     * Reports the difference between the initial values and final values in a
     * table format. The results are returned to the listener.
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the table
     * @param initialValues
     *         the {@link Collection} of {@link PerformanceRatio} that were
     *         gathered when the process started
     * @param finalValues
     *         the {@link Collection} of {@link PerformanceRatio} that were
     *         last gathered by the process
     * @param initialIgnitionCycles
     *         the initial value of the number of Ignition Cycles
     * @param finalIgnitionCycles
     *         the final value of the number of Ignition Cycles
     * @param initialObdCounts
     *         the initial value of the number of OBD Monitoring Conditions
     *         Encountered
     * @param finalObdCounts
     *         the final value of the number of OBD Monitoring Conditions
     *         Encountered
     * @param initialTime
     *         the formatted time when the process started
     * @param finalTime
     *         the formatted time when the process ended
     */
    public void reportPerformanceRatios(ResultsListener listener,
                                        Collection<PerformanceRatio> initialValues,
                                        Collection<PerformanceRatio> finalValues,
                                        int initialIgnitionCycles,
                                        int finalIgnitionCycles,
                                        int initialObdCounts,
                                        int finalObdCounts,
                                        String initialTime,
                                        String finalTime) {

        // Sorts the lists by Source then by Ratio Name
        Comparator<? super PerformanceRatio> comparator = Comparator.comparing(o -> (o.getSource() + " " + o.getName()));

        List<PerformanceRatio> startingRatios = new ArrayList<>(initialValues);
        startingRatios.sort(comparator);

        List<PerformanceRatio> endingRatios = new ArrayList<>(finalValues);
        endingRatios.sort(comparator);

        int nameLen = 32;
        int srcLen = 3;

        listener.onResult(getTime() + " Vehicle Composite Results of DM20:");
        // Make String of spaces for the Source Column
        String sourceSpace = padRight("", srcLen);

        // Make a String of spaces for the Name Column
        String nameSpace = padRight("", nameLen);

        String separator1 = ("+ " + sourceSpace + " + " + nameSpace + " +-----------------+-----------------+")
                .replaceAll(" ", "-");

        String separator2 = ("+ " + sourceSpace + " + " + nameSpace + " +--------+--------+--------+--------+")
                .replaceAll(" ", "-");

        String[] initialDateTime = initialTime.split("T");
        String[] finalDateTime = finalTime.split("T");
        listener.onResult(separator1);
        listener.onResult("| " + sourceSpace + " | " + nameSpace + " |  Initial Status |   Last Status   |");
        listener.onResult("| " + sourceSpace + " | " + nameSpace + " |    " + initialDateTime[0] + "   |    "
                                  + finalDateTime[0] + "   |");
        listener.onResult("| " + sourceSpace + " | " + nameSpace + " |   " + initialDateTime[1] + "  |   "
                                  + finalDateTime[1] + "  |");
        listener.onResult(separator1);

        boolean diff = initialIgnitionCycles != finalIgnitionCycles;
        listener.onResult("|" + (diff ? "*" : " ") + sourceSpace + " | " + padRight("Ignition Cycles", nameLen) + " |  "
                                  + padLeft("" + initialIgnitionCycles, 14) + " |  " + padLeft("" + finalIgnitionCycles,
                                                                                               13)
                                  + (diff ? " *" : "  ") + "|");

        diff = initialObdCounts != finalObdCounts;

        listener.onResult("|" + (diff ? "*" : " ") + sourceSpace + " | "
                                  + padRight("OBD Monitoring Conditions Count",
                                             nameLen) + " |  " + padLeft("" + initialObdCounts, 14)
                                  + " |  " + padLeft("" + finalObdCounts, 13) + (diff ? " *" : "  ") + "|");

        listener.onResult(separator2);
        listener.onResult("| " + padRight("Src", srcLen) + " | " + padRight("Monitor", nameLen)
                                  + " |  Num'r |  Den'r |  Num'r |  Den'r |");
        listener.onResult(separator2);

        for (PerformanceRatio ratio1 : startingRatios) {
            PerformanceRatio ratio2 = null;
            for (int j = 0; j < endingRatios.size(); j++) {
                if (ratio1.getId() == endingRatios.get(j).getId()) {
                    ratio2 = endingRatios.remove(j);
                    break;
                }
            }

            String name = condense(ratio1.getName(), nameLen);
            String source = String.valueOf(ratio1.getSourceAddress());
            int num1 = ratio1.getNumerator();
            int dem1 = ratio1.getDenominator();
            int num2 = ratio2 != null ? ratio2.getNumerator() : -1;
            int dem2 = ratio2 != null ? ratio2.getDenominator() : -1;

            listener.onResult(rowForDM20(srcLen, source, nameLen, name, num1, dem1, num2, dem2));
        }

        for (PerformanceRatio ratio2 : endingRatios) {
            String name = condense(ratio2.getName(), nameLen);
            String source = String.valueOf(ratio2.getSourceAddress());
            int num1 = -1;
            int dem1 = -1;
            int num2 = ratio2.getNumerator();
            int dem2 = ratio2.getDenominator();

            listener.onResult(rowForDM20(srcLen, source, nameLen, name, num1, dem1, num2, dem2));
        }

        listener.onResult(separator2);
    }

    /**
     * Sends a global request for DM20 Packets. The request and results will be
     * returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM20MonitorPerformanceRatioPacket}s
     */
    public RequestResult<DM20MonitorPerformanceRatioPacket> requestDM20(ResultsListener listener,
                                                                        boolean fullString) {
        return getPacketsFromGlobal("Global DM20 Request",
                                    DM20MonitorPerformanceRatioPacket.PGN,
                                    DM20MonitorPerformanceRatioPacket.class,
                                    listener,
                                    fullString);
    }

    /**
     * Sends a destination specific request for DM20 Packets. The request and
     * results will be returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM20MonitorPerformanceRatioPacket}s
     */
    public BusResult<DM20MonitorPerformanceRatioPacket> requestDM20(ResultsListener listener,
                                                                    boolean fullString, int obdAddress) {

        return getPacketDS("Destination Specific DM20 Request to " + getAddressName(obdAddress),
                           DM20MonitorPerformanceRatioPacket.PGN,
                           DM20MonitorPerformanceRatioPacket.class,
                           listener,
                           fullString,
                           obdAddress);
    }

    /**
     * Sends a global request for DM21 Packets. The request and results will be
     * returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM21DiagnosticReadinessPacket}s
     */
    public RequestResult<DM21DiagnosticReadinessPacket> requestDM21Packets(ResultsListener listener,
                                                                           boolean fullString) {
        return getPacketsFromGlobal("Global DM21 Request",
                                    DM21DiagnosticReadinessPacket.PGN,
                                    DM21DiagnosticReadinessPacket.class,
                                    listener,
                                    fullString);
    }

    /**
     * Sends a global request for DM5 Packets. The request and results will be
     * returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM5DiagnosticReadinessPacket}s
     */
    public RequestResult<DM5DiagnosticReadinessPacket> requestDM5(ResultsListener listener, boolean fullString) {

        return getPacketsFromGlobal("Global DM5 Request",
                                    DM5DiagnosticReadinessPacket.PGN,
                                    DM5DiagnosticReadinessPacket.class,
                                    listener,
                                    fullString);
    }

    /**
     * Sends a destination request for DM5 Packets. The request and results will
     * be returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @param obdAddress
     *         address of module to which request is to be made
     * @return the {@link List} of {@link DM5DiagnosticReadinessPacket}s
     */
    public BusResult<DM5DiagnosticReadinessPacket> requestDM5(ResultsListener listener, boolean fullString,
                                                              int obdAddress) {

        return getPacketDS("Destination Specific DM5 Request to " + getAddressName(obdAddress),
                           DM5DiagnosticReadinessPacket.PGN,
                           DM5DiagnosticReadinessPacket.class,
                           listener,
                           fullString,
                           obdAddress);
    }

    /**
     * Sends a global request for DM5 Packets. The request and results will be
     * returned to the {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} for the results
     * @param fullString
     *         true to include the full string of the results in the report;
     *         false to only include the returned raw packet in the report
     * @return the {@link List} of {@link DM5DiagnosticReadinessPacket}s
     */
    public RequestResult<DM5DiagnosticReadinessPacket> requestDM5Packets(ResultsListener listener, boolean fullString) {
        List<DM5DiagnosticReadinessPacket> parsedPackets = getPacketsFromGlobal("Global DM5 Request",
                                                                                DM5DiagnosticReadinessPacket.PGN,
                                                                                DM5DiagnosticReadinessPacket.class,
                                                                                listener,
                                                                                fullString)
                // FIXME this ignores NACKs
                .getPackets();

        if (!parsedPackets.isEmpty()) {
            listener.onResult("");
            listener.onResult("Vehicle Composite of DM5:");
            List<CompositeMonitoredSystem> systems = getCompositeSystems(parsedPackets, true);
            listener.onResult(systems.stream().map(MonitoredSystem::toString).collect(Collectors.toList()));
        }
        return new RequestResult<>(false, parsedPackets, Collections.emptyList());
    }

    private String rowForDM20(int sourceLength,
                              String source,
                              int nameLength,
                              String name,
                              Integer initialNumerator,
                              Integer initialDenominator,
                              Integer finalNumerator,
                              Integer finalDenominator) {

        String iNum = initialNumerator == null ? "" : NumberFormatter.format(initialNumerator);
        String iDem = initialDenominator == null ? "" : NumberFormatter.format(initialDenominator);
        String fNum = finalNumerator == null ? "" : NumberFormatter.format(finalNumerator);
        String fDem = finalDenominator == null ? "" : NumberFormatter.format(finalDenominator);

        boolean nDiff = !iNum.equals(fNum);
        boolean dDiff = !iDem.equals(fDem);

        return "|" + (dDiff | nDiff ? "*" : " ") + padLeft(source, sourceLength) + " | " + padRight(name, nameLength)
                + (name.endsWith(".") ? "." : " ") + "| " + padLeft(iNum, 6) + " | " + padLeft(iDem, 6) + " | "
                + padLeft(fNum, 5) + (nDiff ? "*" : " ") + " | " + padLeft(fDem, 5) + (dDiff ? " *" : "  ") + "|";
    }

}

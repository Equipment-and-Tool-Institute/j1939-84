/**
 * Copyright 2017 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult.TestResult;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * The {@link FunctionalModule} that collects the Scaled Test Results from the
 * OBD Modules
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class OBDTestsModule extends FunctionalModule {

    /**
     * Constructor
     */
    public OBDTestsModule() {
        this(new DateTimeModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param dateTimeModule
     *                       the {@link DateTimeModule} to use
     */
    public OBDTestsModule(DateTimeModule dateTimeModule) {
        super(dateTimeModule);
    }

    /**
     * Helper method to create a DM7 packet with Test ID of 247, FMI 31 and the
     * given SPN. The request will be sent to the specific destination
     *
     * @param destination
     *                    the destination address for the packet
     * @param spn
     *                    the SPN
     * @return Packet
     */
    private Packet createDM7Packet(int destination, int spn) {
        return Packet.create(DM7CommandTestsPacket.PGN | destination,
                getJ1939().getBusAddress(),
                true,
                247,
                spn & 0xFF,
                (spn >> 8) & 0xFF,
                (((spn >> 16) & 0xFF) << 5) | 31,
                0xFF,
                0xFF,
                0xFF,
                0xFF);
    }

    /**
     * Queries the vehicle to get all Scaled Tests Results and reports then back
     * to the listener
     *
     * @param listener
     *                   the {@link ResultsListener}
     * @param obdModules
     *                   the {@link List} of addresses for ODB Modules
     */
    public void reportOBDTests(ResultsListener listener, List<Integer> obdModules) {
        Map<Integer, List<ScaledTestResult>> allTestResults = new HashMap<>();
        for (DM24SPNSupportPacket packet : requestSupportedSpnPackets(listener, obdModules).getPackets()) {
            int destination = packet.getSourceAddress();
            String moduleName = Lookup.getAddressName(destination);
            // Find tests that support scaled results, remove duplicates and use
            // a predictable order for testing.
            List<Integer> spns = packet.getSupportedSpns().stream().filter(t -> t.supportsScaledTestResults())
                    .map(s -> s.getSpn()).sorted().distinct().collect(Collectors.toList());
            if (spns.isEmpty()) {
                listener.onResult(moduleName + " does not have any tests that support scaled tests results");
                listener.onResult("");
            } else {
                List<ScaledTestResult> testResults = requestScaledTestResultsFromModule(listener,
                        destination,
                        moduleName,
                        spns);
                allTestResults.put(destination, testResults);
                if (testResults.isEmpty()) {
                    listener.onResult("No Scaled Tests Results from " + moduleName);
                    listener.onResult("");
                }
            }
        }

        boolean hasTests = false;
        List<String> incompleteTests = new ArrayList<>();
        for (Entry<Integer, List<ScaledTestResult>> entry : allTestResults.entrySet()) {
            int key = entry.getKey();
            String module = Lookup.getAddressName(key);
            List<ScaledTestResult> results = entry.getValue();
            hasTests |= !results.isEmpty();
            // Find the tests that are incomplete and add them as string to the
            // list of incomplete tests
            incompleteTests.addAll(results.stream().filter(t -> t.getTestResult() == TestResult.NOT_COMPLETE)
                    .map(t -> "  " + module + ": " + t.toString()).collect(Collectors.toList()));
        }
        Collections.sort(incompleteTests);

        if (!hasTests) {
            listener.onResult("ERROR No tests results returned");
        } else if (incompleteTests.isEmpty()) {
            listener.onResult("All Tests Complete");
        } else {
            listener.onResult("Incomplete Tests: [");
            listener.onResult(incompleteTests);
            listener.onResult("]");
            listener.onResult(incompleteTests.size() + " Incomplete Test" + (incompleteTests.size() == 1 ? "" : "s"));
        }
    }

    /**
     * Sends a request to the given destination address for the Scaled Test
     * Results for the specified SPN
     *
     * @param listener
     *                    the {@link ResultsListener}
     * @param destination
     *                    the destination address to send the request to
     * @param spn
     *                    the SPN for which the Scaled Test Results are being
     *                    requested
     * @return the {@link List} of {@link DM30ScaledTestResultsPacket} returned.
     */
    private List<ScaledTestResult> requestScaledTestResultsForSpn(ResultsListener listener, int destination, int spn) {
        Packet request = createDM7Packet(destination, spn);
        listener.onResult(getTime() + " " + request.toString());
        DM30ScaledTestResultsPacket packet = getJ1939()
                .requestPacket(request, DM30ScaledTestResultsPacket.class, destination, 3).orElse(null);
        if (packet == null) {
            listener.onResult(TIMEOUT_MESSAGE);
            listener.onResult("");
            return Collections.emptyList();
        } else {
            listener.onResult(packet.getPacket().toString(getDateTimeModule().getTimeFormatter()));
            listener.onResult(packet.toString());
            listener.onResult("");
            return packet.getTestResults();
        }
    }

    /**
     * Send a request to the given destination address for the Scaled Test
     * Results for all the Supported SPNs
     *
     * @param listener
     *                    the {@link ResultsListener}
     * @param destination
     *                    the destination address to send the request to
     * @param moduleName
     *                    the name of the vehicle module for the report
     * @param spns
     *                    the {@link List} of SPNs that will be requested
     * @return List of {@link ScaledTestResult}s
     */
    private List<ScaledTestResult> requestScaledTestResultsFromModule(ResultsListener listener,
            int destination,
            String moduleName,
            List<Integer> spns) {
        List<ScaledTestResult> scaledTestResults = new ArrayList<>();
        listener.onResult(getDateTime() + " Direct DM30 Requests to " + moduleName);
        for (int spn : spns) {
            List<ScaledTestResult> results = requestScaledTestResultsForSpn(listener, destination, spn);
            scaledTestResults.addAll(results);
        }
        return scaledTestResults;
    }

    /**
     * Sends a request to the vehicle for {@link DM24SPNSupportPacket}s
     *
     * @param listener
     *                 the {@link ResultsListener}
     * @return {@link List} of {@link DM24SPNSupportPacket}s
     */
    public RequestResult<DM24SPNSupportPacket> requestSupportedSpnPackets(ResultsListener listener,
            Collection<Integer> obdModuleAddresses) {
        List<DM24SPNSupportPacket> packets = new ArrayList<>();
        boolean retryUsed = false;

        for (int address : obdModuleAddresses) {
            Packet request = getJ1939().createRequestPacket(DM24SPNSupportPacket.PGN, address);
            listener.onResult(getDateTime() + " Direct DM24 Request to " + Lookup.getAddressName(address));
            listener.onResult(getTime() + " " + request.toString());
            Optional<BusResult<DM24SPNSupportPacket>> results = getJ1939()
                    .requestPacket(request, DM24SPNSupportPacket.class, address, 3, TimeUnit.SECONDS.toMillis(15));
            if (!results.isPresent()) {
                listener.onResult(TIMEOUT_MESSAGE);
            } else {
                DM24SPNSupportPacket packet = results.get().getPacket();
                listener.onResult(packet.getPacket().toString(getDateTimeModule().getTimeFormatter()));
                listener.onResult(packet.toString());
                packets.add(packet);
            }
            listener.onResult("");
        }
        return new RequestResult<>(retryUsed, packets);
    }

}

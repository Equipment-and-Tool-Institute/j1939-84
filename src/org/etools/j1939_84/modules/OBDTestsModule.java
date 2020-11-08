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
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult.TestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
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
     *            the {@link DateTimeModule} to use
     */
    public OBDTestsModule(DateTimeModule dateTimeModule) {
        super(dateTimeModule);
    }

    /**
     * Helper method to create a DM7 packet with Test ID of 247, FMI 31 and the
     * given SPN. The request will be sent to the specific destination
     *
     * @param destination
     *            the destination address for the packet
     * @param spn
     *            the SPN
     * @param destination
     *            the destination address for the packet
     * @param spn
     *            the SPN
     * @return Packet
     */
    public Packet createDM7Packet(int destination, int spn) {
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
     * Sends a destination specific request to the vehicle for
     * {@link DM7CommandTestsPacket}s
     *
     * @param listener
     *            the {@link ResultsListener}
     * @return {@link List} of {@link DM30ScaledTestResultsPacket}s
     */
    public List<DM30ScaledTestResultsPacket> getDM30Packets(ResultsListener listener, int address, SupportedSPN spn) {
        List<DM30ScaledTestResultsPacket> dm30Packets = requestDM30Packets(listener, address, spn.getSpn())
                .getPackets();
        reportDM30Results(listener, dm30Packets);
        return dm30Packets;
    }

    private void reportDM30Results(ResultsListener listener, List<DM30ScaledTestResultsPacket> requestedPackets) {

        requestedPackets.stream().forEach(packet -> {
            listener.onResult(packet.getPacket().toString(getDateTimeModule().getTimeFormatter()));
        });

    }

    private void reportObdTests(ResultsListener listener, List<DM24SPNSupportPacket> requestedPackets) {
        Map<Integer, List<ScaledTestResult>> allTestResults = new HashMap<>();
        for (DM24SPNSupportPacket packet : requestedPackets) {
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
     * Queries the vehicle to get all Scaled Tests Results and reports then back
     * to the listener
     *
     * @param listener
     *            the {@link ResultsListener}
     * @param obdModules
     *            the {@link List} of addresses for ODB Modules
     */
    public void reportOBDTests(ResultsListener listener, List<Integer> obdModules) {

        List<DM24SPNSupportPacket> requestedPackets = requestSupportedSpnPackets(listener, obdModules).getPackets();

        reportObdTests(listener, requestedPackets);
    }

    @SuppressWarnings("unused")
    private void reportResults(ResultsListener listener, List<DM24SPNSupportPacket> requestedPackets) {
        Map<Integer, List<ScaledTestResult>> allTestResults = new HashMap<>();
        for (DM24SPNSupportPacket packet : requestedPackets) {
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
                listener.onResult("No Scaled Tests Results from " + moduleName);
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
     * Sends a destination specific request to a module for
     * {@link DM24SPNSupportPacket}s DM24 are only destination specific messages
     *
     * @param listener
     *            {@link ResultsListener}
     * @param obdModuleAddress
     *            {@link Integer}
     * @param tries
     *            {@link Integer}
     * @param timeOutInMillis
     *            {@link Integer}
     * @return {@link List} of {@link DM24SPNSupportPacket}s
     */
    public BusResult<DM24SPNSupportPacket> requestDM24(ResultsListener listener,
            int obdModuleAddress) {

        Packet request = getJ1939().createRequestPacket(DM24SPNSupportPacket.PGN, obdModuleAddress);
        listener.onResult(getTime() + " Direct DM24 Request to " + Lookup.getAddressName(obdModuleAddress));
        listener.onResult(getTime() + " " + request.toString());
        return getJ1939().requestDS(DM24SPNSupportPacket.class, request);

    }

    /**
     * Sends a destination specific request to the vehicle for
     * {@link DM7CommandTestsPacket}s
     *
     * @param listener
     *            the {@link ResultsListener}
     * @return {@link List} of {@link DM30ScaledTestResultsPacket}s
     */
    public RequestResult<DM30ScaledTestResultsPacket> requestDM30Packets(ResultsListener listener, int address,
            int spn) {
        Packet request = createDM7Packet(address, spn);
        listener.onResult(getTime() + " " + request.toString());

        DM30ScaledTestResultsPacket packet = getJ1939()
                .requestDm7(request).getPacket()
                .flatMap(e -> e.left)
                .orElse(null);
        if (packet == null) {
            listener.onResult(TIMEOUT_MESSAGE);
            listener.onResult("");
            return new RequestResult<>(true, Collections.emptyList());
        } else {
            listener.onResult(packet.getPacket().toString(getDateTimeModule().getTimeFormatter()));
            listener.onResult(packet.toString());
            listener.onResult("");
            return new RequestResult<>(false, Collections.singletonList(packet), Collections.emptyList());
        }
    }

    public RequestResult<DM24SPNSupportPacket> requestObdTests(ResultsListener listener,
            List<Integer> obdModuleAddresses) {
        RequestResult<DM24SPNSupportPacket> requestedPackets = requestSupportedSpnPackets(listener, obdModuleAddresses);
        reportObdTests(listener, requestedPackets.getPackets());
        return requestedPackets;
    }

    /**
     * Sends a request to the given destination address for the Scaled Test
     * Results for the specified SPN
     *
     * @param listener
     *            the {@link ResultsListener}
     * @param destination
     *            the destination address to send the request to
     * @param spn
     *            the SPN for which the Scaled Test Results are being requested
     * @return the {@link List} of {@link DM30ScaledTestResultsPacket} returned.
     */
    private List<ScaledTestResult> requestScaledTestResultsForSpn(ResultsListener listener, int destination, int spn) {
        Packet request = createDM7Packet(destination, spn);
        listener.onResult(getTime() + " " + request.toString());
        DM30ScaledTestResultsPacket packet = getJ1939().requestDm7(request).getPacket()
                .flatMap(br -> br.left)
                .orElse(null);
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
     *            the {@link ResultsListener}
     * @param destination
     *            the destination address to send the request to
     * @param moduleName
     *            the name of the vehicle module for the report
     * @param spns
     *            the {@link List} of SPNs that will be requested
     * @return List of {@link ScaledTestResult}s
     */
    private List<ScaledTestResult> requestScaledTestResultsFromModule(ResultsListener listener,
            int destination,
            String moduleName,
            List<Integer> spns) {
        List<ScaledTestResult> scaledTestResults = new ArrayList<>();
        listener.onResult(getTime() + " Direct DM30 Requests to " + moduleName);
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
     *            the {@link ResultsListener}
     * @param obdModuleAddresses
     *            {@link Collection} of Integers}
     * @return {@link List} of {@link DM24SPNSupportPacket}s
     */
    public RequestResult<DM24SPNSupportPacket> requestSupportedSpnPackets(ResultsListener listener,
            List<Integer> obdModuleAddresses) {
        List<DM24SPNSupportPacket> packets = new ArrayList<>();
        boolean retryUsed = false;

        for (int address : obdModuleAddresses) {
            Packet request = getJ1939().createRequestPacket(DM24SPNSupportPacket.PGN, address);
            listener.onResult(getTime() + " Direct DM24 Request to " + Lookup.getAddressName(address));
            listener.onResult(getTime() + " " + request.toString());
            // FIXME, this should be 220 ms, not 3 s. 6.1.4.1.b
            BusResult<DM24SPNSupportPacket> busResult = getJ1939().requestDS(DM24SPNSupportPacket.class, request);
            retryUsed |= busResult.isRetryUsed();
            busResult
                    .getPacket()
                    .ifPresentOrElse(packet -> {
                        // log DM24 or ACK
                        ParsedPacket pp = packet.resolve();
                        listener.onResult(pp.getPacket().toString(getDateTimeModule().getTimeFormatter()));
                        listener.onResult(pp.toString());
                        // only return DM24s
                        packet.left.ifPresent(p -> packets.add(p));
                    },
                            // log missing responses
                            () -> listener.onResult(TIMEOUT_MESSAGE));
            listener.onResult("");
        }
        reportObdTests(listener, packets);
        return new RequestResult<>(retryUsed, packets, Collections.emptyList());
    }

}

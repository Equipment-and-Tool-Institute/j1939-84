/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.utils.CollectionUtils.join;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939tools.bus.Packet;


/**
 * Parses the DM30 Scaled Test Results packet
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM30ScaledTestResultsPacket extends GenericPacket {

    public static final int PGN = 41984;
    private List<ScaledTestResult> testResults;

    public DM30ScaledTestResultsPacket(Packet packet) {
        super(packet);
    }

    public static DM30ScaledTestResultsPacket create(int source, int destination, ScaledTestResult... testResults) {

        int[] data = new int[0];
        for (ScaledTestResult testResult : testResults) {
            data = join(data, testResult.getData());
        }

        return new DM30ScaledTestResultsPacket(Packet.create(PGN | destination, source, data));
    }

    @Override
    public String getName() {
        return "DM30";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix());
        boolean moreThanOne = getTestResults().size() > 1;
        sb.append(moreThanOne ? "[" + NL : "");
        getTestResults().stream().sorted().forEach(testResult -> {
            sb.append(moreThanOne ? "  " : "");
            sb.append(testResult);
            sb.append(moreThanOne ? NL : "");
        });
        sb.append(moreThanOne ? "]" : "");
        return sb.toString();
    }

    @Override
    protected String getStringPrefix() {
        return getName() + " from " + getSourceAddress() + ": ";
    }

    /**
     * Returns the {@link ScaledTestResult}s
     *
     * @return a {@link List} of {@link ScaledTestResult}s
     */
    public List<ScaledTestResult> getTestResults() {
        if (testResults == null) {
            testResults = new ArrayList<>();
            int length = getPacket().getLength();
            for (int i = 0; i + 11 < length; i = i + 12) {
                testResults.add(parseTestResult(i));
            }
        }
        return testResults;
    }

    private ScaledTestResult parseTestResult(int index) {
        int[] data = getPacket().getData(index, index + 12);
        return new ScaledTestResult(data);
    }

}

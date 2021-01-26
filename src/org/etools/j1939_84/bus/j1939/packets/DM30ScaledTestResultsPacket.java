/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * Parses the DM30 Scaled Test Results packet
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM30ScaledTestResultsPacket extends GenericPacket {

    public static final int PGN = 41984;

    public static DM30ScaledTestResultsPacket create(int source, ScaledTestResult... testResults) {

        int[] data = new int[0];
        for (ScaledTestResult testResult : testResults) {
            data = join(data, testResult.getData());
        }

        return new DM30ScaledTestResultsPacket(Packet.create(PGN, source, data));
    }

    private List<ScaledTestResult> testResults;

    public DM30ScaledTestResultsPacket(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    @Override
    public String getName() {
        return "DM30";
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
            final int length = getPacket().getLength();
            for (int i = 0; i + 11 < length; i = i + 12) {
                testResults.add(parseTestResult(i));
            }
        }
        return testResults;
    }

    private ScaledTestResult parseTestResult(int index) {
        final int[] data = getPacket().getData(index, index + 12);
        return new ScaledTestResult(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix());
        boolean moreThanOne = getTestResults().size() > 1;
        sb.append(moreThanOne ? "[" + NL : "");
        for (ScaledTestResult testResult : getTestResults()) {
            sb.append(moreThanOne ? "  " : "");
            sb.append(testResult);
            sb.append(moreThanOne ? NL : "");
        }
        sb.append(moreThanOne ? "]" : "");
        return sb.toString();
    }

}

/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939tools.CommunicationsListener;

/**
 * Helper class used as a {@link CommunicationsListener} for testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class TestResultsListener implements CommunicationsListener {

    private final List<String> messages = new ArrayList<>();
    private final List<String> results = new ArrayList<>();

    @Override
    public void onResult(String result) {
        results.add(result);
    }

    public String getMessages() {
        return String.join(NL, messages);
    }

    public String getResults() {
        StringBuilder sb = new StringBuilder();
        results.forEach(t -> {
            sb.append(t).append(NL);
        });
        return sb.toString();
    }
}

/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.List;
import java.util.Objects;

public class ResultEvent implements Event {

    private final List<String> results;

    public ResultEvent(List<String> results) {
        this.results = results;
    }

    public ResultEvent(String result) {
        results = List.of(result);
    }

    public String getResult() {
        if (results.size() == 1) {
            return results.get(0);
        } else {
            StringBuilder sb = new StringBuilder();
            results.forEach(r -> sb.append(r).append(NL));
            return sb.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResultEvent that = (ResultEvent) o;
        return Objects.equals(results, that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results);
    }
}

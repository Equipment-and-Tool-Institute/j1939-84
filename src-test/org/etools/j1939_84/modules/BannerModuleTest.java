/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.BuildNumber;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for the {@link BannerModule}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BannerModuleTest {

    private BuildNumber buildNumber;
    private BannerModule instance;
    private TestResultsListener listener;

    @Before
    public void setup() {
        listener = new TestResultsListener();
        buildNumber = new BuildNumber() {
            @Override
            public String getVersionNumber() {
                return "1.2.0";
            }
        };
        DateTimeModule.setInstance(new TestDateTimeModule());
    }

    @Test
    public void testAborted() {
        instance = new BannerModule(buildNumber);
        String expected = "10:15:30.0000 J1939-84 Tool Aborted" + NL;
        instance.reportAborted(listener);
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testFooter() {
        instance = new BannerModule(buildNumber);
        String expected = "10:15:30.0000 J1939-84 Tool END OF REPORT" + NL;
        instance.reportFooter(listener);
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testHeader() {
        instance = new BannerModule(buildNumber);
        String expected = "";
        expected += "J1939-84 Tool version 1.2.0" + NL;
        instance.reportHeader(listener);
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testStopped() {
        instance = new BannerModule(buildNumber);
        String expected = "10:15:30.0000 J1939-84 Tool Stopped" + NL;
        instance.reportStopped(listener);
        assertEquals(expected, listener.getResults());
    }

}

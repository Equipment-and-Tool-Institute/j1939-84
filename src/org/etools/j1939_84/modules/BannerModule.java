/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;

import org.etools.j1939_84.BuildNumber;
import org.etools.j1939_84.controllers.ResultsListener;

/**
 * Generates the Banner for the Reports
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BannerModule extends FunctionalModule {

    /**
     * The string that indicates the report section is complete
     */
    static final String END_OF_REPORT = "END OF REPORT";

    /**
     * The name of the tool for inclusion in the report
     */
    static final String TOOL_NAME = "J1939-84 Tool";

    private final BuildNumber buildNumber;

    /**
     * Constructor
     */
    public BannerModule() {
        this(new DateTimeModule(), new BuildNumber());
    }

    /**
     * Constructor exposed to for testing
     *
     * @param dateTimeModule
     *                       the {@link DateTimeModule}
     * @param buildNumber
     *                       the {@link BuildNumber}
     */
    public BannerModule(DateTimeModule dateTimeModule, BuildNumber buildNumber) {
        super(dateTimeModule);
        this.buildNumber = buildNumber;
    }

    private String getFooter(String suffix) {
        return getTime() + " " + TOOL_NAME + " " + suffix;
    }

    public String getHeader() {
        return getTime() + " " + TOOL_NAME + " version " + buildNumber.getVersionNumber();
    }

    public String getSummaryHeader() {
        return "Summary of " + BannerModule.TOOL_NAME + " Execution" + NL;
    }

    /**
     * Writes the Aborted Footer for the report to the {@link ResultsListener}
     *
     * @param listener
     *                 the {@link ResultsListener}
     */
    public void reportAborted(ResultsListener listener) {
        listener.onResult(getFooter("Aborted"));
    }

    /**
     * Writes the Failure Footer for the report to the {@link ResultsListener}
     *
     * @param listener
     *                 the {@link ResultsListener}
     */
    public void reportFailed(ResultsListener listener) {
        listener.onResult(getFooter("Failed"));
    }

    /**
     * Writes the Footer for the report to the {@link ResultsListener}
     *
     * @param listener
     *                 the {@link ResultsListener}
     */
    public void reportFooter(ResultsListener listener) {
        listener.onResult(getFooter(END_OF_REPORT));
    }

    /**
     * Writes the Header for the report to the {@link ResultsListener}
     *
     * @param listener
     *                 the {@link ResultsListener} to give the header to
     */
    public void reportHeader(ResultsListener listener) {
        listener.onResult(getHeader());
    }

    /**
     * Writes the Stopped Footer for the report to the {@link ResultsListener}
     *
     * @param listener
     *                 the {@link ResultsListener}
     */
    public void reportStopped(ResultsListener listener) {
        listener.onResult(getFooter("Stopped"));
    }

}

/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The super class for the controllers that collect information from the vehicle
 * and generates the report
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public abstract class Controller {

    /**
     * The {@link ResultsListener} that combines other listeners for easier
     * reporting
     *
     */
    private static class CompositeResultsListener implements ResultsListener {

        private final ResultsListener[] listeners;

        private CompositeResultsListener(ResultsListener... listeners) {
            this.listeners = listeners;
        }

        @Override
        public void onComplete(boolean success) {
            Arrays.stream(listeners).forEach(l -> l.onComplete(success));
        }

        @Override
        public void onMessage(String message, String title, MessageType type) {
            Arrays.stream(listeners).forEach(l -> l.onMessage(message, title, type));
        }

        @Override
        public void onProgress(int currentStep, int totalSteps, String message) {
            Arrays.stream(listeners).forEach(l -> l.onProgress(currentStep, totalSteps, message));
        }

        @Override
        public void onProgress(String message) {
            Arrays.stream(listeners).forEach(l -> l.onProgress(message));
        }

        @Override
        public void onResult(List<String> results) {
            Arrays.stream(listeners).forEach(l -> l.onResult(results));
        }

        @Override
        public void onResult(String result) {
            Arrays.stream(listeners).forEach(l -> l.onResult(result));
        }

        @Override
        public void onUrgentMessage(String message, String title, MessageType type) {
            Arrays.stream(listeners).forEach(l -> l.onUrgentMessage(message, title, type));
        }

        @Override
        public void onVehicleInformationNeeded(VehicleInformationListener listener) {
            Arrays.stream(listeners).forEach(l -> l.onVehicleInformationNeeded(listener));
        }
    }

    /**
     * The possible ending for an execution cycle
     */
    protected enum Ending {
        ABORTED("Aborted"), COMPLETED("Completed"), FAILED("Failed"), STOPPED("Stopped");

        private final String string;

        private Ending(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    /**
     * The Endings that indicate the procedure should be halted
     */
    private static final List<Ending> INTERUPPTABLE_ENDINGS = Arrays
            .asList(new Ending[] { Ending.STOPPED, Ending.ABORTED, Ending.FAILED });

    /**
     * The {@link BannerModule} used to generate the headers and footers
     */
    private final BannerModule bannerModule;

    /**
     * The {@link CompositeResultsListener} use to combine listeners into one
     */
    private CompositeResultsListener compositeListener;

    /**
     * The current step in the controller execution
     */
    private int currentStep;

    /**
     * The {@link DateTimeModule} used to generate the date/time stamp for the
     * report
     */
    private final DateTimeModule dateTimeModule;

    /**
     * How the execution ended
     */
    private Ending ending;

    /**
     * The {@link EngineSpeedModule} used to determine if the engine is
     * communicating
     */
    private final EngineSpeedModule engineSpeedModule;

    /**
     * The {@link ScheduledExecutorService} used to run the task
     */
    private final ScheduledExecutorService executor;

    /**
     * The {@link J1939} use for vehicle communications
     */
    private J1939 j1939;

    /**
     * The maximum number of steps in the controller execution
     */
    private int maxSteps;

    /**
     * The {@link ReportFileModule} used to read and write the report
     */
    private ReportFileModule reportFileModule;

    /**
     * The {@link VehicleInformationModule} used to read general information
     * from the vehicle
     */
    private final VehicleInformationModule vehicleInformationModule;

    /**
     * Constructor
     *
     * @param executor
     *                                 the {@link ScheduledExecutorService} used to
     *                                 run the process.
     * @param engineSpeedModule
     *                                 the {@link EngineSpeedModule} that will
     *                                 determine if the
     *                                 engine is communicating
     * @param bannerModule
     *                                 the {@link BannerModule} used to generate the
     *                                 headers and
     *                                 footers for the report
     * @param dateTimeModule
     *                                 the {@link DateTimeModule} used to generate
     *                                 the timestamps for
     *                                 the report
     * @param vehicleInformationModule
     *                                 the {@link VehicleInformationModule} that
     *                                 will gather general
     *                                 information from the vehicle for the report
     */
    protected Controller(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule,
            VehicleInformationModule vehicleInformationModule) {
        this.executor = executor;
        this.engineSpeedModule = engineSpeedModule;
        this.bannerModule = bannerModule;
        this.dateTimeModule = dateTimeModule;
        this.vehicleInformationModule = vehicleInformationModule;
    }

    /**
     * Adds a blank line to the report
     */
    protected void addBlankLineToReport() {
        getListener().onResult("");
    }

    protected void addFailure(String message) {
        getListener().onResult("Action Failed: " + message);
    }

    /**
     * Adds a warning to the report
     *
     * @param message the warning to add to the report
     */
    protected void addWarning(String warning) {
        getListener().onResult("WARN: " + warning);
    }

    /**
     * Checks the Ending value and will throw an {@link InterruptedException} if
     * the value has been set to Stopped or Aborted
     *
     * @throws InterruptedException
     *                              if the ending has been set
     */
    private void checkEnding() throws InterruptedException {
        if (INTERUPPTABLE_ENDINGS.contains(getEnding())) {
            throw new InterruptedException(getEnding().toString());
        }
    }

    /**
     * Checks the Engine Speed
     *
     * @throws InterruptedException
     *                              if the controller was stopped
     */
    private void checkEngineSpeed() throws InterruptedException {
        incrementProgress("Reading Engine Speed");
        if (!getEngineSpeedModule().isEngineCommunicating()) {
            getListener().onMessage(
                    "The engine is not communicating.  Please check the adapter connection with the vehicle and/or turn the key on/start the vehicle.",
                    "Engine Not Communicating",
                    MessageType.WARNING);
            updateProgress("Engine Not Communicating.  Please start vehicle or push Stop");
            while (!getEngineSpeedModule().isEngineCommunicating()) {
                Thread.sleep(100);
                checkEnding();
            }
        }
    }

    /**
     * Executes the Controller. The results are passed to the
     * {@link ResultsListener}
     *
     * @param listener
     *                         the {@link ResultsListener} that will be given the
     *                         results
     * @param j1939
     *                         the {@link J1939} to use for communications
     * @param reportFileModule
     *                         the {@link ReportFileModule} that will be used to
     *                         read and
     *                         generate the report
     */
    public void execute(ResultsListener listener, J1939 j1939, ReportFileModule reportFileModule) {
        setupRun(listener, j1939, reportFileModule);
        getExecutor().execute(getRunnable());
    }

    /**
     * Called when the Controller is finished, either through normal operations
     * or because of an abort
     */
    protected void finished() {
        addBlankLineToReport();

        if (ending == null) {
            ending = Ending.ABORTED;
        }

        switch (ending) {
            case ABORTED:
                getBannerModule().reportAborted(getListener());
                break;
            case COMPLETED:
                getBannerModule().reportFooter(getListener());
                break;
            case STOPPED:
                getBannerModule().reportStopped(getListener());
                break;
            case FAILED:
                getBannerModule().reportFailed(getListener());
                break;
        }

        addBlankLineToReport();

        String message = getEnding().toString();
        getListener().onProgress(maxSteps, maxSteps, message);

        getListener().onComplete(getEnding() == Ending.COMPLETED);
    }

    /**
     * Returns the {@link BannerModule}
     *
     * @return {@link BannerModule}
     */
    protected BannerModule getBannerModule() {
        return bannerModule;
    }

    /**
     * Returns the current date/time stamp for the report
     *
     * @return {@link String}
     */
    protected String getDateTime() {
        return getDateTimeModule().getDateTime();
    }

    /**
     * Returns the {@link DateTimeModule}
     *
     * @return {@link DateTimeModule}
     */
    protected DateTimeModule getDateTimeModule() {
        return dateTimeModule;
    }

    public abstract String getDisplayName();

    /**
     * @return the ending
     */
    protected Ending getEnding() {
        return ending;
    }

    /**
     * Returns the {@link EngineSpeedModule}
     *
     * @return {@link EngineSpeedModule}
     */
    protected EngineSpeedModule getEngineSpeedModule() {
        return engineSpeedModule;
    }

    /**
     * @return the executor
     */
    private ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Returns the {@link J1939}
     *
     * @return {@link J1939}
     */
    protected J1939 getJ1939() {
        return j1939;
    }

    /**
     * Returns the {@link ResultsListener}
     *
     * @return {@link ResultsListener}
     */
    protected ResultsListener getListener() {
        return compositeListener;
    }

    /**
     * Returns the {@link Logger} used to write to the log file
     *
     * @return {@link Logger}
     */
    protected Logger getLogger() {
        return J1939_84.getLogger();
    }

    /**
     * Returns the {@link ReportFileModule}
     *
     * @return {@link ReportFileModule}
     */
    protected ReportFileModule getReportFileModule() {
        return reportFileModule;
    }

    /**
     * Creates a {@link Runnable} that is used to run the controller
     *
     * @return {@link Runnable}
     */
    private Runnable getRunnable() {
        return () -> {
            try {
                setupProgress(getTotalSteps());

                checkEngineSpeed();

                // Call to the specific controller
                run();

                setEnding(Ending.COMPLETED);
            } catch (Throwable e) {
                getLogger().log(Level.SEVERE, "Error", e);
                if (!(e instanceof InterruptedException)) {
                    String message = e.getMessage();
                    if (message == null) {
                        message = "An Error Occurred";
                    }
                    getListener().onMessage(message, "Error", MessageType.ERROR);
                }
            } finally {
                finished();
            }
        };
    }

    /**
     * Returns the total number of steps that be sent back to the listener
     *
     * @return int
     */
    protected abstract int getTotalSteps();

    /**
     * Returns the {@link VehicleInformationModule}
     *
     * @return {@link VehicleInformationModule}
     */
    protected VehicleInformationModule getVehicleInformationModule() {
        return vehicleInformationModule;
    }

    /**
     * Increments the overall progress and sends the message to the listener
     *
     * @param message
     *                the {@link String} message to display
     * @throws InterruptedException
     *                              if the operation has been Stopped
     */
    protected void incrementProgress(String message) throws InterruptedException {
        getListener().onProgress(++currentStep, maxSteps, message);
        checkEnding();
    }

    /**
     * Returns true if the controller is still running
     *
     * @return boolean
     */
    public boolean isActive() {
        return ending == null;
    }

    /**
     * Performs the logic of this controller. This is to be implemented by
     * subclasses. Callers should use execute() instead.
     *
     * @throws Throwable
     *                   if there is a problem
     */
    protected abstract void run() throws Throwable;

    public void run(ResultsListener listener, J1939 j1939, ReportFileModule reportFileModule) {
        setupRun(listener, j1939, reportFileModule);
        getRunnable().run();
    }

    /**
     * @param ending
     *               the ending to set
     * @throws InterruptedException
     *                              if the ending was set to ABORTED or STOPPED
     */
    protected void setEnding(Ending ending) throws InterruptedException {
        this.ending = ending;
        checkEnding();
    }

    /**
     * Sets the {@link J1939} to be used for communications
     *
     * @param j1939
     *              the {@link J1939} to set
     */
    private void setJ1939(J1939 j1939) {
        this.j1939 = j1939;
        getVehicleInformationModule().setJ1939(this.j1939);
        getEngineSpeedModule().setJ1939(this.j1939);
    }

    /**
     * @param reportFileModule the reportFileModule to set
     */
    public void setReportFileModule(ReportFileModule reportFileModule) {
        this.reportFileModule = reportFileModule;
    }

    /**
     * Resets the progress to zero, clears the message displayed to the user and
     * sets the maximum number of steps
     *
     * @param maxSteps
     *                 the maximum number of steps in the operation
     */
    private void setupProgress(int maxSteps) {
        currentStep = 0;
        this.maxSteps = maxSteps;
        getListener().onProgress(currentStep, maxSteps, "");
    }

    public void setupRun(ResultsListener listener, J1939 j1939, ReportFileModule reportFileModule) {
        setJ1939(j1939);
        setReportFileModule(reportFileModule);
        ending = null;
        compositeListener = new CompositeResultsListener(listener, reportFileModule);
    }

    /**
     * Interrupts and ends the execution of the controller
     */
    public void stop() {
        ending = Ending.STOPPED;
        getJ1939().interrupt();
    }

    /**
     * Sends the message to the listener without incrementing the overall
     * progress
     *
     * @param message
     *                the message to send
     * @throws InterruptedException
     *                              if the operation has been Stopped
     */
    protected void updateProgress(String message) throws InterruptedException {
        getListener().onProgress(currentStep, maxSteps, message);
        checkEnding();
    }
}

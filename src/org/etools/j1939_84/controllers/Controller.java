/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.model.VehicleInformation;
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
        public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
            Arrays.stream(listeners).forEach(l -> l.addOutcome(partNumber, stepNumber, outcome, message));
        }

        @Override
        public void beginPart(PartResult partResult) {
            Arrays.stream(listeners).forEach(l -> l.beginPart(partResult));

        }

        @Override
        public void beginStep(StepResult stepResult) {
            Arrays.stream(listeners).forEach(l -> l.beginStep(stepResult));

        }

        @Override
        public void endPart(PartResult partResult) {
            Arrays.stream(listeners).forEach(l -> l.endPart(partResult));
        }

        @Override
        public void endStep(StepResult stepResult) {
            Arrays.stream(listeners).forEach(l -> l.endStep(stepResult));
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

        @Override
        public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
            Arrays.stream(listeners).forEach(l -> l.onVehicleInformationReceived(vehicleInformation));
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

    private final Map<Integer, PartResult> partResultsMap = new HashMap<>();

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

    protected void addFailure(int partNumber, int stepNumber, String message) {
        getListener().addOutcome(partNumber, stepNumber, Outcome.FAIL, message);
        getListener().onResult("FAIL: " + message);
    }

    protected void addPass(int partNumber, int stepNumber) {

    }

    /**
     * Adds a warning to the report
     *
     * @param message the warning to add to the report
     */
    protected void addWarning(int partNumber, int stepNumber, String message) {
        getListener().addOutcome(partNumber, stepNumber, Outcome.WARN, message);
        getListener().onResult("WARN: " + message);
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

    protected void executeTests(int partNumber, int totalSteps) throws InterruptedException {
        PartResult partResult = getPartResult(partNumber);
        getListener().beginPart(partResult);
        getListener().onResult("Begin " + partResult);

        for (int i = 1; i <= totalSteps; i++) {
            StepResult stepResult = partResult.getStepResult(i);

            getListener().beginStep(stepResult);
            getListener().onResult(NL);
            getListener().onResult("Start " + stepResult);

            incrementProgress(stepResult.toString());
            getListener().onResult("Do Testing;\nWait for Responses;\nWrite Messages, etc");
            // Thread.sleep(100);

            getListener().endStep(stepResult);
            getListener().onResult("End " + stepResult);
        }
        getListener().endPart(partResult);
        getListener().onResult("End " + partResult);
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
     * Returns the {@link DateTimeModule}
     *
     * @return {@link DateTimeModule}
     */
    private DateTimeModule getDateTimeModule() {
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

    protected PartResult getPartResult(int partNumber) {
        PartResult result = partResultsMap.get(partNumber);
        if (result == null) {
            result = new PartResult(partNumber, Lookup.getPartName(partNumber));
            partResultsMap.put(partNumber, result);
        }
        return result;
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

                // checkEngineSpeed();

                // Call to the specific controller
                run();

            } catch (Throwable e) {
                getLogger().log(Level.SEVERE, "Error", e);
                if (!(e instanceof InterruptedException)) {
                    String message = e.getMessage();
                    if (message == null) {
                        message = "An Error Occurred";
                    }
                    getListener().onMessage(message, "Error", MessageType.ERROR);
                }
            }
        };
    }

    protected StepResult getStepResult(int partNumber, int stepNumber) {
        return getPartResult(partNumber).getStepResult(stepNumber);
    }

    /**
     * Returns the current date/time stamp for the report
     *
     * @return {@link String}
     */
    protected String getTime() {
        return getDateTimeModule().getTime();
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

    public void run(ResultsListener listener, J1939 j1939) {
        setupRun(listener, j1939, null);
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
        if (reportFileModule != null) {
            setReportFileModule(reportFileModule);
            compositeListener = new CompositeResultsListener(listener, reportFileModule);
        } else {
            compositeListener = new CompositeResultsListener(listener);
        }
        ending = null;
    }

    /**
     * Interrupts and ends the execution of the controller
     */
    public void stop() {
        ending = Ending.STOPPED;
        j1939.close();
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

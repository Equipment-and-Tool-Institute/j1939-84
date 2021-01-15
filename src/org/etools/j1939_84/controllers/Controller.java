/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.j1939.J1939;
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
 */
public abstract class Controller {

    /**
     * The {@link ResultsListener} that combines other listeners for easier
     * reporting
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
        public void onUrgentMessage(String message, String title, MessageType type, QuestionListener listener) {
            Arrays.stream(listeners).forEach(l -> l.onUrgentMessage(message, title, type, listener));
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

        Ending(String string) {
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
            .asList(Ending.STOPPED, Ending.ABORTED, Ending.FAILED);

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
    private static int currentStep;

    /**
     * How the execution ended
     */
    private static Ending ending;

    /**
     * The {@link EngineSpeedModule} used to determine if the engine is
     * communicating
     */
    private final EngineSpeedModule engineSpeedModule;

    /**
     * The {@link Executor} used to run the task
     */
    private final Executor executor;

    /**
     * The {@link J1939} use for vehicle communications
     */
    private J1939 j1939;

    /**
     * The maximum number of steps in the controller execution
     */
    private static int maxSteps;

    private final PartResultRepository partResultRepository;

    /**
     * The {@link VehicleInformationModule} used to read general information
     * from the vehicle
     */
    private final VehicleInformationModule vehicleInformationModule;

    /**
     * The {@link DateTimeModule} used to read general information
     * from the vehicle
     */
    private final DateTimeModule dateTimeModule;

    /**
     * Constructor
     *
     * @param executor
     *         the {@link Executor} used to run the process.
     * @param engineSpeedModule
     *         the {@link EngineSpeedModule} that will determine if the
     *         engine is communicating
     * @param bannerModule
     *         the {@link BannerModule} used to generate the headers and
     *         footers for the report
     * @param vehicleInformationModule
     *         the {@link VehicleInformationModule} that will gather general
     *         information from the vehicle for the report
     * @param dateTimeModule
     *         the {@link DateTimeModule} that will for time tracking
     */
    protected Controller(Executor executor, EngineSpeedModule engineSpeedModule,
                         BannerModule bannerModule,
                         VehicleInformationModule vehicleInformationModule,
                         DateTimeModule dateTimeModule) {
        this(executor, engineSpeedModule, bannerModule, vehicleInformationModule, dateTimeModule, PartResultRepository.getInstance());
    }

    protected Controller(Executor executor, EngineSpeedModule engineSpeedModule,
                         BannerModule bannerModule,
                         VehicleInformationModule vehicleInformationModule,
                         DateTimeModule dateTimeModule,
                         PartResultRepository partResultRepository) {
        this.executor = executor;
        this.engineSpeedModule = engineSpeedModule;
        this.bannerModule = bannerModule;
        this.vehicleInformationModule = vehicleInformationModule;
        this.dateTimeModule = dateTimeModule;
        this.partResultRepository = partResultRepository;
    }

    /**
     * Adds a blank line to the report
     */
    protected void addBlankLineToReport() {
        getListener().onResult("");
    }

    /**
     * Adds a failure to the report
     *
     * @param partNumber
     *         the part number to add to the report
     * @param stepNumber
     *         the step number where the warning originated
     * @param message
     *         the warning to add to the report
     */
    protected void addFailure(int partNumber, int stepNumber, String message) {
        getListener().addOutcome(partNumber, stepNumber, Outcome.FAIL, message);
        getListener().onResult("FAIL: " + message);
    }

    /**
     * Adds a warning to the report
     *
     * @param partNumber
     *         the part number to add to the report
     * @param stepNumber
     *         the step number where the warning originated
     * @param message
     *         the warning to add to the report
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
     *         if the ending has been set
     */
    private void checkEnding() throws InterruptedException {
        if (INTERUPPTABLE_ENDINGS.contains(getEnding())) {
            throw new InterruptedException(getEnding().toString());
        }
    }

    /**
     * Executes the Controller. The results are passed to the
     * {@link ResultsListener}
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the results
     * @param j1939
     *         the {@link J1939} to use for communications
     * @param reportFileModule
     *         the {@link ReportFileModule} that will be used to read and
     *         generate the report
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
    private Executor getExecutor() {
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
        return partResultRepository.getPartResult(partNumber);
    }

    /**
     * Creates a {@link Runnable} that is used to run the controller
     *
     * @return {@link Runnable}
     */
    private Runnable getRunnable() {
        return () -> {
            try {
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

    /**
     * Returns the current date/time stamp for the report
     *
     * @return {@link String}
     */
    protected String getTime() {
        return getDateTimeModule().getTime();
    }

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
     *         the {@link String} message to display
     * @throws InterruptedException
     *         if the operation has been Stopped
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
     *         if there is a problem
     */
    protected abstract void run() throws Throwable;

    public void run(ResultsListener listener, J1939 j1939) {
        setupRun(listener, j1939, null);
        getRunnable().run();
    }

    /**
     * @param ending
     *         the ending to set
     * @throws InterruptedException
     *         if the ending was set to ABORTED or STOPPED
     */
    protected void setEnding(Ending ending) throws InterruptedException {
        this.ending = ending;
        checkEnding();
    }

    /**
     * Sets the {@link J1939} to be used for communications
     *
     * @param j1939
     *         the {@link J1939} to set
     */
    private void setJ1939(J1939 j1939) {
        this.j1939 = j1939;
        getVehicleInformationModule().setJ1939(this.j1939);
        getEngineSpeedModule().setJ1939(this.j1939);
    }

    /**
     * Resets the progress to zero, clears the message displayed to the user and
     * sets the maximum number of steps
     *
     * @param maxSteps
     *         the maximum number of steps in the operation
     */
    protected void setupProgress(int maxSteps) {
        Controller.currentStep = 0;
        Controller.maxSteps = maxSteps;
        getListener().onProgress(currentStep, maxSteps, "");
    }

    public void setupRun(ResultsListener listener, J1939 j1939, ReportFileModule reportFileModule) {
        setJ1939(j1939);
        if (reportFileModule != null) {
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
    }

    /**
     * Sends the message to the listener without incrementing the overall
     * progress
     *
     * @param message
     *         the message to send
     * @throws InterruptedException
     *         if the operation has been Stopped
     */
    protected void updateProgress(String message) throws InterruptedException {
        getListener().onProgress(currentStep, maxSteps, message);
        checkEnding();
    }
}

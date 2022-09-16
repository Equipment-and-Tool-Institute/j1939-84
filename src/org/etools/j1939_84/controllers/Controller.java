/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.ABORT;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The super class for the controllers that collect information from the vehicle
 * and generates the report
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public abstract class Controller {

    private static final List<Ending> INTERRUPTIBLE_ENDINGS = List.of(Ending.STOPPED,
                                                                      Ending.ABORTED,
                                                                      Ending.FAILED,
                                                                      Ending.COMPLETED);
    private static int currentStep;
    private static Ending ending;
    private static int maxSteps;
    private final BannerModule bannerModule;
    private final EngineSpeedModule engineSpeedModule;
    private final Executor executor;
    private final PartResultRepository partResultRepository;
    private final VehicleInformationModule vehicleInformationModule;
    private final DateTimeModule dateTimeModule;
    private final CommunicationsModule communicationsModule;
    private final DataRepository dataRepository;
    private CompositeResultsListener compositeListener;
    private J1939 j1939;

    protected Controller(Executor executor,
                         BannerModule bannerModule,
                         DateTimeModule dateTimeModule,
                         DataRepository dataRepository,
                         EngineSpeedModule engineSpeedModule,
                         VehicleInformationModule vehicleInformationModule,
                         CommunicationsModule communicationsModule) {
        this(executor,
             bannerModule,
             dateTimeModule,
             dataRepository,
             PartResultRepository.getInstance(),
             engineSpeedModule,
             vehicleInformationModule,
             communicationsModule);
    }

    protected Controller(Executor executor,
                         BannerModule bannerModule,
                         DateTimeModule dateTimeModule,
                         DataRepository dataRepository,
                         PartResultRepository partResultRepository,
                         EngineSpeedModule engineSpeedModule,
                         VehicleInformationModule vehicleInformationModule,
                         CommunicationsModule communicationsModule) {
        this.executor = executor;
        this.engineSpeedModule = engineSpeedModule;
        this.bannerModule = bannerModule;
        this.vehicleInformationModule = vehicleInformationModule;
        this.dateTimeModule = dateTimeModule;
        this.communicationsModule = communicationsModule;
        this.partResultRepository = partResultRepository;
        this.dataRepository = dataRepository;
    }

    /**
     * Checks the Ending value and will throw an {@link InterruptedException} if
     * the value has been set to Stopped or Aborted
     *
     * @throws InterruptedException
     *                                  if the ending has been set
     */
    public static void checkEnding() throws InterruptedException {
        if (getEnding() != null && INTERRUPTIBLE_ENDINGS.contains(getEnding())) {
            throw new InterruptedException(getEnding().toString());
        }
    }

    protected List<? extends GenericPacket> read(int pg, int timeout, TimeUnit timeUnit) {
        return getCommunicationsModule().read(pg, timeout, timeUnit, getListener());
    }

    protected <T extends GenericPacket> List<T> read(Class<T> clazz, int timeout, TimeUnit timeUnit) {
        return getCommunicationsModule().read(clazz, timeout, timeUnit, getListener());
    }

    protected <T extends GenericPacket> RequestResult<T> request(int pg) {
        return getCommunicationsModule().request(pg, getListener());
    }

    protected <T extends GenericPacket> BusResult<T> request(int pg, int address) {
        return getCommunicationsModule().request(pg, address, getListener());
    }

    /**
     * @return the ending
     */
    protected static Ending getEnding() {
        return ending;
    }

    /**
     * @param  ending
     *                                  the ending to set
     * @throws InterruptedException
     *                                  if the ending was set to ABORTED or STOPPED
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Artifact of bad design")
    protected void setEnding(Ending ending) throws InterruptedException {
        Controller.ending = ending;
        checkEnding();
    }

    /**
     * Adds a blank line to the report
     */
    protected void addBlankLineToReport() {
        getListener().onResult("");
    }

    /**
     * Executes the Controller. The results are passed to the
     * {@link ResultsListener}
     *
     * @param listener
     *                             the {@link ResultsListener} that will be given the results
     * @param j1939
     *                             the {@link J1939} to use for communications
     * @param reportFileModule
     *                             the {@link ReportFileModule} that will be used to read and
     *                             generate the report
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
     * Sets the {@link J1939} to be used for communications
     *
     * @param j1939
     *                  the {@link J1939} to set
     */
    private void setJ1939(J1939 j1939) {
        this.j1939 = j1939;
        getVehicleInformationModule().setJ1939(this.j1939);
        getEngineSpeedModule().setJ1939(this.j1939);
        getCommunicationsModule().setJ1939(this.j1939);
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
    protected static Logger getLogger() {
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
                if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                    if (this instanceof StepController) {
                        int partNumber = ((StepController) this).getPartNumber();
                        int stepNumber = ((StepController) this).getStepNumber();
                        if (getOutcome(partNumber, stepNumber) != ABORT) {
                            String message = "User cancelled testing at Part " + partNumber + " Step " + stepNumber;
                            getListener().addOutcome(partNumber, stepNumber, ABORT, message);
                        }
                    }
                    return;
                }

                String message = e.getMessage();
                if (message == null) {
                    message = "An Error Occurred";
                }
                getListener().onMessage(message, "Error", MessageType.ERROR);
            }
        };
    }

    private Outcome getOutcome(int partNumber, int stepNumber) {
        return getPartResult(partNumber).getStepResult(stepNumber).getOutcome();
    }

    /**
     * Returns the current date/time stamp for the report
     *
     * @return {@link String}
     */
    protected String getTime() {
        return getDateTimeModule().getTime();
    }

    protected VehicleInformationModule getVehicleInformationModule() {
        return vehicleInformationModule;
    }

    protected CommunicationsModule getCommunicationsModule() {
        return communicationsModule;
    }

    protected DataRepository getDataRepository() {
        return dataRepository;
    }

    /**
     * Increments the overall progress and sends the message to the listener
     *
     * @param  message
     *                                  the {@link String} message to display
     * @throws InterruptedException
     *                                  if the operation has been Stopped
     */
    protected void incrementProgress(String message) throws InterruptedException {
        checkEnding();
        getListener().onProgress(++currentStep, maxSteps, message);
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
     *                       if there is a problem
     */
    protected abstract void run() throws Throwable;

    public void run(ResultsListener listener, J1939 j1939) {
        setupRun(listener, j1939, null);
        getRunnable().run();
    }

    /**
     * Resets the progress to zero, clears the message displayed to the user and
     * sets the maximum number of steps
     *
     * @param maxSteps
     *                     the maximum number of steps in the operation
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Artifact of bad design")
    protected void setupProgress(int maxSteps) {
        Controller.currentStep = 0;
        Controller.maxSteps = maxSteps;
        getListener().onProgress(currentStep, maxSteps, "");
    }

    private void setupRun(ResultsListener listener, J1939 j1939, ReportFileModule reportFileModule) {
        setJ1939(j1939);
        if (listener instanceof CompositeResultsListener) {
            compositeListener = (CompositeResultsListener) listener;
        } else {
            compositeListener = new CompositeResultsListener(listener, reportFileModule, partResultRepository);
        }
        ending = null;
    }

    /**
     * Interrupts and ends the execution of the controller
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Artifact of bad design")
    public void stop() {
        ending = Ending.STOPPED;
    }

    /**
     * Sends the message to the listener without incrementing the overall
     * progress
     *
     * @param  message
     *                                  the message to send
     * @throws InterruptedException
     *                                  if the operation has been Stopped
     */
    protected void updateProgress(String message) throws InterruptedException {
        checkEnding();
        getListener().onProgress(currentStep, maxSteps, message);
    }

    public enum Ending {
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
     * The {@link ResultsListener} that combines other listeners for easier reporting
     */
    private static class CompositeResultsListener implements ResultsListener {

        private final ResultsListener[] listeners;

        private CompositeResultsListener(ResultsListener... listeners) {
            this.listeners = listeners;
        }

        @Override
        public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
            Arrays.stream(listeners).forEach(l -> {
                l.addOutcome(partNumber, stepNumber, outcome, message);
            });
        }

        @Override
        public void onComplete(boolean success) {
            Arrays.stream(listeners).forEach(l -> {
                l.onComplete(success);
            });
        }

        @Override
        public void onMessage(String message, String title, MessageType type) {
            Arrays.stream(listeners).forEach(l -> {
                l.onMessage(message, title, type);
            });
        }

        @Override
        public void onProgress(int currentStep, int totalSteps, String message) {
            Arrays.stream(listeners).forEach(l -> {
                l.onProgress(currentStep, totalSteps, message);
            });
        }

        @Override
        public void onProgress(String message) {
            Arrays.stream(listeners).forEach(l -> {
                l.onProgress(message);
            });
        }

        @Override
        public void onResult(List<String> results) {
            Arrays.stream(listeners).forEach(l -> {
                l.onResult(results);
            });
        }

        @Override
        public void onResult(String result) {
            Arrays.stream(listeners).forEach(l -> {
                l.onResult(result);
            });
        }

        @Override
        public void onUrgentMessage(String message, String title, MessageType type) {
            Arrays.stream(listeners).forEach(l -> {
                l.onUrgentMessage(message, title, type);
            });
        }

        @Override
        public void onUrgentMessage(String message, String title, MessageType type, QuestionListener listener) {
            Arrays.stream(listeners).forEach(l -> {
                l.onUrgentMessage(message, title, type, listener);
            });
        }

        @Override
        public void onVehicleInformationNeeded(VehicleInformationListener listener) {
            Arrays.stream(listeners).forEach(l -> {
                l.onVehicleInformationNeeded(listener);
            });
        }

        @Override
        public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
            Arrays.stream(listeners).forEach(l -> {
                l.onVehicleInformationReceived(vehicleInformation);
            });
        }
    }

}

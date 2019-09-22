/**
 * Copyright 2017 Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static org.etools.j1939_84.J1939_84.NL;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Adapter;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.RP1210;
import org.etools.j1939_84.bus.RP1210Bus;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.OverallController;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.ui.help.HelpView;

/**
 * The Class that controls the behavior of the {@link UserInterfaceView}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class UserInterfacePresenter implements UserInterfaceContract.Presenter {

    /**
     * The default extension for report files created by this application
     */
    static final String FILE_SUFFIX = "j1939-84";

    /**
     * The possible {@link Adapter} that can be used for communications with the
     * vehicle
     */
    private List<Adapter> adapters;

    private Bus bus;

    private final Executor executor;

    private final HelpView helpView;

    private final OverallController overallController;

    /**
     * The {@link File} where the report is stored
     */
    private File reportFile;

    private final ReportFileModule reportFileModule;

    private final RP1210 rp1210;

    /**
     * The Adapter being used to communicate with the vehicle
     */
    private Adapter selectedAdapter;

    private final VehicleInformationModule vehicleInformationModule;

    /**
     * The {@link IUserInterfaceView} that is being controlled
     */
    private final UserInterfaceContract.View view;

    private String vin;

    /**
     * Default Constructor
     *
     * @param view
     *             The {@link UserInterfaceView} to control
     */
    public UserInterfacePresenter(UserInterfaceContract.View view) {
        this(view, new VehicleInformationModule(), new RP1210(), new ReportFileModule(), Runtime.getRuntime(),
                Executors.newSingleThreadExecutor(), new HelpView(), new OverallController());
    }

    /**
     * Constructor used for testing
     *
     * @param view
     *                                 The {@link UserInterfaceView} to control
     * @param vehicleInformationModule
     *                                 the {@link VehicleInformationModule}
     * @param rp1210
     *                                 the {@link RP1210}
     * @param reportFileModule
     *                                 the {@link ReportFileModule}
     * @param runtime
     *                                 the {@link Runtime}
     * @param executor
     *                                 the {@link Executor} used to execute
     *                                 {@link Thread} s
     * @param helpView
     *                                 the {@link HelpView} that will display
     *                                 help for the application
     *
     * @param overallController        the {@link OverallController} which will
     *                                 run all the other parts
     */
    public UserInterfacePresenter(UserInterfaceContract.View view, VehicleInformationModule vehicleInformationModule,
            RP1210 rp1210, ReportFileModule reportFileModule, Runtime runtime, Executor executor, HelpView helpView,
            OverallController overallController) {
        this.view = view;
        this.vehicleInformationModule = vehicleInformationModule;
        this.rp1210 = rp1210;
        this.reportFileModule = reportFileModule;
        this.executor = executor;
        this.helpView = helpView;
        this.overallController = overallController;
        runtime.addShutdownHook(new Thread(() -> reportFileModule.onProgramExit(), "Shutdown Hook Thread"));
    }

    private void checkSetupComplete() {
        getView().setAdapterComboBoxEnabled(true);
        getView().setSelectFileButtonEnabled(true);
        if (getSelectedAdapter() == null) {
            getView().setProgressBarText("Select Vehicle Adapter");
            getView().setSelectFileButtonEnabled(false);
        } else if (getReportFile() == null) {
            getView().setProgressBarText("Select Report File");
        } else {
            getView().setProgressBarText("Push Read Vehicle Info Button");
            getView().setReadVehicleInfoButtonEnabled(true);
        }
    }

    @Override
    public void disconnect() {
        if (bus != null && bus instanceof RP1210Bus) {
            try {
                ((RP1210Bus) bus).stop();
            } catch (BusException e) {
                getLogger().log(Level.SEVERE, "Unable to disconnect from adapter", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.etools.j1939_84.ui.IUserInterfaceController#getAdapters()
     */
    @Override
    public List<Adapter> getAdapters() {
        if (adapters == null) {
            adapters = new ArrayList<>();
            try {
                adapters.addAll(rp1210.getAdapters());
            } catch (Exception e) {
                getView().displayDialog("The List of Communication Adapters could not be loaded.",
                        "Failure",
                        JOptionPane.ERROR_MESSAGE,
                        false);
            }
        }
        return adapters;
    }

    private VehicleInformationModule getComparisonModule() {
        return vehicleInformationModule;
    }

    private Logger getLogger() {
        return J1939_84.getLogger();
    }

    @Override
    public J1939 getNewJ1939() {
        return new J1939(bus);
    }

    /**
     * Return the Report File
     *
     * @return the reportFile
     */
    File getReportFile() {
        return reportFile;
    }

    /**
     * Returns the {@link ReportFileModule}
     *
     * @return the {@link ReportFileModule}
     */
    @Override
    public ReportFileModule getReportFileModule() {
        return reportFileModule;
    }

    private ResultsListener getResultsListener() {
        return new ResultsListener() {
            @Override
            public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beginPart(PartResult partResult) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beginStep(StepResult stepResult) {
                // TODO Auto-generated method stub

            }

            @Override
            public void endPart(PartResult partResult) {
                // TODO Auto-generated method stub

            }

            @Override
            public void endStep(StepResult stepResult) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onComplete(boolean success) {
                getView().setStopButtonEnabled(false);
            }

            @Override
            public void onMessage(String message, String title, MessageType type) {
                getView().displayDialog(message, title, type.getValue(), false);
            }

            @Override
            public void onProgress(int currentStep, int totalSteps, String message) {
                getView().setProgressBarValue(0, totalSteps, currentStep);
                getView().setProgressBarText(message);
            }

            @Override
            public void onProgress(String message) {
                getView().setProgressBarText(message);
            }

            @Override
            public void onResult(List<String> results) {
                for (String result : results) {
                    getView().appendResults(result + NL);
                }
            }

            @Override
            public void onResult(String result) {
                getView().appendResults(result + NL);
            }

            @Override
            public void onUrgentMessage(String message, String title, MessageType type) {
                getView().displayDialog(message, title, type.getValue(), true);
            }

            @Override
            public void onVehicleInformationNeeded(VehicleInformationListener listener) {
                getView().displayForm(listener, getNewJ1939());
            }

            @Override
            public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
                // TODO Auto-generated method stub

            }

        };
    }

    /**
     * Returns the selected Adapter
     *
     * @return the selectedAdapter
     */
    Adapter getSelectedAdapter() {
        return selectedAdapter;
    }

    private UserInterfaceContract.View getView() {
        return view;
    }

    @Override
    public String getVin() {
        return vin;
    }

    @Override
    public void onAdapterComboBoxItemSelected(String selectedAdapterName) {
        // Connecting to the adapter can take "a while"
        executor.execute(() -> {
            resetView();
            getView().setAdapterComboBoxEnabled(false);
            getView().setSelectFileButtonEnabled(false);
            getView().setProgressBarText("Connecting to Adapter");

            Adapter matchedAdapter = null;
            for (Adapter adapter : getAdapters()) {
                String name = adapter.getName();
                if (name.equals(selectedAdapterName)) {
                    matchedAdapter = adapter;
                    break;
                }
            }
            setSelectedAdapter(matchedAdapter);
            checkSetupComplete();
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.etools.j1939_84.ui.IUserInterfaceController#onFileChosen(java.io.
     * File)
     */
    @Override
    public void onFileChosen(File file) {
        executor.execute(() -> {
            resetView();
            getView().setAdapterComboBoxEnabled(false);
            getView().setSelectFileButtonEnabled(false);
            getView().setProgressBarText("Scanning Report File");

            try {
                File reportFile = setupReportFile(file);
                setReportFile(reportFile);
                getView().setSelectFileButtonText(reportFile.getAbsolutePath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Error Reading File", e);
                reportFile = null;

                getView().setSelectFileButtonText(null);
                String message = "File cannot be used.";
                if (e.getMessage() != null) {
                    message += NL + e.getMessage();
                }
                message += NL + "Please select a different file.";

                getView().displayDialog(message, "File Error", JOptionPane.ERROR_MESSAGE, false);
            }
            checkSetupComplete();
            getView().setAdapterComboBoxEnabled(true);
            getView().setSelectFileButtonEnabled(true);
        });
    }

    @Override
    public void onHelpButtonClicked() {
        helpView.setVisible(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.etools.j1939_84.ui.IUserInterfaceController#
     * onReadVehicleInfoButtonClicked()
     */
    @Override
    public void onReadVehicleInfoButtonClicked() {
        executor.execute(() -> {
            resetView();
            getView().setAdapterComboBoxEnabled(false);
            getView().setSelectFileButtonEnabled(false);
            boolean result = false;

            ResultsListener resultsListener = getResultsListener();
            try {
                resultsListener.onProgress(1, 6, "Reading Vehicle Identification Number");
                vin = getComparisonModule().getVin();
                getView().setVin(vin);

                resultsListener.onProgress(2, 6, "Reading Vehicle Calibrations");
                String cals = getComparisonModule().getCalibrationsAsString();
                getView().setEngineCals(cals);

                result = true;
            } catch (IOException e) {
                getView().setProgressBarText(e.getMessage());
                getView().displayDialog(e.getMessage(), "Communications Error", JOptionPane.ERROR_MESSAGE, false);
            } finally {
                if (result) {
                    getView().setProgressBarText("Push Go Button");
                }
                getView().setStartButtonEnabled(result);
                getView().setStopButtonEnabled(false);
                getView().setReadVehicleInfoButtonEnabled(true);
                getView().setAdapterComboBoxEnabled(true);
                getView().setSelectFileButtonEnabled(true);
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.etools.j1939_84.ui.IUserInterfaceController#
     * onSelectFileButtonClicked()
     */
    @Override
    public void onSelectFileButtonClicked() {
        getView().displayFileChooser();
    }

    @Override
    public void onStartButtonClicked() {
        getView().setStartButtonEnabled(false);
        getView().setStopButtonEnabled(true);
        getView().setReadVehicleInfoButtonEnabled(false);
        getView().setSelectFileButtonEnabled(false);
        overallController.execute(getResultsListener(), getNewJ1939(), getReportFileModule());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.etools.j1939_84.ui.IUserInterfaceController#onStopButtonClicked()
     */
    @Override
    public void onStopButtonClicked() {
        if (overallController.isActive()) {
            overallController.stop();
        }
        getResultsListener().onComplete(false);
        getResultsListener().onProgress("User cancelled operation");
        getView().setStopButtonEnabled(false);
    }

    private void resetView() {
        getComparisonModule().reset();
        vin = null;
        getView().setVin("");
        getView().setEngineCals("");
        getView().setStartButtonEnabled(false);
        getView().setStopButtonEnabled(false);
        getView().setReadVehicleInfoButtonEnabled(false);
    }

    private void setBus(Bus bus) throws BusException {
        this.bus = bus;
        getComparisonModule().setJ1939(getNewJ1939());
    }

    /**
     * Sets the Report File with no additional logic. This should only be used
     * for testing.
     *
     * @param file
     *             the report file to use
     * @throws IOException
     *                     if there is a problem setting the report file
     */
    void setReportFile(File file) throws IOException {
        getReportFileModule().setReportFile(getResultsListener(), file);
        reportFile = file;
    }

    /**
     * Sets the selected adapter. This should only be used for testing.
     *
     * @param selectedAdapter
     *                        the selectedAdapter to set
     */
    private void setSelectedAdapter(Adapter selectedAdapter) {
        try {
            Bus bus;
            if (selectedAdapter != null) {
                bus = rp1210.setAdapter(selectedAdapter, 0xF9);
            } else {
                bus = null;
            }
            this.selectedAdapter = selectedAdapter;
            if (bus != null) {
                setBus(bus);
            }
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error Setting Adapter", e);
            getView().displayDialog("Communications could not be established using the selected adapter.",
                    "Communication Failure",
                    JOptionPane.ERROR_MESSAGE,
                    false);
        }
    }

    /**
     * Checks the given {@link File} to determine if it's a valid file for using
     * to store the Report. If it's valid, the report file is returned.
     *
     * @param file
     *             the {@link File} to check
     * @return The file to be used for the report
     * @throws IOException
     *                     if the file cannot be used
     */

    private File setupReportFile(File file) throws IOException {
        File reportFile = file;
        if (!file.exists()) {
            // Append the file extension if the file doesn't have one.
            if (!file.getName().endsWith("." + FILE_SUFFIX)) {
                return setupReportFile(new File(file.getAbsolutePath() + "." + FILE_SUFFIX));
            }
            if (!reportFile.createNewFile()) {
                throw new IOException("File cannot be created");
            }
        } else {
            throw new IOException("File already exists");
        }
        return reportFile;
    }
}
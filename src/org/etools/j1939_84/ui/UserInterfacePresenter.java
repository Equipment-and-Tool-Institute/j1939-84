/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static org.etools.j1939_84.J1939_84.NL;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.controllers.OverallController;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.engine.simulated.Engine;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.Adapter;
import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.EchoBus;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RP1210;
import org.etools.j1939tools.bus.RP1210Bus;
import org.etools.j1939tools.j1939.J1939;

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
    static final String FILE_SUFFIX = "txt";

    private final Executor executor;

    private final OverallController overallController;

    private final ReportFileModule reportFileModule;

    private final RP1210 rp1210;

    private final VehicleInformationModule vehicleInformationModule;

    private final UserInterfaceContract.View view;

    private List<Adapter> adapters;

    private Bus bus;

    private File reportFile;

    private Adapter selectedAdapter;

    private String vin;

    private J1939 j1939;

    private String selectedConnectionString;

    /**
     * The device Id used to indicate the adapter is not a physical one
     */
    public static final short FAKE_DEV_ID = (short) -1;

    private AutoCloseable engine;

    /**
     * Default Constructor
     *
     * @param view
     *                 The {@link UserInterfaceView} to control
     */
    public UserInterfacePresenter(UserInterfaceContract.View view) {
        this(view,
             new VehicleInformationModule(),
             new RP1210(),
             new ReportFileModule(),
             Runtime.getRuntime(),
             Executors.newSingleThreadExecutor(),
             new OverallController(),
             new J1939());
    }

    /**
     * Constructor used for testing
     *
     * @param view
     *                                     The {@link UserInterfaceView} to control
     * @param vehicleInformationModule
     *                                     the {@link VehicleInformationModule}
     * @param rp1210
     *                                     the {@link RP1210}
     * @param reportFileModule
     *                                     the {@link ReportFileModule}
     * @param runtime
     *                                     the {@link Runtime}
     * @param executor
     *                                     the {@link Executor} used to execute {@link Thread} s
     *
     * @param overallController
     *                                     the {@link OverallController} which will run all the other
     *                                     parts
     */
    public UserInterfacePresenter(UserInterfaceContract.View view,
                                  VehicleInformationModule vehicleInformationModule,
                                  RP1210 rp1210,
                                  ReportFileModule reportFileModule,
                                  Runtime runtime,
                                  Executor executor,
                                  OverallController overallController,
                                  J1939 j1939) {
        this.view = view;
        this.vehicleInformationModule = vehicleInformationModule;
        this.rp1210 = rp1210;
        this.reportFileModule = reportFileModule;
        this.executor = executor;
        this.overallController = overallController;
        this.j1939 = j1939;
        // vehicleInformationModule.setJ1939(this.j1939);
        runtime.addShutdownHook(new Thread(reportFileModule::onProgramExit, "Shutdown Hook Thread"));
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
                // second time avoids re-parsing and will pickup synthetic
                // adapters.
                try {
                    adapters.addAll(rp1210.getAdapters());
                } catch (BusException e1) {
                    getLogger().log(Level.SEVERE, "Unable to load any adapters.", e1);
                }
            }
        }
        return adapters;
    }

    @Override
    public J1939 getJ1939() {
        return j1939;
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

    @Override
    public String getVin() {
        return vin;
    }

    @Override
    public void onAdapterComboBoxItemSelected(Adapter adapter, String connectionString) {
        // Connecting to the adapter can take "a while"
        executor.execute(() -> {
            selectedAdapter = adapter;
            selectedConnectionString = connectionString;
            resetView();
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
        try {
            File helpFile = File.createTempFile("J1939-84-help-", ".pdf");
            Files.copy(getClass().getResourceAsStream("help.pdf"),
                       helpFile.toPath(),
                       StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(helpFile);
        } catch (IOException e) {
            String message = "Error opening help.";
            getLogger().log(Level.SEVERE, message, e);
            if (e.getMessage() != null) {
                message += NL + e.getMessage();
            }
            getView().displayDialog(message, "Unable to open help.", JOptionPane.ERROR_MESSAGE, false);
        }

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
            setSelectedAdapter(selectedAdapter, selectedConnectionString, 0xF9);
            ResultsListener resultsListener = getResultsListener();
            try {
                if (bus.imposterDetected()) {
                    throw new IOException("Unexpected Service Tool Message from SA 0xF9 observed. " + NL
                            + "Please disconnect the other ECU using SA 0xF9." + NL
                            + "The application must be restarted in order to continue.");
                }

                resultsListener.onProgress(1, 3, "Reading Vehicle Identification Number");
                vin = vehicleInformationModule.getVin();
                getView().setVin(vin);

                resultsListener.onProgress(2, 3, "Reading Vehicle Calibrations");
                String cals = vehicleInformationModule.getCalibrationsAsString();
                getView().setEngineCals(cals);

                result = true;
                resultsListener.onProgress(3, 3, "Complete");
            } catch (Throwable e) {
                getLogger().log(Level.WARNING, "Communications error", e);
                resultsListener.onProgress(3, 3, e.getMessage());
                getView().displayDialog(e.getMessage(), "Communications Error", JOptionPane.ERROR_MESSAGE, false);
            } finally {
                if (result) {
                    getView().setProgressBarText("Push Start Button");
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
        getView().setAdapterComboBoxEnabled(false);

        overallController.execute(getResultsListener(), getJ1939(), getReportFileModule());
        getReportFileModule().setJ1939(getJ1939());
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

    private Logger getLogger() {
        return J1939_84.getLogger();
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
     * Sets the Report File with no additional logic. This should only be used
     * for testing.
     *
     * @param  file
     *                         the report file to use
     * @throws IOException
     *                         if there is a problem setting the report file
     */
    void setReportFile(File file) throws IOException {
        getReportFileModule().setReportFile(file);
        reportFile = file;
    }

    private ResultsListener getResultsListener() {
        return new ResultsListener() {
            @Override
            public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
                onResult(new ActionOutcome(outcome, message).toString());
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
                getView().displayDialog(message, title, type.getValue(), true, null);
            }

            @Override
            public void onUrgentMessage(String message,
                                        String title,
                                        MessageType type,
                                        QuestionListener questionListener) {
                getView().displayDialog(message, title, type.getValue(), true, questionListener);
            }

            @Override
            public void onVehicleInformationNeeded(VehicleInformationListener listener) {
                getView().displayForm(listener, getJ1939());
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

    /**
     * Sets the selected adapter. This should only be used for testing.
     *
     * @param selectedAdapter
     *                            the selectedAdapter to set
     * @param address
     */
    private void setSelectedAdapter(Adapter selectedAdapter, String connectionString, int address) {
        try {
            // close old bus before opening new bus
            setBus(null);
            Bus bus;
            if (selectedAdapter != null) {
                bus = getBus(selectedAdapter, connectionString, address);
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
     * Helper method that sets the {@link Adapter} that will be used for communication
     * with the vehicle. A {@link Bus} is returned which will be used to send and read
     * {@link Packet}s
     *
     * @param  selectedAdapter
     *                              the {@link Adapter} to use for communications
     * @param  connectionString
     *                              the source address of the tool
     * @return                  An {@link Bus}
     * @throws BusException
     *                              if there is a problem setting the adapter
     */
    private Bus getBus(Adapter selectedAdapter, String connectionString, int address) throws BusException {

        if (engine != null) {
            try {
                engine.close();
            } catch (Exception e) {
                throw new IllegalStateException("Unexpected error closing simulated engine.", e);
            }
            engine = null;
        }

        if (selectedAdapter.getDeviceId() == FAKE_DEV_ID) {
            EchoBus bus = new EchoBus(address);
            engine = new Engine(bus);
            return bus;
        } else {
            return RP1210.createBus(selectedAdapter, connectionString, address);
        }
    }

    private UserInterfaceContract.View getView() {
        return view;
    }

    private void resetView() {
        vehicleInformationModule.reset();
        vin = null;
        getView().setVin("");
        getView().setEngineCals("");
        getView().setStartButtonEnabled(false);
        getView().setStopButtonEnabled(false);
        getView().setReadVehicleInfoButtonEnabled(false);
    }

    void setBus(Bus bus) throws BusException {
        // clear old values
        if (this.bus != null) {
            this.bus.close();
            this.bus = null;
            this.j1939.closeLogger();
            this.j1939 = null;
            vehicleInformationModule.setJ1939(null);
        }
        // set new values
        if (bus != null) {
            this.bus = bus;
            this.j1939 = new J1939(bus);
            this.j1939.startLogger();
            vehicleInformationModule.setJ1939(getJ1939());
        }
    }

    /**
     * Checks the given {@link File} to determine if it's a valid file for using
     * to store the Report. If it's valid, the report file is returned.
     *
     * @param  file
     *                         the {@link File} to check
     * @return             The file to be used for the report
     * @throws IOException
     *                         if the file cannot be used
     */
    private static File setupReportFile(File file) throws IOException {
        if (!file.exists()) {
            // Append the file extension if the file doesn't have one.
            if (!file.getName().endsWith("." + FILE_SUFFIX)) {
                return setupReportFile(new File(file.getAbsolutePath() + "." + FILE_SUFFIX));
            }
            if (!file.createNewFile()) {
                throw new IOException("File cannot be created");
            }
        } else {
            throw new IOException("File already exists");
        }
        return file;
    }
}

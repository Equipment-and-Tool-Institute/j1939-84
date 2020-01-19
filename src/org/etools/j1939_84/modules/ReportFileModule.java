/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;

/**
 * The {@link FunctionalModule} that's responsible for the log file
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ReportFileModule extends FunctionalModule implements ResultsListener {

    private final BannerModule bannerModule;

    private final Logger logger;

    private File reportFile;

    private final SummaryModule summaryModule;

    private VehicleInformation vehicleInformation;

    /**
     * The Writer used to write results to the report file
     */
    private Writer writer;

    /**
     * Constructor
     */
    public ReportFileModule() {
        this(new DateTimeModule(), J1939_84.getLogger(), new SummaryModule(), new BannerModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param dateTimeModule
     *                       The {@link DateTimeModule}
     * @param logger
     *                       The {@link Logger} to use for logging
     */
    public ReportFileModule(DateTimeModule dateTimeModule, Logger logger, SummaryModule summaryModule,
            BannerModule bannerModule) {
        super(dateTimeModule);
        this.logger = logger;
        this.summaryModule = summaryModule;
        this.bannerModule = bannerModule;
    }

    @Override
    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
        summaryModule.addOutcome(partNumber, stepNumber, outcome, message);
    }

    @Override
    public void beginPart(PartResult partResult) {
        summaryModule.beginPart(partResult);
    }

    @Override
    public void beginStep(StepResult stepResult) {

    }

    @Override
    public void endPart(PartResult partResult) {
        // onResult(NL);
        // onResult("End Part " + partResult);
    }

    @Override
    public void endStep(StepResult stepResult) {
        // onResult(NL);
        summaryModule.endStep(stepResult);
    }

    private Logger getLogger() {
        return logger;
    }

    @Override
    public void onComplete(boolean success) {
        writeFinalReport();
    }

    @Override
    public void onMessage(String message, String title, MessageType type) {
        // Don't care
    }

    /**
     * Called when the tool exits so it can be noted in the log file
     */
    public void onProgramExit() {
        try {
            if (writer != null) {
                write(getTime() + " End of " + BannerModule.TOOL_NAME + " Execution" + NL);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error writing end of program statement", e);
        }
    }

    @Override
    public void onProgress(int currentStep, int totalSteps, String message) {
        // Don't care
    }

    @Override
    public void onProgress(String message) {
        // Don't care
    }

    @Override
    public void onResult(List<String> results) {
        for (String result : results) {
            onResult(result);
        }
    }

    @Override
    public void onResult(String result) {
        try {
            write(result);
            writer.flush();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error Writing to file", e);
        }
    }

    @Override
    public void onUrgentMessage(String message, String title, MessageType type) {
        // Don't care
    }

    @Override
    public void onVehicleInformationNeeded(VehicleInformationListener listener) {
    }

    @Override
    public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }

    /**
     * Reports the information about the report file
     *
     * @param listener
     *                 the {@link ResultsListener} that will be notified of the
     *                 results
     */
    public void reportFileInformation(ResultsListener listener) {
        listener.onResult(getTime() + " File: " + reportFile.getAbsolutePath());
    }

    /**
     * Sets the File that will be used to log results to
     *
     * @param listener
     *                   the {@link ResultsListener} that will be notified of
     *                   progress
     * @param reportFile
     *                   the File used for the report
     * @throws IOException
     *                     if there is problem with the file
     *
     */
    public void setReportFile(ResultsListener listener, File reportFile) throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }

        this.reportFile = reportFile;
        if (reportFile != null) {
            writer = Files.newBufferedWriter(reportFile.toPath(), StandardOpenOption.APPEND);
        }
    }

    /**
     * Writes a result to the report file
     *
     * @param result
     *               the result to write
     * @throws IOException
     *                     if there is a problem writing to the file
     */
    private void write(String result) throws IOException {
        writer.write(result + NL);
    }

    private void writeFinalReport() {
        try {
            Path tempFilePath = Files.createTempFile("report", "J1939-84");
            Writer tempFileWriter = Files.newBufferedWriter(tempFilePath);
            tempFileWriter.write(bannerModule.getHeader() + NL);
            tempFileWriter.write(NL);
            tempFileWriter.write(bannerModule.getSummaryHeader() + NL);
            tempFileWriter.write("Generated " + getTime() + NL);
            tempFileWriter.write(NL);
            tempFileWriter.write(vehicleInformation + NL);
            tempFileWriter.write(NL);
            tempFileWriter.write(summaryModule.generateSummary());
            tempFileWriter.write(NL); // FIXME This should be a page break
            tempFileWriter.flush();

            Reader reportFileReader = Files.newBufferedReader(reportFile.toPath());
            int read = reportFileReader.read();
            // FIXME - Use a byte array to read in chunks
            while (read != -1) {
                tempFileWriter.write(read);
                read = reportFileReader.read();
            }
            tempFileWriter.flush();
            Files.copy(tempFilePath, reportFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

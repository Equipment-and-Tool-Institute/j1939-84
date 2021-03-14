/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.PAGE_BREAK;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;

/**
 * The {@link FunctionalModule} that's responsible for the log file
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class ReportFileModule extends FunctionalModule implements ResultsListener {

    private final BannerModule bannerModule;

    private final Logger logger;
    private final SummaryModule summaryModule;
    private File reportFile;
    private VehicleInformation vehicleInformation;

    /**
     * The Writer used to write results to the report file
     */
    private Writer writer;

    /**
     * Constructor
     */
    public ReportFileModule() {
        this(J1939_84.getLogger(), new SummaryModule(), new BannerModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param logger
     *                          The {@link Logger} to use for logging
     * @param summaryModule
     *                          The {@link SummaryModule}
     * @param bannerModule
     *                          The {@link BannerModule}
     */
    public ReportFileModule(Logger logger,
                            SummaryModule summaryModule,
                            BannerModule bannerModule) {
        super();
        this.logger = logger;
        this.summaryModule = summaryModule;
        this.bannerModule = bannerModule;
    }

    @Override
    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
        onResult(new ActionOutcome(outcome, message).toString());
    }

    @Override
    public void onComplete(boolean success) {
        writeFinalReport();
    }

    @Override
    public void onMessage(String message, String title, MessageType type) {
        // Don't care
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
    public void onUrgentMessage(String message, String title, MessageType type, QuestionListener listener) {
        // Don't care
    }

    @Override
    public void onVehicleInformationNeeded(VehicleInformationListener listener) {
    }

    @Override
    public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }

    private Logger getLogger() {
        return logger;
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

    /**
     * Reports the information about the report file
     *
     * @param listener
     *                     the {@link ResultsListener} that will be notified of the
     *                     results
     */
    public void reportFileInformation(ResultsListener listener) {
        listener.onResult(getTime() + " File: " + reportFile.getAbsolutePath());
    }

    /**
     * Sets the File that will be used to log results to
     *
     * @param  reportFile
     *                         the File used for the report
     * @throws IOException
     *                         if there is problem with the file
     */
    public void setReportFile(File reportFile) throws IOException {
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
     * @param  result
     *                         the result to write
     * @throws IOException
     *                         if there is a problem writing to the file
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
            tempFileWriter.write("Generated " + getDate() + " " + getTime() + NL);
            tempFileWriter.write("Log File Name: " + reportFile + NL);
            tempFileWriter.write(NL);
            tempFileWriter.write(vehicleInformation + NL);
            tempFileWriter.write(NL);
            tempFileWriter.write("Addresses Claimed" + NL);
            if (vehicleInformation != null && vehicleInformation.getAddressClaim() != null) {
                tempFileWriter
                              .write(vehicleInformation.getAddressClaim()
                                                       .getPackets()
                                                       .stream()
                                                       .filter(Objects::nonNull)
                                                       .map(a -> "    " + a.getPacket() + " " + a.getSource())
                                                       .collect(Collectors.joining(NL))
                                      + NL); // FIXME
            } else {
                tempFileWriter.write(" IS EMPTY" + NL);
            }
            tempFileWriter.write(PAGE_BREAK);

            tempFileWriter.write("TEST SUMMARY REPORT" + NL);
            tempFileWriter.write("OUTCOME: " + NL);
            tempFileWriter.write("Failures:    " + summaryModule.getOutcomeCount(Outcome.FAIL) + NL);
            tempFileWriter.write("Warnings:    " + summaryModule.getOutcomeCount(Outcome.WARN) + NL);
            tempFileWriter.write("Information: " + summaryModule.getOutcomeCount(Outcome.INFO) + NL);
            tempFileWriter.write("Incomplete:  " + summaryModule.getOutcomeCount(Outcome.INCOMPLETE) + NL);
            tempFileWriter.write("Timing:      " + getJ1939().getWarnings() + NL);
            tempFileWriter.write("Passes:      " + summaryModule.getOutcomeCount(Outcome.PASS) + NL);
            tempFileWriter.write(NL);
            tempFileWriter.write(vehicleInformation + NL);

            tempFileWriter.write(summaryModule.generateSummary());
            tempFileWriter
                          .write(bannerModule.getDate() + " " + bannerModule.getTime() + " END TEST SUMMARY REPORT"
                                  + NL);

            tempFileWriter.write(PAGE_BREAK);
            tempFileWriter.flush();

            tempFileWriter.write("TEST LOG REPORT" + NL + NL);
            tempFileWriter.write(vehicleInformation + NL);
            try (Reader reportFileReader = Files.newBufferedReader(reportFile.toPath())) {
                reportFileReader.transferTo(tempFileWriter);
            }
            tempFileWriter.flush();
            tempFileWriter.close();
            File raw = new File(reportFile + ".raw");
            reportFile.renameTo(raw);
            Files.copy(tempFilePath, reportFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            raw.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

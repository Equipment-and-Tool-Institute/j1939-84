/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.PAGE_BREAK;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939tools.bus.Adapter;
import org.etools.j1939tools.modules.FunctionalModule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
    private Adapter adapter;

    /**
     * The Writer used to write results to the report file
     */
    private Writer writer;

    private static final String ZIP_FILE_END = "-J1939-84-CAN.zip";

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
            logger.log(SEVERE, "Error Writing to file", e);
        }
    }

    @Override
    public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
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
            logger.log(SEVERE, "Error writing end of program statement", e);
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
     * Sets the adapter to be included in the report
     *
     * @param adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    private String getAdapterString(){
        if (adapter != null){
            return adapter.getDLLName() + " - " + adapter.getName();
        }
        return "";
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

    @SuppressFBWarnings(value = { "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            "REC_CATCH_EXCEPTION", "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE" }, justification = "Several places in the calls down the stack can return null. Not important if zip file deletion sometimes fails.")
    private void writeFinalReport() {
        try {
            String pageHeader = bannerModule.getHeader() + NL
                    + "Generated: " + getDate() + " " + getTime() + NL
                    + "Adapter Selection: " + getAdapterString() + NL
                    + "Log File Name: " + reportFile;

            Path tempFilePath = Files.createTempFile("report", "J1939-84");
            Writer tempWriter = Files.newBufferedWriter(tempFilePath);

            tempWriter.write("Summary of " + BannerModule.TOOL_NAME + " Execution" + NL);
            tempWriter.write(NL);

            tempWriter.write(pageHeader + NL);
            tempWriter.write(NL);

            tempWriter.write("TEST SUMMARY REPORT" + NL);
            tempWriter.write("OUTCOME: " + NL);
            tempWriter.write("Failures:    " + summaryModule.getOutcomeCount(Outcome.FAIL) + NL);
            tempWriter.write("Warnings:    " + summaryModule.getOutcomeCount(Outcome.WARN) + NL);
            tempWriter.write("Information: " + summaryModule.getOutcomeCount(Outcome.INFO) + NL);
            tempWriter.write("Incomplete:  " + summaryModule.getOutcomeCount(Outcome.INCOMPLETE) + NL);
            tempWriter.write("Timing:      " + getJ1939().getWarnings() + NL);
            tempWriter.write("Passes:      " + summaryModule.getOutcomeCount(Outcome.PASS) + NL);
            tempWriter.write(NL);

            tempWriter.write(vehicleInformation + NL);
            tempWriter.write(NL);

            tempWriter.write("Addresses Claimed" + NL);
            if (vehicleInformation != null && vehicleInformation.getAddressClaim() != null) {
                tempWriter.write(getAddressClaimReport() + NL);
            } else {
                tempWriter.write("Error: No addresses were claimed" + NL);
            }

            tempWriter.write(PAGE_BREAK);
            tempWriter.write(pageHeader + NL);
            tempWriter.write(NL);

            tempWriter.write(summaryModule.generateSummary());
            tempWriter.write("End Summary of " + BannerModule.TOOL_NAME + " Execution" + NL);

            tempWriter.write(PAGE_BREAK);
            tempWriter.write(pageHeader + NL);
            tempWriter.write(NL);

            tempWriter.flush();

            tempWriter.write("TEST LOG REPORT" + NL + NL);
            try (Reader reportFileReader = Files.newBufferedReader(reportFile.toPath())) {
                reportFileReader.transferTo(tempWriter);
            }
            tempWriter.write("END TEST LOG REPORT");

            tempWriter.flush();
            tempWriter.close();

            File raw = new File(reportFile + ".raw");
            boolean reNameSuccess = reportFile.renameTo(raw);
            if (!reNameSuccess) {
                logger.log(SEVERE, "Unable to rename file");
            }
            Files.copy(tempFilePath, reportFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            boolean copySuccess = raw.delete();
            if (!copySuccess) {
                logger.log(SEVERE, "Unable to delete file");
            }

            getJ1939().getLogFilePath().ifPresentOrElse(s -> {
                File busLogFile = new File(s);

                String zipFileName = reportFile.toPath().toString();
                zipFileName = zipFileName.substring(0, zipFileName.lastIndexOf(".")) + ZIP_FILE_END;
                File zipFile = new File(zipFileName);

                try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))){
                    zos.putNextEntry(new ZipEntry(busLogFile.getName().toString()));
                    Files.copy(busLogFile.toPath(), zos);
                    zos.closeEntry();
                }catch(IOException e){
                    logger.log(WARNING, "Failed to add .asc CAN log to zip file.");
                }

                //same file management logic as in J1939 class
                Stream.of(zipFile.getParentFile()
                                  .listFiles((dir, name) -> name.endsWith(ZIP_FILE_END)))
                        .sorted(Comparator.comparing(f -> -f.lastModified()))
                        .skip(10)
                        .forEach(f -> f.delete());
            }, () -> logger.log(INFO, "No .asc CAN log found."));
        } catch (Exception e) {
            logger.log(WARNING, "Failure while creating final report or zipped CAN log", e);
        }
    }

    private String getAddressClaimReport() {
        return vehicleInformation.getAddressClaim()
                                 .getPackets()
                                 .stream()
                                 .filter(Objects::nonNull)
                                 .map(a -> "    " + a.getPacket() + " " + a.getSource())
                                 .collect(Collectors.joining(NL));
    }

    @Override
    public void onMessage(String message, String title, MessageType type) {
        if (type == MessageType.ERROR) {
            onResult(type + ": " + title + ": " + message);
        }
    }
}

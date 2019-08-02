/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.controllers.ResultsListener;

/**
 * The {@link FunctionalModule} that's responsible for the log file
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ReportFileModule extends FunctionalModule implements ResultsListener {

	private Logger logger;

	private File reportFile;

	/**
	 * Reports the information about the report file
	 *
	 * @param listener
	 *                 the {@link ResultsListener} that will be notified of the
	 *                 results
	 */
	public void reportFileInformation(ResultsListener listener) {
		listener.onResult(getDateTime() + " File: " + reportFile.getAbsolutePath());
	}

	/**
	 * The Writer used to write results to the report file
	 */
	private BufferedWriter writer;

	/**
	 * Constructor
	 */
	public ReportFileModule() {
		this(new DateTimeModule(), J1939_84.getLogger());
	}

	/**
	 * Constructor exposed for testing
	 *
	 * @param dateTimeModule
	 *                       The {@link DateTimeModule}
	 * @param logger
	 *                       The {@link Logger} to use for logging
	 */
	public ReportFileModule(DateTimeModule dateTimeModule, Logger logger) {
		super(dateTimeModule);
		this.logger = logger;
	}

	private Logger getLogger() {
		return logger;
	}

	@Override
	public void onComplete(boolean success) {
		// Don't care
	}

	@Override
	public void onMessage(String message, String title, int type) {
		// Don't care
	}

	/**
	 * Called when the tool exits so it can be noted in the log file
	 */
	public void onProgramExit() {
		try {
			if (writer != null) {
				write(getDateTime() + " End of " + BannerModule.TOOL_NAME + " Execution" + NL);
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
	public void onUrgentMessage(String message, String title, int type) {
		// Don't care
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
	public void setReportFile(@Nonnull ResultsListener listener, @Nonnull File reportFile) throws IOException {

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

}

/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.etools.j1939_84.controllers.TestResultsListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ReportFileModule} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ReportFileModuleTest {

	private File file;

	private ReportFileModule instance;
	private TestResultsListener listener;
	private Logger logger;

	@Before
	public void setUp() throws Exception {
		file = File.createTempFile("test", ".j1939_84");
		file.deleteOnExit();
		listener = new TestResultsListener();

		TestDateTimeModule dateTimeModule = new TestDateTimeModule() {
			@Override
			public DateTimeFormatter getTimeFormatter() {
				return getSuperTimeFormatter();
			}
		};

		logger = mock(Logger.class);
		instance = new ReportFileModule(dateTimeModule, logger);
	}

	@After
	public void tearDown() throws Exception {
		if (!file.delete()) {
			System.err.println("Could not delete test file");
		}
	}

	@Test
	public void testOnCompleteFalse() {
		instance.onComplete(false);
		// Nothing (bad) happens;
	}

	@Test
	public void testOnCompleteTrue() {
		instance.onComplete(true);
		// Nothing (bad) happens;
	}

	@Test
	public void testOnProgramExit() throws Exception {
		instance.setReportFile(listener, file);
		instance.onProgramExit();
		List<String> lines = Files.readAllLines(file.toPath());
		String expected = "2007-12-03T10:15:30.000 End of J1939-84 Tool Execution";
		assertEquals(expected, lines.get(0));
	}

	@Test
	public void testOnProgramExitWithoutFile() throws Exception {
		instance.onProgramExit();
		// Nothing (bad) happens
	}

	@Test
	public void testOnProgress() {
		instance.onProgress(5, 10, "Message");
		// Nothing (bad) happens
	}

	@Test
	public void testOnProgressWithMessage() {
		instance.onProgress("Message");
		// Nothing (bad) happens
	}

	@Test
	public void testOnResult() throws Exception {
		instance.setReportFile(listener, file);
		List<String> results = new ArrayList<>();
		results.add("Line 1");
		results.add("Line 2");
		results.add("Line 3");
		instance.onResult(results);
		List<String> lines = Files.readAllLines(file.toPath());
		assertEquals(3, lines.size());
		assertEquals("Line 1", lines.get(0));
		assertEquals("Line 2", lines.get(1));
		assertEquals("Line 3", lines.get(2));
	}

	@Test
	public void testReportFileInformation() throws Exception {
		File reportFile = mock(File.class);
		when(reportFile.getAbsolutePath()).thenReturn("files/users/report.j1939_84");
		when(reportFile.toPath()).thenReturn(file.toPath());
		instance.setReportFile(listener, reportFile);

		instance.reportFileInformation(listener);

		String expected = "2007-12-03T10:15:30.000 File: files/users/report.j1939_84" + NL;
		assertEquals(expected, listener.getResults());
	}

	@Test
	public void testSetReportFileWithNull() throws Exception {
		instance.setReportFile(listener, null);
		// Nothing (bad) happens
	}

}

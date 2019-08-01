/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import org.etools.j1939_84.resources.Resources;

/**
 * Read the properties file to return a build number for the application
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BuildNumber {

	private final InputStream inputStream;
	private Properties properties;
	private String result;

	/**
	 * Default constructor. The Build Number will read from the
	 * "version.properties" file
	 */
	public BuildNumber() {
		inputStream = Resources.class.getResourceAsStream("version.properties");
	}

	/**
	 * Constructor used for testing
	 *
	 * @param inputStream
	 *                    the input stream that contains the properties
	 */
	public BuildNumber(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	/**
	 * Returns the Properties loaded from the file
	 *
	 * @return Properties
	 */
	private Properties getProperties() {
		if (properties == null) {
			loadProperties();
		}
		return properties;
	}

	/**
	 * Returns the Version Number formatted as "major.minor[.revision][-date]"
	 * If the major or minor numbers are not provided, "00" will be used
	 * instead.
	 *
	 * @return the Version Number as a string
	 */
	public String getVersionNumber() {
		if (result == null) {
			String major = getProperties().getProperty("build.major.number");
			result = major != null ? major : "00";
			result += ".";

			String minor = getProperties().getProperty("build.minor.number");
			result += minor != null ? minor : "00";

			String revision = getProperties().getProperty("build.revision.number");
			result += revision != null ? "." + revision : "";

			String date = getProperties().getProperty("build.date");
			result += date != null ? " - " + date : "";
		}
		return result;
	}

	/**
	 * Read the properties file and loads the properties
	 */
	private void loadProperties() {
		try {
			properties = new Properties();
			properties.load(inputStream);
		} catch (Exception e) {
			J1939_84.getLogger().log(Level.SEVERE, "Error Loading Version Properties", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Don't care
				}
			}
		}
	}
}
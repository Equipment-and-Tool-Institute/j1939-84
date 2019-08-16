/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility Class used to validate and decode information from the VIN
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VinDecoder {

	private static final int[] LETTER_VALUE = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7,
			8, 0, 1, 2, 3, 4, 5, 0, 7, 0, 9, 2, 3, 4, 5, 6, 7, 8, 9 };

	public static final int MAX_MODEL_YEAR = 2039;

	public static final int MIN_MODEL_YEAR = 2010;

	private static final Map<String, Integer> MODEL_YEARS = new HashMap<>();

	public static final int VIN_LENGTH = 17; // characters

	private static final int[] WEIGHT = { 8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2 };

	static {
		MODEL_YEARS.put("A", 2010);
		MODEL_YEARS.put("B", 2011);
		MODEL_YEARS.put("C", 2012);
		MODEL_YEARS.put("D", 2013);
		MODEL_YEARS.put("E", 2014);
		MODEL_YEARS.put("F", 2015);
		MODEL_YEARS.put("G", 2016);
		MODEL_YEARS.put("H", 2017);
		MODEL_YEARS.put("J", 2018);
		MODEL_YEARS.put("K", 2019);
		MODEL_YEARS.put("L", 2020);
		MODEL_YEARS.put("M", 2021);
		MODEL_YEARS.put("N", 2022);
		MODEL_YEARS.put("P", 2023);
		MODEL_YEARS.put("R", 2024);
		MODEL_YEARS.put("S", 2025);
		MODEL_YEARS.put("T", 2026);
		MODEL_YEARS.put("V", 2027);
		MODEL_YEARS.put("W", 2028);
		MODEL_YEARS.put("X", 2029);
		MODEL_YEARS.put("Y", 2030);
		MODEL_YEARS.put("1", 2031);
		MODEL_YEARS.put("2", 2032);
		MODEL_YEARS.put("3", 2033);
		MODEL_YEARS.put("4", 2034);
		MODEL_YEARS.put("5", 2035);
		MODEL_YEARS.put("6", 2036);
		MODEL_YEARS.put("7", 2037);
		MODEL_YEARS.put("8", 2038);
		MODEL_YEARS.put("9", 2039);
	}

	/**
	 * Calculate VIN checksum char
	 *
	 * @param vin a VIN to get the checksum for
	 * @return checksum for provided VIN
	 */
	public char calculateCheckSum(String vin) {
		int sum = 0;
		for (int i = 0; i < vin.length(); i++) {
			sum += LETTER_VALUE[vin.charAt(i) - '0'] * WEIGHT[i];
		}
		int checkSum = sum % 11;
		return checkSum == 1 ? 'X' : (char) (checkSum + '0');
	}

	/**
	 * Returns the Model Year of the Vehicle based upon the VIN
	 *
	 * @param vin the VIN of interest
	 * @return an int representing the Model Year of the Vehicle. If the VIN is
	 *         invalid, -1 is returned
	 */
	public int getModelYear(String vin) {
		if (isVinValid(vin)) {
			Integer result = MODEL_YEARS.get(Character.toString(vin.charAt(9)));
			if (result != null) {
				return result;
			}
		}
		return -1;
	}

	/**
	 * Determines if the given Model Year is compatible with this decoder
	 *
	 * @param modelYear the Model Year of interest
	 * @return true if the Model Year is usable
	 */
	public boolean isModelYearValid(int modelYear) {
		return MODEL_YEARS.values().contains(modelYear);
	}

	/**
	 * Check whether provided VIN is a valid one
	 *
	 * @param vin a string to validate
	 * @return {@code true} if specified parameter is a valid VIN,
	 *         {@code false} otherwise
	 */
	public boolean isVinValid(String vin) {
		String sanitizedVin = sanitize(vin);
		if (sanitizedVin == null || sanitizedVin.length() != VIN_LENGTH) {
			return false; // Invalid length or Illegal Chars
		}

		return calculateCheckSum(sanitizedVin) == sanitizedVin.charAt(8); // Bad Checksum
	}

	/**
	 * Removes all non-valid VIN characters from the given String
	 *
	 * @param vin the String to sanitize
	 * @return the sanitized String
	 */
	public String sanitize(String vin) {
		return vin != null ? vin.toUpperCase().replaceAll("[^ABCDEFGHJKLMNPRSTUVWXYZ0-9]", "") : null;
	}

}

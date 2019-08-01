/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.resources.Resources;

import com.opencsv.CSVReader;

/**
 * Defines an SAE SLOT (Scaling, Limit, Offset, and Transfer Function)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class Slot {

	/**
	 * The Map of SLOT ID to Slot for lookups
	 */
	private static Map<Integer, Slot> slots;

	/**
	 * Helper method to convert from the given {@link String} to a double
	 *
	 * @param string
	 *                     the {@link String} to convert
	 * @param defaultValue
	 *                     the double value if the string cannot be converted
	 * @return double
	 */
	private static double doubleValue(String string, double defaultValue) {
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Finds a SLOT given the Identifier of the SLOT
	 *
	 * @param id
	 *           the Identifier of the SLOT
	 * @return a SLOT or null if not found
	 */
	public static Slot findSlot(int id) {
		return getSlots().get(id);
	}

	/**
	 * Caches and returns all known {@link Slot}s
	 *
	 * @return a Map of SLOT ID to Slot
	 */
	private static Map<Integer, Slot> getSlots() {
		if (slots == null) {
			slots = loadSlots();

		}
		return slots;
	}

	/**
	 * Helper method to convert from the given {@link String} to an int
	 *
	 * @param string
	 *                     the {@link String} to convert
	 * @param defaultValue
	 *                     the int value if the string cannot be converted
	 * @return int
	 */
	private static int intValue(String string, int defaultValue) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Read the slots.csv file which contains all the SLOTs
	 *
	 * @return Map of SLOT ID ot Slot
	 */
	private static Map<Integer, Slot> loadSlots() {
		Map<Integer, Slot> slots = new HashMap<>();
		String[] values;

		final InputStream is = Resources.class.getResourceAsStream("slots.csv");
		final InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
		try (CSVReader reader = new CSVReader(isReader)) {
			while ((values = reader.readNext()) != null) {
				final Integer id = Integer.valueOf(values[0]);
				final String name = values[1];
				final String type = values[2];
				final double scaling = doubleValue(values[3], 1);
				final String unit = values[4];
				final double offset = doubleValue(values[5], 0);
				final int length = intValue(values[6], -1);
				Slot slot = new Slot(id, name, type, scaling, offset, unit, length);
				slots.put(id, slot);
			}
		} catch (Exception e) {
			J1939_84.getLogger().log(Level.SEVERE, "Error loading map from slots", e);
		}
		return slots;
	}

	private final int id;

	private final int length; // bits

	private final String name;

	private final double offset;

	private final double scaling;

	private final String type;

	private final String unit;

	private Slot(int id, String name, String type, double scaling, double offset, String unit, int length) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.scaling = scaling;
		this.offset = offset;
		this.unit = unit;
		this.length = length;
	}

	/**
	 * Returns the Identifier of the SLOT
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the length of the data in bits
	 *
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Returns the Name of the SLOT
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the offset
	 *
	 * @return the offset
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * Returns the scaling
	 *
	 * @return the scaling
	 */
	public double getScaling() {
		return scaling;
	}

	/**
	 * Returns the Type of the SLOT
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the Units of the SLOT
	 *
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Returns a scaled value. That is result = value * scaling + offset
	 *
	 * @param value
	 *              the value to scale
	 * @return double
	 */
	public double scale(double value) {
		return value * getScaling() + getOffset();
	}
}

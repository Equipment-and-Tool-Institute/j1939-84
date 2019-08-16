/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

/**
 * The various fuel types for engines. See SPN 6317
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public enum FuelType {
	BATT_ELEC(0x08, "Battery/electric vehicle"),
	BI_CNG(0x0D, "Bi-fuel vehicle using CNG"),
	BI_DSL(0x19, "Bi-fuel vehicle using diesel"),
	BI_ELEC(0x0F, "Bi-fuel vehicle using battery"),
	BI_ETH(0x0B, "Bi-fuel vehicle using ethanol"),
	BI_GAS(0x09, "Bi-fuel vehicle using gasoline"),
	BI_LPG(0x0C, "Bi-fuel vehicle using LPG"),
	BI_METH(0x0A, "Bi-fuel vehicle using methanol"),
	BI_MIX(0x10, "Bi-fuel vehicle using battery and combustion engine"),
	BI_NG(0x18, "Bi-fuel vehicle using NG"),
	BI_PROP(0x0E, "Bi-fuel vehicle using propane"),
	CNG(0x06, "Compressed Natural Gas vehicle"),
	DSL(0x04, "Diesel vehicle"),
	DSL_CNG(0x1B, "Dual Fuel vehicle – Diesel and CNG"),
	DSL_LNG(0x1C, "Dual Fuel vehicle – Diesel and LNG"),
	ETH(0x03, "Ethanol vehicle"),
	GAS(0x01, "Gasoline/petrol vehicle"),
	HYB_DSL(0x13, "Hybrid vehicle using diesel engine"),
	HYB_ELEC(0x14, "Hybrid vehicle using battery"),
	HYB_ETH(0x12, "Hybrid vehicle using gasoline engine on ethanol"),
	HYB_GAS(0x11, "Hybrid vehicle using gasoline engine"),
	HYB_MIX(0x15, "Hybrid vehicle using battery and combustion engine"),
	HYB_REG(0x16, "Hybrid vehicle in regeneration mode"),
	LPG(0x05, "Liquefied Petroleum Gas vehicle"),
	METH(0x02, "Methanol vehicle"),
	NG(0x1A, "Natural Gas vehicle (CNG or LNG)"),
	NGV(0x17, "Natural Gas vehicle"),
	NONE(0x00, "Not available"),
	PROP(0x07, "Propane vehicle");

	/**
	 * The display name which may be shown to the user
	 */
	private final String string;

	/**
	 * The SAE defined value for the Fuel Type
	 */
	private final int value;

	/**
	 * Constructor
	 *
	 * @param value  the SAE defined value for the Fuel Type
	 * @param string the display name which may be shown to the user
	 */
	private FuelType(int value, String string) {
		this.string = string;
		this.value = value;
	}

	/**
	 * Returns the SAE defined value for the Fuel Type
	 * 
	 * @return int
	 */
	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return string;
	}

}

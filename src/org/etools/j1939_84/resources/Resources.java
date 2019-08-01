/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.resources;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * This is a place holder to allow location of other resources in the class path
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Resources {

	/**
	 * Returns an {@link Image} with the Logo for window decoration
	 *
	 * @return {@link Image}
	 */
	public static Image getLogoImage() {
		return new ImageIcon(Resources.class.getResource("logo.png")).getImage();
	}
}

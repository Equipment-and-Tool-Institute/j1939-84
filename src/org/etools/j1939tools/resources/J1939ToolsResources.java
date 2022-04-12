/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939tools.resources;

import java.awt.Image;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;

/**
 * This is a place holder to allow location of other resources in the class path
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class J1939ToolsResources {

    /**
     * Returns an {@link Image} with the Logo for window decoration
     *
     * @return {@link Image}
     */
    public static List<Image> getLogoImages() {
        return Stream.of("logo.png", "logo-128.png", "logo-64.png", "logo-48.png")
                     .map(r -> new ImageIcon(J1939ToolsResources.class.getResource("logo-64.png")).getImage())
                     .collect(Collectors.toList());
    }
}

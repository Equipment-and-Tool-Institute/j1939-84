/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Unit tests for the {@link BuildNumber} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BuildNumberTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Test
    public void testGetBuildNumber() throws Exception {
        String input = "build.major.number=12" + NL + "build.minor.number=34" + NL + "build.revision.number=56" + NL
                + "build.date=2007/08/09 10:11";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF8));
        BuildNumber instance = new BuildNumber(inputStream);
        String expected = "12.34.56 - 2007/08/09 10:11";
        assertEquals("Build number is wrong", expected, instance.getVersionNumber());
    }

    @Test
    public void testGetBuildNumberIsCached() throws Exception {
        String input = "build.major.number=12" + NL + "build.minor.number=34" + NL + "build.revision.number=56" + NL
                + "build.date=2007/08/09 10:11";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF8));
        BuildNumber instance = new BuildNumber(inputStream);
        String expected = "12.34.56 - 2007/08/09 10:11";
        assertEquals("Build number is wrong", expected, instance.getVersionNumber());
        assertSame(instance.getVersionNumber(), instance.getVersionNumber());
    }

    @Test
    public void testGetBuildNumberWithoutDate() throws Exception {
        String input = "build.major.number=12" + NL + "build.minor.number=34" + NL + "build.revision.number=56";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF8));
        BuildNumber instance = new BuildNumber(inputStream);
        String expected = "12.34.56";
        assertEquals("Build number is wrong", expected, instance.getVersionNumber());
    }

    @Test
    public void testGetBuildNumberWithoutFile() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("random.file");
        BuildNumber instance = new BuildNumber(inputStream);
        String expected = "00.00";
        assertEquals("Build number is wrong", expected, instance.getVersionNumber());
    }

    @Test
    public void testGetBuildNumberWithoutRevision() throws Exception {
        String input = "build.major.number=12" + NL + "build.minor.number=34" + NL + "" + NL
                + "build.date=2007/08/09 10:11";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF8));
        BuildNumber instance = new BuildNumber(inputStream);
        String expected = "12.34 - 2007/08/09 10:11";
        assertEquals("Build number is wrong", expected, instance.getVersionNumber());
    }

    @Test
    public void testGetBuildNumberWithoutRevisionAndDate() throws Exception {
        String input = "build.major.number=12" + NL + "build.minor.number=34";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF8));
        BuildNumber instance = new BuildNumber(inputStream);
        String expected = "12.34";
        assertEquals("Build number is wrong", expected, instance.getVersionNumber());
    }

}

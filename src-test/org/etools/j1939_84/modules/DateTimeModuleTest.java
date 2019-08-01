/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Before;
import org.junit.Test;

/**
 * The Unit tests for the {@link DateTimeModule}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DateTimeModuleTest {

    private DateTimeModule instance;

    @Before
    public void setUp() {
        instance = new DateTimeModule();
    }

    @Test
    public void testFormat() throws Exception {
        LocalDateTime dateTime = LocalDateTime.parse("2007-12-03T10:15:30.000");
        assertEquals("2007-12-03T10:15:30.000", instance.format(dateTime));

        LocalTime time = LocalTime.parse("10:15:30.000");
        assertEquals("10:15:30.000", instance.format(time));
    }

    @Test
    public void testGetDateTime() {
        DateTimeModule instance = new DateTimeModule() {
            @Override
            protected LocalDateTime now() {
                return LocalDateTime.parse("2007-12-03T10:15:30.000");
            }
        };
        assertEquals("2007-12-03T10:15:30.000", instance.getDateTime());
    }

    @Test
    public void testGetDateTimeFormatter() throws Exception {
        String expected = "10:15:30.000";
        LocalTime time = LocalTime.parse(expected);
        String actual = instance.getTimeFormatter().format(time);
        assertEquals(expected, actual);

        LocalDateTime dateTime = LocalDateTime.parse("2007-12-03T10:15:30.000");
        String actualDateTime = instance.getTimeFormatter().format(dateTime);
        assertEquals(expected, actualDateTime);
    }

    @Test
    public void testNow() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime actual = instance.now();
        LocalDateTime after = LocalDateTime.now();

        assertTrue(before.isBefore(actual) || before.isEqual(actual));
        assertTrue(after.isAfter(actual) || after.isEqual(actual));
    }

    @Test
    public void testParse() throws Exception {
        LocalDateTime expected = LocalDateTime.parse("2007-12-03T10:15:30.000");
        assertEquals(expected, instance.parse("2007-12-03T10:15:30.000"));

        LocalTime expectedTime = LocalTime.parse("10:15:30.000");
        assertEquals(expectedTime, instance.parse("10:15:30.000"));
    }
}

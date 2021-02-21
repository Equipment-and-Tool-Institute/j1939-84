/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.concurrent.TimeUnit;

/**
 * The Module responsible for the Date/Time
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DateTimeModule {
    private static DateTimeModule instance = new DateTimeModule();
    private DateTimeFormatter timeFormatter;

    protected DateTimeModule() {
    }

    public static DateTimeModule getInstance() {
        return instance;
    }

    /** Only used by tests. */
    public static void setInstance(DateTimeModule instance) {
        DateTimeModule.instance = instance == null ? new DateTimeModule() : instance;
    }

    /**
     *
     * @return Current date as a string.
     */
    public String getDate() {
        return now().format(DateTimeFormatter.ofPattern("yyy/MM/dd"));
    }

    /**
     * Returns the current time formatted as a {@link String}
     *
     * @return {@link String}
     */
    public String getTime() {
        return getTimeFormatter().format(now());
    }

    public long getTimeAsLong() {
        return TimeUnit.NANOSECONDS.toMillis(now().toLocalTime().toNanoOfDay());
    }

    /**
     * Returns the formatter used to format the time
     *
     * @return the {@link DateTimeFormatter}
     */
    public DateTimeFormatter getTimeFormatter() {
        if (timeFormatter == null) {
            timeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive()
                                                          .appendValue(HOUR_OF_DAY, 2)
                                                          .appendLiteral(':')
                                                          .appendValue(MINUTE_OF_HOUR, 2)
                                                          .optionalStart()
                                                          .appendLiteral(':')
                                                          .appendValue(SECOND_OF_MINUTE, 2)
                                                          .optionalStart()
                                                          .appendFraction(NANO_OF_SECOND, 4, 4, true)
                                                          .toFormatter();
        }
        return timeFormatter;
    }

    public int getYear() {
        return now().getYear();
    }

    /**
     * Returns the current date/time. This is exposed to it can be overridden
     * for testing.
     *
     * @return {@link LocalDateTime}
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }

    public void pauseFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
        }
    }

}

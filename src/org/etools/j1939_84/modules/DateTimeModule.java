/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.TimeUnit;

/**
 * The Module responsible for the Date/Time
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DateTimeModule {

    private DateTimeFormatter dateTimeformatter;

    private DateTimeFormatter timeFormatter;

    /**
     * Formats the given {@link TemporalAccessor} as a {@link String}
     *
     * @param time
     *             the {@link TemporalAccessor} to format
     * @return {@link String}
     * @throws DateTimeException
     *                           if an error occurs during formatting
     */
    public String format(TemporalAccessor time) throws DateTimeException {
        try {
            return formatDateTime(time);
        } catch (DateTimeException e) {
            return formatTime(time);
        }
    }

    /**
     * Formats the given {@link TemporalAccessor} as a {@link String} which
     * includes the Date and Time
     *
     * @param dateTime
     *                 the {@link TemporalAccessor} to format
     * @return {@link String}
     * @throws DateTimeException
     *                           if an error occurs during formatting
     */
    private String formatDateTime(TemporalAccessor dateTime) throws DateTimeException {
        return getDateTimeFormatter().format(dateTime);
    }

    /**
     * Formats the given {@link TemporalAccessor} as a {@link String} which
     * includes only the Time
     *
     * @param dateTime
     *                 the {@link TemporalAccessor} to format
     * @return {@link String}
     * @throws DateTimeException
     *                           if an error occurs during formatting
     */
    private String formatTime(TemporalAccessor time) throws DateTimeException {
        return getTimeFormatter().format(time);
    }

    /**
     * Returns the current date/time formatted as a {@link String}
     *
     * @return {@link String}
     */
    public String getDateTime() {
        return formatDateTime(now());
    }

    /**
     * Returns the formatter used to format the date/time
     *
     * @return {@link DateTimeFormatter}
     */
    private DateTimeFormatter getDateTimeFormatter() {
        if (dateTimeformatter == null) {
            // We really want this -> DateTimeFormatter.ISO_OFFSET_DATE_TIME but
            // it doesn't have a constant number of milliseconds
            dateTimeformatter = new DateTimeFormatterBuilder().parseCaseInsensitive()
                    .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(MONTH_OF_YEAR, 2)
                    .appendLiteral('-').appendValue(DAY_OF_MONTH, 2).appendLiteral('T').appendValue(HOUR_OF_DAY, 2)
                    .appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2).optionalStart().appendFraction(NANO_OF_SECOND, 3, 3, true)
                    .toFormatter();
        }
        return dateTimeformatter;
    }

    /**
     * Returns the current time formatted as a {@link String}
     *
     * @return {@link String}
     */
    public String getTime() {
        return formatTime(now());
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
            timeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendValue(HOUR_OF_DAY, 2)
                    .appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2).optionalStart().appendFraction(NANO_OF_SECOND, 3, 3, true)
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

    /**
     * Parsed a {@link String} to return a {@link TemporalAccessor}
     *
     * @param string
     *               the {@link String} to parse
     * @return {@link TemporalAccessor}
     */
    public TemporalAccessor parse(String string) {
        try {
            return LocalDateTime.from(getDateTimeFormatter().parse(string));
        } catch (DateTimeException e) {
            return LocalTime.from(getTimeFormatter().parse(string));
        }
    }

}

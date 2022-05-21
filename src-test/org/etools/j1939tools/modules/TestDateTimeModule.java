/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.modules;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * {@link DateTimeModule} that reports a fixed point in time for testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class TestDateTimeModule extends DateTimeModule {

    /**
     * The {@link DateTimeFormatter} used for testing that will return a static
     * value.
     */
    private final DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder().appendLiteral("10:15:30.0000")
                                                                                  .parseDefaulting(ChronoField.HOUR_OF_DAY,
                                                                                                   10)
                                                                                  .parseDefaulting(ChronoField.MINUTE_OF_HOUR,
                                                                                                   15)
                                                                                  .parseDefaulting(ChronoField.SECOND_OF_MINUTE,
                                                                                                   30)
                                                                                  .parseDefaulting(ChronoField.YEAR_OF_ERA,
                                                                                                   2016)
                                                                                  .parseDefaulting(ChronoField.DAY_OF_YEAR,
                                                                                                   1)
                                                                                  .toFormatter();
    private long timeAsLong = 0L;

    /**
     * Method returns the actual {@link DateTimeFormatter} used in production
     * code, not the test one.
     *
     * @return {@link DateTimeFormatter}
     */
    public DateTimeFormatter getSuperTimeFormatter() {
        return super.getTimeFormatter();
    }

    @Override
    public long getTimeAsLong() {
        return timeAsLong;
    }

    @Override
    public DateTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    @Override
    protected LocalDateTime now() {
        return LocalDateTime.parse("2007-12-03T10:15:30.0000");
    }

    @Override
    public void pauseFor(long milliseconds) {
        timeAsLong += milliseconds;
    }

}

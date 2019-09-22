/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class IndexGeneratorTest {

    private DateTimeModule dateTimeModule;

    private IndexGenerator instance;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        dateTimeModule = new DateTimeModule();
        instance = IndexGenerator.instance();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        assertEquals("", instance.index());
    }

}

/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui.widgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test for the {@link TextChangeListener}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TextChangeListenerTest {

    private TextChangeListener instance;

    private boolean textChangeCalled;

    @Before
    public void setUp() {
        instance = new TextChangeListener() {
            @Override
            public void textChanged() {
                textChangeCalled = true;
            }
        };
    }

    @Test
    public void testChangedUpdate() {
        instance.changedUpdate(null);
        assertFalse(textChangeCalled);
    }

    @Test
    public void testInsertUpdate() {
        instance.insertUpdate(null);
        assertTrue(textChangeCalled);
    }

    @Test
    public void testRemoveUpdate() {
        instance.removeUpdate(null);
        assertTrue(textChangeCalled);
    }

}

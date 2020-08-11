/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the {@link VehicleInformationDialog}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VehicleInformationDialogTest {

    private VehicleInformationDialog instance;

    @Mock
    private VehicleInformationPresenter presenter;

    @Before
    public void setUp() {
        instance = new VehicleInformationDialog(new JFrame(), presenter);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(presenter);
    }

    @Test
    public void testSetVisibleFalse() {
        instance.setVisible(false);
        verify(presenter).onDialogClosed();
    }

    @Test
    public void testSetVisibleTrue() {
        instance.setVisible(true);
        verify(presenter).initialize();
    }

}

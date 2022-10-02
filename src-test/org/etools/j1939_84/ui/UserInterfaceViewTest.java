/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.etools.j1939_84.BuildNumber;
import org.etools.j1939tools.bus.Adapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The class that unit tests the {@link UserInterfaceView}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UserInterfaceViewTest {

    private BuildNumber buildNumber = new BuildNumber();

    @Mock
    private UserInterfacePresenter controller;

    private UserInterfaceView instance;

    private Adapter adapter1;

    private Adapter adapter2;

    @Before
    public void setUp() throws Exception {
        List<Adapter> adapters = new ArrayList<>();
        adapters.add(adapter1 = new Adapter("Adapter1", "SD", (short) 1));
        adapters.add(adapter2 = new Adapter("Adapter2", "SD", (short) 2));
        when(controller.getAdapters()).thenReturn(adapters);

        instance = new UserInterfaceView(controller, r -> {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InvocationTargetException | InterruptedException e) {
                // nothing
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        verify(controller).getAdapters();
        verifyNoMoreInteractions(controller);
    }

    @Test
    public void testAdapterComboBox() {
        JComboBox<Adapter> adapterComboBox = instance.getAdapterComboBox();
        assertTrue(adapterComboBox.isEnabled());
        assertNull(adapterComboBox.getSelectedItem());
        assertEquals(2, adapterComboBox.getItemCount());
        assertEquals("Adapter1", adapterComboBox.getItemAt(0).getName());
        assertEquals("Adapter2", adapterComboBox.getItemAt(1).getName());

        adapterComboBox.setSelectedIndex(1);
        verify(controller).onAdapterComboBoxItemSelected(adapter2, "J1939:Baud=Auto");

        adapterComboBox.setSelectedIndex(0);
        verify(controller).onAdapterComboBoxItemSelected(adapter1, "J1939:Baud=Auto");

        instance.setAdapterComboBoxEnabled(false);
        assertFalse(adapterComboBox.isEnabled());
    }

    @Test
    public void testFileChooser() {
        JFileChooser fileChooser = instance.getFileChooser();
        assertEquals("Create Report File", fileChooser.getDialogTitle());
        FileFilter fileFilter = fileChooser.getFileFilter();
        assertTrue(fileFilter instanceof FileNameExtensionFilter);
        FileNameExtensionFilter filter = (FileNameExtensionFilter) fileFilter;
        assertEquals("J1939-84 Data Files", filter.getDescription());
        String[] extensions = filter.getExtensions();
        assertEquals(1, extensions.length);
        assertEquals("txt", extensions[0]);
    }

    /**
     * Verifies the title, that it's disabled by default, that it can be
     * enabled, and the onClick behavior
     */
    @Test
    public void testHelpButton() {
        JButton button = instance.getHelpButton();
        assertEquals("Help", button.getText());
        assertTrue(button.isEnabled());

        button.doClick();
        verify(controller).onHelpButtonClicked();
    }

    @Test
    public void testProgressBar() {
        JProgressBar progressBar = instance.getProgressBar();
        assertEquals("Select Vehicle Adapter", progressBar.getString());
        instance.setProgressBarValue(25, 250, 100);
        assertEquals(25, progressBar.getMinimum());
        assertEquals(100, progressBar.getValue());
        assertEquals(250, progressBar.getMaximum());
        instance.setProgressBarText("This is a test");
        assertEquals("This is a test", progressBar.getString());
    }

    /**
     * Verifies the title, that it's disabled by default, that it can be
     * enabled, and the onClick behavior
     */
    @Test
    public void testReadVehicleInfoButton() {
        JButton button = instance.getReadVehicleInfoButton();
        assertEquals("<html><center>Read</center><center>Vehicle</center><center>Info</center><html>",
                     button.getText());
        assertFalse(button.isEnabled());
        assertEquals("Queries the vehicle for VIN and Calibrations", button.getToolTipText());

        instance.setReadVehicleInfoButtonEnabled(true);
        assertTrue(button.isEnabled());

        button.doClick();
        verify(controller).onReadVehicleInfoButtonClicked();
    }

    @Test
    public void testReportTextArea() {
        JTextArea reportTextArea = instance.getReportTextArea();
        assertEquals("", reportTextArea.getText());
        instance.appendResults("This is a result");
        assertEquals("This is a result", reportTextArea.getText());
    }

    @Test
    public void testSelectFileButton() {
        JButton button = instance.getSelectFileButton();
        assertEquals("Select File...", button.getText());
        assertFalse(button.isEnabled());
        assertEquals("Select or create the file for the report", button.getToolTipText());

        instance.setSelectFileButtonEnabled(true);
        assertTrue(button.isEnabled());

        button.doClick();
        verify(controller).onSelectFileButtonClicked();

        instance.setSelectFileButtonEnabled(false);
        assertFalse(button.isEnabled());

        instance.setSelectFileButtonText("New Text");
        assertEquals("New Text", button.getText());

        instance.setSelectFileButtonText(null);
        assertEquals("Select File...", button.getText());
    }

    @Test
    public void testSetEngineCals() {
        String expected = "Engine Calibration";
        instance.setEngineCals(expected);
        assertEquals(expected, instance.getCalsTextField().getText());
    }

    @Test
    public void testSetVin() {
        String expected = "12345678901234567";
        instance.setVin(expected);
        assertEquals(expected, instance.getVinTextField().getText());
    }

    /**
     * Verifies the title, that it's disabled by default, can be enabled and the
     * onClick behavior
     */
    @Test
    public void testStopButton() {
        JButton button = instance.getStopButton();
        assertEquals("Cancel", button.getText());
        assertFalse(button.isEnabled());
        instance.setStopButtonEnabled(true);
        assertTrue(button.isEnabled());
        button.doClick();
        verify(controller).onStopButtonClicked();
    }

    @Test
    public void testTitle() {
        String title = instance.getFrame().getTitle();
        assertTrue(title.matches("J1939-84 Tool v[\\d\\.]+ - \\d{4}/\\d\\d/\\d\\d \\d\\d:\\d\\d"));
        assertEquals("J1939-84 Tool " + UserInterfaceView.getTitle(), title);
    }

}

/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.etools.j1939_84.TestExecutor;
import org.etools.j1939_84.controllers.OverallController;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.modules.ReportFileModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.Adapter;
import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.RP1210;
import org.etools.j1939tools.bus.RP1210Bus;
import org.etools.j1939tools.j1939.J1939;

/**
 * Unit testing the {@link UserInterfacePresenter}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class UserInterfacePresenterTest {

    private static final String path = "file\\location\\name.j1939tools-84";
    private final Adapter adapter1 = new Adapter("Adapter1", "SD", (short) 1);
    private final Adapter adapter2 = new Adapter("Adapter2", "SD", (short) 2);
    @Mock
    private VehicleInformationModule vehicleInformationModule;
    private TestExecutor executor;
    private UserInterfacePresenter instance;
    @Mock
    private OverallController overallController;
    @Mock
    private ReportFileModule reportFileModule;
    @Mock
    private RP1210 rp1210;
    @Mock
    private RP1210Bus rp1210Bus;
    @Mock
    private Runtime runtime;
    private Thread shutdownHook;
    @Mock
    private UserInterfaceContract.View view;
    @Mock
    J1939 j1939;

    private static File mockFile(boolean newFile) throws IOException {
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        when(file.getName()).thenReturn("name.txt");
        when(file.getAbsolutePath()).thenReturn(path);
        when(file.createNewFile()).thenReturn(newFile);
        return file;
    }

    @Before
    public void setUp() throws Exception {
        executor = new TestExecutor();
        List<Adapter> adapters = new ArrayList<>();
        adapters.add(adapter1);
        adapters.add(adapter2);
        when(rp1210.getAdapters()).thenReturn(adapters);
        // when(rp1210.setAdapter(any(), eq("J1939:Baud=Auto"), eq(0xF9))).thenReturn(rp1210Bus);

        instance = new UserInterfacePresenter(view,
                                              vehicleInformationModule,
                                              rp1210,
                                              reportFileModule,
                                              runtime,
                                              executor,
                                              overallController,
                                              j1939);
        ArgumentCaptor<Thread> captor = ArgumentCaptor.forClass(Thread.class);
        verify(runtime).addShutdownHook(captor.capture());
        shutdownHook = captor.getValue();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(reportFileModule,
                                 rp1210,
                                 rp1210Bus,
                                 runtime,
                                 vehicleInformationModule,
                                 view,
                                 j1939);
    }

    @Test
    public void testDisconnect() throws Exception {
        instance.onAdapterComboBoxItemSelected(adapter1, "J1939:Baud=Auto");
        executor.run();

        instance.disconnect();

        verify(vehicleInformationModule).reset();
        // verify(vehicleInformationModule).setJ1939(any(J1939.class));

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        // inOrder.verify(view).setAdapterComboBoxEnabled(false);
        // inOrder.verify(view).setSelectFileButtonEnabled(false);
        // inOrder.verify(view).setProgressBarText("Connecting to Adapter");
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Report File");

        // verify(rp1210Bus).stop();
        // verify(rp1210).getAdapters();
        // verify(rp1210).setAdapter(adapter1, "J1939:Baud=Auto", 0xF9);
    }

    @Test
    public void testDisconnectHandlesException() throws Exception {
        instance.onAdapterComboBoxItemSelected(adapter1, "J1939:Baud=Auto");
        executor.run();

        // Mockito.doThrow(new BusException("Testing")).when(rp1210Bus).stop();

        instance.disconnect();

        verify(vehicleInformationModule).reset();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Report File");
    }

    @Test
    public void testDisconnectWithNull() {
        instance.disconnect();
        // Nothing bad happens
    }

    @Test
    public void testGetAdapters() throws Exception {
        List<Adapter> adapters = instance.getAdapters();
        assertEquals(2, adapters.size());
        assertEquals("Adapter1", adapters.get(0).getName());
        assertEquals("Adapter2", adapters.get(1).getName());
        verify(rp1210).getAdapters();
    }

    @Test
    public void testGetAdaptersHandlesException() throws Exception {
        when(rp1210.getAdapters()).thenThrow(new BusException("Surprise", new Exception()));
        assertEquals(0, instance.getAdapters().size());

        verify(rp1210, times(2)).getAdapters();

        verify(view).displayDialog("The List of Communication Adapters could not be loaded.",
                                   "Failure",
                                   JOptionPane.ERROR_MESSAGE,
                                   false);

        // Doesn't happen again
        assertEquals(0, instance.getAdapters().size());
    }

    @Test
    public void testGetReportFileModule() {
        assertSame(reportFileModule, instance.getReportFileModule());
    }

    @Test
    public void testOnAdapterComboBoxItemSelectedWithFile() throws Exception {
        File file = File.createTempFile("test", ".j1939tools-84");
        instance.setReportFile(file);

        instance.onAdapterComboBoxItemSelected(adapter1, "J1939:Baud=Auto");
        executor.run();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        // inOrder.verify(view).setAdapterComboBoxEnabled(false);
        // inOrder.verify(view).setSelectFileButtonEnabled(false);
        // inOrder.verify(view).setProgressBarText("Connecting to Adapter");
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Push Read Vehicle Info Button");
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(true);

        verify(vehicleInformationModule).reset();
        // verify(vehicleInformationModule).setJ1939(any(J1939.class));

        verify(reportFileModule).setReportFile(eq(file));

        // verify(rp1210).getAdapters();
        // verify(rp1210).setAdapter(adapter1, "J1939:Baud=Auto", 0xF9);
    }

    @Test
    public void testOnAdapterComboBoxItemSelectedWithNoFile() throws Exception {
        instance.setReportFile(null);

        instance.onAdapterComboBoxItemSelected(adapter1, "J1939:Baud=Auto");
        executor.run();

        assertEquals("Adapter1", instance.getSelectedAdapter().getName());

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        // inOrder.verify(view).setAdapterComboBoxEnabled(false);
        // inOrder.verify(view).setSelectFileButtonEnabled(false);
        // inOrder.verify(view).setProgressBarText("Connecting to Adapter");
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Report File");

        verify(vehicleInformationModule).reset();
        // verify(vehicleInformationModule).setJ1939(any(J1939.class));
        verify(reportFileModule).setReportFile(eq(null));

        // verify(rp1210).getAdapters();
        // verify(rp1210).setAdapter(adapter1, "J1939:Baud=Auto", 0xF9);
    }

    @Test
    public void testOnFileChosenExistingFileWithProblem() throws Exception {
        File file = mockFile(true);

        Mockito.doThrow(new IOException("There was a failure"))
               .when(reportFileModule)
               .setReportFile(eq(file));

        instance.onFileChosen(file);
        executor.run();

        assertNull(instance.getReportFile());
        verify(view).displayDialog("File cannot be used." + NL + "There was a failure" + NL
                + "Please select a different file.",
                                   "File Error",
                                   JOptionPane.ERROR_MESSAGE,
                                   false);

        verify(vehicleInformationModule).reset();

        InOrder inOrder = inOrder(view, reportFileModule);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(false);
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setProgressBarText("Scanning Report File");
        inOrder.verify(reportFileModule).setReportFile(eq(file));
        inOrder.verify(view).setSelectFileButtonText(null);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Vehicle Adapter");
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
    }

    @Test
    public void testOnFileChosenNewFileWithExtensionCreated() throws Exception {
        File file = mockFile(true);

        instance.onFileChosen(file);
        executor.run();

        File reportFile = instance.getReportFile();
        assertNotNull(reportFile);
        assertEquals(path, reportFile.getAbsolutePath());
        verify(view).setSelectFileButtonText(path);

        verify(reportFileModule).setReportFile(eq(file));

        verify(vehicleInformationModule).reset();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(false);
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setProgressBarText("Scanning Report File");
        inOrder.verify(view).setSelectFileButtonText(file.getAbsolutePath());
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Vehicle Adapter");
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
    }

    @Test
    public void testOnFileChosenNewFileWithExtensionNotCreated() throws Exception {
        File file = mockFile(false);

        instance.onFileChosen(file);
        executor.run();

        assertNull(instance.getReportFile());
        verify(view).displayDialog("File cannot be used." + NL + "File cannot be created" + NL
                + "Please select a different file.",
                                   "File Error",
                                   JOptionPane.ERROR_MESSAGE,
                                   false);

        verify(vehicleInformationModule).reset();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(false);
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setProgressBarText("Scanning Report File");
        inOrder.verify(view).setSelectFileButtonText(null);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Vehicle Adapter");
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
    }

    @Test
    public void testOnFileChosenNewFileWithoutExtensionCreated() throws Exception {
        File tempFile = File.createTempFile("testing", "");
        assertTrue(tempFile.delete());
        File file = new File(tempFile.getAbsolutePath());

        instance.onFileChosen(file);
        executor.run();

        File reportFile = instance.getReportFile();
        assertNotNull(reportFile);
        assertTrue(reportFile.getAbsolutePath().endsWith(tempFile.getName() + ".txt"));

        verify(reportFileModule).setReportFile(eq(reportFile));

        verify(vehicleInformationModule).reset();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(false);
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setProgressBarText("Scanning Report File");
        inOrder.verify(view).setSelectFileButtonText(reportFile.getAbsolutePath());
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Vehicle Adapter");
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
    }

    @Test
    public void testOnFileChosenWithAdapter() throws Exception {
        instance.onAdapterComboBoxItemSelected(adapter1, "J1939:Baud=Auto");
        executor.run();

        // verify(rp1210).setAdapter(adapter1, "J1939:Baud=Auto", 0xF9);
        verify(vehicleInformationModule).reset();

        InOrder inOrder1 = inOrder(view);
        inOrder1.verify(view).setVin("");
        inOrder1.verify(view).setEngineCals("");
        inOrder1.verify(view).setStartButtonEnabled(false);
        inOrder1.verify(view).setStopButtonEnabled(false);
        inOrder1.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder1.verify(view).setAdapterComboBoxEnabled(true);
        inOrder1.verify(view).setSelectFileButtonEnabled(true);
        // inOrder1.verify(view).setReadVehicleInfoButtonEnabled(false);
        // inOrder1.verify(view).setAdapterComboBoxEnabled(false);
        // inOrder1.verify(view).setSelectFileButtonEnabled(false);
        // inOrder1.verify(view).setProgressBarText("Connecting to Adapter");
        // inOrder1.verify(view).setAdapterComboBoxEnabled(true);
        // inOrder1.verify(view).setSelectFileButtonEnabled(true);
        inOrder1.verify(view).setProgressBarText("Select Report File");

        File file = mockFile(true);
        instance.onFileChosen(file);
        executor.run();

        verify(reportFileModule).setReportFile(eq(file));

        assertSame(file, instance.getReportFile());

        verify(vehicleInformationModule, times(2)).reset();

        InOrder inOrder2 = inOrder(view);
        inOrder2.verify(view, times(2)).setVin("");
        inOrder2.verify(view).setEngineCals("");
        inOrder2.verify(view).setStartButtonEnabled(false);
        inOrder2.verify(view).setStopButtonEnabled(false);
        inOrder2.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder2.verify(view).setAdapterComboBoxEnabled(false);
        inOrder2.verify(view).setSelectFileButtonEnabled(false);
        inOrder2.verify(view).setProgressBarText("Scanning Report File");
        inOrder2.verify(view).setSelectFileButtonText(file.getAbsolutePath());
        inOrder2.verify(view).setAdapterComboBoxEnabled(true);
        inOrder2.verify(view).setSelectFileButtonEnabled(true);
        inOrder2.verify(view).setProgressBarText("Push Read Vehicle Info Button");
        inOrder2.verify(view).setReadVehicleInfoButtonEnabled(true);
        inOrder2.verify(view).setAdapterComboBoxEnabled(true);
        inOrder2.verify(view).setSelectFileButtonEnabled(true);
    }

    @Test
    public void testOnFileChosenWithNoAdapter() throws Exception {
        File file = mockFile(true);
        instance.onFileChosen(file);
        executor.run();

        verify(reportFileModule).setReportFile(eq(file));

        assertSame(file, instance.getReportFile());

        verify(vehicleInformationModule).reset();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(false);
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setProgressBarText("Scanning Report File");
        inOrder.verify(view).setSelectFileButtonText(file.getAbsolutePath());
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
        inOrder.verify(view).setProgressBarText("Select Vehicle Adapter");
        inOrder.verify(view).setSelectFileButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(true);
        inOrder.verify(view).setSelectFileButtonEnabled(true);
    }

    @Test
    public void testOnHelpButtonClicked() {
        // just verify no exception
        instance.onHelpButtonClicked();
    }

//    @Test
    public void testOnReadVehicleInfoButtonClickedWithNullCals() throws Exception {
        when(vehicleInformationModule.getVin()).thenReturn("12345678901234567890");
        when(vehicleInformationModule.getCalibrationsAsString()).thenThrow(new IOException("Cals not read"));

        var bus = mock(Bus.class);
        when(bus.imposterDetected()).thenReturn(false);
        instance.setBus(bus);
        instance.onReadVehicleInfoButtonClicked();
        executor.run();

        verify(vehicleInformationModule, times(2)).setJ1939(any());
//        verify(vehicleInformationModule).getVin();
//        verify(vehicleInformationModule).getCalibrationsAsString();
        verify(vehicleInformationModule).reset();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(false);
        inOrder.verify(view).setSelectFileButtonEnabled(false);
//        inOrder.verify(view).setProgressBarValue(0, 3, 1);
//        inOrder.verify(view).setProgressBarText("Reading Vehicle Identification Number");
//        inOrder.verify(view).setVin("12345678901234567890");
//        inOrder.verify(view).setProgressBarValue(0, 3, 2);
//        inOrder.verify(view).setProgressBarText("Reading Vehicle Calibrations");
//        inOrder.verify(view).setProgressBarValue(0, 3, 3);
//        inOrder.verify(view).setProgressBarText("Cals not read");
//        inOrder.verify(view).displayDialog("Cals not read", "Communications Error", JOptionPane.ERROR_MESSAGE, false);
//        inOrder.verify(view).setStartButtonEnabled(false);
//        inOrder.verify(view).setStopButtonEnabled(false);
//        inOrder.verify(view).setReadVehicleInfoButtonEnabled(true);
//        inOrder.verify(view).setAdapterComboBoxEnabled(true);
//        inOrder.verify(view).setSelectFileButtonEnabled(true);
    }

//    @Test
    public void testOnReadVehicleInfoButtonClickedWithNullVin() throws Exception {
        when(vehicleInformationModule.getVin()).thenThrow(new IOException("VIN not read"));

        var bus = mock(Bus.class);
        when(bus.imposterDetected()).thenReturn(false);
        instance.setBus(bus);
        instance.onReadVehicleInfoButtonClicked();
        executor.run();

        verify(vehicleInformationModule, times(2)).setJ1939(any());
//        verify(vehicleInformationModule).getVin();
        verify(vehicleInformationModule).reset();
        verify(view).setVin("");
        verify(view).setEngineCals("");
        verify(view).setReadVehicleInfoButtonEnabled(false);
        verify(view, times(2)).setStartButtonEnabled(false);
        verify(view, times(2)).setStopButtonEnabled(false);
        verify(view).setAdapterComboBoxEnabled(false);
        verify(view).setSelectFileButtonEnabled(false);

//        verify(view).setProgressBarValue(0, 3, 1);
//        verify(view).setProgressBarText("Reading Vehicle Identification Number");
        verify(view).setProgressBarValue(0, 3, 3);
//        verify(view).setProgressBarText("VIN not read");
//        verify(view).displayDialog("VIN not read", "Communications Error", JOptionPane.ERROR_MESSAGE, false);

        verify(view).setReadVehicleInfoButtonEnabled(true);
        verify(view).setAdapterComboBoxEnabled(true);
        verify(view).setSelectFileButtonEnabled(true);
    }

//    @Test
    public void testOnReadVehicleInfoButtonClickedWithImposter() throws Exception {
        var bus = mock(Bus.class);
        when(bus.imposterDetected()).thenReturn(true);

        instance.setBus(bus);
        instance.onReadVehicleInfoButtonClicked();
        executor.run();

        verify(vehicleInformationModule, times(2)).setJ1939(any());
        verify(vehicleInformationModule).reset();
        verify(view).setVin("");
        verify(view).setEngineCals("");
        verify(view).setReadVehicleInfoButtonEnabled(false);
        verify(view, times(2)).setStartButtonEnabled(false);
        verify(view, times(2)).setStopButtonEnabled(false);
        verify(view).setAdapterComboBoxEnabled(false);
        verify(view).setSelectFileButtonEnabled(false);
        verify(view).setProgressBarValue(0, 3, 3);
        String text = "Unexpected Service Tool Message from SA 0xF9 observed. " + NL +
                "Please disconnect the other ECU using SA 0xF9." + NL +
                "The application must be restarted in order to continue.";
//        verify(view).setProgressBarText(text);
//        verify(view).displayDialog(text, "Communications Error", JOptionPane.ERROR_MESSAGE, false);

        verify(view).setReadVehicleInfoButtonEnabled(true);
        verify(view).setAdapterComboBoxEnabled(true);
        verify(view).setSelectFileButtonEnabled(true);
    }

//    @Test
    public void testOnReadVehicleInfoButtonClickedWithReportFileMatched() throws Exception {
        when(vehicleInformationModule.getVin()).thenReturn("12345678901234567890");
        when(vehicleInformationModule.getCalibrationsAsString()).thenReturn("Engine Cals");

        var bus = mock(Bus.class);
        when(bus.imposterDetected()).thenReturn(false);
        instance.setBus(bus);
        instance.onReadVehicleInfoButtonClicked();
        executor.run();

//        assertEquals("12345678901234567890", instance.getVin());

//        verify(vehicleInformationModule).setJ1939(any());
        verify(vehicleInformationModule).reset();
//        verify(vehicleInformationModule).getVin();
//        verify(vehicleInformationModule).getCalibrationsAsString();

        InOrder inOrder = inOrder(view);
        inOrder.verify(view).setVin("");
        inOrder.verify(view).setEngineCals("");
        inOrder.verify(view).setStartButtonEnabled(false);
        inOrder.verify(view).setStopButtonEnabled(false);
        inOrder.verify(view).setReadVehicleInfoButtonEnabled(false);
        inOrder.verify(view).setAdapterComboBoxEnabled(false);
        inOrder.verify(view).setSelectFileButtonEnabled(false);
//        inOrder.verify(view).setProgressBarValue(0, 3, 1);
//        inOrder.verify(view).setProgressBarText("Reading Vehicle Identification Number");
//        inOrder.verify(view).setVin("12345678901234567890");
//        inOrder.verify(view).setProgressBarValue(0, 3, 2);
//        inOrder.verify(view).setProgressBarText("Reading Vehicle Calibrations");
//        inOrder.verify(view).setEngineCals("Engine Cals");
//        inOrder.verify(view).setProgressBarValue(0, 3, 3);
//        inOrder.verify(view).setProgressBarText("Complete");
//        inOrder.verify(view).setProgressBarText("Push Start Button");
//        inOrder.verify(view).setStartButtonEnabled(true);
//        inOrder.verify(view).setStopButtonEnabled(false);
//        inOrder.verify(view).setReadVehicleInfoButtonEnabled(true);
//        inOrder.verify(view).setAdapterComboBoxEnabled(true);
//        inOrder.verify(view).setSelectFileButtonEnabled(true);
    }

    @Test
    public void testOnSelectFileButtonClicked() {
        instance.onSelectFileButtonClicked();
        verify(view).displayFileChooser();
    }

    @Test
    public void testOnStartButtonClicked() {
        instance.onStartButtonClicked();
        verify(overallController).execute(any(ResultsListener.class), any(), eq(reportFileModule));
        verify(view).setStartButtonEnabled(false);
        verify(view).setReadVehicleInfoButtonEnabled(false);
        verify(view).setSelectFileButtonEnabled(false);
        verify(view).setStopButtonEnabled(true);
        verify(view).setAdapterComboBoxEnabled(false);
        verify(reportFileModule).setJ1939(any());
    }

    @Test
    public void testOnStopButtonClicked() {
        when(overallController.isActive()).thenReturn(true);

        instance.onStopButtonClicked();

        verify(overallController).stop();
        verify(view).setProgressBarText("User cancelled operation");
        verify(view, times(2)).setStopButtonEnabled(false);
    }

    @Test
    public void testOnStopButtonClickedWithNoController() {
        instance.onStopButtonClicked();

        verify(view).setProgressBarText("User cancelled operation");
        verify(view, times(2)).setStopButtonEnabled(false);
    }

    @Test
    public void testOnStopButtonClickedWithStoppedController() {
        when(overallController.isActive()).thenReturn(false);

        instance.onStopButtonClicked();

        verify(overallController, never()).stop();
        verify(view).setProgressBarText("User cancelled operation");
        verify(view, times(2)).setStopButtonEnabled(false);
    }

    @SuppressFBWarnings(value = "RU_INVOKE_RUN", justification = "Run is correct here for testing")
    @Test
    public void testShutdownHook() throws InterruptedException {
        assertEquals("Shutdown Hook Thread", shutdownHook.getName());
        shutdownHook.start();
        Thread.sleep(100);
        verify(reportFileModule).onProgramExit();
    }

}

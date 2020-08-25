/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VehicleInformationPresenterTest {

    @Mock
    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    private VehicleInformationPresenter instance;

    @Mock
    private J1939 j1939;

    @Mock
    private VehicleInformationListener listener;
    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Mock
    private VehicleInformationContract.View view;

    @Mock
    private VinDecoder vinDecoder;

    @Before
    public void setUp() {
        instance = new VehicleInformationPresenter(view,
                listener,
                j1939,
                dateTimeModule,
                vehicleInformationModule,
                diagnosticReadinessModule,
                vinDecoder);
        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(view, listener, j1939, dateTimeModule, vehicleInformationModule, vinDecoder);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testInitialize() throws IOException {
        when(vehicleInformationModule.getVin()).thenReturn("vin");
        when(vinDecoder.getModelYear("vin")).thenReturn(2);
        when(vinDecoder.isModelYearValid(2)).thenReturn(true);
        when(vehicleInformationModule.reportAddressClaim(ResultsListener.NOOP)).thenReturn(RequestResult.empty());
        when(vehicleInformationModule.getEngineModelYear()).thenReturn(4);
        when(vehicleInformationModule.getEngineFamilyName()).thenReturn("family");

        instance.initialize();

        verify(vehicleInformationModule).getVin();
        verify(vinDecoder).getModelYear("vin");
        verify(vinDecoder).isModelYearValid(2);
        verify(view).setFuelType(FuelType.DSL);
        verify(view).setNumberOfTripsForFaultBImplant(1);
        verify(view).setEmissionUnits(0);
        verify(view).setVin("vin");
        verify(view).setVehicleModelYear(2);
        verify(view).setCalIds(0);
        verify(vehicleInformationModule).getEngineModelYear();
        verify(view).setEngineModelYear(4);
        verify(vehicleInformationModule).reportAddressClaim(ArgumentMatchers.any());
        verify(vehicleInformationModule).reportCalibrationInformation(ArgumentMatchers.any());
        verify(vehicleInformationModule).getEngineFamilyName();
        verify(view).setCertificationIntent("family");
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testInitializeWithError() throws IOException {
        when(vehicleInformationModule.getVin()).thenThrow(new IOException());
        when(vinDecoder.getModelYear(null)).thenReturn(-1);
        when(vinDecoder.isModelYearValid(-1)).thenReturn(false);
        when(dateTimeModule.getYear()).thenReturn(500);
        when(vehicleInformationModule.reportAddressClaim(ResultsListener.NOOP)).thenReturn(RequestResult.empty());
        when(vehicleInformationModule.getEngineModelYear()).thenThrow(new IOException());
        when(vehicleInformationModule.getEngineFamilyName()).thenThrow(new IOException());

        instance.initialize();

        verify(vehicleInformationModule).getVin();
        verify(vinDecoder).getModelYear(null);
        verify(vinDecoder).isModelYearValid(-1);
        verify(view).setFuelType(FuelType.DSL);
        verify(view).setNumberOfTripsForFaultBImplant(1);
        verify(view).setEmissionUnits(0);
        verify(view).setCalIds(0);
        verify(dateTimeModule).getYear();
        verify(view).setVehicleModelYear(500);
        verify(vehicleInformationModule).reportAddressClaim(ArgumentMatchers.any());
        verify(vehicleInformationModule).reportCalibrationInformation(ArgumentMatchers.any());
        verify(vehicleInformationModule).getEngineModelYear();
        verify(vehicleInformationModule).getEngineFamilyName();
    }

    @Test
    public void testOnCancelButtonClicked() {
        instance.onCancelButtonClicked();
        verify(view).setVisible(false);
    }

    @Test
    public void testOnDialogClosed() {
        instance.onDialogClosed();
        verify(listener).onResult(null);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testValidateInvalidCertification() {
        when(vinDecoder.isVinValid("vin")).thenReturn(true);
        when(vinDecoder.getModelYear("vin")).thenReturn(2);
        when(vinDecoder.isModelYearValid(1)).thenReturn(true);

        instance.onCertificationChanged("    ");
        instance.onEmissionUnitsChanged(1);
        instance.onEngineModelYearChanged(1);
        instance.onFuelTypeChanged(FuelType.DSL);
        instance.onVehicleModelYearChanged(2);
        instance.onVinChanged("vin");

        verify(vinDecoder, times(5)).isVinValid(null);
        verify(vinDecoder, times(1)).isVinValid("vin");
        verify(vinDecoder, times(5)).getModelYear(null);
        verify(vinDecoder, times(1)).getModelYear("vin");
        verify(vinDecoder, times(2)).isModelYearValid(0);
        verify(vinDecoder, times(4)).isModelYearValid(1);

        verify(view, times(5)).setVinValid(false);
        verify(view, times(1)).setVinValid(true);
        verify(view, times(1)).setVehicleModelYearValid(false);
        verify(view, times(5)).setVehicleModelYearValid(true);
        verify(view, times(6)).setOkButtonEnabled(false);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testValidateInvalidEmissionsCount() {
        when(vinDecoder.isVinValid("vin")).thenReturn(true);
        when(vinDecoder.getModelYear("vin")).thenReturn(2);
        when(vinDecoder.isModelYearValid(1)).thenReturn(true);

        instance.onCertificationChanged("cert");
        instance.onEmissionUnitsChanged(0);
        instance.onEngineModelYearChanged(1);
        instance.onFuelTypeChanged(FuelType.DSL);
        instance.onVehicleModelYearChanged(2);
        instance.onVinChanged("vin");

        verify(vinDecoder, times(5)).isVinValid(null);
        verify(vinDecoder, times(1)).isVinValid("vin");
        verify(vinDecoder, times(5)).getModelYear(null);
        verify(vinDecoder, times(1)).getModelYear("vin");
        verify(vinDecoder, times(2)).isModelYearValid(0);
        verify(vinDecoder, times(4)).isModelYearValid(1);

        verify(view, times(5)).setVinValid(false);
        verify(view, times(1)).setVinValid(true);
        verify(view, times(1)).setVehicleModelYearValid(false);
        verify(view, times(5)).setVehicleModelYearValid(true);
        verify(view, times(6)).setOkButtonEnabled(false);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testValidateInvalidEngineModelYear() {
        when(vinDecoder.isVinValid("vin")).thenReturn(true);
        when(vinDecoder.getModelYear("vin")).thenReturn(2);
        when(vinDecoder.isModelYearValid(1)).thenReturn(false);

        instance.onCertificationChanged("cert");
        instance.onEmissionUnitsChanged(1);
        instance.onEngineModelYearChanged(1);
        instance.onFuelTypeChanged(FuelType.DSL);
        instance.onVehicleModelYearChanged(2);
        instance.onVinChanged("vin");

        verify(vinDecoder, times(5)).isVinValid(null);
        verify(vinDecoder, times(1)).isVinValid("vin");
        verify(vinDecoder, times(5)).getModelYear(null);
        verify(vinDecoder, times(1)).getModelYear("vin");
        verify(vinDecoder, times(2)).isModelYearValid(0);
        verify(vinDecoder, times(4)).isModelYearValid(1);

        verify(view, times(5)).setVinValid(false);
        verify(view, times(1)).setVinValid(true);
        verify(view, times(1)).setVehicleModelYearValid(false);
        verify(view, times(5)).setVehicleModelYearValid(true);
        verify(view, times(6)).setOkButtonEnabled(false);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testValidateInvalidFuelType() {
        when(vinDecoder.isVinValid("vin")).thenReturn(true);
        when(vinDecoder.getModelYear("vin")).thenReturn(2);
        when(vinDecoder.isModelYearValid(1)).thenReturn(true);

        instance.onCertificationChanged("cert");
        instance.onEmissionUnitsChanged(1);
        instance.onEngineModelYearChanged(1);
        instance.onFuelTypeChanged(null);
        instance.onVehicleModelYearChanged(2);
        instance.onVinChanged("vin");

        verify(vinDecoder, times(5)).isVinValid(null);
        verify(vinDecoder, times(1)).isVinValid("vin");
        verify(vinDecoder, times(5)).getModelYear(null);
        verify(vinDecoder, times(1)).getModelYear("vin");
        verify(vinDecoder, times(2)).isModelYearValid(0);
        verify(vinDecoder, times(4)).isModelYearValid(1);

        verify(view, times(5)).setVinValid(false);
        verify(view, times(1)).setVinValid(true);
        verify(view, times(1)).setVehicleModelYearValid(false);
        verify(view, times(5)).setVehicleModelYearValid(true);
        verify(view, times(6)).setOkButtonEnabled(false);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testValidateInvalidVehicleModelYear() {
        when(vinDecoder.isVinValid("vin")).thenReturn(true);
        when(vinDecoder.getModelYear("vin")).thenReturn(3);
        when(vinDecoder.isModelYearValid(1)).thenReturn(true);

        instance.onCertificationChanged("cert");
        instance.onEmissionUnitsChanged(1);
        instance.onEngineModelYearChanged(1);
        instance.onFuelTypeChanged(FuelType.DSL);
        instance.onVehicleModelYearChanged(2);
        instance.onVinChanged("vin");

        verify(vinDecoder, times(5)).isVinValid(null);
        verify(vinDecoder, times(1)).isVinValid("vin");
        verify(vinDecoder, times(5)).getModelYear(null);
        verify(vinDecoder, times(1)).getModelYear("vin");
        verify(vinDecoder, times(2)).isModelYearValid(0);
        verify(vinDecoder, times(4)).isModelYearValid(1);

        verify(view, times(5)).setVinValid(false);
        verify(view, times(1)).setVinValid(true);
        verify(view, times(2)).setVehicleModelYearValid(false);
        verify(view, times(4)).setVehicleModelYearValid(true);
        verify(view, times(6)).setOkButtonEnabled(false);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testValidateInvalidVin() {
        when(vinDecoder.isVinValid("vin")).thenReturn(false);
        when(vinDecoder.getModelYear("vin")).thenReturn(2);
        when(vinDecoder.isModelYearValid(1)).thenReturn(true);

        instance.onCertificationChanged("cert");
        instance.onEmissionUnitsChanged(1);
        instance.onEngineModelYearChanged(1);
        instance.onFuelTypeChanged(FuelType.DSL);
        instance.onVehicleModelYearChanged(2);
        instance.onVinChanged("vin");

        verify(vinDecoder, times(5)).isVinValid(null);
        verify(vinDecoder, times(1)).isVinValid("vin");
        verify(vinDecoder, times(5)).getModelYear(null);
        verify(vinDecoder, times(1)).getModelYear("vin");
        verify(vinDecoder, times(2)).isModelYearValid(0);
        verify(vinDecoder, times(4)).isModelYearValid(1);

        verify(view, times(6)).setVinValid(false);
        verify(view, times(1)).setVehicleModelYearValid(false);
        verify(view, times(5)).setVehicleModelYearValid(true);
        verify(view, times(6)).setOkButtonEnabled(false);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testValidateOkClickedAndDialogClosed() {
        when(vinDecoder.isVinValid("vin")).thenReturn(true);
        when(vinDecoder.getModelYear("vin")).thenReturn(2);
        when(vinDecoder.isModelYearValid(1)).thenReturn(true);

        instance.onCertificationChanged("cert");
        instance.onEmissionUnitsChanged(4);
        instance.onCalIdsChanged(6);
        instance.onNumberOfTripsForFaultBImplantChanged(1);
        instance.onEngineModelYearChanged(1);
        instance.onFuelTypeChanged(FuelType.DSL);
        instance.onVehicleModelYearChanged(2);
        instance.onVinChanged("vin");

        verify(vinDecoder, times(7)).isVinValid(null);
        verify(vinDecoder, times(1)).isVinValid("vin");
        verify(vinDecoder, times(7)).getModelYear(null);
        verify(vinDecoder, times(1)).getModelYear("vin");
        verify(vinDecoder, times(4)).isModelYearValid(0);
        verify(vinDecoder, times(4)).isModelYearValid(1);

        verify(view, times(7)).setVinValid(false);
        verify(view, times(1)).setVinValid(true);
        verify(view, times(1)).setVehicleModelYearValid(false);
        verify(view, times(7)).setVehicleModelYearValid(true);
        verify(view, times(7)).setOkButtonEnabled(false);
        verify(view, times(1)).setOkButtonEnabled(true);
        // verify(view).setCalIds(0);
        // verify(vehicleInformationModule).reportCalibrationInformation(ArgumentMatchers.any());

        instance.onOkButtonClicked();
        verify(view).setVisible(false);

        instance.onDialogClosed();

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setCertificationIntent("cert");
        vehicleInformation.setEmissionUnits(4);
        vehicleInformation.setCalIds(6);
        vehicleInformation.setNumberOfTripsForFaultBImplant(1);
        vehicleInformation.setEngineModelYear(1);
        vehicleInformation.setFuelType(FuelType.DSL);
        vehicleInformation.setVehicleModelYear(2);
        vehicleInformation.setVin("vin");

        verify(listener).onResult(vehicleInformation);
    }

}

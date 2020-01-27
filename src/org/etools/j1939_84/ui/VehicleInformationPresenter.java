/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;

/**
 * The Presenter which controls the logic in the
 * {@link VehicleInformationDialog}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleInformationPresenter implements VehicleInformationContract.Presenter {

    /**
     * The value the user has entered for the certification intent
     */
    private String certificationIntent;

    /**
     * The module used to retrieve the Date/Time
     */
    private final DateTimeModule dateTimeModule;

    /**
     * The value the user has entered for the number of emissions units on the
     * vehicle
     */
    private int emissionUnits;

    /**
     * The value the user has entered for the engine model year
     */
    private int engineModelYear;

    /**
     * The value the user has entered for the Fuel Type
     */
    private FuelType fuelType;

    /**
     * The listener that will be notified when the user is done
     */
    private final VehicleInformationListener listener;

    /**
     * The VehicleInformation that will be returned to the listener
     */
    private VehicleInformation vehicleInformation;

    /**
     * The module used to gather information about the connected vehicle
     */
    private final VehicleInformationModule vehicleInformationModule;

    /**
     * The vehicle model year the user has entered
     */
    private int vehicleModelYear;

    /**
     * The View that's being controlled
     */
    private final VehicleInformationContract.View view;

    /**
     * The VIN the user has entered
     */
    private String vin;

    /**
     * The decoder used to determine if the VIN is valid
     */
    private final VinDecoder vinDecoder;

    /**
     * Constructor
     *
     * @param view     the View to be controlled
     * @param listener the {@link VehicleInformationListener} that will be given the
     *                 {@link VehicleInformation}
     * @param j1939    the vehicle bus
     */
    public VehicleInformationPresenter(VehicleInformationContract.View view, VehicleInformationListener listener,
            J1939 j1939) {
        this(view, listener, j1939, new DateTimeModule(), new VehicleInformationModule(), new VinDecoder());
    }

    /**
     * Constructor exposed for testing
     *
     * @param view                     the View to be controlled
     * @param listener                 the {@link VehicleInformationListener} that
     *                                 will be given the {@link VehicleInformation}
     * @param dateTimeModule           the {@link DateTimeModule}
     * @param vehicleInformationModule the {@link VehicleInformationModule}
     * @param vinDecoder               the {@link VinDecoder}
     * @param j1939                    the vehicle interface
     */
    public VehicleInformationPresenter(VehicleInformationContract.View view, VehicleInformationListener listener,
            J1939 j1939, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            VinDecoder vinDecoder) {
        this.view = view;
        this.listener = listener;
        this.dateTimeModule = dateTimeModule;
        this.vehicleInformationModule = vehicleInformationModule;
        this.vehicleInformationModule.setJ1939(j1939);
        this.vinDecoder = vinDecoder;
    }

    @Override
    public void initialize() {
        view.setFuelType(FuelType.DSL); // Assuming this used mostly on Diesel engines
        view.setEmissionUnits(1); // Assuming there's usually 1 Emission Unit

        try {
            vin = vehicleInformationModule.getVin();
            view.setVin(vin);
        } catch (Exception e) {
            // Don't care
        }

        int modelYear = vinDecoder.getModelYear(vin);
        if (!vinDecoder.isModelYearValid(modelYear)) {
            modelYear = dateTimeModule.getYear();
        }
        view.setVehicleModelYear(modelYear);

        try {
            engineModelYear = vehicleInformationModule.getEngineModelYear();
            view.setEngineModelYear(engineModelYear);
        } catch (Exception e) {
            // Don't care
        }

        try {
            certificationIntent = vehicleInformationModule.getEngineFamilyName();
            view.setCertificationIntent(certificationIntent);
        } catch (Exception e) {
            // Don't care
        }
    }

    @Override
    public void onCancelButtonClicked() {
        view.setVisible(false);
    }

    @Override
    public void onCertificationChanged(String certification) {
        certificationIntent = certification;
        validate();
    }

    @Override
    public void onDialogClosed() {
        listener.onResult(vehicleInformation);
    }

    @Override
    public void onEmissionUnitsChanged(int count) {
        emissionUnits = count;
        validate();
    }

    @Override
    public void onEngineModelYearChanged(int modelYear) {
        engineModelYear = modelYear;
        validate();
    }

    @Override
    public void onFuelTypeChanged(FuelType fuelType) {
        this.fuelType = fuelType;
        validate();
    }

    @Override
    public void onOkButtonClicked() {
        vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(vehicleModelYear);
        vehicleInformation.setEngineModelYear(engineModelYear);
        vehicleInformation.setFuelType(fuelType);
        vehicleInformation.setEmissionUnits(emissionUnits);
        vehicleInformation.setCertificationIntent(certificationIntent);

        // vehicleInformation is returned to listener when the dialog is closed

        view.setVisible(false);
    }

    @Override
    public void onVehicleModelYearChanged(int modelYear) {
        vehicleModelYear = modelYear;
        validate();
    }

    @Override
    public void onVinChanged(String vin) {
        this.vin = vin;
        validate();
    }

    /**
     * Validates the information in the form to determine if the user can proceed
     */
    private void validate() {
        boolean vinValid = vinDecoder.isVinValid(vin);
        view.setVinValid(vinValid);

        boolean vehicleMYMatch = vinDecoder.getModelYear(vin) == vehicleModelYear;
        view.setVehicleModelYearValid(vehicleMYMatch);

        boolean enabled = true;
        enabled &= vinValid;
        enabled &= vehicleMYMatch;
        enabled &= vinDecoder.isModelYearValid(engineModelYear);
        enabled &= fuelType != null;
        enabled &= emissionUnits > 0;
        enabled &= certificationIntent != null && certificationIntent.trim().length() > 0;

        view.setOkButtonEnabled(enabled);
    }
}

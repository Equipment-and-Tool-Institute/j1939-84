/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import static java.util.logging.Level.INFO;
import static org.etools.j1939_84.J1939_84.getLogger;
import static org.etools.j1939_84.controllers.ResultsListener.NOOP;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.AddressClaimPacket;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * The Presenter which controls the logic in the
 * {@link VehicleInformationDialog}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class VehicleInformationPresenter implements VehicleInformationContract.Presenter {

    /**
     * The listener that will be notified when the user is done
     */
    private final VehicleInformationListener listener;
    /**
     * The module used to gather information about the connected vehicle
     */
    private final VehicleInformationModule vehicleInformationModule;
    /**
     * The View that's being controlled
     */
    private final VehicleInformationContract.View view;
    /**
     * The decoder used to determine if the VIN is valid
     */
    private final VinDecoder vinDecoder;
    private RequestResult<AddressClaimPacket> addressClaim;
    /**
     * The value the user has entered for the number of CalIDs on the vehicle
     */
    private int calIds;
    private List<DM19CalibrationInformationPacket> calIdsFound = Collections.emptyList();
    /**
     * The value the user has entered for the certification intent
     */
    private String certificationIntent;
    /**
     * The value the user selected to indicate the intent of this test run.
     */
    private boolean usCarb;

    /**
     * The communications module for talking to bus
     */
    private final CommunicationsModule communicationsModule;

    /**
     * The value the user has entered for the number of emissions units on the
     * vehicle
     */
    private int emissionUnits;
    /**
     * The component Id for the emissions units on the vehicle
     */
    private final List<ComponentIdentificationPacket> emissionUnitsFound = new ArrayList<>();
    /**
     * The value the user has entered for the engine model year
     */
    private int engineModelYear;
    /**
     * The value the user has entered for the Fuel Type
     */
    private FuelType fuelType;
    /**
     * The number of trips for fault B implant
     */
    private int numberOfTripsForFaultBImplant;
    /**
     * The VehicleInformation that will be returned to the listener
     */
    private VehicleInformation vehicleInformation;
    /**
     * The vehicle model year the user has entered
     */
    private int vehicleModelYear;
    /**
     * The VIN the user has entered
     */
    private String vin;

    private boolean isOverridden;

    /**
     * Constructor
     *
     * @param view
     *                     the View to be controlled
     * @param listener
     *                     the {@link VehicleInformationListener} that will be given the
     *                     {@link VehicleInformation}
     * @param j1939
     *                     the vehicle bus
     */
    public VehicleInformationPresenter(VehicleInformationContract.View view,
                                       VehicleInformationListener listener,
                                       J1939 j1939) {
        this(view, listener, j1939, new VehicleInformationModule(), new VinDecoder(), new CommunicationsModule());
    }

    /**
     * Constructor exposed for testing
     */
    public VehicleInformationPresenter(VehicleInformationContract.View view,
                                       VehicleInformationListener listener,
                                       J1939 j1939,
                                       VehicleInformationModule vehicleInformationModule,
                                       VinDecoder vinDecoder,
                                       CommunicationsModule communicationsModule) {
        this.view = swingProxy(view, VehicleInformationContract.View.class);
        this.listener = listener;
        this.vehicleInformationModule = vehicleInformationModule;
        this.vehicleInformationModule.setJ1939(j1939);
        this.vinDecoder = vinDecoder;
        this.communicationsModule = communicationsModule;
        this.communicationsModule.setJ1939(j1939);
    }

    @SuppressWarnings("unchecked")
    public static <T> T swingProxy(T o, Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(),
                                          new Class<?>[] { cls },
                                          (proxy, method, args) -> {
                                              if (SwingUtilities.isEventDispatchThread()) {
                                                  return method.invoke(o, args);
                                              }
                                              CompletableFuture<Object> f = new CompletableFuture<>();
                                              SwingUtilities.invokeLater(() -> {
                                                  try {
                                                      f.complete(method.invoke(o, args));
                                                  } catch (Throwable e) {
                                                      e.printStackTrace();
                                                  }
                                              });
                                              return f.join();
                                          });
    }

    @Override
    public void readVehicle() {
        addressClaim = vehicleInformationModule.reportAddressClaim(listener.getResultsListener());

        view.setFuelType(FuelType.DSL); // Assuming this used mostly on Diesel engines

        numberOfTripsForFaultBImplant = 1;
        view.setNumberOfTripsForFaultBImplant(numberOfTripsForFaultBImplant);

        try {
            vin = vehicleInformationModule.getVin();
            view.setVin(vin);
        } catch (Exception e) {
            getLogger().log(INFO, "Error reading VIN", e);
        }

        int modelYear = vinDecoder.getModelYear(vin);
        if (!vinDecoder.isModelYearValid(modelYear)) {
            modelYear = DateTimeModule.getInstance().getYear();
        }
        view.setVehicleModelYear(modelYear);

        try {
            engineModelYear = vehicleInformationModule.getEngineModelYear();
            view.setEngineModelYear(engineModelYear);
        } catch (Exception e) {
            view.setEngineModelYear(modelYear);
            getLogger().log(INFO, "Error reading engine model year", e);
        }

        try {
            certificationIntent = vehicleInformationModule.getEngineFamilyName();
            view.setCertificationIntent(certificationIntent);
        } catch (Exception e) {
            getLogger().log(INFO, "Error reading engine family", e);
        }

        try {
            List<Integer> obdModules = vehicleInformationModule.getOBDModules(NOOP);
            view.setEmissionUnits(obdModules.size());

            obdModules.stream().forEach(address -> {
                emissionUnitsFound.addAll(communicationsModule.request(ComponentIdentificationPacket.PGN,
                                                                       address,
                                                                       NOOP)
                                                              .toPacketStream()
                                                              .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                                                              .collect(Collectors.toList()));
            });

        } catch (Exception e) {
            getLogger().log(INFO, "Error reading OBD ECUs", e);
        }

        try {
            calIdsFound = communicationsModule.requestDM19(NOOP).toPacketStream().collect(Collectors.toList());
            view.setCalIds((int) calIdsFound.stream().mapToLong(p -> p.getCalibrationInformation().size()).sum());
        } catch (Exception e) {
            getLogger().log(INFO, "Error reading calibration IDs", e);
        }

    }

    @Override
    public void onCalIdsChanged(int count) {
        calIds = count;
        validate();
    }

    @Override
    public void onCancelButtonClicked() {
        vehicleInformation = null;
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
    public void onNumberOfTripsForFaultBImplantChanged(int numberOfTripsForFaultBImplant) {
        this.numberOfTripsForFaultBImplant = numberOfTripsForFaultBImplant;
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
        vehicleInformation.setCalIds(calIds);
        vehicleInformation.setCertificationIntent(certificationIntent);
        vehicleInformation.setUsCarb(usCarb);
        vehicleInformation.setNumberOfTripsForFaultBImplant(numberOfTripsForFaultBImplant);

        vehicleInformation.setCalIdsFound(calIdsFound);
        vehicleInformation.setEmissionUnitsFound(emissionUnitsFound);
        vehicleInformation.setAddressClaim(addressClaim);

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

    @Override
    public void onOverrideChanged(boolean checked) {
        isOverridden = checked;
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
        enabled &= calIds > 0;
        enabled &= certificationIntent != null && certificationIntent.trim().length() > 0;
        enabled &= numberOfTripsForFaultBImplant > 0;

        if (enabled) {
            isOverridden = false;
        }
        view.setOverrideControlVisible(!enabled);

        view.setOkButtonEnabled(enabled || isOverridden);
    }

    @Override
    public void onUsCarb(boolean usCarb) {
        this.usCarb = usCarb;
    }
}

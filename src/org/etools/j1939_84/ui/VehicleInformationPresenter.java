/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
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

    @SuppressWarnings("unchecked")
    public static <T> T swingProxy(T o, Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(),
                new Class<?>[] { cls }, (InvocationHandler) (proxy, method, args) -> {
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

    private RequestResult<AddressClaimPacket> addressClaim;

    /**
     * The value the user has entered for the number of CalIDs on the vehicle
     */
    private int calIds;

    private List<CalibrationInformation> calIdsFound = Collections.emptyList();

    /**
     * The value the user has entered for the certification intent
     */
    private String certificationIntent;

    /**
     * The module used to retrieve the Date/Time
     */
    private final DateTimeModule dateTimeModule;
    /**
     * The module used to gather information about the module readiness
     */
    private final DiagnosticReadinessModule diagnosticReadinessModule;
    /**
     * The value the user has entered for the number of emissions units on the
     * vehicle
     */
    private int emissionUnits;

    /**
     * The component Id for the emissions units on the vehicle
     */
    private List<ComponentIdentificationPacket> emissionUnitsFound = Collections.emptyList();

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
     * The number of trips for fault B implant
     */
    private int numberOfTripsForFaultBImplant;

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
     * @param view
     *            the View to be controlled
     * @param listener
     *            the {@link VehicleInformationListener} that will be given the
     *            {@link VehicleInformation}
     * @param j1939
     *            the vehicle bus
     */
    public VehicleInformationPresenter(VehicleInformationContract.View view, VehicleInformationListener listener,
            J1939 j1939) {
        this(view, listener, j1939, new DateTimeModule(), new VehicleInformationModule(),
                new DiagnosticReadinessModule(), new VinDecoder());
    }

    /**
     * Constructor exposed for testing
     *
     * @param view
     *            the View to be controlled
     * @param listener
     *            the {@link VehicleInformationListener} that will be given the
     *            {@link VehicleInformation}
     * @param dateTimeModule
     *            the {@link DateTimeModule}
     * @param vehicleInformationModule
     *            the {@link VehicleInformationModule}
     * @param vinDecoder
     *            the {@link VinDecoder}
     * @param j1939
     *            the vehicle interface
     */
    public VehicleInformationPresenter(VehicleInformationContract.View view, VehicleInformationListener listener,
            J1939 j1939, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            DiagnosticReadinessModule diagnosticReadinessModule, VinDecoder vinDecoder) {
        this.view = swingProxy(view, VehicleInformationContract.View.class);
        this.listener = listener;
        this.dateTimeModule = dateTimeModule;
        this.vehicleInformationModule = vehicleInformationModule;
        this.vehicleInformationModule.setJ1939(j1939);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.diagnosticReadinessModule.setJ1939(j1939);
        this.vinDecoder = vinDecoder;
    }

    @Override
    public void initialize() {
        addressClaim = vehicleInformationModule.reportAddressClaim(ResultsListener.NOOP);

        view.setFuelType(FuelType.DSL); // Assuming this used mostly on Diesel
                                        // engines
        numberOfTripsForFaultBImplant = 1;
        view.setNumberOfTripsForFaultBImplant(numberOfTripsForFaultBImplant);
        try {
            List<Integer> obdModules = diagnosticReadinessModule.getOBDModules(ResultsListener.NOOP);
            emissionUnitsFound = new ArrayList<>();
            obdModules.forEach(address -> {
                emissionUnitsFound
                        .add(vehicleInformationModule.reportComponentIdentification(ResultsListener.NOOP, address)
                                .getPacket()
                                .map(e -> e.resolve(p -> p,
                                        ack -> ComponentIdentificationPacket.error(address, "ERROR")))
                                .orElse(ComponentIdentificationPacket.error(address, "MISSING")));
            });
            view.setEmissionUnits(emissionUnitsFound.size());
        } catch (Exception e) {
            // Don't care
            e.printStackTrace(); // FXIME remove this
        }

        try {
            calIdsFound = vehicleInformationModule.reportCalibrationInformation(ResultsListener.NOOP).stream()
                    .flatMap(dm19 -> dm19.getCalibrationInformation().stream()).collect(Collectors.toList());
            view.setCalIds(calIdsFound.size());
        } catch (Exception e) {
            // Don't care
        }

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
        vehicleInformation.setCalIds(emissionUnits);
        vehicleInformation.setCertificationIntent(certificationIntent);

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

    /**
     * Validates the information in the form to determine if the user can
     * proceed
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

        view.setOkButtonEnabled(enabled);
    }
}

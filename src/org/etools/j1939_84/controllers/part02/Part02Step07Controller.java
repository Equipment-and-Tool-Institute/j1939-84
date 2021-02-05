/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for Component ID: Make, Model, Serial Number Support
 */
public class Part02Step07Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    Part02Step07Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step07Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              new DiagnosticMessageModule(), dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        //6.2.7.3 Actions2: [Note: No warning message shall be provided for responses from non-OBD devices for PGN 59904].
        // a. Global Request for Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588)
        List<ComponentIdentificationPacket> globalPackets = getVehicleInformationModule()
                //b. Display each positive return in the log.
                .reportComponentIdentification(getListener()).getPackets();

        List<OBDModuleInformation> zeroFunctionObdModules = new ArrayList<>();

        final ComponentIdentificationPacket[] dsFunctionZeroPacket = { null };
        //6.2.7 Component ID: Make, Model, Serial Number Support
        dataRepository.getObdModules().stream()
                //get modules that responded in Part01 from dataRepo
                .filter(obdModuleInformation -> obdModuleInformation.getComponentIdentification() != null)
                .forEach(module -> {
                    int moduleAddress = module.getSourceAddress();
                    String moduleName = Lookup.getAddressName(moduleAddress);

                    //6.2.7.1.a Actions: Destination Specific (DS) Component ID request
                    // (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588) to each OBD ECU.
                    getVehicleInformationModule()
                            .reportComponentIdentification(getListener(), moduleAddress)
                            .getPacket()
                            .ifPresentOrElse(p -> {
                                //  6.2.7.2.b Fail criteria: Fail if there is any difference between the
                                //  part 2 response and the part 1 response, as PGN 65259 data is defined
                                //  to be static values.
                                // PGN 65259 -> ComponentIdentificationPacket
                                p.left.ifPresent(leftPacket -> {
                                    if (module.getFunction() == 0) {
                                        dsFunctionZeroPacket[0] = leftPacket;
                                        // Filter the modules responded to be only
                                        // ones with function = 0 (engine function)
                                        // - needed for 6.2.7.4.a check
                                        zeroFunctionObdModules.add(module);
                                    }
                                    if (!leftPacket.getComponentIdentification()
                                            .equals(module.getComponentIdentification())) {
                                        addFailure("6.2.7.2.b - " + moduleName + " reported component identification as: "
                                                           + leftPacket.getComponentIdentification().toString() +
                                                           ", Part 01 Step 09 reported it as: " +
                                                           module.getComponentIdentification().toString());
                                    }
                                });
                            }, () -> {
                                //6.2.7.2 Fail criteria:
                                // a. Fail if any device does not support PGN 65259 with the engine running that supported PGN 65259 with the engine off in part 1.
                                addFailure(
                                        "6.2.7.2.a - There are no positive responses to a DS Component ID request from "
                                                + moduleName);
                            });
                    //6.2.7.5 Warn criteria2 for OBD ECUs other than function 0:
                    // a. Warn if Component ID not supported for the global query
                    // in 6.2.7.3 with engine running.
                    if (module.getFunction() != 0 &&
                            globalPackets.stream()
                                    //not supported means didn't respond?
                                    .noneMatch(packet -> packet.getSourceAddress() == moduleAddress)) {
                        addWarning("6.2.7.5.a - " + moduleName + " did not provide a positive respond to global query while engine running");
                    }
                });

        //Get zero function module address (should only be one)
        int function0SourceAddress = zeroFunctionObdModules.isEmpty() ? -1
                : zeroFunctionObdModules.get(0).getSourceAddress();

        //Get the packets from the global response for the zero function module
        List<ComponentIdentificationPacket> zeroFunctionPackets = globalPackets.stream()
                .filter(zeroPacket -> zeroPacket.getSourceAddress() == function0SourceAddress)
                .collect(Collectors.toList());

        //6.2.7.4.b Fail if the global response does not match the
        // destination specific response from function 0.
        ComponentIdentificationPacket zeroFunctionPacket = null;
        if (!zeroFunctionPackets.isEmpty()) {
            zeroFunctionPacket = zeroFunctionPackets.get(0);
        }

        if (zeroFunctionObdModules.isEmpty() || zeroFunctionPackets.isEmpty()
                || !dsFunctionZeroPacket[0].equals(zeroFunctionPacket)) {
            // b. Fail if the global response does not match the destination
            // specific response from function 0.
            String errorMessage = "6.2.7.4.b - ";
            if (zeroFunctionObdModules.isEmpty()) {
                errorMessage += "No OBD module claimed function 0";
            } else if (zeroFunctionPackets.isEmpty() || zeroFunctionPacket == null) {
                errorMessage += "No packet was received for " +
                        Lookup.getAddressName(function0SourceAddress) +
                        " which claimed function 0 in Part 1 Step 9";
            } else {
                errorMessage += "The Component ID Global responses do not contain a match for "
                        + Lookup.getAddressName(zeroFunctionPacket.getSourceAddress())
                        + ", which claimed function 0 in Part 1 Step 9";
            }
            addFailure(errorMessage);
        }
    }
}

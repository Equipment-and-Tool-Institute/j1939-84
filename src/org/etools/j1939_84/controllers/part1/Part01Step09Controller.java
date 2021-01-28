/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.ComponentIdentification;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.StringUtils;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * The controller for 6.1.9 Component ID: Make, Model, Serial Number Support
 */
public class Part01Step09Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    Part01Step09Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                dataRepository,
                DateTimeModule.getInstance());
    }

    protected Part01Step09Controller(Executor executor,
                                     EngineSpeedModule engineSpeedModule,
                                     BannerModule bannerModule,
                                     VehicleInformationModule vehicleInformationModule,
                                     DataRepository dataRepository,
                                     DateTimeModule dateTimeModule) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dateTimeModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.9.1 ACTIONS:
        // Send destination specific message and grab only the instance of ComponentIdentificationPacket
        List<ComponentIdentificationPacket> dsPackets = dataRepository.getObdModules()
                .stream()
                // convert each address to it's component ID, ignoring the NACKs
                .flatMap(module -> getVehicleInformationModule()
                        .reportComponentIdentification(getListener(), module.getSourceAddress())
                        .getPacket()
                        .flatMap(e -> e.left).stream())
                .collect(Collectors.toList());
        if (dsPackets.isEmpty()) {
            addFailure("6.1.9.1.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)");
        }
        List<OBDModuleInformation> zeroFunctionObdBDModules= new ArrayList<>();
        //Update data in the dataRepository for use in Part 02 Step 7
        dsPackets.forEach(packet -> {
            OBDModuleInformation module = dataRepository.getObdModule(packet.getSourceAddress());
                module.setComponentInformationIdentification(new ComponentIdentification(packet));
                // Filter the modules responded to be only ones with function = 0 (engine function)
                if(module.getFunction() == 0){
                    zeroFunctionObdBDModules.add(module);
                }
                // Save the updated data back to the dataRepository for use later in testing
                dataRepository.putObdModule(module);
        });
        // Log missing engine response
        if (zeroFunctionObdBDModules.size() != 1) {
            int numberOfZeroFunctionObds = zeroFunctionObdBDModules.size();
            getListener().onResult(WARN + ": " + numberOfZeroFunctionObds +
                            " module(s) have claimed function 0 - only one module should");
        }
        int function0SourceAddress = zeroFunctionObdBDModules.isEmpty() ? -1
                : zeroFunctionObdBDModules.get(0).getSourceAddress();

        List<ComponentIdentificationPacket> zeroFunctionPackets = dsPackets.stream()
                .filter(zeroPacket -> zeroPacket.getSourceAddress() == function0SourceAddress)
                .collect(Collectors.toList());

        ComponentIdentificationPacket zeroFunctionPacket = null;
        if (zeroFunctionObdBDModules.isEmpty() || zeroFunctionPackets.isEmpty()) {
            addFailure(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.2.b None of the positive responses were provided by the same SA as the SA that claims to be function 0 (engine)");
        } else {
            zeroFunctionPacket = zeroFunctionPackets.get(0);
        }

        // 6.1.9.2 Fail Criteria:
        // c. Fail if the serial number field (SPN 588) from any function 0
        // device does not end in 6 numeric characters (ASCII 0 through ASCII 9).
        String serialNumber = zeroFunctionPacket != null ? zeroFunctionPacket.getSerialNumber() : "";

        if (serialNumber.length() >= 6 && !StringUtils.containsOnlyNumericAsciiCharacters(serialNumber.substring(
                (zeroFunctionPacket.getSerialNumber().length() - 6),
                zeroFunctionPacket.getSerialNumber().length()))) {
            addFailure(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.2.c Serial number field (SPN 588) from any function 0 device does not end in 6 numeric characters (ASCII 0 through ASCII 9)");
        }

        // d. Fail if the make (SPN 586), model (SPN 587), or serial number (SPN
        // 588) from any OBD ECU contains any unprintable ASCII characters.
        String make = zeroFunctionPacket != null ? zeroFunctionPacket.getMake() : "";
        String model = zeroFunctionPacket != null ? zeroFunctionPacket.getModel() : "";
        if (StringUtils.containsNonPrintableAsciiCharacter(serialNumber) ||
                StringUtils.containsNonPrintableAsciiCharacter(make) ||
                StringUtils.containsNonPrintableAsciiCharacter(model)) {
            addFailure(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters");

        }

        // 6.1.9.3 Warn Criteria for OBD ECUs:
        // a. Warn if the serial number field (SPN 588) from any function 0
        // device is less than 8 characters long.
        if (serialNumber.length() < 8) {
            addWarning(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.3.a Serial number field (SPN 588) from any function 0 device is less than 8 characters long");
        }
        // b. Warn if the make field (SPN 586) is longer than 5 ASCII characters.
        if (!make.isEmpty() && make.length() > 5) {
            addWarning(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.3.b Make field (SPN 586) is longer than 5 ASCII characters");
        }
        // c. Warn if the make field (SPN 586) is less than 2 ASCII characters.
        if (make.length() == 1) {
            addWarning(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.3.c Make field (SPN 586) is less than 2 ASCII characters");
        }
        // d. Warn if the model field (SPN 587) is less than 1 character long.
        if (model != null && model.length() < 1) {
            addWarning(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.3.d Model field (SPN 587) is less than 1 character long");
        }

        // 6.1.9.4 Actions2: [Note: No warning message shall be provided for
        // responses from non-OBD devices for PGN 59904].

        // a. Global Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588).
        // b. Display each positive return in the log.
        List<ComponentIdentificationPacket> globalPackets = getVehicleInformationModule()
                .reportComponentIdentification(getListener()).getPackets();
        if (globalPackets.isEmpty()) {
            addWarning(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.4.a & 6.1.9.4.b Global Component ID request for PGN 65259 did not receive any packets");
        }

        // 6.1.9.5 Fail Criteria2 for function 0:
        List<ComponentIdentificationPacket> globalPacketsFunctionZero = new ArrayList<>();
        zeroFunctionObdBDModules.forEach(moduleInfo -> globalPacketsFunctionZero.addAll(globalPackets.stream()
                .filter(packet -> packet.getSourceAddress() == moduleInfo.getSourceAddress())
                .collect(Collectors.toList())));

        if (globalPacketsFunctionZero.isEmpty()) {
            // a. Fail if there is no positive response from function 0. (Global
            // request not supported or timed out)
            addFailure(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.5.a There is no positive response from function 0");
        }

        if (!globalPacketsFunctionZero.contains(zeroFunctionPacket)) {
            // b. Fail if the global response does not match the destination
            // specific response from function 0.
            addFailure(getPartNumber(),
                    getStepNumber(),
                    "6.1.9.5.b Global response does not match the destination specific response from function 0");
        }

        //6.1.9.6 Warn Criteria2 for OBD ECUs other than function 0:
        // a. Warn if Component ID not supported for the global query in
        //    6.1.9.4, when supported by destination specific query
        dsPackets.stream().filter(packet -> !zeroFunctionObdBDModules.contains(packet.getSourceAddress()))
                .forEach(singlePacket -> {
            if (!globalPackets.contains(singlePacket)) {
                addFailure(getPartNumber(),
                        getStepNumber(),
                        "6.1.9.6.a Component ID not supported for the global query, when supported by destination specific query");

            }
        });
    }
}

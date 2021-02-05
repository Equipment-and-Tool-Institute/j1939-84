/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.utils.StringUtils.containsNonPrintableAsciiCharacter;
import static org.etools.j1939_84.utils.StringUtils.containsOnlyNumericAsciiCharacters;
import static org.etools.j1939_84.utils.StringUtils.stripLeadingAndTrailingNulls;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
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
              new DiagnosticMessageModule(),
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.9.1.a Send destination specific message and grab only the instance of ComponentIdentificationPacket
        List<ComponentIdentificationPacket> dsPackets = dataRepository.getObdModules()
                .stream()
                .map(OBDModuleInformation::getSourceAddress)
                .map(address -> getVehicleInformationModule().reportComponentIdentification(getListener(), address))
                .flatMap(r -> r.requestResult().getPackets().stream())
                .peek(packet -> {
                    //Update data in the dataRepository for use in Part 02 Step 7
                    OBDModuleInformation module = dataRepository.getObdModule(packet.getSourceAddress());
                    module.setComponentInformationIdentification(packet.getComponentIdentification());
                    // Save the updated data back to the dataRepository for use later in testing
                    dataRepository.putObdModule(module);
                })
                .collect(Collectors.toList());
        if (dsPackets.isEmpty()) {
            addFailure("6.1.9.2.a - There are no positive responses");
        }

        int function0SourceAddress = dataRepository.getFunctionZeroAddress();
        String function0Name = Lookup.getAddressName(function0SourceAddress);
        getListener().onResult("Function 0 module is " + function0Name);

        ComponentIdentificationPacket function0Packet = dsPackets.stream()
                .filter(p -> p.getSourceAddress() == function0SourceAddress)
                .findFirst()
                .orElse(null);

        if (function0Packet == null) {
            addFailure("6.1.9.2.b - None of the positive responses were provided by " + function0Name);
        } else {
            String serialNumber = stripLeadingAndTrailingNulls(function0Packet.getSerialNumber());
            int length = serialNumber.length();
            if (length >= 6) {
                String endingCharacters = serialNumber.substring((length - 6), length);
                if (!containsOnlyNumericAsciiCharacters(endingCharacters)) {
                    // 6.1.9.2.c. Fail if the serial number field (SPN 588) from any function 0
                    // device does not end in 6 numeric characters (ASCII 0 through ASCII 9).
                    addFailure("6.1.9.2.c - Serial number field (SPN 588) from " + function0Name + " does not end in 6 numeric characters");
                }
            }

            // 6.1.9.3.a. Warn if the serial number field (SPN 588) from any function 0 device is less than 8 characters long.
            if (length < 8) {
                addWarning("6.1.9.3.a - Serial number field (SPN 588) from " + function0Name + " is less than 8 characters long");
            }
        }

        // 6.1.9.2.d. Fail if the make (SPN 586) from any OBD ECU contains any unprintable ASCII characters.
        dsPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> containsNonPrintableAsciiCharacter(p.getMake()))
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addFailure("6.1.9.2.d - The make (SPN 586) from " + moduleName + " contains any unprintable ASCII characters"));

        // 6.1.9.2.d. Fail if the model (SPN 587) from any OBD ECU contains any unprintable ASCII characters.
        dsPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> containsNonPrintableAsciiCharacter(p.getModel()))
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addFailure("6.1.9.2.d - The model (SPN 587) from " + moduleName + " contains any unprintable ASCII characters"));

        // 6.1.9.2.d. Fail if the serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.
        dsPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> containsNonPrintableAsciiCharacter(p.getSerialNumber()))
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addFailure("6.1.9.2.d - The serial number (SPN 588) from " + moduleName + " contains any unprintable ASCII characters"));

        // 6.1.9.3.b. For OBD ECUs, Warn if the make field (SPN 586) is longer than 5 ASCII characters.
        dsPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> {
                    String make = p.getMake();
                    return make != null && make.length() > 5;
                })
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addWarning("6.1.9.3.b - The make field (SPN 586) from " + moduleName + " is longer than 5 ASCII characters"));

        // 6.1.9.3.c. For OBD ECUs, Warn if the make field (SPN 586) is less than 2 ASCII characters.
        dsPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> {
                    String make = p.getMake();
                    return make == null || make.length() < 2;
                })
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addWarning("6.1.9.3.c - The make field (SPN 586) from " + moduleName + " is less than 2 ASCII characters"));

        // 6.1.9.3.d. For OBD ECUs, Warn if the model field (SPN 587) is less than 1 character long.
        dsPackets.stream()
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .filter(p -> {
                    String model = p.getModel();
                    return model == null || model.length() < 1;
                })
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addWarning("6.1.9.3.d - The model field (SPN 587) from " + moduleName + " is less than 1 ASCII characters"));

        // 6.1.9.4.a. Global Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588).
        // 6.1.9.4.b. Display each positive return in the log.
        List<ComponentIdentificationPacket> globalPackets = getVehicleInformationModule()
                .reportComponentIdentification(getListener()).getPackets();

        // 6.1.9.5 Fail Criteria2 for function 0:
        ComponentIdentificationPacket globalFunction0Packet = globalPackets.stream()
                .filter(packet -> packet.getSourceAddress() == function0SourceAddress)
                .findFirst()
                .orElse(null);

        if (globalFunction0Packet == null) {
            // 6.1.9.5.a. Fail if there is no positive response from function 0.
            addFailure("6.1.9.5.a - There is no positive response from " + function0Name);
        } else {
            // 6.1.9.5.b. Fail if the global response does not match the destination specific response from function 0.
            if (!globalFunction0Packet.equals(function0Packet)) {
                addFailure("6.1.9.5.b - Global response does not match the destination specific response from " + function0Name);
            }
        }

        //6.1.9.6.a. for OBD ECUs other than function 0, Warn if Component ID not supported for the
        // global query in 6.1.9.4, when supported by destination specific query
        dsPackets.stream()
                .filter(packet -> packet.getSourceAddress() != function0SourceAddress)
                .forEach(packet -> {
                    if (!globalPackets.contains(packet)) {
                        String moduleName = Lookup.getAddressName(packet.getSourceAddress());
                        addFailure("6.1.9.6.a - " + moduleName + " did not supported the Component ID for the global query, but supported it in the destination specific query");
                    }
                });
    }
}

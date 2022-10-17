/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.utils.StringUtils.containsNonPrintableAsciiCharacter;
import static org.etools.j1939_84.utils.StringUtils.containsOnlyNumericAsciiCharacters;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.9 Component ID: Make, Model, Serial Number Support
 */
public class Part01Step09Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part01Step09Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    protected Part01Step09Controller(Executor executor,
                                     EngineSpeedModule engineSpeedModule,
                                     BannerModule bannerModule,
                                     VehicleInformationModule vehicleInformationModule,
                                     DataRepository dataRepository,
                                     DateTimeModule dateTimeModule,
                                     CommunicationsModule communicationsModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.9.1.a Destination Specific (DS) Component ID request (PG 59904) for PG 65259 (SPs 586, 587, and 588)
        // to each OBD ECU.
        // 6.1.9.1.b Display each positive return in the log.
        var dsPackets = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> request(ComponentIdentificationPacket.PGN, a))
                                           .map(BusResult::requestResult)
                                           .flatMap(r -> r.getPackets().stream())
                                           .collect(Collectors.toList());

        if (dsPackets.isEmpty()) {
            addFailure("6.1.9.2.a - There are no positive responses");
        }
        dsPackets.forEach(this::save);

        int function0SourceAddress = getDataRepository().getFunctionZeroAddress();
        String function0Name = Lookup.getAddressName(function0SourceAddress);
        getListener().onResult("Function 0 ECU is " + function0Name);

        ComponentIdentificationPacket function0Packet = dsPackets.stream()
                                                                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                                                                 .filter(p -> p.getSourceAddress() == function0SourceAddress)
                                                                 .findFirst()
                                                                 .orElse(null);

        if (function0Packet == null) {
            addFailure("6.1.9.2.b - None of the positive responses were provided by " + function0Name);
        } else {
            String serialNumber = function0Packet.getSerialNumber();
            int length = serialNumber.length();
            if (length >= 5) {
                String endingCharacters = serialNumber.substring((length - 5), length);
                if (!containsOnlyNumericAsciiCharacters(endingCharacters)) {
                    // 6.1.9.2.c. Fail if the serial number field (SP 588) from any function 0
                    // device does not end in five numeric characters (ASCII 0 through ASCII 9).
                    addFailure("6.1.9.2.c - Serial number field (SP 588) from " + function0Name
                            + " does not end in five numeric characters");
                }
            } else {
                addFailure("6.1.9.2.c - Serial number field (SP 588) from " + function0Name
                        + " does not end in five numeric characters");
            }

            // 6.1.9.3.a. Warn if the serial number field (SP 588) from any function 0 device is
            // less than six characters long.
            if (length < 6) {
                addWarning("6.1.9.3.a - Serial number field (SP 588) from " + function0Name
                        + " is less than six characters long");
            }
        }

        // 6.1.9.2.d. Fail if the make (SP 586) from any OBD ECU contains any unprintable ASCII characters.
        dsPackets.stream()
                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                 .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                 .filter(p -> containsNonPrintableAsciiCharacter(p.getMake()))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.1.9.2.d - The make (SP 586) from " + moduleName
                             + " contains any unprintable ASCII characters");
                 });

        // 6.1.9.2.d. Fail if the model (SP 587) from any OBD ECU contains any unprintable ASCII characters.
        dsPackets.stream()
                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                 .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                 .filter(p -> containsNonPrintableAsciiCharacter(p.getModel()))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.1.9.2.d - The model (SP 587) from " + moduleName
                             + " contains any unprintable ASCII characters");
                 });

        // 6.1.9.2.d. Fail if the serial number (SP 588) from any OBD ECU contains any unprintable ASCII characters.
        dsPackets.stream()
                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                 .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                 .filter(p -> containsNonPrintableAsciiCharacter(p.getSerialNumber()))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.1.9.2.d - The serial number (SP 588) from " + moduleName
                             + " contains any unprintable ASCII characters");
                 });

        // 6.1.9.3.b. For OBD ECUs, Warn if the make field (SP 586) is longer than five ASCII characters.
        dsPackets.stream()
                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                 .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                 .filter(p -> {
                     String make = p.getMake();
                     return make != null && make.length() > 5;
                 })
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addWarning("6.1.9.3.b - The make field (SP 586) from " + moduleName
                             + " is longer than five ASCII characters");
                 });

        // 6.1.9.3.c. For OBD ECUs, Warn if the make field (SP 586) is less than two ASCII characters.
        dsPackets.stream()
                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                 .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                 .filter(p -> {
                     String make = p.getMake();
                     return make == null || make.length() < 2;
                 })
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addWarning("6.1.9.3.c - The make field (SP 586) from " + moduleName
                             + " is less than two ASCII characters");
                 });

        // 6.1.9.3.d. For OBD ECUs, Warn if the model field (SP 587) is less than one character long.
        dsPackets.stream()
                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                 .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                 .filter(p -> {
                     String model = p.getModel();
                     return model == null || model.length() < 1;
                 })
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addWarning("6.1.9.3.d - The model field (SP 587) from " + moduleName
                             + " is less than one ASCII characters");
                 });

        // 6.1.9.4.a. Global Component ID request (PG 59904) for PG 65259 (SPs 586, 587, and 588).
        // 6.1.9.4.b. Display each positive return in the log.
        var globalPackets = request(ComponentIdentificationPacket.PGN).getPackets();

        // 6.1.9.5 Fail Criteria2 for function 0:
        var globalFunction0Packet = globalPackets.stream()
                                                 .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                                                 .filter(packet -> packet.getSourceAddress() == function0SourceAddress)
                                                 .findFirst()
                                                 .orElse(null);

        if (globalFunction0Packet == null) {
            // 6.1.9.5.a. Fail if there is no positive response from function 0.
            addFailure("6.1.9.5.a - There is no positive response from " + function0Name);
        } else {
            // 6.1.9.5.b. Fail if the global response does not match the destination specific response from function 0.
            if (!globalFunction0Packet.equals(function0Packet)) {
                addFailure("6.1.9.5.b - Global response does not match the destination specific response from "
                        + function0Name);
            }
        }

        // 6.1.9.6.a. for OBD ECUs other than function 0, Warn if Component ID not supported for the
        // global query in 6.1.9.4, when supported by destination specific query
        dsPackets.stream()
                 .filter(packet -> packet.getSourceAddress() != function0SourceAddress)
                 .forEach(packet -> {
                     if (!globalPackets.contains(packet)) {
                         addWarning("6.1.9.6.a - " + packet.getModuleName()
                                 + " did not supported the Component ID for the global query, but supported it in the destination specific query");
                     }
                 });
    }
}

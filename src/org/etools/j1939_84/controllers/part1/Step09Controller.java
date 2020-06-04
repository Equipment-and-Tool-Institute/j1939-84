/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.StringUtils;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 * The controller for 6.1.9 Component ID: Make, Model, Serial Number
 * Support
 */
public class Step09Controller extends Controller {

    private final DataRepository dataRepository;

    Step09Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(),
                dataRepository);
    }

    protected Step09Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 9";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.9.1 ACTIONS:
        // Send destination specific message and grab only the instanceof
        // ComponentIdentificationPacket
        List<ComponentIdentificationPacket> packets = dataRepository.getObdModuleAddresses()
                .stream()
                .flatMap(moduleAddress -> {
                    return getVehicleInformationModule().reportComponentIdentification(getListener(), moduleAddress)
                            .stream();
                })
                .filter(packet -> {
                    return packet instanceof ComponentIdentificationPacket;
                })
                .map(packet -> (ComponentIdentificationPacket) packet)
                .collect(Collectors.toList());
        if (packets.isEmpty()) {
            getListener().addOutcome(1,
                    9,
                    Outcome.FAIL,
                    "6.1.9.1.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)");
        }

        // Filter the modules responded to be only ones with function = 0 (engine
        // function)
        List<OBDModuleInformation> zeroFunctionObdBDModules = dataRepository.getObdModules()
                .stream()
                .filter(module -> module.getFunction() == 0)
                .collect(Collectors.toList());

        // Log missing engine response
        if (zeroFunctionObdBDModules.size() != 1) {
            // TODO Only one module should be reporting function 0
            getLogger().log(Level.WARNING,
                    String.format(
                            "%s module(s) have claimed function 0 - only one module should",
                            zeroFunctionObdBDModules.size()));
        }
        int function0SourceAddress = zeroFunctionObdBDModules.isEmpty() ? -1
                : zeroFunctionObdBDModules.get(0).getSourceAddress();

        List<ComponentIdentificationPacket> zeroFunctionPackets = packets.stream()
                .filter(zeroPacket -> {
                    return zeroPacket.getSourceAddress() == function0SourceAddress;
                })
                .collect(Collectors.toList());

        ComponentIdentificationPacket zeroFunctionPacket = null;
        if (zeroFunctionObdBDModules.isEmpty() || zeroFunctionPackets.isEmpty()) {
            getListener().addOutcome(1,
                    9,
                    Outcome.FAIL,
                    "6.1.9.2.b None of the positive responses were provided by the same SA as the SA that claims to be function 0 (engine)");
        } else {
            zeroFunctionPacket = zeroFunctionPackets.get(0);
        }

        // 6.1.9.2 Fail Criteria:
        // c. Fail if the serial number field (SPN 588) from any function 0 device does
        // not end in 6 numeric characters (ASCII 0 through ASCII 9).
        String serialNumber = zeroFunctionPacket != null ? zeroFunctionPacket.getSerialNumber() : "";

        if (serialNumber.length() >= 6 && !StringUtils.containsOnlyNumericAsciiCharacters(serialNumber.substring(
                (zeroFunctionPacket.getSerialNumber().length() - 6),
                zeroFunctionPacket.getSerialNumber().length()))) {
            getListener().addOutcome(1,
                    9,
                    Outcome.FAIL,
                    "6.1.9.2.c Serial number field (SPN 588) from any function 0 device does not end in 6 numeric characters (ASCII 0 through ASCII 9)");
        }

        // d. Fail if the make (SPN 586), model (SPN 587), or serial number (SPN 588)
        // from any OBD ECU contains any unprintable ASCII characters.
        String make = zeroFunctionPacket != null ? zeroFunctionPacket.getMake() : "";
        String model = zeroFunctionPacket != null ? zeroFunctionPacket.getModel() : null;

        if (StringUtils.containsNonPrintableAsciiCharacter(serialNumber) ||
                StringUtils.containsNonPrintableAsciiCharacter(make) || (model != null &&
                        StringUtils.containsNonPrintableAsciiCharacter(model))) {
            getListener().addOutcome(1,
                    9,
                    Outcome.FAIL,
                    "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.");

        }

        // 6.1.9.3 Warn Criteria for OBD ECUs:
        // a. Warn if the serial number field (SPN 588) from any function 0 device is
        // less than 8 characters long.
        if (!serialNumber.isEmpty() && serialNumber.length() < 8) {
            getListener().addOutcome(1,
                    9,
                    Outcome.WARN,
                    "6.1.9.3.a Serial number field (SPN 588) from any function 0 device is less than 8 characters long");
        }
        // b. Warn if the make field (SPN 586) is longer than 5 ASCII characters.
        if (!make.isEmpty() && make.length() > 5) {
            getListener().addOutcome(1,
                    9,
                    Outcome.WARN,
                    "6.1.9.3.b Make field (SPN 586) is longer than 5 ASCII characters");
        }
        // c. Warn if the make field (SPN 586) is less than 2 ASCII characters.
        if (!make.isEmpty() && make.length() < 2) {
            getListener().addOutcome(1,
                    9,
                    Outcome.WARN,
                    "6.1.9.3.c Make field (SPN 586) is less than 2 ASCII characters");
        }
        // d. Warn if the model field (SPN 587) is less than 1 character long.
        if (model != null && model.length() < 1) {
            getListener().addOutcome(1,
                    9,
                    Outcome.WARN,
                    "6.1.9.3.d Model field (SPN 587) is less than 1 character long");
        }

        // 6.1.9.4 Actions2: [Note: No warning message shall be provided for responses
        // from non-OBD devices for PGN 59904].

        // a. Global Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and
        // 588).
        // b. Display each positive return in the log.
        List<ComponentIdentificationPacket> globalPackets = getVehicleInformationModule()
                .reportComponentIdentification(getListener()).stream()
                .filter(globalPacket -> globalPacket instanceof ComponentIdentificationPacket)
                .collect(Collectors.toList());

        // 6.1.9.5 Fail Criteria2 for function 0:

        // a. Fail if there is no positive response from function 0. (Global request not
        // supported or timed out)
        // b. Fail if the global response does not match the destination specific
        // response from function 0.

        // FIXME This needs to check the packets to have source addresses from the
        // function=0 Module.
        List<OBDModuleInformation> globalObdModuleInformations = dataRepository.getObdModules().stream()
                .filter(module -> module.getFunction() == 0).collect(Collectors.toList());
        if (globalObdModuleInformations.size() == 0) {
            getListener().addOutcome(1,
                    9,
                    Outcome.FAIL,
                    "6.1.9.5.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)");

        }
        if (globalObdModuleInformations.size() != 1 && globalObdModuleInformations.size() != 0) {
            getListener().addOutcome(1,
                    9,
                    Outcome.FAIL,
                    "6.1.9.5.b None of the positive responses are provided by the same SA as the SA that claims to be function 0 (engine). (SPN 588 ESN not supported by the engine function)");

        }

        // 6.1.9.6 Warn Criteria2 for OBD ECUs other than function 0:
        // a. Warn if Component ID not supported for the global query in 6.1.9.4, when
        // supported by destination specific query
        packets.forEach(singlePacket -> {
            if (!globalPackets.contains(singlePacket)) {
                getListener().addOutcome(1,
                        9,
                        Outcome.FAIL,
                        "6.1.9.6.a Component ID not supported for the global query in 6.1.9.4, when supported by destination specific query");

            }
        });

    }

}

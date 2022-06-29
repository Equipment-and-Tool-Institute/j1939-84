package org.etools.j1939_84.controllers.part01;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.5 PGN 65260 VIN Verification
 */
public class Part01Step05Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    private final VinDecoder vinDecoder;

    Part01Step05Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new VinDecoder(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    Part01Step05Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           VinDecoder vinDecoder,
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
        this.vinDecoder = vinDecoder;
    }

    @Override
    protected void run() throws Throwable {
        // 6.1.5.1.a. Global Request (PGN 59904) for PGN 65260 Vehicle ID (SPN 237) VIN.
        List<VehicleIdentificationPacket> packets = request(VehicleIdentificationPacket.PGN)
                                                                                              .toPacketStream()
                                                                                              .map(p -> new VehicleIdentificationPacket(p.getPacket()))
                                                                                              .collect(Collectors.toList());

        // 6.1.5.2.a. Fail if no VIN is provided by any ECU.
        if (packets.isEmpty()) {
            addFailure("6.1.5.2.a - No VIN was provided by any ECU");
            return;
        }

        // 6.1.5.2.b. Fail if more than one OBD ECU responds with VIN.
        long obdResponses = packets.stream()
                                   .filter(p -> isObdModule(p.getSourceAddress()))
                                   .count();
        if (obdResponses > 1) {
            addFailure("6.1.5.2.b - More than one OBD ECU responded with VIN");
        }

        VehicleIdentificationPacket packet = packets.get(0);
        String vin = packet.getVin();

        VehicleInformation vehicleInformation = getDataRepository().getVehicleInformation();

        // 6.1.5.2.c. Fail if VIN does not match user entered VIN from earlier in this section.
        if (!vehicleInformation.getVin().equals(vin)) {
            addFailure("6.1.5.2.c - VIN does not match user entered VIN");
        }

        // 6.1.5.2.e. Fail per Section A.3, Criteria for VIN Validation.
        if (!vinDecoder.isVinValid(vin)) {
            addFailure("6.1.5.2.e - VIN is not valid (not 17 legal chars, incorrect checksum, or non-numeric sequence");
        } else {
            // 6.1.5.2.d. Fail if 10th character of VIN does not match model year of vehicle (not engine) entered by
            // user earlier in this part.
            if (vinDecoder.getModelYear(vin) != vehicleInformation.getVehicleModelYear()) {
                addFailure("6.1.5.2.d - 10th character of VIN does not match model year of vehicle entered by user earlier in this part");
            }
        }

        // 6.1.5.3.a. Warn if VIN response from non-OBD ECU.
        long nonObdResponses = packets.stream()
                                      .filter(p -> !isObdModule(p.getSourceAddress()))
                                      .count();
        if (nonObdResponses > 0) {
            addWarning("6.1.5.3.a - Non-OBD ECU responded with VIN");
        }

        // 6.1.5.3.b. Warn if more than one VIN response from any individual ECU.
        long respondingSources = packets.stream().mapToInt(ParsedPacket::getSourceAddress).distinct().count();
        if (packets.size() > respondingSources) {
            addWarning("6.1.5.3.b - More than one VIN response from an ECU");
        }

        // 6.1.5.3.c. Warn if VIN provided from more than one non-OBD ECU.
        if (nonObdResponses > 1) {
            addWarning("6.1.5.3.c - VIN provided from more than one non-OBD ECU");
        }

        // 6.1.5.3.d. Warn if manufacturer defined data follows the VIN.
        if (!packet.getManufacturerData().isEmpty()) {
            addWarning("6.1.5.3.d - Manufacturer defined data follows the VIN");
        }
    }

}

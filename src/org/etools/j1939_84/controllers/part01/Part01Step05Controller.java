package org.etools.j1939_84.controllers.part01;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;

public class Part01Step05Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;
    private final VinDecoder vinDecoder;

    Part01Step05Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new VinDecoder(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step05Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           VinDecoder vinDecoder,
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
        this.vinDecoder = vinDecoder;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        List<VehicleIdentificationPacket> packets = getVehicleInformationModule().reportVin(getListener());
        if (packets.isEmpty()) {
            addFailure(1, 5, "6.1.5.2.a - No VIN was provided");
            return; // No point in continuing
        }

        long obdResponses = packets.stream()
                .filter(p -> dataRepository.getObdModuleAddresses().contains(p.getSourceAddress())).count();
        if (obdResponses > 1) {
            addFailure(1, 5, "6.1.5.2.b - More than one OBD ECU responded with VIN");
        }

        VehicleIdentificationPacket packet = packets.get(0);
        String vin = packet.getVin();
        if (!dataRepository.getVehicleInformation().getVin().equals(vin)) {
            addFailure(1, 5, "6.1.5.2.c - VIN does not match user entered VIN");
        }

        if (vinDecoder.getModelYear(vin) != dataRepository.getVehicleInformation().getVehicleModelYear()) {
            addFailure(1, 5, "6.1.5.2.d - VIN Model Year does not match user entered Vehicle Model Year");
        }

        if (!vinDecoder.isVinValid(vin)) {
            addFailure(1,
                       5,
                       "6.1.5.2.e - VIN is not valid (not 17 legal chars, incorrect checksum, or non-numeric sequence");
        }

        long nonObdResponses = packets.stream()
                .filter(p -> !dataRepository.getObdModuleAddresses().contains(p.getSourceAddress())).count();
        if (nonObdResponses > 0) {
            addWarning(1, 5, "6.1.5.3.a - Non-OBD ECU responded with VIN");
        }

        long respondingSources = packets.stream().mapToInt(ParsedPacket::getSourceAddress).distinct().count();
        if (packets.size() > respondingSources) {
            addWarning(1, 5, "6.1.5.3.b - More than one VIN response from an ECU");
        }

        if (nonObdResponses > 1) {
            addWarning(1, 5, "6.1.5.3.c - VIN provided from more than one non-OBD ECU");
        }

        if (!packet.getManufacturerData().isEmpty()) {
            addWarning(1, 5, "6.1.5.3.d - Manufacturer defined data follows the VIN");
        }
    }

}

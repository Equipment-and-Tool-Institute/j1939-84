package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;

public class Step05Controller extends Controller {

    private final DataRepository dataRepository;
    private final VinDecoder vinDecoder;

    Step05Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(), new VinDecoder(),
                dataRepository);
    }

    Step05Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, VinDecoder vinDecoder, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.vinDecoder = vinDecoder;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 5";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
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

        long respondingSources = packets.stream().mapToInt(p -> p.getSourceAddress()).distinct().count();
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

package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step03Controller extends Controller {

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Step03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), dataRepository);
    }

    Step03Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DiagnosticReadinessModule diagnosticReadinessModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 3";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        diagnosticReadinessModule.setJ1939(getJ1939());

        List<ParsedPacket> packets = diagnosticReadinessModule.requestDM5Packets(getListener(), true);

        boolean nacked = packets.stream().anyMatch(packet -> packet instanceof AcknowledgmentPacket
                && ((AcknowledgmentPacket) packet).getResponse() == Response.NACK);
        if (nacked) {
            addFailure(1, 3, "6.1.3.2.b - The request for DM5 was NACK'ed");
        }

        Stream<DM5DiagnosticReadinessPacket> dm5Packets = packets.stream()
                .filter(p -> p instanceof DM5DiagnosticReadinessPacket)
                .map(p -> (DM5DiagnosticReadinessPacket) p);

        dm5Packets.filter(p -> p.isObd()).forEach(p -> {
            OBDModuleInformation info = new OBDModuleInformation(p.getSourceAddress());
            info.setObdCompliance(p.getOBDCompliance());
            dataRepository.putObdModule(p.getSourceAddress(), info);
        });

        Collection<OBDModuleInformation> modules = dataRepository.getObdModules();
        if (modules.isEmpty()) {
            addFailure(1, 3, "6.1.3.2.a - There needs to be at least one OBD Module");
        }

        long distinctCount = new HashSet<>(modules).size();
        if (distinctCount > 1) {
            // All the values should be the same
            addWarning(1,
                    3,
                    "6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");
        }
    }

}

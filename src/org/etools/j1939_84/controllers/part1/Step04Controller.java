package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 *         The controller for DM24: SPN support
 */
public class Step04Controller extends StepController {

    private final DataRepository dataRepository;
    private final OBDTestsModule obdTestsModule;
    private final SupportedSpnModule supportedSpnModule;

    Step04Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(), new OBDTestsModule(),
                new SupportedSpnModule(), dataRepository);
    }

    Step04Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, OBDTestsModule obdTestsModule, SupportedSpnModule supportedSpnModule,
            DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.obdTestsModule = obdTestsModule;
        this.supportedSpnModule = supportedSpnModule;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 4";
    }

    @Override
    public int getStepNumber() {
        return 4;
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        obdTestsModule.setJ1939(getJ1939());

        RequestResult<DM24SPNSupportPacket> result = obdTestsModule.requestDM24Packets(getListener(),
                dataRepository.getObdModuleAddresses());
        List<DM24SPNSupportPacket> globalPackets = result.getPackets().stream()
                .filter(packet -> packet instanceof DM24SPNSupportPacket).map(p -> p)
                .collect(Collectors.toList());

        if (result.isRetryUsed()) {
            addFailure(1, 4, "6.1.4.2.a - Retry was required to obtain DM24 response");
        }

        globalPackets.stream().forEach(p -> {
            OBDModuleInformation info = dataRepository.getObdModule(p.getSourceAddress());
            if (info != null) {
                info.setSupportedSpns(p.getSupportedSpns());
            }
        });

        Set<Integer> dataStreamSpns = dataRepository.getObdModules()
                .stream()
                .map(info -> info.getDataStreamSpns())
                .flatMap(spns -> spns.stream())
                .map(s -> s.getSpn())
                .collect(Collectors.toSet());

        boolean dataStreamOk = supportedSpnModule
                .validateDataStreamSpns(getListener(),
                        dataStreamSpns,
                        dataRepository.getVehicleInformation().getFuelType());
        if (!dataStreamOk) {
            addFailure(1, 4, "6.1.4.2.b - One or more SPNs for data stream is not supported");
        }

        Set<Integer> freezeFrameSpns = dataRepository
                .getObdModules()
                .stream()
                .map(info -> info.getFreezeFrameSpns())
                .flatMap(spns -> spns.stream())
                .map(s -> s.getSpn())
                .collect(Collectors.toSet());

        boolean freezeFrameOk = supportedSpnModule.validateFreezeFrameSpns(getListener(), freezeFrameSpns);
        if (!freezeFrameOk) {
            addFailure(1, 4, "6.1.4.2.c - One or more SPNs for freeze frame are not supported");
        }
    }
}

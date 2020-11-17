/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
 *
 */
public class Step10Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;
    private final DiagnosticReadinessModule diagnosticReadinessModule;
    private final DTCModule dtcModule;
    private final OBDTestsModule obdTestsModule;

    Step10Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new VehicleInformationModule(), new DTCModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), new OBDTestsModule(), dataRepository);
    }

    protected Step10Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule, DTCModule dtcModule,
            PartResultFactory partResultFactory, DiagnosticReadinessModule diagnosticReadinessModule,
            OBDTestsModule obdTestsModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, partResultFactory,
                PART_NUMBER, STEP_NUMBER, TOTAL_STEPS);
        this.dataRepository = dataRepository;
        this.dtcModule = dtcModule;
        this.obdTestsModule = obdTestsModule;
        this.diagnosticReadinessModule = diagnosticReadinessModule;
    }

    @Override
    protected void run() throws Throwable {
        dtcModule.setJ1939(getJ1939());
        diagnosticReadinessModule.setJ1939(getJ1939());
        obdTestsModule.setJ1939(getJ1939());

        dataRepository.getObdModuleAddresses().stream()
                .collect(Collectors.toList());

        // 6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
        List<AcknowledgmentPacket> globalDM11Packets = dtcModule.requestDM11(getListener())
                .getAcks();

        // c. Allow 5 s to elapse before proceeding with test step 6.1.9.2.
        getDateTimeModule().pauseFor(5L * 1L * 1000L);

        // 6.1.10.2 Fail criteria:
        // a. Fail if NACK received from any HD OBD ECU.
        // from the dataRepo grab the obdModule addresses
        boolean nacked = globalDM11Packets.stream().anyMatch(packet -> packet.getResponse() == Response.NACK);
        if (nacked) {
            addWarning(1, 10, "6.1.10.3.a - The request for DM11 was NACK'ed");
        }

        // 6.1.10.3 Warn criteria:
        // a. Warn if ACK received from any HD OBD ECU.16
        boolean acked = globalDM11Packets.stream().anyMatch(packet -> packet.getResponse() == Response.ACK);
        if (acked) {
            addWarning(1, 10, "6.1.10.3.a - The request for DM11 was ACK'ed");
        }
    }

}

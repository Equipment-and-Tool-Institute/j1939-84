/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *         6.3.3 DM27: All pending DTCs
 */
public class Part03Step03Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part03Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step03Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.3.3.1.a Global DM27 (send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 3038, 1706)).
        List<DM27AllPendingDTCsPacket> globalPackets = getDiagnosticMessageModule().requestDM27(getListener())
                                                                                   .getPackets();
        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();
        obdModuleAddresses.forEach(this::clearLastDm27);

        globalPackets.stream()
                     .peek(this::verifyDtcsAreSame)
                     .peek(this::checkForAdditionalDtc)
                     .forEach(this::updateObdLastDM27);

        // 6.3.3.4.a DS DM27 to each OBD ECU.
        List<RequestResult<DM27AllPendingDTCsPacket>> dsResults = obdModuleAddresses.stream()
                                                                                    .map(address -> getDiagnosticMessageModule()
                                                                                                                                .requestDM27(getListener(),
                                                                                                                                             address)
                                                                                                                                .requestResult())
                                                                                    .collect(Collectors.toList());
        List<DM27AllPendingDTCsPacket> dsPackets = filterRequestResultPackets(dsResults);

        // 6.3.3.5.a Fail if (if supported) any difference compared to data received with global request.
        compareRequestPackets(globalPackets, dsPackets, "6.3.3.5.a");

        // 6.3.3.5.b Fail if NACK not received from OBD ECUs that did not respond to global query.
        List<AcknowledgmentPacket> dsAcks = filterRequestResultAcks(dsResults);
        checkForNACKs(globalPackets, dsAcks, obdModuleAddresses, "6.3.3.5.b");
    }

    private void clearLastDm27(int obdAddress) {
        OBDModuleInformation obdModule = getDataRepository().getObdModule(obdAddress);
        obdModule.remove(DM27AllPendingDTCsPacket.class);
        getDataRepository().putObdModule(obdModule);
    }

    private void updateObdLastDM27(DM27AllPendingDTCsPacket packet) {
        OBDModuleInformation obdModule = getDataRepository().getObdModule(packet.getSourceAddress());
        obdModule.set(packet);
        getDataRepository().putObdModule(obdModule);
    }

    private void verifyDtcsAreSame(DM27AllPendingDTCsPacket packet) {
        List<DiagnosticTroubleCode> packetDtcs = packet.getDtcs();
        OBDModuleInformation obdModule = getDataRepository().getObdModule(packet.getSourceAddress());
        DM6PendingEmissionDTCPacket dm6 = obdModule.get(DM6PendingEmissionDTCPacket.class);
        List<DiagnosticTroubleCode> obdDtcs = dm6 == null ? List.of() : dm6.getDtcs();
        // 6.3.3.2.a Fail if (if supported) no ECU reports the same DTC observed in step 6.3.2.1 in a positive DM27
        // response.
        if (packetDtcs.size() != obdDtcs.size() || !packetDtcs.equals(obdDtcs)) {
            addFailure(
                       "6.3.3.2.a - OBD module " + getAddressName(obdModule.getSourceAddress()) +
                               " reported different DTC than observed in Step 6.3.2.1");
        }
    }

    private void checkForAdditionalDtc(DM27AllPendingDTCsPacket packet) {
        List<DiagnosticTroubleCode> packetDtcs = packet.getDtcs();
        OBDModuleInformation obdModule = getDataRepository().getObdModule(packet.getSourceAddress());
        DM6PendingEmissionDTCPacket dm6 = obdModule.get(DM6PendingEmissionDTCPacket.class);
        List<DiagnosticTroubleCode> obdDtcs = dm6 == null ? List.of() : dm6.getDtcs();
        // 6.3.3.3.a. Warn if (if supported) any ECU additional DTCs are provided than the DTC observed in step 6.3.2.1
        // in a positive DM27 response.
        if (packetDtcs.size() > obdDtcs.size()) {
            addWarning("6.3.3.3.a - OBD module " + getAddressName(packet.getSourceAddress()) +
                    "reported " + obdDtcs.size() + " DTCs in response to DM6 in 6.3.2.1 and " +
                    packetDtcs.size() + " DTCs when responding to DM27");
        }
    }
}

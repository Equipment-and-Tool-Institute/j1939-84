/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Garrison Garland {garrison@soliddesign.net)
 */

public class Step16Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository);
    }

    protected Step16Controller(Executor executor,
                               EngineSpeedModule engineSpeedModule,
                               BannerModule bannerModule,
                               VehicleInformationModule vehicleInformationModule,
                               DTCModule dtcModule,
                               DataRepository dataRepository) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);

        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());
        // 6.1.16.1 Actions:
        // a. Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs
        // 1213-1215, 3038, 1706)).
        RequestResult<DM2PreviouslyActiveDTC> globalDiagnosticTroubleCodePackets = dtcModule.requestDM2(getListener(),
                                                                                                        true);

        // Get DM2PrevisoulyActiveDTC so we can get DTCs and report accordingly
        List<DM2PreviouslyActiveDTC> globalDM2s = globalDiagnosticTroubleCodePackets.getPackets();

        // 6.1.16.2.a Fail if any OBD ECU reports a previously active DTC.
        if (globalDM2s.stream().flatMap(packet -> packet.getDtcs().stream()).findAny().isPresent()) {
            getListener().addOutcome(1,
                                     16,
                                     Outcome.FAIL,
                                     "6.1.16.2.a - OBD ECU reported a previously active DTC");
        }

        // 6.1.16.2.b Fail if any OBD ECU does not report MIL (Malfunction
        // Indicator Lamp) off
        if (globalDM2s.stream().filter(packet -> packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF).findAny()
                .isPresent()) {
            getListener().addOutcome(1,
                                     16,
                                     Outcome.FAIL,
                                     "6.1.16.2.b - OBD ECU does not report MIL off");
        }

        // 6.1.16.2.c Fail if any non-OBD ECU does not report MIL off or not
        // supported - LampStatus of OTHER
        List<Integer> obdAddresses = dataRepository.getObdModuleAddresses();
        if (globalDM2s.stream()
                .filter(p -> !obdAddresses.contains(p.getSourceAddress()))
                .filter(p -> p.getMalfunctionIndicatorLampStatus() != LampStatus.OFF
                        && p.getMalfunctionIndicatorLampStatus() != LampStatus.OTHER)
                .findAny()
                .isPresent()) {
            getListener().addOutcome(1,
                                     16,
                                     Outcome.FAIL,
                                     "6.1.16.2.c - non-OBD ECU does not report MIL off or not supported");
        }

        // 6.1.16.3.a DS DM2 to each OBD ECU
        List<Either<DM2PreviouslyActiveDTC, AcknowledgmentPacket>> dsResult = obdAddresses.stream()
                .flatMap(address -> dtcModule.requestDM2(getListener(), true, address).getPacket().stream())
                .collect(Collectors.toList());

        List<DM2PreviouslyActiveDTC> dsDM2s = dsResult.stream().filter(r -> r.left.isPresent()).map(p -> p.left.get())
                .collect(Collectors.toList());

        List<DM2PreviouslyActiveDTC> unmatchedPackets = globalDiagnosticTroubleCodePackets.getPackets().stream()
                .filter(packet -> !dsDM2s.contains(packet))
                .collect(Collectors.toList());

        // 6.1.16.4.a Fail if any responses differ from global responses
        if (!unmatchedPackets.isEmpty()) {
            getListener().addOutcome(1,
                                     16,
                                     Outcome.FAIL,
                                     "6.1.16.4.a DS DM2 responses differ from global responses");
        }

        Collection<Integer> responseAddresses = globalDM2s.stream().map(p -> p.getSourceAddress())
                .collect(Collectors.toSet());
        obdAddresses.removeAll(responseAddresses);

        // 6.1.16.4.b Fail if NACK not received from OBD ECUs that did not
        // respond to global query
        Collection<Integer> nackAddresses = dsResult.stream()
                .filter(r -> r.right.isPresent())
                .map(r -> r.right.get())
                .filter(packet -> packet.getResponse() == Response.NACK).map(p -> p.getSourceAddress())
                .collect(Collectors.toSet());
        obdAddresses.removeAll(nackAddresses);

        if (!obdAddresses.isEmpty()) {
            getListener().addOutcome(1,
                                     16,
                                     Outcome.FAIL,
                                     "6.1.16.4.b Nack not received from OBD ECUs that did not respond to global query");
        }
    }
}
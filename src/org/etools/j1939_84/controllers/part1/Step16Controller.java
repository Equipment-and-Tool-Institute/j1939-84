/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Garrison Garland {garrison@soliddesign.net)
 *
 */
public class Step16Controller extends Controller {

    private final DataRepository dataRepository;
    private final DTCModule dtcModule;

    Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new DTCModule(),
                new PartResultFactory(), dataRepository);
    }

    protected Step16Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule, DTCModule dtcmodule,
            PartResultFactory partResultFactory, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        dtcModule = dtcmodule;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        // TODO Auto-generated method stub
        return "Part 1 Step 16";
    }

    @Override
    protected int getTotalSteps() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        /**
         * 6.1.16.1 Actions:
         *
         * a. Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038,
         * 1706)).
         * globalDM2 =
         *
         * 6.1.16.2 Fail criteria (if supported):
         *
         * a. Fail if any OBD ECU reports a previously active DTC.
         *
         * b. Fail if any OBD ECU does not report MIL off.
         *
         * c. Fail if any non-OBD ECU does not report MIL off or not supported.
         *
         * 6.1.16.3 Actions2:
         *
         * a. DS DM2 to each OBD ECU.
         *
         * 6.1.16.4 Fail criteria2 (if supported):
         *
         * a. Fail if any responses differ from global responses.
         *
         * a. Fail if NACK not received from OBD ECUs that did not respond to global
         * query.
         */

        dtcModule.setJ1939(getJ1939());
        // 6.1.16.1.a. Global DM2 (send Request (PGN 59904) for PGN 65227
        // RequestResult<ParsedPacket> globalDiagnosticTroubleCodePackets =
        // dtcModule.requestDM2(getListener(), true);
        List<DM2PreviouslyActiveDTC> globalDM2s = dtcModule.requestDM2(getListener(), true).getPackets().stream()
                .filter(p -> p instanceof DM2PreviouslyActiveDTC).map(p -> (DM2PreviouslyActiveDTC) p)
                .collect(Collectors.toList());
        // FIXME Not sure if this is the right statement. I'm not sure if it's
        // retrieving the DTCs properly.
        // List<DiagnosticTroubleCode> dtcs = globalDM2s.get(0).getDtcs();
        // source.stream()
        // .map(String::length)
        // .forEachOrdered(target::add);

        // 6.1.16.2.a Fail if any OBD ECU reports a previously active DTC
        List<DiagnosticTroubleCode> dtcs = new ArrayList<>();

        globalDM2s.forEach(packet -> {
            if (packet.getDtcs() != null) {
                dtcs.addAll(packet.getDtcs());
            }
        });
        System.out.println("dtcs.size() is: " + dtcs.size());

        if (dtcs.isEmpty()) {
        } else {
            getListener().addOutcome(1,
                    16,
                    Outcome.FAIL,
                    "6.1.16.2.a - OBD ECU reported a previously active DTC.");
        }

        // 6.1.16.2.b Fail if any OBD ECU does not report MIL (Malfunction Indicator
        // Lamp) off
        globalDM2s.stream().forEach(packet -> {
            if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                getListener().addOutcome(1,
                        16,
                        Outcome.FAIL,
                        "6.1.16.2.b - OBD ECU does not report MIL off.");
            }
        });

        // 6.1.16.2.c Fail if any non-OBD ECU does not report MIL off or not supported
        globalDM2s.stream().filter(p -> !dataRepository.getObdModuleAddresses().contains(p.getSourceAddress()))
                .forEach(packet -> {
                    // if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF
                    // || packet.getMalfunctionIndicatorLampStatus() != null) {
                    // getListener().addOutcome(1,
                    // 16,
                    // Outcome.FAIL,
                    // "6.1.16.2.c - non-OBD ECU does not report MIL off or not supported.");
                    // }
                    if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                        getListener().addOutcome(1,
                                16,
                                Outcome.FAIL,
                                "6.1.16.2.c - non-OBD ECU does not report MIL off or not supported.");
                    } else if (packet.getMalfunctionIndicatorLampStatus() != null) {
                        getListener().addOutcome(1,
                                16,
                                Outcome.FAIL,
                                "6.1.16.2.c - non-OBD ECU does not report MIL off or not supported.");
                    }
                });

        // 6.1.16.3.a DS DM2 to each OBD ECU
        List<ParsedPacket> dsDM2s = new ArrayList<>();
        dataRepository.getObdModuleAddresses().stream().forEach(address -> {
            dsDM2s.addAll(dtcModule.getDM2Packets(getListener(), true, address));
        });

        // 6.1.16.4.a Fail if any responses differ from global responses
        dsDM2s.removeAll(globalDM2s);
        if (dsDM2s.isEmpty()) {
        } else {
            getListener().addOutcome(1,
                    16,
                    Outcome.FAIL,
                    "6.1.16.4.a DS DM2 responses differ from global responses");
        }
        // long dsDM2response = dsDM2s.stream().count();
        // long globalResponse = globalDM2s.stream().count();
        // for (int i = 0; i < dsDM2response; i++) {
        // if (dsDM2s.get(i).getPacket().equals(globalResponse)) {
        // } else {
        // getListener().addOutcome(1,
        // 16,
        // Outcome.FAIL,
        // "6.1.16.4.a DS DM2 responses differ from global responses");
        // }
        // }

        // 6.1.16.4.b Fail if NACK not received from OBD ECUs that did not respond to
        // global query
        Set<Integer> globalAddresses = globalDM2s.stream().map(p -> p.getSourceAddress()).collect(Collectors.toSet());
        Set<Integer> obdAddresses = dataRepository.getObdModuleAddresses();
        obdAddresses.removeAll(globalAddresses);

        for (int address : obdAddresses) {
            getVehicleInformationModule().reportCalibrationInformation(getListener(),
                    address);
            long nackCount = dsDM2s.stream()
                    .filter(p -> p instanceof AcknowledgmentPacket)
                    .map(p -> (AcknowledgmentPacket) p)
                    .filter(p -> p.getResponse() != Response.NACK)
                    .count();
            if (nackCount != 1) {
                getListener().addOutcome(1,
                        7,
                        Outcome.FAIL,
                        "6.1.16.4.a - NACK not received from OBD ECU that did not respond to global query.");
            }
        }
    }
}

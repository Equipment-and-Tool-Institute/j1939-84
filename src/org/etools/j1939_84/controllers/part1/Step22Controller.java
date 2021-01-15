/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.22 DM29: Regulated DTC counts
 */

public class Step22Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 22;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step22Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Step22Controller(Executor executor,
                     EngineSpeedModule engineSpeedModule,
                     BannerModule bannerModule,
                     VehicleInformationModule vehicleInformationModule,
                     DTCModule dtcModule,
                     DataRepository dataRepository,
                     DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());

        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();

        // 6.1.22.1.a. Global DM29 (send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)).
        RequestResult<DM29DtcCounts> globalResponse = dtcModule.requestDM29(getListener());

        List<DM29DtcCounts> globalPackets = globalResponse.getPackets();
        for (DM29DtcCounts dm29 : globalPackets) {
            // 6.1.22.2.a. For ECUs that support DM27, fail if any ECU does not report
            // pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0
            if (dm29.isDM27Supported() && (dm29.getEmissionRelatedPendingDTCCount() != 0
                    || dm29.getEmissionRelatedMILOnDTCCount() != 0
                    || dm29.getEmissionRelatedPreviouslyMILOnDTCCount() != 0
                    || dm29.getEmissionRelatedPermanentDTCCount() != 0)) {
                addFailure(
                        "6.1.22.2.a - An ECU did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0");
                break;
            }
        }

        for (DM29DtcCounts dm29 : globalPackets) {
            // 6.1.22.2.b. For ECUs that do not support DM27, fail if any ECU does not
            // report pending/all pending/MIL on/previous MIL on/permanent =0/0xFF/0/0/0.
            if (!dm29.isDM27Supported() &&
                    (dm29.getEmissionRelatedPendingDTCCount() != 0x00
                            || dm29.getEmissionRelatedMILOnDTCCount() != 0x00
                            || dm29.getEmissionRelatedPreviouslyMILOnDTCCount() != 0x00
                            || dm29.getEmissionRelatedPermanentDTCCount() != 0x00)) {
                addFailure(
                        "6.1.22.2.b - An ECU did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0");
                break;
            }
        }

        for (DM29DtcCounts dm29 : globalPackets) {
            // 6.1.22.2.c. For non-OBD ECUs, fail if any ECU reports pending, MIL-on, previously MIL-on or permanent DTC count greater than 0
            if (!obdModuleAddresses.contains(dm29.getSourceAddress())
                    && (dm29.getEmissionRelatedPendingDTCCount() > 0
                    || dm29.getEmissionRelatedMILOnDTCCount() > 0
                    || dm29.getEmissionRelatedPreviouslyMILOnDTCCount() > 0
                    || dm29.getEmissionRelatedPermanentDTCCount() > 0)) {
                addFailure("6.1.22.2.c - A non-OBD ECU reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0");
                break;
            }
        }

        // 6.1.22.2.d. Fail if no OBD ECU provides DM29.
        boolean noObdResponses = globalPackets
                .stream()
                .map(ParsedPacket::getSourceAddress)
                .filter(obdModuleAddresses::contains)
                .findAny()
                .isEmpty();
        if (noObdResponses) {
            addFailure("6.1.22.2.d - No OBD ECU provided DM29");
        }

        // 6.1.22.3.a. DS DM29 to each OBD ECU.
        List<BusResult<DM29DtcCounts>> dsResults = obdModuleAddresses.stream()
                .map(address -> dtcModule.requestDM29(getListener(), address))
                .collect(Collectors.toList());

        // 6.1.22.4.a. Fail if any difference compared to data received during global request.
        List<DM29DtcCounts> dsPackets = filterPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.1.22.4.a");

        // 6.1.22.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        List<AcknowledgmentPacket> dsAcks = filterAcks(dsResults);
        checkForNACKs(globalPackets, dsAcks, obdModuleAddresses, "6.1.22.4.b");
    }
}

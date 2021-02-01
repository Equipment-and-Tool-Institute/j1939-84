/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Part02Step13Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;
    private final DTCModule dtcModule;

    Part02Step13Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new DTCModule());
    }

    Part02Step13Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
                           DTCModule dtcModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              new DiagnosticMessageModule(), dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
        this.dtcModule = dtcModule;
    }

    @Override
    protected void run() throws Throwable {
        dtcModule.setJ1939(getJ1939());
        //6.2.13 DM31: DTC to Lamp Association
        // 6.2.13.1 Actions:
        //  a. DS DM31 (send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117)) to each OBD ECU.
        RequestResult<DM31DtcToLampAssociation> globalResponse = dtcModule.requestDM31(getListener());

        // 6.2.13.2 Fail criteria (if supported):
        List<DM31DtcToLampAssociation> milOnPackets = globalResponse.getPackets().stream()
                .filter(packet -> packet.getDtcLampStatuses().stream()
                        .map(DTCLampStatus::getMalfunctionIndicatorLampStatus)
                        .anyMatch(lampStatus ->  lampStatus != LampStatus.OFF
                                && lampStatus != LampStatus.ALTERNATE_OFF))
                .collect(Collectors.toList());
//                .flatMap(p -> p.getDtcLampStatuses().stream().map(DTCLampStatus::getMalfunctionIndicatorLampStatus))
//                .filter(mil -> mil != LampStatus.OFF && mil != LampStatus.ALTERNATE_OFF)
             //   .collect(Collectors.toList());
        //  a. Fail if any ECU does not report MIL off. See section A.8 for allowed values.
        if (!milOnPackets.isEmpty()) {
           // String obds = String.Join(",", milOnPackets.stream()..Select(x => x.Name));
            //spns.stream().map(i -> i.toString()).collect(Collectors.joining(","))
//            DM31DtcToLampAssociation packet = new DM31DtcToLampAssociation(Packet.)
//            String statusError =
//            String errormsg = milOnPackets.stream().map(p -> Lookup.getAddressName(p.getSourceAddress() + " " +
//                                                                         p.getDtcLampStatuses().stream().map(DTCLampStatus::getMalfunctionIndicatorLampStatus)).toString() + NL).toString());
//                    //+ p.getDtcLampStatuses().stream().map(DTCLampStatus::getMalfunctionIndicatorLampStatus)
                    //.collect(

            //milOnPackets.stream().map(packet -> Lookup.getAddressName(packet.getSourceAddress()).collect(Collectors.joining(", "));
            addFailure("6.2.13.2.a - The following ECU(s) did not report MIL off");// + errormsg);
        }
        //  b. Fail if NACK not received from OBD ECUs that did not provide DM31.
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        checkForNACKs(globalResponse.getPackets(), globalResponse.getAcks(), obdModuleAddresses, "6.2.13.2.b");
    }
}

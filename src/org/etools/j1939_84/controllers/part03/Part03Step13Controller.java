/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA2ValueValidator;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.FreezeFrame;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.3.13 DM25: Expanded freeze frame
 */
public class Part03Step13Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final TableA2ValueValidator validator;

    Part03Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new TableA2ValueValidator(PART_NUMBER, STEP_NUMBER));
    }

    Part03Step13Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           TableA2ValueValidator validator) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.validator = validator;
    }

    @Override
    protected void run() throws Throwable {
        // 6.3.13.1.a. DS DM25 (send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214-1215)) to each OBD ECU.
        List<RequestResult<DM25ExpandedFreezeFrame>> responses = getDataRepository().getObdModuleAddresses()
                                                                                    .stream()
                                                                                    .map(address -> getCommunicationsModule()
                                                                                                                             .requestDM25(getListener(),
                                                                                                                                          address,
                                                                                                                                          get(DM24SPNSupportPacket.class, address, 1)))
                                                                                    .map(BusResult::requestResult)
                                                                                    .collect(Collectors.toList());

        // 6.3.13.1.b. If no response (transport protocol RTS or NACK(Busy) in 220 ms), then retry DS DM25 request to
        // the OBD ECU.
        // [Do not attempt retry for NACKS that indicate not supported]
        var responseAddresses = filterRequestResultPackets(responses)
                                                                     .stream()
                                                                     .map(ParsedPacket::getSourceAddress)
                                                                     .collect(Collectors.toSet());

        var nackAddresses = filterRequestResultAcks(responses)
                                                              .stream()
                                                              .filter(r -> r.getResponse() == NACK)
                                                              .map(ParsedPacket::getSourceAddress)
                                                              .collect(Collectors.toSet());

        List<Integer> missingAddresses = getDataRepository().getObdModuleAddresses();
        missingAddresses.removeAll(responseAddresses);
        missingAddresses.removeAll(nackAddresses);
        // DS request for missing DM25s
        missingAddresses.stream()
                        .map(address -> getCommunicationsModule().requestDM25(getListener(), address, get(DM24SPNSupportPacket.class, address, 1)))
                        .map(BusResult::requestResult)
                        .forEach(responses::add);

        // capture results with global and DS responses.
        List<DM25ExpandedFreezeFrame> packets = filterRequestResultPackets(responses);

        // 6.3.13.1.c. Translate and print in log file all received freeze frame data with data labels assuming data
        // received in order expected by DM24 response for visual check by test log reviewer.
        for (OBDModuleInformation moduleInformation : getDataRepository().getObdModules()) {
            DM24SPNSupportPacket dm24 = moduleInformation.get(DM24SPNSupportPacket.PGN, 1);
            if (dm24 == null) {
                // skip any modules that did not respond to DM24
                continue;
            }

            List<SupportedSPN> supportedSPNs = dm24.getFreezeFrameSPNsInOrder();
            int moduleAddress = moduleInformation.getSourceAddress();
            String moduleName = Lookup.getAddressName(moduleAddress);

            // for DM25 for this module
            packets.stream()
                   .filter(p -> p.getSourceAddress() == moduleAddress)
                   .findFirst()
                   .ifPresent(p -> {
                       p.setSupportedSpns(supportedSPNs);
                       for (FreezeFrame freezeFrame : p.getFreezeFrames()) {
                           getListener().onResult("6.3.13.1.c - Received from " + moduleName + ": ");
                           getListener().onResult(freezeFrame.toString());
                           getListener().onResult("");

                           // 6.3.13.2.e. Fail/warn per section A.2, Criteria for Freeze Frame Evaluation.
                           validator.reportWarnings(freezeFrame, getListener(), "6.3.13.2.e");
                       }
                   });
        }

        // 6.3.13.2.a. Fail if retry was required to obtain DM25 response.
        missingAddresses.stream()
                        .map(Lookup::getAddressName)
                        .forEach(moduleName -> addFailure("6.3.13.2.a - Retry was required to obtain DM25 response from "
                                + moduleName));

        // 6.3.13.2.b. Fail if no ECU has freeze frame data to report.
        boolean freezeFrameReceived = packets.stream().anyMatch(p -> !p.getFreezeFrames().isEmpty());
        if (!freezeFrameReceived) {
            addFailure("6.3.13.2.b - No ECU has freeze frame data to report");
        }

        // 6.3.13.2.c. Fail if received data does not match expected number of bytes based on DM24 supported SPN list
        // for that ECU.
        for (DM25ExpandedFreezeFrame dm25 : packets) {
            String moduleName = dm25.getModuleName();
            int expectedLength = getDataRepository().getObdModule(dm25.getSourceAddress())
                                                    .getFreezeFrameSPNs()
                                                    .stream()
                                                    .mapToInt(SupportedSPN::getLength)
                                                    .sum();
            for (FreezeFrame freezeFrame : dm25.getFreezeFrames()) {
                int actualLength = freezeFrame.getSpnData().length;
                if (expectedLength != actualLength) {
                    addFailure(
                               "6.3.13.2.c - Received data (" + actualLength
                                       + ") does not match expected number of bytes (" + expectedLength
                                       + ") based on DM24 supported SPN list for " + moduleName);
                }
            }
        }

        // 6.3.13.2.d. Fail if freeze frame data does not include the same SPN+FMI as DM6 pending DTC earlier in this
        // part.
        for (DM25ExpandedFreezeFrame dm25 : packets) {
            int address = dm25.getSourceAddress();
            var pendingDTCs = getDTCs(address);
            dm25.getFreezeFrames()
                .stream()
                .map(FreezeFrame::getDtc)
                .forEach(pendingDTCs::remove);

            if (!pendingDTCs.isEmpty()) {
                addFailure("6.3.13.2.d - Freeze frame data from " + dm25.getModuleName()
                        + " does not include the same SPN+FMI as DM6 Pending DTC earlier in this part.");
            }
        }

        // 6.3.13.2.f. Warn if more than 1 freeze frame data set is included in the response.
        packets.stream()
               .filter(p -> p.getFreezeFrames().size() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.3.13.2.e - More than 1 freeze frame data set is included in the response from "
                           + moduleName);
               });

        // 6.3.13.2.g. Fail if NACK not received from OBD ECUs that did not provide DM25 response to query.
        checkForNACKsDS(packets, filterRequestResultAcks(responses), "6.3.13.2.g");
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return new ArrayList<>(getDTCs(DM6PendingEmissionDTCPacket.class, address, 3));
    }

}

/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_ACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_REQ;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_ACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_REQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.SectionA5Verifier;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;;

/**
 * 6.9.3 DM22: Individual Clear/Reset of Active and Previously Active DTC
 */
public class Part09Step03Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    private final SectionA5Verifier verifier;

    Part09Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SectionA5Verifier(true, PART_NUMBER, STEP_NUMBER));
    }

    Part09Step03Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           SectionA5Verifier verifier) {
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
        this.verifier = verifier;
    }

    @Override
    protected void run() throws Throwable {
        verifier.setJ1939(getJ1939());

        // 6.9.3.1.a DS DM22 (PGN 49920) to OBD ECU(s) without a DM12 MIL on DTC stored
        // using the MIL On DTC SPN and FMI and control byte = 17, Request to Clear/Reset Active DTC.
        List<Integer> addresses = getDataRepository().getObdModuleAddresses()
                                                     .stream()
                                                     .filter(a -> Optional.ofNullable(get(DM12MILOnEmissionDTCPacket.class,
                                                                                          a,
                                                                                          9))
                                                                          .map(p -> !p.getMalfunctionIndicatorLampStatus()
                                                                                      .isActive())
                                                                          .orElse(true))
                                                     .collect(Collectors.toList());

        var dsResults = addresses.stream()
                                 .map(a -> getCommunicationsModule().requestDM22(getListener(),
                                                                                 a,
                                                                                 CLR_ACT_REQ,
                                                                                 0x7FFFF,
                                                                                 31))
                                 .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        var acks = filterAcks(dsResults);

        // 6.9.3.2.a. Fail if the ECU provides CLR_PA_ACK (as described in SAE J1939-73 paragraph 5.7.22).
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_PA_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.2.a - " + moduleName + " provided CLR_PA_ACK");
               });

        // 6.9.3.2.a. Fail if the ECU provides CLR_ACT_ACK (as described in SAE J1939-73 paragraph 5.7.22).
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.2.a - " + moduleName + " provided CLR_ACT_ACK");
               });

        // 6.9.3.2.b. Fail if the ECU provides J1939-21 ACK for PGN 49920.
        acks.stream()
            .filter(p -> p.getResponse() == ACK)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addFailure("6.9.3.2.b - " + moduleName
                        + " provided J1939-21 ACK for PGN 49920");
            });

        // 6.9.3.2.c. Fail if the ECU provides CLR_ACT_NACK with an acknowledgement code greater than 0.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_NACK && p.getAcknowledgementCode() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.2.c - " + moduleName
                           + " provided CLR_ACT_NACK with an acknowledgement code greater than 0");
               });

        // 6.9.3.2.c. Fail if the ECU provides CLR_PA_NACK with an acknowledgement code greater than 0.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_PA_NACK && p.getAcknowledgementCode() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.2.c - " + moduleName
                           + " provided CLR_PA_NACK with an acknowledgement code greater than 0");
               });

        // 6.9.3.3.a. Info: if DM22 (PGN 49920) [CLR]_PA_NACK or [CLR]_ACT_NACK is not received with an acknowledgement
        // code of 0.
        for (int address : addresses) {
            boolean found = packets.stream()
                                   .filter(p -> p.getSourceAddress() == address)
                                   .anyMatch(p -> (p.getControlByte() == CLR_PA_NACK
                                           || p.getControlByte() == CLR_ACT_NACK) && p.getAcknowledgementCode() == 0);
            if (!found) {
                addWarning("6.9.3.3.a - " + Lookup.getAddressName(address)
                        + " did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
            }
        }

        // 6.9.3.3.b. WARN: if J1939-21 NACK for PGN 49920 is received.
        acks.stream()
            .filter(p -> p.getResponse() == NACK)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addWarning("6.9.3.3.b - " + moduleName + " provided J1939-21 NACK for PGN 49920");
            });

        // 6.9.3.4.a. DS DM22 to OBD ECU with a DM12 MIL on DTC stored using the DM12 MIL On DTC SPN and FMI and
        // control byte = 1, Request to Clear/Reset Previously Active DTC.
        addresses = getDataRepository().getObdModuleAddresses()
                                       .stream()
                                       .filter(a -> Optional.ofNullable(get(DM12MILOnEmissionDTCPacket.class,
                                                                            a,
                                                                            9))
                                                            .map(p -> p.getMalfunctionIndicatorLampStatus()
                                                                       .isActive())
                                                            .orElse(false))
                                       .collect(Collectors.toList());

        dsResults = new ArrayList<>();
        for (int address : addresses) {
            for (DiagnosticTroubleCode dtc : getDTCs(address)) {
                dsResults.add(getCommunicationsModule().requestDM22(getListener(),
                                                                    address,
                                                                    CLR_PA_REQ,
                                                                    dtc.getSuspectParameterNumber(),
                                                                    dtc.getFailureModeIndicator()));
            }
        }

        packets = filterPackets(dsResults);
        acks = filterAcks(dsResults);

        // 6.9.3.5.a. Fail if the ECU provides DM22 with CLR_PA_ACK .
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_PA_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.5.a - " + moduleName + " provided CLR_PA_ACK");
               });

        // 6.9.3.5.a. Fail if the ECU provides DM22 with CLR_ACT_ACK.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.5.a - " + moduleName + " provided CLR_ACT_ACK");
               });

        // 6.9.3.5.b. Fail if the ECU provides J1939-21 ACK for PGN 49920.
        acks.stream()
            .filter(p -> p.getResponse() == ACK)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addFailure("6.9.3.5.b - " + moduleName + " provided J1939-21 ACK for PGN 49920");
            });

        // 6.9.3.5.c. Fail if the ECU provides CLR_ACT_NACK with an acknowledgement code greater than 0.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_NACK && p.getAcknowledgementCode() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.5.c - " + moduleName
                           + " provided CLR_ACT_NACK with an acknowledgement code greater than 0");
               });

        // 6.9.3.6.a. Warn if DM22 (PGN 49920) [CLR]_PA_NACK or [CLR]_ACT_NACK is not received with an acknowledgement
        // code of 0.
        for (int address : addresses) {
            boolean found = packets.stream()
                                   .filter(p -> p.getSourceAddress() == address)
                                   .anyMatch(p -> (p.getControlByte() == CLR_PA_NACK
                                           || p.getControlByte() == CLR_ACT_NACK) && p.getAcknowledgementCode() == 0);
            if (!found) {
                // FIXME forth coming VERSION 9
                addInfo("6.9.3.6.a - " + Lookup.getAddressName(address)
                        + " did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
            }
        }

        // 6.9.3.6.b. Warn if J1939-21 NACK for PGN 49920 is received.
        acks.stream()
            .filter(p -> p.getResponse() == NACK)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addWarning("6.9.3.6.b - " + moduleName + " provided J1939-21 NACK for PGN 49920");
            });

        // 6.9.3.7.a. Global DM22 using DM12 MIL On DTC SPN and FMI with control byte = 1, Request to Clear/Reset
        // Previously Active DTC.
        var dtc = getDataRepository().getObdModules()
                                     .stream()
                                     .flatMap(m -> getDTCs(m.getSourceAddress()).stream())
                                     .findFirst();
        if (dtc.isEmpty()) {
            addInfo("6.9.3.7.a No DTC found to clear.");
        }
        var globalResults = getCommunicationsModule().requestDM22(getListener(),
                                                                  CLR_PA_REQ,
                                                                  dtc.map(d -> d.getSuspectParameterNumber())
                                                                     .orElse(0x7FFFF),
                                                                  dtc.map(d -> d.getFailureModeIndicator()).orElse(31));
        packets = globalResults.getPackets();
        acks = globalResults.getAcks();

        // 6.9.3.8.a. Fail if any ECU provides DM22 with CLR_PA_ACK.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_PA_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.8.a - " + moduleName + " provided DM22 with CLR_PA_ACK");
               });

        // 6.9.3.8.a. Fail if any ECU provides DM22 with CLR_ACT_ACK.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.8.a - " + moduleName + " provided DM22 with CLR_ACT_ACK");
               });

        // 6.9.3.8.b. Fail if any ECU provides J1939-21 ACK for PGN 49920.
        acks.stream()
            .filter(p -> p.getResponse() == ACK)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addFailure("6.9.3.8.b - " + moduleName + " provided J1939-21 ACK for PGN 49920");
            });

        // 6.9.3.8.c. Fail if any ECU provides CLR_ACT_NACK or CLR_PA_NACK with an acknowledgement code greater than 0.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_NACK && p.getAcknowledgementCode() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.8.c - " + moduleName
                           + " provided CLR_ACT_NACK with an acknowledgement code greater than 0");
               });

        // 6.9.3.8.c. Fail if any ECU provides CLR_PA_NACK with an acknowledgement code greater than 0.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_PA_NACK && p.getAcknowledgementCode() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.8.c - " + moduleName
                           + " provided CLR_PA_NACK with an acknowledgement code greater than 0");
               });

        // 6.9.3.9.a. Global DM22 using DM12 MIL On DTC SPN and FMI with control byte = 17, Request to Clear/Reset
        // Active DTC.
        globalResults = getCommunicationsModule().requestDM22(getListener(), CLR_ACT_REQ, 0x7FFFF, 31);
        packets = globalResults.getPackets();
        acks = globalResults.getAcks();

        // 6.9.3.10.a. Fail if any ECU provides CLR_PA_ACK.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_PA_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.10.a - " + moduleName + " provided DM22 with CLR_PA_ACK");
               });

        // 6.9.3.10.a. Fail if any ECU provides CLR_ACT_ACK.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.10.a - " + moduleName + " provided DM22 with CLR_ACT_ACK");
               });

        // 6.9.3.10.b. Fail if any ECU provides J1939-21 ACK for PGN 49920.
        acks.stream()
            .filter(p -> p.getResponse() == ACK)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addFailure("6.9.3.10.b - " + moduleName + " provided J1939-21 ACK for PGN 49920");
            });

        // 6.9.3.10.c. Fail if any ECU provides CLR_ACT_NACK with an acknowledgement code greater than 0.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_ACT_NACK && p.getAcknowledgementCode() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.10.c - " + moduleName
                           + " provided CLR_ACT_NACK with an acknowledgement code greater than 0");
               });

        // 6.9.3.10.c. Fail if any ECU provides CLR_PA_NACK with an acknowledgement code greater than 0.
        packets.stream()
               .filter(p -> p.getControlByte() == CLR_PA_NACK && p.getAcknowledgementCode() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.3.10.c - " + moduleName
                           + " provided CLR_PA_NACK with an acknowledgement code greater than 0");
               });

        // 6.9.3.10.d. Fail if any OBD ECU erases any diagnostic information. See Section A.5 for more information.
        verifier.verifyDataNotErased(getListener(), "6.9.3.10.d");
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, address, 9);
    }
}

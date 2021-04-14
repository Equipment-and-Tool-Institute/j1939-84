/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.BUSY;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.StringUtils;

/**
 * 6.1.7 DM19: Calibration Information
 */
public class Part01Step07Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part01Step07Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new DiagnosticMessageModule());
    }

    Part01Step07Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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
        // 6.1.7.1.a. Global DM19 (send Request (PGN 59904) for PGN 54016
        // 6.1.7.1.c. Display this list in the log.
        var globalPackets = getVehicleInformationModule().requestDM19(getListener());

        // 6.1.7.1.a.b. Create list of ECU address + CAL ID + CVN
        globalPackets.forEach(this::save);

        // 6.1.7.2.a. Fail if total number of reported CAL IDs is < user entered value for number of emission or
        // diagnostic critical control units (test 6.1.2).
        List<String> calIds = globalPackets.stream()
                                           .map(DM19CalibrationInformationPacket::getCalibrationInformation)
                                           .flatMap(Collection::stream)
                                           .map(CalibrationInformation::getCalibrationIdentification)
                                           .collect(Collectors.toList());
        if (calIds.size() < getCalIdCount()) {
            addFailure("6.1.7.2.a - Total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units");
        }

        // 6.1.7.3.b - Warn if more than one CAL ID and CVN pair is provided in a single DM19 message.
        globalPackets.stream()
                     .filter(p -> p.getCalibrationInformation().size() > 1)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addWarning("6.1.7.3.b - " + moduleName
                                 + " provided more than one CAL ID and CVN pair in a single DM19 message");
                     });

        // 6.1.7.3.c.i. For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID.
        globalPackets.stream()
                     .filter(p -> !isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getCalibrationInformation().size() > 0)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addWarning("6.1.7.3.c.i - Non-OBD ECU " + moduleName + " provided CAL ID");
                     });

        // 6.1.7.2.b.i. For responses from OBD ECUs: Fail if <> 1 CVN for every CAL ID.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> {
                         for (CalibrationInformation calInfo : p.getCalibrationInformation()) {
                             String calId = calInfo.getCalibrationIdentification();
                             String cvn = calInfo.getCalibrationVerificationNumber();
                             if (calId.isEmpty() || cvn.isEmpty()) {
                                 return true;
                             }
                         }
                         return false;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.7.2.b.i - " + moduleName + " <> 1 CVN for every CAL ID");
                     });

        // 6.1.7.3.c.ii For responses from non-OBD ECUs: Warn if <> 1 CVN for every CAL ID.
        globalPackets.stream()
                     .filter(p -> !isObdModule(p.getSourceAddress()))
                     .filter(p -> {
                         for (CalibrationInformation calInfo : p.getCalibrationInformation()) {
                             String calId = calInfo.getCalibrationIdentification();
                             String cvn = calInfo.getCalibrationVerificationNumber();
                             if (calId.isEmpty() || cvn.isEmpty()) {
                                 return true;
                             }
                         }
                         return false;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.7.3.c.ii - " + moduleName + " <> 1 CVN for every CAL ID");
                     });

        // 6.1.7.3.b.ii. Fail if CAL ID not formatted correctly (printable ASCII, padded incorrectly, etc.).
        // 6.1.7.3.c.iii. Warn if CAL ID not formatted correctly (contains non-printable ASCII, padded
        // incorrectly, etc.).
        for (DM19CalibrationInformationPacket packet : globalPackets) {
            List<CalibrationInformation> calInfoList = packet.getCalibrationInformation();
            for (CalibrationInformation calInfo : calInfoList) {
                boolean isObdModule = isObdModule(packet.getSourceAddress());
                String calId = calInfo.getCalibrationIdentification();

                if (StringUtils.containsNonPrintableAsciiCharacter(calId)) {
                    String moduleName = packet.getModuleName();
                    if (isObdModule) {
                        addFailure("6.1.7.2.b.ii - " + moduleName
                                + " CAL ID not formatted correctly (contains non-printable ASCII)");
                    } else {
                        addWarning("6.1.7.3.c.iii - " + moduleName
                                + " CAL ID not formatted correctly (contains non-printable ASCII)");
                    }
                }

                byte[] rawCalId = calInfo.getRawCalId();
                boolean paddingStarted = false;
                for (byte val : rawCalId) {
                    if (val == 0) {
                        paddingStarted = true;
                    } else if (paddingStarted) {
                        String moduleName = packet.getModuleName();
                        if (isObdModule(packet.getSourceAddress())) {
                            addFailure("6.1.7.2.b.ii - " + moduleName
                                    + " CAL ID not formatted correctly (padded incorrectly)");
                        } else {
                            addWarning("6.1.7.3.c.iii - " + moduleName
                                    + " CAL ID not formatted correctly (padded incorrectly)");
                        }
                    }
                }
            }
        }

        // 6.1.7.2.b.iii. Fail if any received CAL ID is all 0xFF or any CVN is all 0x00.
        // 6.1.7.3.c.iv. Warn if any received CAL ID is all 0xFF or any CVN is all 0x00.
        for (DM19CalibrationInformationPacket packet : globalPackets) {
            List<CalibrationInformation> calInfoList = packet.getCalibrationInformation();
            for (CalibrationInformation calInfo : calInfoList) {
                byte[] rawCalId = calInfo.getRawCalId();
                boolean allFF = rawCalId.length > 0;
                for (byte val : rawCalId) {
                    if ((val & 0xFF) != 0xFF) {
                        allFF = false;
                        break;
                    }
                }
                if (allFF) {
                    String moduleName = packet.getModuleName();
                    if (isObdModule(packet.getSourceAddress())) {
                        addFailure("6.1.7.2.b.iii - Received CAL ID is all 0xFF from " + moduleName);
                    } else {
                        addWarning("6.1.7.3.c.iv - Received CAL ID is all 0xFF from " + moduleName);
                    }
                }
            }

            for (CalibrationInformation calInfo : calInfoList) {
                byte[] rawCvn = calInfo.getRawCvn();
                boolean allZeros = rawCvn.length > 0;
                for (byte val : rawCvn) {
                    if (val != 0x00) {
                        allZeros = false;
                        break;
                    }
                }
                if (allZeros) {
                    String moduleName = packet.getModuleName();
                    if (isObdModule(packet.getSourceAddress())) {
                        addFailure("6.1.7.2.b.iii - Received CVN is all 0x00 from " + moduleName);
                    } else {
                        addFailure("6.1.7.3.c.iv Received CVN is all 0x00 from " + moduleName);
                    }
                }
            }
        }

        // 6.1.7.4.a. Destination Specific (DS) DM19 to each OBD ECU (plus all
        // ECUs that responded to global DM19).
        var dsResults = Stream.concat(getDataRepository().getObdModuleAddresses().stream(),
                                      globalPackets.stream().map(ParsedPacket::getSourceAddress))
                              .distinct()
                              .sorted()
                              .map(a -> getVehicleInformationModule().requestDM19(getListener(), a))
                              .collect(Collectors.toList());

        // 6.1.7.5.a Compare to ECU address + CAL ID + CVN list created from global DM19 request and fail if any
        // difference.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.1.7.5.a");

        // 6.1.7.5.b Fail if NACK (PGN 59392) with mode/control byte = 3 (busy) received.
        filterAcks(dsResults).stream()
                             .filter(p -> p.getResponse() == BUSY)
                             .map(ParsedPacket::getModuleName)
                             .forEach(moduleName -> {
                                 addFailure("6.1.7.5.b - " + moduleName
                                         + " responded NACK with control byte = 3 (busy)");
                             });

        // 6.1.7.5.c Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.1.7.5.c");
    }

    private int getCalIdCount() {
        return getDataRepository().getVehicleInformation().getCalIds();
    }
}

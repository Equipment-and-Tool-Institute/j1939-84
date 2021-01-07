/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.StringUtils;

public class Step07Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    Step07Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                dataRepository,
                DateTimeModule.getInstance());
    }

    Step07Controller(Executor executor,
            EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule,
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
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        // 6.1.7.1.a. Global DM19 (send Request (PGN 59904) for PGN 54016
        List<DM19CalibrationInformationPacket> globalDM19s = getVehicleInformationModule()
                .reportCalibrationInformation(getListener());

        // 6.1.7.1.a.b. Create list of ECU address + CAL ID + CVN
        for (DM19CalibrationInformationPacket packet : globalDM19s) {
            int sourceAddress = packet.getSourceAddress();
            OBDModuleInformation info = dataRepository.getObdModule(sourceAddress);
            if (info == null) {
                info = new OBDModuleInformation(sourceAddress);
            }
            info.setCalibrationInformation(packet.getCalibrationInformation());
            dataRepository.putObdModule(sourceAddress, info);
        }

        List<String> calIds = globalDM19s.stream()
                .map(DM19CalibrationInformationPacket::getCalibrationInformation)
                .flatMap(Collection::stream)
                .map(CalibrationInformation::getCalibrationIdentification)
                .collect(Collectors.toList());
        int expectedCalIdCount = dataRepository.getVehicleInformation().getCalIds();
        if (calIds.size() < expectedCalIdCount) {
            addFailure(PART_NUMBER,
                    STEP_NUMBER,
                    "6.1.7.2.a Total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units");
        } else if (calIds.size() > expectedCalIdCount) {
            addWarning(PART_NUMBER,
                    STEP_NUMBER,
                    "6.1.7.3.a Total number of reported CAL IDs is > user entered value for number of emission or diagnostic critical control units");
        }

        for (DM19CalibrationInformationPacket packet : globalDM19s) {
            boolean isObdModule = dataRepository.getObdModule(packet.getSourceAddress()) != null;
            List<CalibrationInformation> calInfoList = packet.getCalibrationInformation();
            if (calInfoList.size() > 1) {
                addWarning(PART_NUMBER,
                        STEP_NUMBER,
                        "6.1.7.3.b More than one CAL ID and CVN pair is provided in a single DM19 message");
            }
            if (!isObdModule && calInfoList.size() > 0) {
                addWarning(PART_NUMBER, STEP_NUMBER, "6.1.7.3.c.i Warn if any non-OBD ECU provides CAL ID");
            }

            for (CalibrationInformation calInfo : calInfoList) {
                String calId = calInfo.getCalibrationIdentification();
                String cvn = calInfo.getCalibrationVerificationNumber();
                if (calId.isEmpty() || cvn.isEmpty()) {
                    if (isObdModule) {
                        addFailure(PART_NUMBER, STEP_NUMBER, "6.1.7.2.b.i <> 1 CVN for every CAL ID");
                    } else {
                        addWarning(PART_NUMBER, STEP_NUMBER, "6.1.7.3.c.ii <> 1 CVN for every CAL ID");
                    }
                }
                if (StringUtils.containsNonPrintableAsciiCharacter(calId)) {
                    if (isObdModule) {
                        addFailure(PART_NUMBER, STEP_NUMBER,
                                "6.1.7.2.b.ii CAL ID not formatted correctly (contains non-printable ASCII)");
                    } else {
                        addWarning(PART_NUMBER, STEP_NUMBER,
                                "6.1.7.3.c.iii Warn if CAL ID not formatted correctly (contains non-printable ASCII)");
                    }
                }

                byte[] rawCalId = calInfo.getRawCalId();
                boolean paddingStarted = false;
                for (byte val : rawCalId) {
                    if (val == 0) {
                        paddingStarted = true;
                    } else if (paddingStarted) {
                        if (isObdModule) {
                            addFailure(PART_NUMBER, STEP_NUMBER,
                                    "6.1.7.2.b.ii CAL ID not formatted correctly (padded incorrectly)");
                        } else {
                            addWarning(PART_NUMBER, STEP_NUMBER,
                                    "6.1.7.3.c.iii CAL ID not formatted correctly (padded incorrectly)");
                        }
                        break;
                    }
                }

                boolean allFF = rawCalId.length > 0;
                for (byte val : rawCalId) {
                    if ((val & 0xFF) != 0xFF) {
                        allFF = false;
                        break;
                    }
                }
                if (allFF) {
                    if (isObdModule) {
                        addFailure(PART_NUMBER, STEP_NUMBER, "6.1.7.2.b.iii Received CAL ID is all 0xFF");
                    } else {
                        addWarning(PART_NUMBER, STEP_NUMBER, "6.1.7.3.c.iv Received CAL ID is all 0xFF");
                    }
                }
                byte[] rawCvn = calInfo.getRawCvn();
                boolean allZeros = rawCvn.length > 0;
                for (byte val : rawCvn) {
                    if (val != 0x00) {
                        allZeros = false;
                        break;
                    }
                }
                if (allZeros) {
                    if (isObdModule) {
                        addFailure(PART_NUMBER, STEP_NUMBER, "6.1.7.2.b.iii Received CVN is all 0x00");
                    } else {
                        addFailure(PART_NUMBER, STEP_NUMBER, "6.1.7.3.c.iv Received CVN is all 0x00");
                    }
                }
                // TODO 6.1.7.2.b.iv. Fail if CVN padded incorrectly (must use 0x00 in MSB for unused bytes)
                // TODO 6.1.7.3.c.v. Warn if CVN padded incorrectly (must use 0x00 in MSB for unused bytes)
            }
        }

        // 6.1.7.4.a. Destination Specific (DS) DM19 to each OBD ECU (plus all
        // ECUs that responded to global DM19).
        globalDM19s.forEach(dm19 -> {
            BusResult<DM19CalibrationInformationPacket> result = getVehicleInformationModule()
                    .reportCalibrationInformation(
                            getListener(),
                            dm19.getSourceAddress());
            result.getPacket()
                    // treat NACKs as failed response
                    .flatMap(e -> e.left)
                    // filter for only those that match
                    .filter(info -> Objects.equals(dm19.getCalibrationInformation(), info.getCalibrationInformation()))
                    // report everything that failed to respond or doesn't match
                    .ifPresentOrElse(x -> {
                    }, () -> addFailure(PART_NUMBER,
                            STEP_NUMBER,
                            "6.1.7.5.a Compared ECU address + CAL ID + CVN list created from global DM19 request and found difference "
                                    + dm19.getCalibrationInformation()));
            if (result.isRetryUsed()) {
                addFailure(PART_NUMBER, STEP_NUMBER,
                        "6.1.7.5.b NACK (PGN 59392) with mode/control byte = 3 (busy) received");
            }
        });

        Set<Integer> globalAddresses = globalDM19s.stream()
                .map(ParsedPacket::getSourceAddress)
                .collect(Collectors.toSet());
        List<Integer> obdAddresses = dataRepository.getObdModuleAddresses();
        obdAddresses.removeAll(globalAddresses);

        for (int address : obdAddresses) {
            BusResult<DM19CalibrationInformationPacket> results = getVehicleInformationModule()
                    .reportCalibrationInformation(
                            getListener(),
                            address);
            results.getPacket()
                    .flatMap(e -> e.left)
                    // if there is a DM19, then there was not a NACK
                    .ifPresent(dm19 -> getListener().addOutcome(1,
                            7,
                            Outcome.FAIL,
                            "6.1.7.5.c NACK not received from OBD ECU that did not respond to global query"));
        }
    }
}

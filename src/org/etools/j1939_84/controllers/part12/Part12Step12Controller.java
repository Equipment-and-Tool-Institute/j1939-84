/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.j1939.J1939DaRepository;
import net.soliddesign.j1939tools.j1939.Lookup;
import net.soliddesign.j1939tools.j1939.model.Spn;
import net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket;
import net.soliddesign.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import net.soliddesign.j1939tools.j1939.packets.Slot;
import net.soliddesign.j1939tools.j1939.packets.SupportedSPN;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.12.11 DM7/ DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part12Step12Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part12Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step12Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
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
    }

    @Override
    protected void run() throws Throwable {

        // DM7/ DM30: Command Non-Continuously Monitored Test/Scaled Test Results
        // Actions
        // a. DS DM7 with TID 250 for specific ECU address + SPN + FMI that had non-initialized values earlier in part
        // 12 test 8.
        // b. Use responses to help verify coordinated DM11 code clear in this part (i.e., all or no ECUs clear).
        // For example, this will be evident in the increased count of initialized test results for the SPN and FMI
        // pairs that were listed with non-initialized values.
        // Actions2:
        // a. DS DM7 with TID 245 (for DM58) using FMI 31 for each SP identified as supporting DM58 in a DM24 response
        // In step 6.1.4.1 to the SP’s respective OBD ECU.
        // b. Display the scaled engineering value for the requested SP.
        // Fail/Warn criteria2:
        Collection<OBDModuleInformation> obdModules = getDataRepository().getObdModules();
        obdModules.forEach(module -> {
            module.getSupportedSPNs()
                  .stream()
                  .filter(SupportedSPN::supportsRationalityFaultData)
                  .forEach(supportedSPN -> {
                      var packets = getCommunicationsModule().requestDM58(getListener(),
                                                                          module.getSourceAddress(),
                                                                          supportedSPN.getSpn())
                                                             .requestResult()
                                                             .getEither()
                                                             .stream()
                                                             .peek(p -> {
                                                                 String moduleName = Lookup.getAddressName(module.getSourceAddress());
                                                                 if (p.right.isPresent()) {
                                                                     // 12.12.a. Fail if NACK received for DM7 PG from
                                                                     // OBD ECU
                                                                     addFailure("6.12.12.a - NACK received for DM7 PG from OBD ECU "
                                                                             + moduleName);
                                                                 }
                                                                 if (p.left.isPresent()) {
                                                                     DM58RationalityFaultSpData packet = p.resolve();
                                                                     // c. Fail, if expected unused bytes in DM58 are
                                                                     // not padded with FFh
                                                                     if (!areUnusedBytesPaddedWithFFh(packet)) {
                                                                         addFailure("6.12.12.c - Unused bytes in DM58 are not padded with FFh in the response from "
                                                                                 + moduleName);
                                                                     }
                                                                     // d. Fail, if data returned is greater than FBh
                                                                     // (for 1 byte SP), FBFFh (for 2 byte SP), or
                                                                     // FBFFFFFFh (for 4 byte SP)
                                                                     if (isGreaterThanFb(packet)) {
                                                                         addFailure("6.12.12.d - Data returned is greater than 0xFB... threshold");
                                                                     }
                                                                 }
                                                             })
                                                             .collect(Collectors.toList());

                      // b. Fail, if DM58 not received (after allowed retries)
                      if (packets.isEmpty()) {
                          addFailure("6.12.12.d - DM58 not received (after allowed retries)");
                      }

                  });
        });

        // Actions3:
        // a. DS DM7 with TID 245 (for DM58) using FMI 31 for first SP identified as not supporting DM58 in a DM24
        // response In step 6.1.4.1 to the SP’s respective OBD ECU. (Use of an SP that supports test results is
        // preferred when available).
        // Fail/Warn criteria3:
        // a. Fail if a NACK is not received
        obdModules.forEach(module -> {
            getDM58(module.getSourceAddress());
        });
    }

    private void getDM58(int moduleAddress) {
        var allSps = getDataRepository().getObdModules()
                                        .stream()
                                        .flatMap(m -> m.getFilteredDataStreamSPNs().stream())
                                        .collect(Collectors.toList());
        SupportedSPN requestSpn = null;

        if (allSps.isEmpty()) {
            addWarning("6.12.12.3 - No modules reported supported SPs");
        } else {
            requestSpn = allSps.get(0);
        }

        var nonRatFaultSps = allSps.stream()
                                   .filter(spn -> !spn.supportsRationalityFaultData())
                                   .collect(Collectors.toList());

        if (nonRatFaultSps.isEmpty()) {
            addWarning("6.12.12.3.a - No SPs found that do NOT indicate support for DM58 in the DM24 response - using first SP found");
        } else {
            requestSpn = nonRatFaultSps.get(0);
        }

        var supportTestResultsSp = nonRatFaultSps.stream()
                                                 .filter(SupportedSPN::supportsScaledTestResults)
                                                 .collect(
                                                          Collectors.toList());
        if (supportTestResultsSp.isEmpty()) {
            addWarning("6.12.12.3.a - No SPs found that do NOT indicate support for DM58 in the DM24 response && supports test result - using first SP found");
        } else {
            requestSpn = supportTestResultsSp.get(0);
        }

        if (requestSpn != null) {
            var packet = getCommunicationsModule().requestDM58(getListener(), moduleAddress, requestSpn.getSpn())
                                                  .requestResult()
                                                  .getAcks()
                                                  .stream()
                                                  .findFirst()
                                                  .orElse(null);
            if (packet == null) {
                addFailure("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU "
                        + Lookup.getAddressName(moduleAddress) + " for spn " + requestSpn.getSpn());
                return;
            }
            if (packet.getResponse() != AcknowledgmentPacket.Response.NACK) {
                addFailure("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU "
                        + Lookup.getAddressName(moduleAddress) + " "
                        + requestSpn.getSpn() + " returned a(n) " + packet.getResponse());
            }
        } else {
            addFailure("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU "
                    + Lookup.getAddressName(moduleAddress) + " has no recorded SPs");
        }
    }

    private boolean areUnusedBytesPaddedWithFFh(DM58RationalityFaultSpData packet) {
        Slot slot = J1939DaRepository.findSlot(packet.getSpn().getId(), packet.getSpnId());

        int slotLength = slot.getByteLength();
        int spnLength = packet.getSpnDataBytes().length;

        if (slotLength == spnLength) {
            return true;
        }
        byte[] paddingBytes = Arrays.copyOf(packet.getSpnDataBytes(), packet.getSpnDataBytes().length);

        switch (slotLength) {
            case 1:
                paddingBytes = Arrays.copyOfRange(packet.getSpnDataBytes(), slotLength, spnLength);
                break;
            case 2:
                byte[] packetBytes = Arrays.copyOf(packet.getSpnDataBytes(), packet.getSpnDataBytes().length);
                paddingBytes = new byte[] { packetBytes[0], packetBytes[2], packetBytes[3] };
                break;
            case 4:
                // we don't get here unless we are supported...
                return !allBytesAreFF(paddingBytes);
            default: {
                break;
            }
        }
        return allBytesAreFF(paddingBytes);
    }

    private boolean allBytesAreFF(byte[] dataBytes) {
        for (byte bYte : dataBytes) {
            if (bYte != (byte) 0xFF) {
                return false;
            }
        }
        return true;
    }

    private boolean isGreaterThanFb(DM58RationalityFaultSpData packet) {
        Spn spn = packet.getSpn();
        long rawValue = spn.getRawValue();

        switch (spn.getSlot().getByteLength()) {
            case 1:
                return rawValue > 0xFB;
            case 2:
                return rawValue > 0xFBFF;
            case 4:
                return rawValue > 0xFBFFFFFFL;
            default:
                break;
        }
        return false;
    }
}

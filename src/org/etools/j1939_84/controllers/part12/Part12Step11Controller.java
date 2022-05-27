/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnFmi;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.12.10 DM7/ DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part12Step11Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part12Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step11Controller(Executor executor,
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
        // 6.12.11.1.a. DS DM7 with TID 250 for specific ECU address + SPN + FMI that had non-initialized values earlier
        // in part 12 test 8.
        for (OBDModuleInformation moduleInformation : getDataRepository().getObdModules()) {
            moduleInformation.getNonInitializedTests()
                             .stream()
                             .map(SpnFmi::of)
                             .distinct()
                             .forEach(k -> {
                                 getCommunicationsModule().requestTestResults(getListener(),
                                                                              moduleInformation.getSourceAddress(),
                                                                              250,
                                                                              k.spn,
                                                                              k.fmi);
                             });
        }

        // 6.12.11.1.b. Use responses to help verify coordinated DM11 code clear in this part
        // (i.e., all or no ECUs clear). For example, this will be evident in the increased count of initialized test
        // results for the SPN and FMI pairs that were listed with non-initialized values.

        // 6.12.11.2 Actions2:
        // a. DS DM7 with TID 245 (for DM58) using FMI 31 for each SP identified as supporting DM58 in a DM24 response
        // In step 6.1.4.1 to the SP’s respective OBD ECU.
        // b. Display the scaled engineering value for the requested SP.
        getDataRepository().getObdModules().stream().forEach(module -> {
            module.getSupportedSPNs()
                  .stream()
                  .filter(SupportedSPN::supportsRationalityFaultData)
                  .forEach(spn -> {
                      getCommunicationsModule().requestDM58(getListener(),
                                                            module.getSourceAddress(),
                                                            spn.getSpn())
                                               .requestResult()
                                               .getEither()
                                               .stream()
                                               .findFirst()
                                               .ifPresentOrElse(response -> {
                                                   // // 6.12.11.3.a - Fail if NACK received for DM7 PG
                                                   // from OBD ECU
                                                   if (response.right.isPresent()) {
                                                       addFailure("6.12.11.3.a - NACK received for DM7 PG from OBD ECU from "
                                                               +
                                                               module.getModuleName() + " for SP " + spn);
                                                   } else {
                                                       var dm58 = response.left;

                                                       DM58RationalityFaultSpData packet = dm58.get();
                                                       // c. Fail, if expected unused bytes in DM58 are
                                                       // not padded with FFh
                                                       if (!areUnusedBytesPaddedWithFFh(packet)) {
                                                           addFailure(
                                                                      "6.12.12.3.c - Unused bytes in DM58 are not padded with FFh in the response from "
                                                                              + module.getModuleName()
                                                                              + " for SP " + spn);
                                                       }
                                                       // d. Fail, if data returned is greater than FBh
                                                       // (for 1 byte SP), FBFFh (for 2 byte SP), or
                                                       // FBFFFFFFh (for 4 byte SP)
                                                       if (isGreaterThanFb(packet)) {
                                                           addFailure(
                                                                      "6.12.12.3.d - Data returned is greater than 0xFB... threshold from "
                                                                              + module.getModuleName()
                                                                              + " for " + spn);

                                                       }
                                                   }
                                               },
                                                                () -> {
                                                                    // 6.12.11.3.b. Fail, if DM58 not
                                                                    // received
                                                                    addFailure("6.12.11.3.b. DM58 not received from "
                                                                            +
                                                                            module.getModuleName()
                                                                            + " for SP " + spn);
                                                                });
                  });
            getDm58AndVerifyData(module.getSourceAddress());
        });
    }

    private void getDm58AndVerifyData(int moduleAddress) {

        // 6.12.11.4 Actions3:
        // a. DS DM7 with TID 245 (for DM58) using FMI 31 for first SP identified as not supporting DM58 in a DM24
        // response In step 6.1.4.1 to the SP’s respective OBD ECU. (Use of an SP that supports test results is
        // preferred when available).
        var nonRatFaultSps = getDataRepository().getObdModules()
                                                .stream()
                                                .filter(module -> module.getSourceAddress() == moduleAddress)
                                                .flatMap(m -> m.getSupportedSPNs().stream())
                                                .filter(supported -> !supported.supportsRationalityFaultData())
                                                .collect(Collectors.toList());
        if (nonRatFaultSps.isEmpty()) {
            getListener().onResult("6.12.11.4.a - No SPs found that do NOT indicate support for DM58 in the DM24 response from "
                    + Lookup.getAddressName(moduleAddress));
        } else {
            int requestSpn = nonRatFaultSps.stream()
                                           .filter(SupportedSPN::supportsScaledTestResults)
                                           .findFirst()
                                           .orElseGet(() -> nonRatFaultSps.get(0))
                                           .getSpn();

            var packet = getCommunicationsModule().requestDM58(getListener(), moduleAddress, requestSpn)
                                                  .requestResult()
                                                  .getAcks()
                                                  .stream()
                                                  .findFirst()
                                                  .orElse(null);

            // 6.12.11.5 Fail/Warn criteria3:
            // a. Fail if a NACK is not received
            if (packet == null || packet.getResponse() != AcknowledgmentPacket.Response.NACK) {
                addFailure("6.12.11.5.a - NACK not received for DM7 PG from OBD ECU "
                        + Lookup.getAddressName(moduleAddress) + " for spn " + requestSpn);
            }
        }
    }

    private boolean areUnusedBytesPaddedWithFFh(DM58RationalityFaultSpData packet) {
        Slot slot = J1939DaRepository.findSlot(packet.getSpn().getSlot().getId(), packet.getSpn().getId());

        int slotLength = slot.getByteLength();
        int spnLength = packet.getSpnDataBytes().length;

        byte[] paddingBytes;

        switch (slotLength) {
            case 1:
                paddingBytes = new byte[] { packet.getSpnDataBytes()[1], packet.getSpnDataBytes()[2],
                        packet.getSpnDataBytes()[3] };
                break;
            case 2:
                paddingBytes = new byte[] { packet.getSpnDataBytes()[2], packet.getSpnDataBytes()[3] };
                break;
            case 3:
                paddingBytes = new byte[] { packet.getSpnDataBytes()[3] };
                break;
            case 4:
                return true;
            default: {
                getListener().onResult("Not checking for FF - SP " + packet.getSpn() + " length is " + slotLength);
                return true;
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

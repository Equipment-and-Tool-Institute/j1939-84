/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.6 DM56: Model year and certification engine family
 */
public class Part01Step06Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part01Step06Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    Part01Step06Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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

        // 6.1.6.1.a. Global DM56 (send Request (PGN 59904) for PGN 64711 (SPNs 5844 and 5845)).
        List<DM56EngineFamilyPacket> packets = getCommunicationsModule().requestDM56(getListener());
        int engineModelYear = getEngineModelYear();

        if (packets.isEmpty()) {
            if (engineModelYear > 2024) {
                addFailure("6.1.6.2.f - MY2024+ Engine does not support DM56");
            } else {
                getListener().onResult("6.1.6.1.a - DM56 is not supported");
            }
            return;
        }

        packets.forEach(this::save);

        for (DM56EngineFamilyPacket packet : packets) {
            if (packet.getEngineModelYear() != engineModelYear) {
                addFailure("6.1.6.2.a - Engine model year does not match user input");
                break;
            }
        }

        for (DM56EngineFamilyPacket packet : packets) {
            String type = packet.getModelYearField().substring(4, 5);
            if ("V".equals(type)) {
                addFailure("6.1.6.2.b - Indicates 'V' instead of 'E' for cert type");
                break;
            }
        }

        for (DM56EngineFamilyPacket packet : packets) {
            String modelYearField = packet.getModelYearField();
            String expected = packet.getEngineModelYear() + "E-MY";

            if (!expected.equals(modelYearField)) {
                addFailure("6.1.6.2.c - Not formatted correctly");
                break;
            }
        }

        for (DM56EngineFamilyPacket packet : packets) {
            // TODO: See the citation for Karl Simon’s manufacturer guidance in 2.1.3.
            // The description of the coding for engine model year is defined in CSID-07-03,
            // a manufacturer letter that is available from US EPA at
            // http://iaspub.epa.gov/otaqpub/publist_gl.jsp?guideyear=2007
            //
            // d. Fail if MY designation in engine family (1st digit) does not match user MY input

            String familyName = packet.getFamilyName();
            char char13 = familyName.length() >= 14 ? familyName.charAt(13) : Character.MIN_VALUE;

            int asteriskIndex = familyName.indexOf('*');

            if ((-1 < asteriskIndex && asteriskIndex <= 12)
                    || (char13 != Character.MIN_VALUE && char13 != '*' && familyName.contains("*"))) {
                addFailure(
                           "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
                break;
            } else if (familyName.length() < 13 || !familyName.contains("*") && char13 != Character.MIN_VALUE) {
                addFailure("6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
                break;
            }
        }
    }

}

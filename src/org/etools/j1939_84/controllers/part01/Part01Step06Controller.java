/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    Map<String, Integer> myCodes = new HashMap<>();
    {
        myCodes.put("1", 2001);
        myCodes.put("2", 2002);
        myCodes.put("3", 2003);
        myCodes.put("4", 2004);
        myCodes.put("5", 2005);
        myCodes.put("6", 2006);
        myCodes.put("7", 2007);
        myCodes.put("8", 2008);
        myCodes.put("9", 2009);

        myCodes.put("A", 2010);
        myCodes.put("B", 2011);
        myCodes.put("C", 2012);
        myCodes.put("D", 2013);
        myCodes.put("E", 2014);
        myCodes.put("F", 2015);
        myCodes.put("G", 2016);
        myCodes.put("H", 2017);
        myCodes.put("J", 2018);

        myCodes.put("K", 2019);
        myCodes.put("L", 2020);
        myCodes.put("M", 2021);
        myCodes.put("N", 2022);
        myCodes.put("P", 2023);
        myCodes.put("R", 2024);
        myCodes.put("S", 2025);
        myCodes.put("T", 2026);
        myCodes.put("V", 2027);
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
            String firstCharacter = packet.getFamilyName().substring(0, 1);
            Integer myCode = myCodes.get(firstCharacter);
            if (myCode == null || myCode != engineModelYear) {
                addFailure("6.1.6.2.d - MY designation in engine family (1st digit) does not match user MY input");
                break;
            }
        }

        for (DM56EngineFamilyPacket packet : packets) {
            String familyName = packet.getFamilyName();
            char char13 = familyName.length() >= 14 ? familyName.charAt(13) : Character.MIN_VALUE;

            int asteriskIndex = familyName.indexOf('*');

            if ((-1 < asteriskIndex && asteriskIndex <= 12)
                    || (char13 != Character.MIN_VALUE && char13 != '*' && familyName.contains("*"))) {
                addFailure(
                           "6.1.6.2.e - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
                break;
            } else if (familyName.length() < 13 || !familyName.contains("*") && char13 != Character.MIN_VALUE) {
                addFailure("6.1.6.2.e - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
                break;
            }
        }
    }

}

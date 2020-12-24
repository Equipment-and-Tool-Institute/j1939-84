/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step06Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    Step06Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                dataRepository);
    }

    Step06Controller(Executor executor,
            EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule,
            DataRepository dataRepository) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        // DM56: Model year and certification engine family
        List<DM56EngineFamilyPacket> packets = getVehicleInformationModule().reportEngineFamily(getListener());
        if (packets.isEmpty()) {
            getListener().onResult("DM56 is not supported");
            return;
        }

        for (DM56EngineFamilyPacket packet : packets) {
            if (packet.getEngineModelYear() != dataRepository.getVehicleInformation().getEngineModelYear()) {
                addFailure(1, 6, "6.1.6.2.a - Engine model year does not match user input");
            }

            String modelYearField = packet.getModelYearField();
            String type = modelYearField.substring(4, 5);
            if ("V".equals(type)) {
                addFailure(1, 6, "6.1.6.2.b - Indicates 'V' instead of 'E' for cert type");
            }

            String expected = packet.getEngineModelYear() + "E-MY";

            if (!expected.equals(modelYearField)) {
                addFailure(1, 6, "6.1.6.2.c - Not formatted correctly");
            }

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
                addFailure(1,
                        6,
                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
            } else if (familyName.length() < 13 || !familyName.contains("*") && char13 != Character.MIN_VALUE) {
                addFailure(1,
                        6,
                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
            }
        }
    }
}

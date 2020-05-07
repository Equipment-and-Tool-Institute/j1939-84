package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step06Controller extends Controller {

    private final DataRepository dataRepository;

    Step06Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(), dataRepository);
    }

    Step06Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 6";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
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
            // d. Fail if MY designation in engine family (1st digit) does not match user MY
            // input.11

            String familyName = packet.getFamilyName();

            // Zero based for char count in strings.
            char char13 = familyName.charAt(13);

            int asteriskIndex = familyName.indexOf('*');

            if ((-1 < asteriskIndex && asteriskIndex <= 12)
                    || (char13 != Character.MIN_VALUE && char13 != '*' && familyName.contains("*"))) {
                addFailure(1,
                        6,
                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
            } else if (familyName.indexOf("*") == -1 && char13 != Character.MIN_VALUE) {
                addFailure(1,
                        6,
                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
            }

        }

    }

}

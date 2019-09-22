/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The {@link Controller} for the Part 1 Tests
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class Part05Controller extends Controller {

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    /**
     * Map of OBD Module Source Address to {@link OBDModuleInformation}
     */
    private final Map<Integer, OBDModuleInformation> obdModules = new HashMap<>();

    private final OBDTestsModule obdTestsModule;

    /**
     * Constructor
     */
    public Part05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new DiagnosticReadinessModule(),
                new OBDTestsModule(), new SupportedSpnModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param executor                 the {@link ScheduledExecutorService}
     * @param engineSpeedModule        the {@link EngineSpeedModule}
     * @param bannerModule             the {@link BannerModule}
     * @param dateTimeModule           the {@link DateTimeModule}
     * @param vehicleInformationModule the {@link VehicleInformationModule}
     * @param obdTestsModule           the {@link OBDTestsModule}
     * @param supportedSpnModule       the {@link SupportedSpnModule}
     */
    public Part05Controller(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            DiagnosticReadinessModule diagnosticReadinessModule, OBDTestsModule obdTestsModule,
            SupportedSpnModule supportedSpnModule) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.obdTestsModule = obdTestsModule;
    }

    @Override
    public String getDisplayName() {
        return "Part 5 Test";
    }

    @Override
    protected int getTotalSteps() {
        return 7;
    }

    @Override
    protected void run() throws Throwable {
        diagnosticReadinessModule.setJ1939(getJ1939());
        obdTestsModule.setJ1939(getJ1939());

        executeTests(5, 7);
    }

}

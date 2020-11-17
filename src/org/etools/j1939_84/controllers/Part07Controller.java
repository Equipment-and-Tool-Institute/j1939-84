/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
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
public class Part07Controller extends Controller {

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    private final OBDTestsModule obdTestsModule;

    /**
     * Constructor
     */
    public Part07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new VehicleInformationModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), new OBDTestsModule(), new SupportedSpnModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param executor
     *            the {@link Executor}
     * @param engineSpeedModule
     *            the {@link EngineSpeedModule}
     * @param bannerModule
     *            the {@link BannerModule}
     * @param vehicleInformationModule
     *            the {@link VehicleInformationModule}
     * @param partResultFactory
     *            the {@link PartResultFactory}
     * @param diagnosticReadinessModule
     *            the {@link DiagnosticReadinessModule}
     * @param obdTestsModule
     *            the {@link OBDTestsModule}
     * @param supportedSpnModule
     *            the {@link SupportedSpnModule}
     */
    public Part07Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, DiagnosticReadinessModule diagnosticReadinessModule,
            OBDTestsModule obdTestsModule, SupportedSpnModule supportedSpnModule) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, partResultFactory);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.obdTestsModule = obdTestsModule;
    }

    @Override
    public String getDisplayName() {
        return "Part 7 Test";
    }

    @Override
    protected int getTotalSteps() {
        return 17;
    }

    @Override
    protected void run() throws Throwable {
        diagnosticReadinessModule.setJ1939(getJ1939());
        obdTestsModule.setJ1939(getJ1939());

        executeTests(7, 17);
    }

}

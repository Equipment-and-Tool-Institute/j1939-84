/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.mockito.ArgumentCaptor;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         This class provide the basic method for running a test on a
 *         class that extends {@link Controller}. It also provides a list
 *         of a type for easy mocking of packets and other lists of objects.
 *
 */
public abstract class AbstractControllerTest {

    private EngineSpeedModule engineSpeedModule;

    private Executor executor;
    private Controller instance;
    private J1939 j1939;
    private TestResultsListener listener;
    private ReportFileModule reportFileModule;
    private VehicleInformationModule vehicleInformationModule;

    /**
     *
     * @param <T>
     * @param item
     * @return List<T>
     */
    protected <T> List<T> listOf(T item) {
        List<T> result = new ArrayList<>();
        result.add(item);
        return result;
    }

    /**
     *
     */
    protected void runTest() {
        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        // Since we these interactions are mocked for every test that extends this
        // class, we need to verify them here.
        verify(engineSpeedModule).setJ1939(j1939);
        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     *
     * @param instance
     * @param listener
     * @param j1939
     * @param engineSpeedModule
     * @param reportFileModule
     * @param executor
     * @param vehicleInformationModule
     */
    protected void setup(Controller instance,
            TestResultsListener listener,
            J1939 j1939,
            EngineSpeedModule engineSpeedModule,
            ReportFileModule reportFileModule,
            Executor executor,
            VehicleInformationModule vehicleInformationModule) {
        this.instance = instance;
        this.listener = listener;
        this.engineSpeedModule = engineSpeedModule;
        this.j1939 = j1939;
        this.reportFileModule = reportFileModule;
        this.executor = executor;
        this.vehicleInformationModule = vehicleInformationModule;
    }

}

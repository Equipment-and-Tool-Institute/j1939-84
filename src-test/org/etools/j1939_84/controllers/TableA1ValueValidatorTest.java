/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939tools.j1939.model.FuelType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableA1ValueValidatorTest {

    private DataRepository dataRepository;

    private TableA1ValueValidator instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        instance = new TableA1ValueValidator(dataRepository);
    }

    @Test
    public void isImplausible() {
        assertFalse(instance.isImplausible(92, null, true));

        assertFalse(instance.isImplausible(92, 49.0, true));
        assertTrue(instance.isImplausible(92, 51.0, true));
        assertFalse(instance.isImplausible(92, 0.0, false));
        assertTrue(instance.isImplausible(92, 0.1, false));

        assertFalse(instance.isImplausible(513, 49.0, true));
        assertTrue(instance.isImplausible(513, 51.0, true));
        assertFalse(instance.isImplausible(513, 0.0, false));
        assertTrue(instance.isImplausible(513, 0.1, false));

        assertTrue(instance.isImplausible(512, 1.0, true));
        assertFalse(instance.isImplausible(512, 0.0, true));

        assertTrue(instance.isImplausible(91, 1.0, true));
        assertFalse(instance.isImplausible(91, 0.0, true));

        assertTrue(instance.isImplausible(514, 0.0, true));
        assertFalse(instance.isImplausible(514, 1.0, true));
        assertTrue(instance.isImplausible(514, 1.0, false));
        assertFalse(instance.isImplausible(514, 0.0, false));

        assertTrue(instance.isImplausible(2978, 8.1, true));
        assertFalse(instance.isImplausible(2978, 8.0, true));
        assertTrue(instance.isImplausible(2978, 1.0, false));
        assertFalse(instance.isImplausible(2978, 0.0, false));

        assertTrue(instance.isImplausible(539, 9.9, true));
        assertFalse(instance.isImplausible(539, 10.0, true));

        assertTrue(instance.isImplausible(540, 9.9, true));
        assertFalse(instance.isImplausible(540, 10.0, true));

        assertTrue(instance.isImplausible(541, 29.0, true));
        assertFalse(instance.isImplausible(541, 31.0, true));
        assertTrue(instance.isImplausible(541, 19.0, false));
        assertFalse(instance.isImplausible(541, 21.0, false));

        assertTrue(instance.isImplausible(542, 19.0, true));
        assertFalse(instance.isImplausible(542, 21.0, true));
        assertTrue(instance.isImplausible(542, 14.0, false));
        assertFalse(instance.isImplausible(542, 16.0, false));

        assertTrue(instance.isImplausible(543, 100.0, true));
        assertFalse(instance.isImplausible(543, 99.9, true));

        assertTrue(instance.isImplausible(110, -7.1, true));
        assertFalse(instance.isImplausible(110, 0.0, true));
        assertTrue(instance.isImplausible(110, 110.1, false));
        assertFalse(instance.isImplausible(110, 0.0, false));

        assertTrue(instance.isImplausible(1637, -7.1, true));
        assertFalse(instance.isImplausible(1637, 0.0, true));
        assertTrue(instance.isImplausible(1637, 110.1, false));
        assertFalse(instance.isImplausible(1637, 0.0, false));

        assertTrue(instance.isImplausible(4076, -7.1, true));
        assertFalse(instance.isImplausible(4076, 0.0, true));
        assertTrue(instance.isImplausible(4076, 110.1, false));
        assertFalse(instance.isImplausible(4076, 0.0, false));

        assertTrue(instance.isImplausible(4193, -7.1, true));
        assertFalse(instance.isImplausible(4193, 0.0, true));
        assertTrue(instance.isImplausible(4193, 110.1, false));
        assertFalse(instance.isImplausible(4193, 0.0, false));

        assertTrue(instance.isImplausible(3031, -7.1, true));
        assertFalse(instance.isImplausible(3031, 0.0, true));
        assertTrue(instance.isImplausible(3031, 110.1, false));
        assertFalse(instance.isImplausible(3031, 0.0, false));

        assertTrue(instance.isImplausible(3515, -7.1, true));
        assertFalse(instance.isImplausible(3515, 0.0, true));
        assertTrue(instance.isImplausible(3515, 110.1, false));
        assertFalse(instance.isImplausible(3515, 0.0, false));

        assertTrue(instance.isImplausible(190, 249.0, true));
        assertFalse(instance.isImplausible(190, 251.0, true));
        assertTrue(instance.isImplausible(190, 1.0, false));
        assertFalse(instance.isImplausible(190, 0.0, false));

        assertTrue(instance.isImplausible(4201, 249.0, true));
        assertFalse(instance.isImplausible(4201, 251.0, true));
        assertTrue(instance.isImplausible(4201, 1.0, false));
        assertFalse(instance.isImplausible(4201, 0.0, false));

        assertTrue(instance.isImplausible(723, 249.0, true));
        assertFalse(instance.isImplausible(723, 251.0, true));
        assertTrue(instance.isImplausible(723, 1.0, false));
        assertFalse(instance.isImplausible(723, 0.0, false));

        assertTrue(instance.isImplausible(4202, 249.0, true));
        assertFalse(instance.isImplausible(4202, 251.0, true));
        assertTrue(instance.isImplausible(4202, 1.0, false));
        assertFalse(instance.isImplausible(4202, 0.0, false));

        assertTrue(instance.isImplausible(84, 1.0, true));
        assertFalse(instance.isImplausible(84, 0.0, true));

        assertFalse(instance.isImplausible(108, 101.0, true));
        assertFalse(instance.isImplausible(108, 24.9, true));
        assertTrue(instance.isImplausible(108, 111.0, false));
        assertTrue(instance.isImplausible(108, 24.9, false));
        assertFalse(instance.isImplausible(108, 75.0, false));

        assertTrue(instance.isImplausible(158, 5.9, true));
        assertFalse(instance.isImplausible(158, 6.1, true));
        assertFalse(instance.isImplausible(158, 6.1, false));
        assertFalse(instance.isImplausible(158, 5.9, false));

        assertTrue(instance.isImplausible(168, 5.9, true));
        assertFalse(instance.isImplausible(168, 6.1, true));
        assertFalse(instance.isImplausible(168, 6.1, false));
        assertFalse(instance.isImplausible(168, 5.9, false));

        assertFalse(instance.isImplausible(3700, 1.0, true));
        assertFalse(instance.isImplausible(3700, 0.0, true));
        assertTrue(instance.isImplausible(3700, 1.0, false));
        assertFalse(instance.isImplausible(3700, 0.0, false));

        assertTrue(instance.isImplausible(183, 0.0, true));
        assertTrue(instance.isImplausible(183, 4.1, true));
        assertFalse(instance.isImplausible(183, 3.9, true));
        assertTrue(instance.isImplausible(183, 1.0, false));
        assertFalse(instance.isImplausible(183, 0.0, false));

        assertTrue(instance.isImplausible(6895, 14.0, true));
        assertFalse(instance.isImplausible(6895, 15.0, true));
        assertTrue(instance.isImplausible(6895, 1.0, false));
        assertFalse(instance.isImplausible(6895, 0.0, false));

        assertFalse(instance.isImplausible(7333, 0.0, true));
        assertFalse(instance.isImplausible(7333, 0.1, true));
        assertFalse(instance.isImplausible(7333, 0.0, false));
        assertTrue(instance.isImplausible(7333, 0.1, false));

        assertFalse(instance.isImplausible(3609, 0.0, true));
        assertFalse(instance.isImplausible(3609, 0.1, true));
        assertFalse(instance.isImplausible(3609, 0.0, false));
        assertTrue(instance.isImplausible(3609, 3.1, false));

        assertFalse(instance.isImplausible(3610, 0.0, true));
        assertFalse(instance.isImplausible(3610, 0.1, true));
        assertFalse(instance.isImplausible(3610, 0.0, false));
        assertTrue(instance.isImplausible(3610, 3.1, false));

        assertFalse(instance.isImplausible(3251, 0.0, true));
        assertFalse(instance.isImplausible(3251, 0.1, true));
        assertFalse(instance.isImplausible(3251, 0.0, false));
        assertTrue(instance.isImplausible(3251, 3.1, false));

        assertFalse(instance.isImplausible(3226, 1000.0, true));
        assertFalse(instance.isImplausible(3226, (double) 0xFB00, true));
        assertFalse(instance.isImplausible(3226, 500.0, false));
        assertFalse(instance.isImplausible(3226, 3012.8, false));
        assertTrue(instance.isImplausible(3226, 500.1, false));

        assertTrue(instance.isImplausible(132, -0.1, true));
        assertFalse(instance.isImplausible(132, 0.0, true));
        assertFalse(instance.isImplausible(132, 0.19, false));
        assertTrue(instance.isImplausible(132, 0.21, false));

        assertTrue(instance.isImplausible(6393, -0.1, true));
        assertFalse(instance.isImplausible(6393, 0.0, true));
        assertFalse(instance.isImplausible(6393, 0.03, false));
        assertTrue(instance.isImplausible(6393, 0.05, false));

        assertTrue(instance.isImplausible(102, 21.1, true));
        assertFalse(instance.isImplausible(102, 21.0, true));
        assertFalse(instance.isImplausible(102, 4.0, false));
        assertTrue(instance.isImplausible(102, 4.1, false));

        assertTrue(instance.isImplausible(1127, 21.1, true));
        assertFalse(instance.isImplausible(1127, 21.0, true));
        assertFalse(instance.isImplausible(1127, 4.0, false));
        assertTrue(instance.isImplausible(1127, 4.1, false));

        assertTrue(instance.isImplausible(106, 121.1, true));
        assertFalse(instance.isImplausible(106, 121.0, true));
        assertFalse(instance.isImplausible(106, 105.0, false));
        assertTrue(instance.isImplausible(106, 105.1, false));

        assertTrue(instance.isImplausible(3563, 121.1, true));
        assertFalse(instance.isImplausible(3563, 121.0, true));
        assertFalse(instance.isImplausible(3563, 105.0, false));
        assertTrue(instance.isImplausible(3563, 105.1, false));

        assertTrue(instance.isImplausible(5829, 51.0, true));
        assertFalse(instance.isImplausible(5829, 50.0, true));
        assertTrue(instance.isImplausible(5829, 6.0, false));
        assertFalse(instance.isImplausible(5829, 5.0, false));

        assertFalse(instance.isImplausible(123, 66.6, false));

        assertFalse(instance.isImplausible(4360, 0.0, true));
        assertTrue(instance.isImplausible(4360, 6.9, false));
        assertFalse(instance.isImplausible(4360, 7.0, false));

        assertFalse(instance.isImplausible(4363, 0.0, true));
        assertTrue(instance.isImplausible(4363, 6.9, false));
        assertFalse(instance.isImplausible(4363, 7.0, false));

        assertFalse(instance.isImplausible(2659, 0.0, true));
        assertFalse(instance.isImplausible(2659, 0.1, true));
        assertFalse(instance.isImplausible(2659, 0.0, false));
        assertTrue(instance.isImplausible(2659, 0.1, false));

        assertFalse(instance.isImplausible(12750, 0.0, true));
        assertFalse(instance.isImplausible(12750, 0.1, true));
        assertFalse(instance.isImplausible(12750, 0.0, false));
        assertTrue(instance.isImplausible(12750, 0.1, false));

        assertFalse(instance.isImplausible(12751, 0.0, true));
        assertFalse(instance.isImplausible(12751, 0.1, true));
        assertFalse(instance.isImplausible(12751, 0.0, false));
        assertTrue(instance.isImplausible(12751, 0.1, false));

        assertFalse(instance.isImplausible(12752, 0.0, true));
        assertFalse(instance.isImplausible(12752, 0.1, true));
        assertFalse(instance.isImplausible(12752, 0.0, false));
        assertTrue(instance.isImplausible(12752, 0.1, false));

        assertFalse(instance.isImplausible(6894, 0.0, true));
        assertFalse(instance.isImplausible(6894, 0.1, true));
        assertFalse(instance.isImplausible(6894, 0.0, false));
        assertTrue(instance.isImplausible(6894, 0.1, false));

        assertFalse(instance.isImplausible(4331, 0.0, true));
        assertFalse(instance.isImplausible(4331, 0.1, true));
        assertFalse(instance.isImplausible(4331, 0.0, false));
        assertTrue(instance.isImplausible(4331, 0.1, false));

        assertFalse(instance.isImplausible(6595, 0.0, true));
        assertFalse(instance.isImplausible(6595, 0.1, true));
        assertFalse(instance.isImplausible(6595, 0.0, false));
        assertTrue(instance.isImplausible(6595, 0.1, false));

        assertFalse(instance.isImplausible(4348, 0.0, true));
        assertFalse(instance.isImplausible(4348, 0.1, true));
        assertFalse(instance.isImplausible(4348, 0.0, false));
        assertTrue(instance.isImplausible(4348, 0.1, false));

        assertFalse(instance.isImplausible(6593, 0.0, true));
        assertFalse(instance.isImplausible(6593, 0.1, true));
        assertFalse(instance.isImplausible(6593, 0.0, false));
        assertTrue(instance.isImplausible(6593, 0.1, false));

        assertFalse(instance.isImplausible(3481, 0.0, true));
        assertFalse(instance.isImplausible(3481, 0.1, true));
        assertFalse(instance.isImplausible(3481, 0.0, false));
        assertTrue(instance.isImplausible(3481, 0.1, false));

        assertFalse(instance.isImplausible(5503, 0.0, true));
        assertFalse(instance.isImplausible(5503, 0.1, true));
        assertFalse(instance.isImplausible(5503, 0.0, false));
        assertTrue(instance.isImplausible(5503, 0.1, false));

        assertFalse(instance.isImplausible(12743, 0.0, true));
        assertFalse(instance.isImplausible(12743, 0.1, true));
        assertFalse(instance.isImplausible(12743, 0.0, false));
        assertTrue(instance.isImplausible(12743, 0.1, false));

        assertFalse(instance.isImplausible(3479, 0.0, true));
        assertFalse(instance.isImplausible(3479, 0.1, true));
        assertFalse(instance.isImplausible(3479, 0.0, false));
        assertTrue(instance.isImplausible(3479, 0.1, false));

        assertFalse(instance.isImplausible(5444, 0.0, true));
        assertFalse(instance.isImplausible(5444, 0.1, true));
        assertFalse(instance.isImplausible(5444, 0.0, false));
        assertTrue(instance.isImplausible(5444, 0.1, false));
    }

    @Test
    public void isImplausibleSpn544() {
        dataRepository.setKoeoEngineReferenceTorque(499.0);

        assertTrue(instance.isImplausible(544, 199.9, true));
        assertTrue(instance.isImplausible(544, 4000.1, true));
        assertTrue(instance.isImplausible(544, 500.0, true));
        assertFalse(instance.isImplausible(544, 499.0, true));

        assertTrue(instance.isImplausible(544, 199.9, false));
        assertEquals(199.9, dataRepository.getKoeoEngineReferenceTorque(), 0.0);

        assertTrue(instance.isImplausible(544, 4000.1, false));
        assertEquals(4000.1, dataRepository.getKoeoEngineReferenceTorque(), 0.0);

        assertFalse(instance.isImplausible(544, 499.9, false));
        assertEquals(499.9, dataRepository.getKoeoEngineReferenceTorque(), 0.0);
    }

    @Test
    public void isImplausibleSpn5837asDSL() {
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        assertTrue(instance.isImplausible(5837, 1.0, true));
        assertFalse(instance.isImplausible(5837, 4.0, true));
    }

    @Test
    public void isImplausibleSpn5837asGas() {
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehInfo);

        assertTrue(instance.isImplausible(5837, 4.0, false));
        assertFalse(instance.isImplausible(5837, 1.0, false));
    }
}

/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The {@link Controller} for the Part 1 Tests
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class Part1Controller extends Controller {

	/**
	 * Constructor
	 */
	public Part1Controller() {
		this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
				new DateTimeModule(), new VehicleInformationModule());
	}

	/**
	 * Constructor exposed for testing
	 *
	 * @param executor                 the {@link ScheduledExecutorService}
	 * @param engineSpeedModule        the {@link EngineSpeedModule}
	 * @param bannerModule             the {@link BannerModule}
	 * @param dateTimeModule           the {@link DateTimeModule}
	 * @param vehicleInformationModule the {@link VehicleInformationModule}
	 */
	public Part1Controller(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
			BannerModule bannerModule, DateTimeModule dateTimeModule,
			VehicleInformationModule vehicleInformationModule) {
		super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule);
	}

	@Override
	public String getDisplayName() {
		return "Part 1 Test";
	}

	@Override
	protected int getTotalSteps() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	protected void run() throws Throwable {
		String message = "Ready to begin Part 1" + NL;
		message += "a. Confirm the vehicle is in a safe location and condition for the test." + NL;
		message += "b. Confirm that the vehicle battery is well charged. (Battery voltage >> 12 volts)." + NL;
		message += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions."
				+ NL;

		getListener().onUrgentMessage(message, "Start Part 1", MessageType.WARNING);

		while (!getEngineSpeedModule().isEngineCommunicating()) {
			getListener().onUrgentMessage("Please turn the ignition key to on.", "Key On", MessageType.PLAIN);
		}

		getListener().onVehicleInformationNeeded(result -> {
			getLogger().log(Level.FINE, "Result" + result);
		});
	}

}

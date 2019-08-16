/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The Controller that manages the other Controllers each of which is
 * responsible for one part only.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class OverallController extends Controller {
	/**
	 * The other Controllers in the order they will be executed.
	 */

	private final List<Controller> controllers = new ArrayList<>();

	public OverallController() {
		this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
				new DateTimeModule(), new VehicleInformationModule(), new Part1Controller());
	}

	/**
	 * Constructor expose for testing
	 *
	 * @param executor                 {@link ScheduledExecutorService}
	 * @param engineSpeedModule        the {@link EngineSpeedModule} used to request
	 *                                 engine speed
	 * @param bannerModule             the {@link BannerModule} used to display
	 *                                 headers and footers on the report
	 * @param dateTimeModule           the {@link DateTimeModule} used to determine
	 *                                 the date and time
	 * @param vehicleInformationModule the {@link VehicleInformationModule} used to
	 *                                 gather information about the vehicle
	 * @param part1Controller          the {@link Controller} for Part1
	 */
	public OverallController(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
			BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
			Part1Controller part1Controller) {
		super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule);
		controllers.add(part1Controller);
	}

	@Override
	public String getDisplayName() {
		return "Overall Controller";
	}

	@Override
	protected int getTotalSteps() {
		return controllers.size();
	}

	@Override
	protected void run() throws Throwable {
		for (int i = 0; i < controllers.size(); i++) {
			Controller controller = controllers.get(i);
			getListener().onProgress(i, getTotalSteps(), controller.getDisplayName());
			controller.run(getListener(), getJ1939(), getReportFileModule());
			getListener().onProgress(i++, getTotalSteps(), controller.getDisplayName());
		}
	}

}

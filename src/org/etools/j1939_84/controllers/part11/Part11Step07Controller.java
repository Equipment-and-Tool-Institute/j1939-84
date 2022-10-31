/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static java.lang.String.format;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ReplayListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA1Validator;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.Either;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.11.7 DM20/DM28/Broadcast data: Waiting until General Denominator Is Met
 */
public class Part11Step07Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    private final ReplayListener replayListener = new ReplayListener();

    private final TableA1Validator validator;
    private final ScheduledExecutorService executor;

    private final AtomicBoolean isComplete = new AtomicBoolean(false);
    private final Set<String> reportedFailures = new HashSet<>();
    private final Predicate<Either<GenericPacket, AcknowledgmentPacket>> stopPredicate = e -> !isComplete.get();
    private int requestCount = 0;

    public Part11Step07Controller() {
        this(Executors.newScheduledThreadPool(5),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new TableA1Validator(PART_NUMBER, STEP_NUMBER));
    }

    Part11Step07Controller(ScheduledExecutorService executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           TableA1Validator validator) {
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
        this.executor = executor;
        this.validator = validator;
    }

    @Override
    protected void run() throws Throwable {

        getEngineSpeedModule().startMonitoringEngineSpeed(executor, stopPredicate);

        // Report the engine data while the test is going on
        executor.scheduleAtFixedRate(() -> {
            String msg = getDateTimeModule().getTime() + " Test Update:" + NL;
            msg += "          Engine Speed: " + getEngineSpeedModule().currentEngineSpeed() + " RPM" + NL;
            msg += "      WMA Engine Speed: " + getEngineSpeedModule().averagedEngineSpeed() + " RPM" + NL;
            msg += "     Idle Engine Speed: " + getEngineSpeedModule().idleEngineSpeed() + " RPM" + NL;
            msg += "        Pedal Position: " + getEngineSpeedModule().pedalPosition() + " %" + NL;
            msg += "  Run Time >= 1150 RPM: " + getEngineSpeedModule().secondsAtSpeed() + " seconds" + NL;
            msg += "      Run Time at Idle: " + getEngineSpeedModule().secondsAtIdle() + " seconds" + NL;
            msg += "        Total Run Time: " + totalRunTimeSeconds() + " seconds" + NL;
            getListener().onResult(msg);
        }, 0L, 1L, TimeUnit.MINUTES);

        // 6.11.7.1.b. Wait 3 minutes.
        pause("Step 6.11.7.1.b - Waiting %1$d seconds", 3 * 60);

        // 6.11.7.1.c. Increase engine speed over 1150 rpm (a minimum of 300 seconds at this speed is required).
        long secondsToGo = calculateSecondsRemainingAtSpeed();

        String msg = "Please increase engine speed over 1150 rpm for a minimum of %1$d seconds" + NL + NL;
        msg += "Press OK to continue";
        displayInstructionAndWait(format(msg, secondsToGo), "Step 6.11.7.1.c", WARNING);

        String message = "Step 6.11.7.1.c - Increase engine speed over 1150 rpm for %1$d seconds";
        do {
            updateProgress(format(message, secondsToGo));

            // 6.11.7.1.d - f Periodic DS DM20 and DS DM28 with Fail Criteria
            requestPeriodicMessages();

            getDateTimeModule().pauseFor(1000);
            secondsToGo = calculateSecondsRemainingAtSpeed();
        } while (secondsToGo > 0);

        // 6.11.7.1.f. After 300 seconds have been exceeded, reduce the engine speed back to idle.
        String msg2 = "Please reduce engine speed back to idle" + NL;
        msg2 += "Test will continue for an additional %1$d seconds" + NL + NL;
        msg2 += "Press OK to continue";
        displayInstructionAndWait(format(msg2, calculateSecondsRemaining()), "Step 6.11.7.1.f", WARNING);

        // This logic has been moved to only report implausible values when the engine is at idle after the engine
        // has been at 1150 RPMs for over 300 seconds
        // 6.11.7.1.a. Broadcast data received shall comply with the values defined in Section A.1.
        // 6.11.7.3.a. Identify any broadcast data meeting warning criteria in Table A1 during engine idle periods.
        // 6.11.7.2.c. Fail if any broadcast data is missing according to Table A1,
        // or otherwise meets failure criteria during engine idle speed periods.
        executor.submit(() -> {
            getJ1939().readGenericPacket(stopPredicate)
                      .filter(p -> getEngineSpeedModule().isEngineAtIdle())
                      .forEach(p -> {
                          validator.reportImplausibleSPNValues(p, getListener(), true, "6.11.7.3.a");
                      });
        });

        // 6.11.7.4.a. Once 620 seconds of engine operation overall in part 11 have elapsed (including over 300 seconds
        // of engine operation over 1150 rpm), end periodic DM20 and DM28 and continue with test 6.11.8.
        secondsToGo = calculateSecondsRemaining();
        do {
            updateProgress(format("Step 6.11.7.4.a - Continue to run engine at idle for an additional %1$d seconds",
                                  secondsToGo));
            getDateTimeModule().pauseFor(1000);
            secondsToGo = calculateSecondsRemaining();
        } while (secondsToGo > 0);

        executor.shutdownNow();
        isComplete.set(true);
    }

    private void requestPeriodicMessages() {
        boolean logPackets = requestCount++ % 10 == 0;

        // 6.11.7.1.d. Periodic DS DM20 to ECUs that reported data earlier in this part
        // while timing engine operation versus the general denominator timing requirement.
        // 6.11.7.1.e. [Every 10th query set may be reported in the log unless the failure criteria for DM20 are met].
        // 6.11.7.2.a. Fail if there is any DM20 response that indicates any denominator is greater than the value it
        // was earlier in this part before general denominator timing has elapsed.
        getDataRepository().getObdModuleAddresses()
                           .stream()
                           .filter(this::isPart11DM20Provided)
                           .flatMap(a -> requestDM20(logPackets, a))
                           .filter(this::denominatorsIncreased)
                           .map(ParsedPacket::getModuleName)
                           .map(moduleName -> "6.11.7.2.a - " + moduleName
                                   + " DM20 response indicates a denominator is greater than the value it was earlier in this part")
                           .filter(this::isNotReported)
                           .peek(m -> replayListener.replayResults(getListener()))
                           .forEach(this::addFailure);

        // 6.11.7.1.d. Periodic DS DM28s to ECU that reported permanent DTC earlier in this part
        // while timing engine operation versus the general denominator timing requirement.
        // 6.11.7.1.e. [Every 10th query set may be reported in the log unless the failure criteria for DM28 are met].
        // 6.11.7.2.b. Fail if there is any DM28 response that indicates the permanent DTC is no longer present before
        // general denominator timing has elapsed.
        getDataRepository().getObdModuleAddresses()
                           .stream()
                           .filter(this::isPart11DM28Provided)
                           .flatMap(a -> requestDM28(logPackets, a))
                           .filter(p -> !p.hasDTCs())
                           .map(ParsedPacket::getModuleName)
                           .map(moduleName -> "6.11.7.2.b - " + moduleName
                                   + " DM28 response indicates the permanent DTC is no longer present")
                           .filter(this::isNotReported)
                           .peek(m -> replayListener.replayResults(getListener()))
                           .forEach(this::addFailure);
    }

    private long calculateSecondsRemainingAtSpeed() {
        return 300 - getEngineSpeedModule().secondsAtSpeed();
    }

    private long calculateSecondsRemaining() {
        return 620 - totalRunTimeSeconds();
    }

    private long totalRunTimeSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(getDateTimeModule().getTimeAsLong()
                - getDataRepository().getPart11StartTime());
    }

    @Override
    protected void addFailure(String message) {
        reportedFailures.add(message);
        super.addFailure(message);
    }

    private boolean isNotReported(String message) {
        return !reportedFailures.contains(message);
    }

    private boolean isPart11DM20Provided(int address) {
        return getDM20(address) != null;
    }

    private boolean isPart11DM28Provided(int address) {
        DM28PermanentEmissionDTCPacket dm28 = getDM28(address);
        return dm28 != null && dm28.hasDTCs();
    }

    private Stream<DM20MonitorPerformanceRatioPacket> requestDM20(boolean logPackets, int address) {
        return getCommunicationsModule().requestDM20(getListener(logPackets), address).toPacketStream();
    }

    private Stream<DM28PermanentEmissionDTCPacket> requestDM28(boolean logPackets, int address) {
        return getCommunicationsModule().requestDM28(getListener(logPackets), address).toPacketStream();
    }

    private boolean denominatorsIncreased(DM20MonitorPerformanceRatioPacket currentPacket) {
        return currentPacket.getRatios().stream().anyMatch(this::isDenominatorIncreased);
    }

    private boolean isDenominatorIncreased(PerformanceRatio currentRatio) {
        return getRepoRatio(currentRatio).stream().anyMatch(o -> currentRatio.getDenominator() > o.getDenominator());
    }

    private Optional<PerformanceRatio> getRepoRatio(PerformanceRatio ratio) {
        var dm20 = getDM20(ratio.getSourceAddress());
        return dm20 == null ? Optional.empty() : dm20.getRatio(ratio.getId());
    }

    private DM20MonitorPerformanceRatioPacket getDM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 11);
    }

    private DM28PermanentEmissionDTCPacket getDM28(int address) {
        return get(DM28PermanentEmissionDTCPacket.class, address, 11);
    }

    private ResultsListener getListener(boolean logPackets) {
        replayListener.reset();
        return logPackets ? getListener() : replayListener;
    }

}

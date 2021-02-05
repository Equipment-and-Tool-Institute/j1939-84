/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.INCOMPLETE;
import static org.etools.j1939_84.model.Outcome.INFO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public abstract class StepController extends Controller {

    private final int partNumber;
    private final int stepNumber;
    private final int totalSteps;

    protected StepController(Executor executor,
                             BannerModule bannerModule,
                             DateTimeModule dateTimeModule,
                             DataRepository dataRepository,
                             EngineSpeedModule engineSpeedModule,
                             VehicleInformationModule vehicleInformationModule,
                             DiagnosticMessageModule diagnosticMessageModule,
                             int partNumber,
                             int stepNumber,
                             int totalSteps) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
        this.totalSteps = totalSteps;
    }

    /** @deprecated Use the other constructor */
    @Deprecated
    protected StepController(Executor executor,
                             EngineSpeedModule engineSpeedModule,
                             BannerModule bannerModule,
                             VehicleInformationModule vehicleInformationModule,
                             DiagnosticMessageModule diagnosticMessageModule,
                             DateTimeModule dateTimeModule,
                             int partNumber,
                             int stepNumber,
                             int totalSteps) {
        this(executor,
             bannerModule,
             dateTimeModule,
             DataRepository.getInstance(),
             engineSpeedModule,
             vehicleInformationModule,
             diagnosticMessageModule,
             partNumber,
             stepNumber,
             totalSteps);
    }

    /**
     * Ensures the Key is on with the Engine Off and prompts the user to make
     * the proper adjustments.
     *
     * @throws InterruptedException
     *         if the user cancels the operation
     */
    protected void ensureKeyOnEngineOn() throws InterruptedException {
        try {
            if (!getEngineSpeedModule().isEngineRunning()) {
                getListener().onUrgentMessage("Please turn the Key ON with Engine ON", "Adjust Key Switch", WARNING);
                while (!getEngineSpeedModule().isEngineRunning()) {
                    updateProgress("Waiting for Key ON, Engine ON...");
                    getDateTimeModule().pauseFor(500);
                }
            }
        } catch (InterruptedException e) {
            getListener().addOutcome(getPartNumber(), getStepNumber(), ABORT, "User cancelled operation");
            setEnding(Ending.STOPPED);
            incrementProgress("User cancelled testing");
            throw e;
        }
    }

    /**
     * Ensures the Key is on with the Engine Off and prompts the user to make
     * the proper adjustments.
     *
     * @throws InterruptedException
     *         if the user cancels the operation
     */
    protected void ensureKeyOnEngineOff() throws InterruptedException {
        try {
            if (!getEngineSpeedModule().isEngineNotRunning()) {
                getListener().onUrgentMessage("Please turn the Key ON with Engine OFF", "Adjust Key Switch", WARNING);
                while (!getEngineSpeedModule().isEngineNotRunning()) {
                    updateProgress("Waiting for Key ON, Engine OFF...");
                    getDateTimeModule().pauseFor(500);
                }
            }
        } catch (InterruptedException e) {
            getListener().addOutcome(1, 2, ABORT, "User cancelled operation");
            setEnding(Ending.STOPPED);
            incrementProgress("User Aborted");
        }
    }

    /**
     * Ensures the Key is off with the Engine Off and prompts the user to make
     * the proper adjustments.
     *
     * @throws InterruptedException
     *         if the user cancels the operation
     */
    protected void ensureKeyOffEngineOff() throws InterruptedException {
        try {
            if (getEngineSpeedModule().isEngineRunning()) {
                getListener().onUrgentMessage("Please turn the Key OFF with Engine OFF", "Adjust Key Switch", WARNING);
                while (getEngineSpeedModule().isEngineRunning()) {
                    updateProgress("Waiting for Key OFF, Engine OFF...");
                    getDateTimeModule().pauseFor(500);
                }
            }
        } catch (InterruptedException e) {
            getListener().addOutcome(getPartNumber(), getStepNumber(), ABORT, "User cancelled operation");
            setEnding(Ending.STOPPED);
            incrementProgress("User cancelled testing");
            throw e;
        }

    }

    protected void addFailure(String message) {
        addFailure(getPartNumber(), getStepNumber(), message);
    }

    protected void addWarning(String message) {
        addWarning(getPartNumber(), getStepNumber(), message);
    }

    protected void addInfo(String message) {
        getListener().addOutcome(partNumber, stepNumber, INFO, message);
        getListener().onResult("INFO: " + message);
    }

    @Override
    public String getDisplayName() {
        return "Part " + getPartNumber() + " Step " + getStepNumber();
    }

    public int getPartNumber() {
        return partNumber;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    protected void compareRequestPackets(List<? extends GenericPacket> globalPackets,
                                         List<? extends GenericPacket> dsPackets,
                                         String section) {
        for (GenericPacket globalPacket : globalPackets) {
            Optional<? extends GenericPacket> dsOptional = dsPackets.stream()
                    .filter(dsPacket -> dsPacket.getSourceAddress() == globalPacket.getSourceAddress())
                    .findFirst();

            if (dsOptional.isPresent()) {
                byte[] dsBytes = dsOptional.get().getPacket().getBytes();
                byte[] globalBytes = globalPacket.getPacket().getBytes();
                if (!Arrays.equals(dsBytes, globalBytes)) {
                    String moduleName = Lookup.getAddressName(dsOptional.get().getSourceAddress());
                    addFailure(section + " - Difference compared to data received during global request from " + moduleName);
                }
            }
        }
    }

    protected void checkForNACKs(List<? extends GenericPacket> globalPackets,
                                 List<? extends AcknowledgmentPacket> dsAcks,
                                 Collection<Integer> obdModuleAddresses,
                                 String section) {
        List<Integer> addresses = new ArrayList<>(obdModuleAddresses);

        globalPackets
                .stream()
                .map(ParsedPacket::getSourceAddress)
                .forEach(addresses::remove);

        dsAcks
                .stream()
                .filter(ack -> ack.getResponse() == NACK)
                .map(ParsedPacket::getSourceAddress)
                .forEach(addresses::remove);

        addresses.stream()
                .distinct()
                .sorted()
                .map(address -> section + " - OBD module " + Lookup.getAddressName(address) + " did not provide a response to Global query and did not provide a NACK for the DS query")
                .forEach(this::addFailure);
    }
    protected QuestionListener getQuestionListener() {
        return answerType -> {
            //end test if user hits cancel button
            if (answerType == CANCEL || answerType == NO) {
                getListener().addOutcome(getPartNumber(),
                                         getStepNumber(),
                                         INCOMPLETE,
                                         "Stopping test - user ended test");
                try {
                    getListener().onResult("User cancelled the test at Part " + getPartNumber() + " Step " + getStepNumber());
                    setEnding(Ending.STOPPED);
                    incrementProgress("User cancelled testing");
                } catch (InterruptedException ignored) {
                }
            }
        };
    }

    protected void displayInstructionAndWait(String message, String boxTitle, MessageType messageType){
        getListener().onUrgentMessage(message, boxTitle, messageType, getQuestionListener());
    }

    protected void reportDuplicateCompositeSystems(List<? extends DiagnosticReadinessPacket> packets, String section) {
        List<CompositeSystem> compositeSystems = packets.stream()
                .flatMap(packet -> packet.getMonitoredSystems().stream())
                .filter(system -> system.getStatus().isEnabled())
                .map(MonitoredSystem::getId)
                .filter(system -> system != CompositeSystem.COMPREHENSIVE_COMPONENT)
                .collect(Collectors.toList());

        compositeSystems.stream()
                .filter(system -> Collections.frequency(compositeSystems, system) > 1)
                .distinct()
                .sorted()
                .map(CompositeSystem::getName)
                .map(String::trim)
                .map(m -> section + " - Required monitor " + m + " is supported by more than one OBD ECU")
                .forEach(this::addWarning);
    }

    protected void pause(String message, long secondsToSleep) {
        long stopTime = getDateTimeModule().getTimeAsLong() + (secondsToSleep * 1000L);
        long secondsToGo;
        while (true) {
            secondsToGo = (stopTime - getDateTimeModule().getTimeAsLong()) / 1000;
            if (secondsToGo > 0) {
                getListener().onProgress(message + secondsToGo + " seconds");
                getDateTimeModule().pauseFor(1000);
            } else {
                break;
            }
        }
    }

    protected static <T extends GenericPacket> List<T> filterRequestResultPackets(List<RequestResult<T>> results) {
        return results.stream().flatMap(r -> r.getPackets().stream()).collect(Collectors.toList());
    }

    protected static <T extends GenericPacket> List<AcknowledgmentPacket> filterRequestResultAcks(List<RequestResult<T>> results) {
        return results.stream().flatMap(r -> r.getAcks().stream()).collect(Collectors.toList());
    }

    protected static <T extends GenericPacket> List<T> filterPackets(List<BusResult<T>> results) {
        return results.stream()
                .map(BusResult::requestResult)
                .flatMap(r -> r.getPackets().stream())
                .collect(Collectors.toList());
    }

    protected static <T extends GenericPacket> List<AcknowledgmentPacket> filterAcks(List<BusResult<T>> results) {
        return results.stream()
                .map(BusResult::getPacket)
                .filter(Optional::isPresent)
                .map(p -> p.get().right)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}

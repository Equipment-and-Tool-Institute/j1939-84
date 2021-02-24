/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.isDevEnv;
import static org.etools.j1939_84.J1939_84.isTesting;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.controllers.Controller.Ending.STOPPED;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.model.OBDModuleInformation;
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

    protected static <T extends GenericPacket> List<T> filterRequestResultPackets(List<RequestResult<T>> results) {
        return results.stream().flatMap(r -> r.getPackets().stream()).collect(Collectors.toList());
    }

    protected static <T extends GenericPacket>
              List<AcknowledgmentPacket>
              filterRequestResultAcks(List<RequestResult<T>> results) {
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

    protected static String toString(List<DiagnosticTroubleCode> dtcs) {
        return dtcs.stream()
                   .map(d -> d.getSuspectParameterNumber() + ":" + d.getFailureModeIndicator())
                   .sorted()
                   .collect(Collectors.joining(","));
    }

    protected static boolean listContainsDTC(List<DiagnosticTroubleCode> dtcs, DiagnosticTroubleCode dtc) {
        var dtcString = toString(List.of(dtc));
        var listString = toString(dtcs);
        return listString.contains(dtcString);
    }

    protected boolean isObdModule(int address) {
        return getDataRepository().isObdModule(address);
    }

    protected boolean supportsDM27(int address) {
        OBDModuleInformation obdModule = getDataRepository().getObdModule(address);
        return obdModule != null && obdModule.supportsDM27();
    }

    protected <T extends DiagnosticTroubleCodePacket> List<DiagnosticTroubleCode> getDTCs(Class<T> packetClass,
                                                                                          int address) {
        var packet = get(packetClass, address);
        return packet == null ? List.of() : packet.getDtcs();
    }

    protected void save(GenericPacket packet) {
        OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(packet.getSourceAddress());
        if (obdModuleInformation != null) {
            obdModuleInformation.set(packet);
            getDataRepository().putObdModule(obdModuleInformation);
        }
    }

    protected <T extends GenericPacket> T get(Class<T> packetClass, int address) {
        OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(address);
        return obdModuleInformation == null ? null : obdModuleInformation.get(packetClass);
    }

    /**
     * Ensures the Key is on with the Engine Off and prompts the user to make
     * the proper adjustments.
     *
     * @throws InterruptedException
     *                                  if the user cancels the operation
     */
    protected void ensureKeyOnEngineOn() throws InterruptedException {
        try {
            reportInitialEngineSpeed();
            if (isTesting()) {
                getVehicleInformationModule().requestKeyOnEngineOn(getListener());
            }
            if (!getEngineSpeedModule().isEngineRunning() && !isDevEnv()) {
                getListener().onUrgentMessage("Please turn the Key ON with Engine ON", "Adjust Key Switch", WARNING);
                while (!getEngineSpeedModule().isEngineRunning()) {
                    updateProgress("Waiting for Key ON, Engine ON...");
                    getDateTimeModule().pauseFor(500);
                }
            }
            reportFinalEngineSpeed();
        } catch (InterruptedException e) {
            abort();
        }
    }

    /**
     * Ensures the Key is on with the Engine Off and prompts the user to make
     * the proper adjustments.
     *
     * @throws InterruptedException
     *                                  if the user cancels the operation
     */
    protected void ensureKeyOnEngineOff() throws InterruptedException {
        try {
            reportInitialEngineSpeed();
            if (isTesting()) {
                getVehicleInformationModule().requestKeyOnEngineOff(getListener());
            }
            if (!getEngineSpeedModule().isEngineNotRunning() && !isDevEnv()) {
                getListener().onUrgentMessage("Please turn the Key ON with Engine OFF", "Adjust Key Switch", WARNING);
                while (!getEngineSpeedModule().isEngineNotRunning()) {
                    updateProgress("Waiting for Key ON, Engine OFF...");
                    getDateTimeModule().pauseFor(500);
                }
            }
            reportFinalEngineSpeed();
        } catch (InterruptedException e) {
            abort();
        }
    }

    /**
     * Ensures the Key is off with the Engine Off and prompts the user to make
     * the proper adjustments.
     *
     * @throws InterruptedException
     *                                  if the user cancels the operation
     */
    protected void ensureKeyOffEngineOff() throws InterruptedException {
        try {
            reportInitialEngineSpeed();
            if (isTesting()) {
                getVehicleInformationModule().requestKeyOffEngineOff(getListener());
            }
            if (getEngineSpeedModule().isEngineCommunicating() && !isDevEnv()) {
                getListener().onUrgentMessage("Please turn the Key OFF with Engine OFF", "Adjust Key Switch", WARNING);
                while (getEngineSpeedModule().isEngineCommunicating()) {
                    updateProgress("Waiting for Key OFF, Engine OFF...");
                    getDateTimeModule().pauseFor(500);
                }
            }
            reportFinalEngineSpeed();
        } catch (InterruptedException e) {
            abort();
        }
    }

    private void reportFinalEngineSpeed() {
        getListener().onResult("Final Engine Speed = " + getEngineSpeedModule().getEngineSpeedAsString());
    }

    private void reportInitialEngineSpeed() {
        getListener().onResult("Initial Engine Speed = " + getEngineSpeedModule().getEngineSpeedAsString());
    }

    protected void addFailure(String message) {
        getListener().addOutcome(getPartNumber(), getStepNumber(), FAIL, message);
    }

    protected void addWarning(String message) {
        getListener().addOutcome(getPartNumber(), getStepNumber(), WARN, message);
    }

    protected void addInfo(String message) {
        getListener().addOutcome(getPartNumber(), getStepNumber(), INFO, message);
    }

    protected void addAbort(String message) {
        getListener().addOutcome(getPartNumber(), getStepNumber(), ABORT, message);
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
                    addFailure(section + " - Difference compared to data received during global request from "
                            + moduleName);
                }
            }
        }
    }

    protected void checkForNACKsGlobal(List<? extends GenericPacket> globalPackets,
                                       List<? extends AcknowledgmentPacket> dsAcks,
                                       String section) {
        List<Integer> addresses = new ArrayList<>(getDataRepository().getObdModuleAddresses());

        globalPackets.stream()
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
                 .map(Lookup::getAddressName)
                 .map(moduleName -> section + " - OBD module " + moduleName
                         + " did not provide a response to Global query and did not provide a NACK for the DS query")
                 .forEach(this::addFailure);
    }

    protected void checkForNACKsDS(List<? extends GenericPacket> packets,
                                   List<? extends AcknowledgmentPacket> acks,
                                   String section) {

        List<Integer> missingAddresses = getDataRepository().getObdModuleAddresses();

        packets.stream().map(ParsedPacket::getSourceAddress).forEach(missingAddresses::remove);
        acks.stream()
            .filter(a -> a.getResponse() == NACK)
            .map(ParsedPacket::getSourceAddress)
            .forEach(missingAddresses::remove);

        missingAddresses.stream()
                        .distinct()
                        .sorted()
                        .map(Lookup::getAddressName)
                        .map(moduleName -> section + " - OBD module " + moduleName
                                + " did not provide a NACK for the DS query")
                        .forEach(this::addFailure);
    }

    protected void waitForFault(String boxTitle) {
        String message = "Implant Fault A according to engine manufacturer’s instruction" + NL;
        message += "Press OK when ready to continue testing" + NL;
        if (!isDevEnv()) {
            displayInstructionAndWait(message, boxTitle, WARNING);
        }
    }

    protected QuestionListener getQuestionListener() {
        return answerType -> {
            // end test if user hits cancel button
            if (answerType == CANCEL || answerType == NO) {
                try {
                    abort();
                } catch (InterruptedException ignored) {
                }
            }
        };
    }

    private void abort() throws InterruptedException {
        String message = "User cancelled testing at Part " + getPartNumber() + " Step " + getStepNumber();
        addAbort(message);
        getListener().onProgress(message);
        setEnding(STOPPED);
    }

    protected void displayInstructionAndWait(String message, String boxTitle, MessageType messageType) {
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
                getListener().onProgress(String.format(message, secondsToGo));
                getDateTimeModule().pauseFor(1000);
            } else {
                break;
            }
        }
    }

}

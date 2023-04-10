/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static java.lang.String.format;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.isDevEnv;
import static org.etools.j1939_84.J1939_84.isTesting;
import static org.etools.j1939_84.controllers.Controller.Ending.STOPPED;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.model.KeyState;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.GhgActiveTechnologyPacket;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.GhgTrackingModule;

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
                             CommunicationsModule communicationsModule,
                             int partNumber,
                             int stepNumber,
                             int totalSteps) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule);
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

    protected boolean isObdModule(int address) {
        return getDataRepository().isObdModule(address);
    }

    protected boolean supportsDM27(int address) {
        OBDModuleInformation obdModule = getDataRepository().getObdModule(address);
        return obdModule != null && obdModule.supportsDM27();
    }

    protected <T extends DiagnosticTroubleCodePacket> List<DiagnosticTroubleCode> getDTCs(Class<T> packetClass,
                                                                                          int address,
                                                                                          int partNumber) {
        var packet = get(packetClass, address, partNumber);
        return packet == null ? List.of() : packet.getDtcs();
    }

    protected void save(GenericPacket packet) {
        OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(packet.getSourceAddress());
        if (obdModuleInformation != null) {
            obdModuleInformation.set(packet, partNumber);
            getDataRepository().putObdModule(obdModuleInformation);
        }
    }

    protected <T extends GenericPacket> T get(Class<T> packetClass, int address, int partNumber) {
        return get(getPg(packetClass), address, partNumber);
    }

    protected <T extends GenericPacket> T get(int pg, int address, int partNumber) {
        OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(address);
        return obdModuleInformation == null ? null : obdModuleInformation.get(pg, partNumber);
    }

    protected List<GenericPacket>
              requestPackets(int address, int... pgns) {
        try {
            String moduleName = Lookup.getAddressName(address);
            List<GenericPacket> packets = new ArrayList<>();

            for (int pgn : pgns) {
                PgnDefinition pgnDef = getPgnDef(pgn);

                incrementProgress("Requesting " + pgnDef.getLabel() + " (" + pgnDef.getAcronym() + ") from "
                        + moduleName);
                var response = getCommunicationsModule().request(pgn, address, getListener())
                                                        .toPacketStream()
                                                        .collect(Collectors.toList());
                packets.addAll(response);
            }
            return packets;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    protected <T extends GenericPacket> GenericPacket haveResponseWithPg(List<T> packets, int pg) {
        return packets.stream().filter(packet -> packet.getPacket().getPgn() == pg).findAny().orElse(null);
    }

    private static PgnDefinition getPgnDef(int pg) {
        return J1939DaRepository.getInstance().findPgnDefinition(pg);
    }

    protected void ensureKeyStateIs(KeyState requestedKeyState, String section) throws InterruptedException {
        getListener().onResult("Initial Engine Speed = " + getEngineSpeedAsString());

        if (getCurrentKeyState() != requestedKeyState) {
            updateProgress("Step " + section + " - " + getWaitingKeyStateAsString(requestedKeyState));
            if (!isDevEnv()) {
                getListener().onUrgentMessage(getCurrentKeyStateAsString(requestedKeyState),
                                              "Step " + section,
                                              WARNING,
                                              getQuestionListener());
            }
            while (getCurrentKeyState() != requestedKeyState) {
                updateProgress("Step " + section + " - " + getWaitingKeyStateAsString(requestedKeyState) + "...");
                getDateTimeModule().pauseFor(500);
                if (isTesting()) {
                    getVehicleInformationModule().changeKeyState(getListener(), requestedKeyState);
                }
            }
        }
        getListener().onResult("Final Engine Speed = " + getEngineSpeedAsString());
    }

    private static String getWaitingKeyStateAsString(KeyState keyState) {
        switch (keyState) {
            case KEY_ON_ENGINE_RUNNING:
                return "Waiting for engine start";
            case KEY_ON_ENGINE_OFF:
                return "Waiting for key on with engine off";
            case KEY_OFF:
                return "Waiting for key off";
            default:
                return "Waiting for unknown";
        }
    }

    private static String getCurrentKeyStateAsString(KeyState keyState) {
        switch (keyState) {
            case KEY_ON_ENGINE_RUNNING:
                return "Please start the engine";
            case KEY_ON_ENGINE_OFF:
                return "Please turn the key on with the engine off";
            case KEY_OFF:
                return "Please turn the key off";
            default:
                return "Please report this error";
        }
    }

    private String getEngineSpeedAsString() {
        return getEngineSpeedModule().getEngineSpeedAsString();
    }

    private KeyState getCurrentKeyState() {
        return getEngineSpeedModule().getKeyState();
    }

    protected FuelType getFuelType() {
        return getDataRepository().getVehicleInformation().getFuelType();
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
                 .map(moduleName -> section + " - OBD ECU " + moduleName
                         + " did not provide a response to Global query and did not provide a NACK for the DS query")
                 .forEach(this::addFailure);
    }

    protected void checkForNACKsDS(List<? extends GenericPacket> packets,
                                   List<? extends AcknowledgmentPacket> acks,
                                   String section) {
        checkForNACKsDS(packets, acks, section, getDataRepository().getObdModuleAddresses());
    }

    protected void checkForNACKsDS(List<? extends GenericPacket> packets,
                                   List<? extends AcknowledgmentPacket> acks,
                                   String section,
                                   List<Integer> addressList) {
        List<Integer> addresses = new ArrayList<>(addressList);
        packets.stream().map(ParsedPacket::getSourceAddress).forEach(addresses::remove);
        acks.stream()
            .filter(a -> a.getResponse() == NACK)
            .map(ParsedPacket::getSourceAddress)
            .forEach(addresses::remove);

        addresses.stream()
                 .distinct()
                 .sorted()
                 .map(Lookup::getAddressName)
                 .map(moduleName -> section + " - OBD ECU " + moduleName
                         + " did not provide a NACK for the DS query")
                 .forEach(this::addFailure);
    }

    private QuestionListener getQuestionListener() {
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
        if (!isDevEnv()) {
            getListener().onUrgentMessage(message, boxTitle, messageType, getQuestionListener());
        }
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
                getListener().onProgress(format(message, secondsToGo));
                getDateTimeModule().pauseFor(1000);
            } else {
                break;
            }
        }

        long timeLeftToWait = stopTime - getDateTimeModule().getTimeAsLong();
        if (timeLeftToWait > 0) {
            getDateTimeModule().pauseFor(timeLeftToWait);
        }
    }

    protected void waitMfgIntervalWithKeyOff(String section) throws InterruptedException {
        updateProgress(section + " - Waiting manufacturer’s recommended interval with the key off");
        String message = "Wait for the manufacturer's recommended interval with the key off" + NL + NL;
        message += "Press OK to continue";
        displayInstructionAndWait(message, section, WARNING);
    }

    protected boolean isNotOff(LampStatus lampStatus) {
        if (lampStatus == ALTERNATE_OFF) {
            addWarning("A.8 - Alternate coding for off (0b00, 0b00) has been accepted");
        }
        return lampStatus != OFF && lampStatus != ALTERNATE_OFF;
    }

    protected boolean isSparkIgnition() {
        return getFuelType().isSparkIgnition();
    }

    protected int getEngineModelYear() {
        return getDataRepository().getVehicleInformation().getEngineModelYear();
    }

    // Helper method to get the pg for the class object
    private static int getPg(Class<? extends GenericPacket> clazz) {
        int pg = 0;
        try {
            pg = clazz.getField("PGN").getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pg;
    }

    protected void validateSpnValueGreaterThanFaBasedSlotLength(OBDModuleInformation module,
                                                                Spn spn,
                                                                Outcome outcome,
                                                                String section) {
        long lowerLimit = 0;
        long upperLimit = 0;
        String fmt = "ERROR";
        switch (spn.getSlot().getByteLength()) {
            case 1:
                lowerLimit = 0xFAL;
                upperLimit = 0xFFL;
                fmt = "%02X";
                break;

            case 2:
                lowerLimit = 0xFAFFL;
                upperLimit = 0xFFFFL;
                fmt = "%04X";
                break;

            case 3:
                lowerLimit = 0xFAFFFFL;
                upperLimit = 0xFFFFFFL;
                fmt = "%06X";
                break;

            case 4:
                lowerLimit = 0xFAFFFFFFL;
                upperLimit = 0xFFFFFFFFL;
                fmt = "%08X";
                break;

            case 5:
                lowerLimit = 0xFAFFFFFFFFL;
                upperLimit = 0xFFFFFFFFFFL;
                fmt = "%010X";
                break;

            case 6:
                lowerLimit = 0xFAFFFFFFFFFFL;
                upperLimit = 0xFFFFFFFFFFFFL;
                fmt = "%012X";
                break;

            case 7:
                lowerLimit = 0xFAFFFFFFFFFFFFL;
                upperLimit = 0xFFFFFFFFFFFFFFL;
                fmt = "%014X";
                break;

            case 8:
                lowerLimit = 0xFAFFFFFFFFFFFFFFL;
                upperLimit = 0xFFFFFFFFFFFFFFFFL;
                fmt = "%016X";
                break;

            default:
                addInfo("Unknown slot size " + spn);
                break;

        }
        if (spn.getRawValue() > lowerLimit && spn.getRawValue() < upperLimit) {
            String fmtStr = String.format("%s - Bin value %sh is greater than %sh and less than %sh from %s for %s",
                                          section,
                                          fmt,
                                          fmt,
                                          fmt,
                                          module.getModuleName(),
                                          spn);
            addFailure(String.format(fmtStr, spn.getRawValue(), lowerLimit, upperLimit));
        }
    }

    protected boolean areUnusedBytesPaddedWithFFh(DM58RationalityFaultSpData packet) {
        Slot slot = J1939DaRepository.findSlot(packet.getSpn().getSlot().getId(), packet.getSpn().getId());

        int slotLength = slot.getByteLength();
        int spnLength = packet.getSpnDataBytes().length;

        byte[] paddingBytes;

        switch (slotLength) {
            case 1:
                paddingBytes = new byte[] { packet.getSpnDataBytes()[1], packet.getSpnDataBytes()[2],
                        packet.getSpnDataBytes()[3] };
                break;
            case 2:
                paddingBytes = new byte[] { packet.getSpnDataBytes()[2], packet.getSpnDataBytes()[3] };
                break;
            case 3:
                paddingBytes = new byte[] { packet.getSpnDataBytes()[3] };
                break;
            case 4:
                return true;
            default: {
                getListener().onResult("Not checking for FF - SP " + packet.getSpn() + " length is " + slotLength);
                return true;
            }
        }
        return allBytesAreFF(paddingBytes);
    }

    protected boolean allBytesAreFF(byte[] dataBytes) {
        for (byte bYte : dataBytes) {
            if (bYte != (byte) 0xFF) {
                return false;
            }
        }
        return true;
    }

    protected boolean isGreaterThanFb(DM58RationalityFaultSpData packet) {
        Spn spn = packet.getSpn();
        Slot slot = J1939DaRepository.getInstance().findSLOT(DM58RationalityFaultSpData.PGN, spn.getId());
        long rawValue = slot.toValue(packet.getSpnDataBytes());

        switch (spn.getSlot().getByteLength()) {
            case 1:
                return rawValue > 0xFBL;
            case 2:
                return rawValue > 0xFBFFL;
            case 4:
                return rawValue > 0xFBFFFFFFL;
            default:
                break;
        }
        return false;
    }

    public String bytesToHex(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (byte b : bytes) {
            sb.append("0x").append(String.format("%02X, ", b));
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
        return sb.toString();
    }

    /** used to match requirement wording. */
    public <T> boolean isNotSubset(Collection<T> a, Collection<T> b) {
        return !b.containsAll(a);
    }
}

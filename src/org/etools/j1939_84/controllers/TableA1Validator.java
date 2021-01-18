/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.PASS;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;

public class TableA1Validator {

    private static void addOutcome(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   String section,
                                   Outcome outcome,
                                   String message) {
        listener.addOutcome(partNumber, stepNumber, outcome, message);
        listener.onResult(outcome + ": " + section + " - " + message);
    }

    private static List<Integer> getFailureSPNs(FuelType fuelType) {
        List<Integer> allRequiredSpns = new ArrayList<>(
                List.of(27, 84, 91, 92,
                        102, 108,
                        235, 247, 248,
                        512, 513, 514, 539, 540, 541, 542, 543, 544,
                        1413,
                        2791, 2978,
                        3563,
                        5837, 5829));

        if (fuelType.isCompressionIgnition()) {
            allRequiredSpns.addAll(List.of(3226, 3700, 5466, 6895, 7333));
        } else if (fuelType.isSparkIgnition()) {
            allRequiredSpns.addAll(List.of(51,
                                           3249, 3241, 3217, 3227, 3464,
                                           4236, 4237, 4240));
        }

        return allRequiredSpns;
    }

    private static List<Integer> getInfoSPNs() {
        return new ArrayList<>(List.of(96, 110, 132, 157, 190, 5827, 5313));
    }

    private static List<Integer> getWarningSPNs(FuelType fuelType) {
        List<Integer> allWarningSPNs = new ArrayList<>(List.of(
                94, 106, 110, 157, 158, 168, 183, 190,
                723,
                1127, 1600, 1637,
                4076, 4193, 4201, 4202,
                5313, 5578
        ));

        if (fuelType.isCompressionIgnition()) {
            allWarningSPNs.addAll(List.of(164,
                                          3031, 3251, 3515, 3516, 3518, 3609, 3610,
                                          5314, 5454, 5466, 5827,
                                          7346));
        }
        return allWarningSPNs;
    }

    private static List<String> getSupportedSPNs(Collection<Integer> supportedSPNs, GenericPacket packet) {
        return packet.getPgnDefinition()
                .getSpnDefinitions()
                .stream()
                .map(SpnDefinition::getSpnId)
                .filter(supportedSPNs::contains)
                .sorted()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    // Map of Source Address to SPN for SPNs with invalid values (to avoid duplicate reporting)
    private final Map<Integer, Set<Integer>> invalidSPNs = new HashMap<>();

    // Map of Source Address to SPN for SPNs with value of Not Available (to avoid duplicate reporting)
    private final Map<Integer, Set<Integer>> notAvailableSPNs = new HashMap<>();

    // Map of Source Address to SPN for SPNs provided by not supported (to avoid duplicate reporting)
    private final Map<Integer, Set<Integer>> providedNotSupportedSPNs = new HashMap<>();

    // Map of Source Address to PGNs for packets already written to the log
    private final Map<Integer, Set<Integer>> foundPackets = new HashMap<>();

    private final DataRepository dataRepository;

    private final TableA1ValueValidator valueValidator;
    private final J1939DaRepository j1939DaRepository;

    public TableA1Validator(DataRepository dataRepository) {
        this(new TableA1ValueValidator(dataRepository), dataRepository, new J1939DaRepository());
    }

    TableA1Validator(TableA1ValueValidator valueValidator,
                     DataRepository dataRepository,
                     J1939DaRepository j1939DaRepository) {
        this.dataRepository = dataRepository;
        this.valueValidator = valueValidator;
        this.j1939DaRepository = j1939DaRepository;
    }

    public void reset() {
        invalidSPNs.clear();
        notAvailableSPNs.clear();
        foundPackets.clear();
    }

    /**
     * Writes a Failure/Warning if any SPNs is provided by more than one module
     */
    public void reportDuplicateSPNs(List<GenericPacket> packets,
                                    ResultsListener listener,
                                    int partNumber,
                                    int stepNumber,
                                    String section) {
        // f. Fail/warn per Table A-1 if two or more ECUs provide an SPN listed in Table A-1
        Map<Integer, Integer> uniques = new HashMap<>();
        Map<Integer, Outcome> duplicateSPNs = new HashMap<>();

        for (GenericPacket packet : packets) {
            for (Spn spn : packet.getSpns()) {
                if (!spn.isNotAvailable()) {
                    int spnId = spn.getId();
                    Integer address = uniques.get(spnId);
                    if (address == null) {
                        uniques.put(spnId, packet.getSourceAddress());
                    } else if (address != packet.getSourceAddress()) {
                        Outcome outcome = Lookup.getOutcomeForDuplicateSpn(spnId);
                        if (outcome != PASS && !duplicateSPNs.containsKey(spnId)) {
                            duplicateSPNs.put(spnId, outcome);
                        }
                    }
                }
            }
        }

        duplicateSPNs.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(entry ->
                                 addOutcome(listener,
                                            partNumber,
                                            stepNumber,
                                            section,
                                            entry.getValue(),
                                            "SPN " + entry.getKey() + " provided by more than one module"));
    }

    /**
     * Reports on SPN values which are implausible given the key state and fuel type
     */
    public void reportImplausibleSPNValues(GenericPacket packet,
                                           ResultsListener listener,
                                           boolean isEngineOn,
                                           FuelType fuelType,
                                           int partNumber,
                                           int stepNumber,
                                           String section) {

        int moduleAddress = packet.getSourceAddress();
        OBDModuleInformation obdModule = dataRepository.getObdModule(moduleAddress);
        if (obdModule == null) {
            return;
        }

        Collection<Integer> moduleSPNs = obdModule
                .getDataStreamSpns()
                .stream()
                .map(SupportedSPN::getSpn)
                .collect(Collectors.toSet());

        packet.getSpns()
                .stream()
                .sorted(Comparator.comparingInt(Spn::getId))
                .forEach(spn -> {
                    int spnId = spn.getId();
                    Double value = spn.getValue();
                    if ((spn.isError() && moduleSPNs.contains(spnId))
                            || valueValidator.isImplausible(spnId, value, isEngineOn, fuelType)) {
                        Set<Integer> invalid = invalidSPNs.getOrDefault(moduleAddress, new HashSet<>());
                        if (!invalid.contains(spnId)) {
                            reportPacketIfNotReported(packet, listener);
                            String message;
                            if (spn.isError()) {
                                message = "SA " + moduleAddress + " reported value for SPN " + spnId + " (ERROR) is implausible";
                            } else {
                                message = "SA " + moduleAddress + " reported value for SPN " + spnId + " (" + value + ") is implausible";
                            }
                            addOutcome(listener, partNumber, stepNumber, section, Outcome.WARN, message);
                            invalid.add(spnId);
                            invalidSPNs.put(moduleAddress, invalid);
                        }
                    }
                });
    }

    private Set<Integer> getReportedPGNs(int moduleAddress) {
        return foundPackets.getOrDefault(moduleAddress, new HashSet<>());
    }

    private boolean isReported(GenericPacket packet) {
        return getReportedPGNs(packet.getSourceAddress()).contains(packet.getPacket().getPgn());
    }

    public void reportPacketIfNotReported(GenericPacket packet, ResultsListener listener) {
        if (!isReported(packet)) {
            int moduleAddress = packet.getSourceAddress();
            int pgn = packet.getPacket().getPgn();

            boolean isNonObdModule = dataRepository.getObdModule(moduleAddress) == null;
            Collection<Integer> spns = isNonObdModule ?
                    getModuleSupportedSPNs() :
                    getModuleSupportedSPNs(moduleAddress);
            List<String> supportedSPNs = getSupportedSPNs(spns, packet);
            if (!supportedSPNs.isEmpty()) {
                listener.onResult("");
                listener.onResult("PGN " + pgn + " with Supported SPNs " + String.join(", ", supportedSPNs));
                listener.onResult("Found: " + packet.toString());
            }

            Set<Integer> modulePackets = getReportedPGNs(moduleAddress);
            modulePackets.add(pgn);
            foundPackets.put(moduleAddress, modulePackets);
        }
    }

    public void reportNotAvailableSPNs(GenericPacket packet,
                                       ResultsListener listener,
                                       int partNumber,
                                       int stepNumber,
                                       String section) {

        int moduleAddress = packet.getSourceAddress();
        Collection<Integer> moduleSPNs = getModuleSupportedSPNs(moduleAddress);

        // Find any Supported SPNs which has a value of Not Available
        packet.getSpns().stream()
                .filter(Spn::isNotAvailable)
                .map(Spn::getId)
                .filter(moduleSPNs::contains)
                .sorted()
                .forEach(spn -> {
                    Set<Integer> naSPNs = notAvailableSPNs.getOrDefault(moduleAddress, new HashSet<>());
                    if (!naSPNs.contains(spn)) {
                        naSPNs.add(spn);
                        notAvailableSPNs.put(moduleAddress, naSPNs);
                        reportPacketIfNotReported(packet, listener);
                        String msg = "SPN " + spn + " was received as NOT AVAILABLE from " + Lookup.getAddressName(
                                moduleAddress);
                        addOutcome(listener,
                                   partNumber,
                                   stepNumber,
                                   section,
                                   Outcome.FAIL,
                                   msg);
                    }
                });
    }

    private Collection<Integer> getModuleSupportedSPNs() {
        return dataRepository.getObdModules()
                .stream()
                .flatMap(m -> m.getDataStreamSpns().stream())
                .map(SupportedSPN::getSpn)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getModuleSupportedSPNs(int moduleAddress) {
        OBDModuleInformation obdModule = dataRepository.getObdModule(moduleAddress);
        if (obdModule == null) {
            return Collections.emptySet();
        } else {
            return obdModule
                    .getDataStreamSpns()
                    .stream()
                    .map(SupportedSPN::getSpn)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Writes a Failure/Warning for any SPNs found in the data stream which aren't in the DM24
     */
    public void reportProvidedButNotSupportedSPNs(GenericPacket packet,
                                                  ResultsListener listener,
                                                  FuelType fuelType,
                                                  int partNumber,
                                                  int stepNumber,
                                                  String section) {

        int sourceAddress = packet.getSourceAddress();
        Collection<Integer> providedSPNs = packet.getSpns()
                .stream()
                .filter(s -> !s.isNotAvailable())
                .map(Spn::getId)
                .filter(s -> !getModuleSupportedSPNs(sourceAddress).contains(s))
                .collect(Collectors.toSet());

        Map<Integer, Outcome> outcomes = new HashMap<>();

        // Failure SPNs
        List<Integer> failureSPNs = getFailureSPNs(fuelType);
        providedSPNs.stream().filter(failureSPNs::contains).forEach(s -> outcomes.put(s, FAIL));

        // Warnings SPNs
        List<Integer> warningSPNs = getWarningSPNs(fuelType);
        providedSPNs.stream().filter(warningSPNs::contains).forEach(s -> outcomes.put(s, WARN));

        // INFO SPNs
        List<Integer> infoSPNs = getInfoSPNs();
        providedSPNs.stream().filter(infoSPNs::contains).forEach(s -> outcomes.put(s, INFO));

        if (!outcomes.isEmpty()) {
            outcomes.keySet().stream().sorted().forEach(spn -> {
                Set<Integer> reportedSPNs = providedNotSupportedSPNs.getOrDefault(sourceAddress, new HashSet<>());
                if (!reportedSPNs.contains(spn)) {
                    reportPacketIfNotReported(packet, listener);
                    addOutcome(listener,
                               partNumber,
                               stepNumber,
                               section,
                               outcomes.get(spn),
                               "Provided SPN " + spn + " is not supported by " + Lookup.getAddressName(sourceAddress));
                    listener.onResult("");
                    reportedSPNs.add(spn);
                    providedNotSupportedSPNs.put(sourceAddress, reportedSPNs);
                }
            });
        }
    }

    /**
     * Writes a Failures/Warning to the report if an OBD Supported SPN is
     * provided by a Non-OBD Module.
     */
    public void reportNonObdModuleProvidedSPNs(GenericPacket packet,
                                               ResultsListener listener,
                                               int partNumber,
                                               int stepNumber,
                                               String section) {

        Collection<Integer> supportedSPNs = getModuleSupportedSPNs();

        if (dataRepository.getObdModule(packet.getSourceAddress()) == null) {
            packet.getSpns().stream()
                    .filter(spn -> !spn.isNotAvailable())
                    .map(Spn::getId)
                    .filter(supportedSPNs::contains)
                    .distinct()
                    .sorted()
                    .forEach(id -> {
                        Outcome outcome = Lookup.getOutcomeForNonObdModuleProvidingSpn(id);
                        if (outcome != PASS) {
                            reportPacketIfNotReported(packet, listener);
                            addOutcome(listener,
                                       partNumber,
                                       stepNumber,
                                       section,
                                       outcome,
                                       "SPN " + id + " provided by non-OBD Module");
                        }
                    });
        }
    }

    public void reportExpectedMessages(ResultsListener listener) {
        listener.onResult("Expecting the following messages:");
        dataRepository.getObdModuleAddresses()
                .stream()
                .sorted()
                .map(dataRepository::getObdModule)
                .forEach(moduleInfo -> {
                    Map<Integer, List<Integer>> pgnMap = getMessages(moduleInfo.getDataStreamSpns(), listener);
                    Stream<Integer> pgns = pgnMap.keySet().stream().sorted();
                    pgns.forEach(pgn -> {

                        String spns = pgnMap.get(pgn).stream().map(Object::toString).collect(Collectors.joining(", "));
                        String msg = "PGN " + pgn + " from SA " + moduleInfo.getSourceAddress() + " with SPNs " + spns;

                        PgnDefinition pgnDefinition = j1939DaRepository.findPgnDefinition(pgn);
                        if (pgnDefinition != null) {
                            if (pgnDefinition.isOnRequest()) {
                                msg = "  Req " + msg;
                            } else {
                                msg = "  B't " + msg;
                            }
                        } else {
                            msg = "  ??? " + msg;
                        }

                        listener.onResult(msg);
                    });
                });
    }

    private Map<Integer, List<Integer>> getMessages(List<SupportedSPN> supportedSPNs, ResultsListener listener) {
        Map<Integer, List<Integer>> pgnMap = new HashMap<>();
        supportedSPNs.stream()
                .map(SupportedSPN::getSpn)
                .distinct()
                .forEach(spn -> {
                    Integer pgn = j1939DaRepository.getPgnForSpn(spn);
                    if (pgn == null) {
                        listener.onResult("Unable to find PGN for SPN " + spn);
                    } else {
                        List<Integer> spns = pgnMap.getOrDefault(pgn, new ArrayList<>());
                        spns.add(spn);
                        pgnMap.put(pgn, spns);
                    }
                });

        //Sort SPNs
        for (int pgn : pgnMap.keySet()) {
            List<Integer> spns = pgnMap.get(pgn);
            spns.sort(Comparator.comparingInt(s -> s));
            pgnMap.put(pgn, spns);
        }

        return pgnMap;
    }

}

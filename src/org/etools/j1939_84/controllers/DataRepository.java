/*
 * Copyright (c) 2020. Electronic Tools Institute
 */

package org.etools.j1939_84.controllers;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939tools.j1939.packets.SupportedSPN;

public class DataRepository {

    private static DataRepository instance = new DataRepository();
    /**
     * Map of OBD Module Source Address to {@link OBDModuleInformation}
     */
    private final Map<Integer, OBDModuleInformation> obdModules = new HashMap<>();
    private double koeoEngineReferenceTorque;
    private VehicleInformation vehicleInformation;
    private long part11StartTime;

    private DataRepository() {
    }

    public static void clearInstance() {
        instance = null;
    }

    public static DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    /** Used by tests to get a clean repository. */
    public static DataRepository newInstance() {
        clearInstance();
        return getInstance();
    }

    public double getKoeoEngineReferenceTorque() {
        return koeoEngineReferenceTorque;
    }

    public void setKoeoEngineReferenceTorque(double koeoEngineReferenceTorque) {
        this.koeoEngineReferenceTorque = koeoEngineReferenceTorque;
    }

    public int getFunctionZeroAddress() {
        return obdModules.values()
                         .stream()
                         .filter(m -> m.getFunction() == 0)
                         .map(OBDModuleInformation::getSourceAddress)
                         .findFirst()
                         .orElse(-1);
    }

    public OBDModuleInformation getObdModule(int sourceAddress) {
        OBDModuleInformation info = obdModules.get(sourceAddress);
        return info == null ? null : info.clone();
    }

    public List<Integer> getObdModuleAddresses() {
        return obdModules.keySet().stream().sorted().collect(Collectors.toList());
    }

    public Collection<OBDModuleInformation> getObdModules() {
        return obdModules.values()
                         .stream()
                         .sorted(Comparator.comparingInt(OBDModuleInformation::getSourceAddress))
                         .collect(Collectors.toList());
    }

    public VehicleInformation getVehicleInformation() {
        return Optional.ofNullable(vehicleInformation).map(VehicleInformation::clone).orElse(null);
    }

    public void setVehicleInformation(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }

    public boolean isObdModule(int sourceAddress) {
        return obdModules.containsKey(sourceAddress);
    }

    public void putObdModule(OBDModuleInformation obdModuleInformation) {
        obdModules.put(obdModuleInformation.getSourceAddress(), obdModuleInformation);
    }

    public long getPart11StartTime() {
        return part11StartTime;
    }

    public void setPart11StartTime(long part11StartTime) {
        this.part11StartTime = part11StartTime;
    }

    private Collection<OBDModuleInformation> getOBDModules(Integer moduleAddress) {
        Collection<OBDModuleInformation> modules;
        if (moduleAddress == null) {
            modules = getObdModules();
        } else {
            OBDModuleInformation obdModule = getObdModule(moduleAddress);
            if (obdModule == null) {
                modules = List.of(); // Don't return Supported SPNs for non-OBD Modules
            } else {
                modules = List.of(obdModule);
            }
        }
        return modules;
    }

    private final Map<Integer, List<Integer>> supportedSpnsByAddress = new HashMap<>();

    public List<Integer> getModuleSupportedSPNs(Integer moduleAddress) {
        return supportedSpnsByAddress.computeIfAbsent(moduleAddress,
                                                      // this is expensive
                                                      a -> getOBDModules(a).stream()
                                                                           .flatMap(m -> m.getFilteredDataStreamSPNs()
                                                                                          .stream())
                                                                           .map(SupportedSPN::getSpn)
                                                                           .distinct()
                                                                           .sorted()
                                                                           .collect(Collectors.toList()));
    }
}

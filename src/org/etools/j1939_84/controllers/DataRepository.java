/*
 * Copyright (c) 2020. Electronic Tools Institute
 */

package org.etools.j1939_84.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;

public class DataRepository {

    private static DataRepository instance = new DataRepository();

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

    private boolean isAfterCodeClear;

    private double koeoEngineReferenceTorque;

    /**
     * Map of OBD Module Source Address to {@link OBDModuleInformation}
     */
    private final Map<Integer, OBDModuleInformation> obdModules = new HashMap<>();

    private VehicleInformation vehicleInformation;

    private DataRepository() {
    }

    public double getKoeoEngineReferenceTorque() {
        return koeoEngineReferenceTorque;
    }

    public int getFunctionZeroAddress() {
        return obdModules.values().stream().filter(m -> m.getFunction() == 0).map(OBDModuleInformation::getSourceAddress).findFirst().orElse(-1);
    }

    public OBDModuleInformation getObdModule(int sourceAddress) {
        OBDModuleInformation info = obdModules.get(sourceAddress);
        return info == null ? null : info.clone();
    }

    public List<Integer> getObdModuleAddresses() {
        return obdModules.keySet().stream().sorted().collect(Collectors.toList());
    }

    public Collection<OBDModuleInformation> getObdModules() {
        return new HashSet<>(obdModules.values());
    }

    public VehicleInformation getVehicleInformation() {
        return Optional.ofNullable(vehicleInformation).map(VehicleInformation::clone).orElse(null);
    }

    /**
     * Flag tracking DM11 code clear was sent {@link DM11ClearActiveDTCsPacket}
     */
    public boolean isAfterCodeClear() {
        return isAfterCodeClear;
    }

    public boolean isObdModule(int sourceAddress) {
        return obdModules.containsKey(sourceAddress);
    }

    @Deprecated
    public void putObdModule(int sourceAddress, OBDModuleInformation information) {
        obdModules.put(sourceAddress, information);
    }

    public void putObdModule(OBDModuleInformation obdModuleInformation) {
        obdModules.put(obdModuleInformation.getSourceAddress(), obdModuleInformation);
    }

    public void setIsAfterCodeClear(boolean isAfterCodeClear) {
        this.isAfterCodeClear = isAfterCodeClear;
    }

    public void setKoeoEngineReferenceTorque(double koeoEngineReferenceTorque) {
        this.koeoEngineReferenceTorque = koeoEngineReferenceTorque;
    }

    public void setVehicleInformation(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }
}

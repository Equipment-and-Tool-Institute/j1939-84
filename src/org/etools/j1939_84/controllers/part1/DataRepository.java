package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;

public class DataRepository {

    private boolean isAfterCodeClear;

    /**
     * Map of OBD Module Source Address to {@link OBDModuleInformation}
     */
    private final Map<Integer, OBDModuleInformation> obdModules = new HashMap<>();

    private VehicleInformation vehicleInformation;

    public OBDModuleInformation getObdModule(int sourceAddress) {
        return obdModules.get(sourceAddress);
    }

    public List<Integer> getObdModuleAddresses() {
        return obdModules.keySet().stream().sorted().collect(Collectors.toList());
    }

    public Collection<OBDModuleInformation> getObdModules() {
        return new HashSet<>(obdModules.values());
    }

    public VehicleInformation getVehicleInformation() {
        return vehicleInformation;
    }

    public boolean isAfterCodeClear() {
        return isAfterCodeClear;
    }

    public int obdModuleCount() {
        return obdModules.size();
    }

    public void putObdModule(int sourceAddress, OBDModuleInformation information) {
        obdModules.put(sourceAddress, information);
    }

    public void setIsAfterCodeClear(boolean isAfterCodeClear) {
        this.isAfterCodeClear = isAfterCodeClear;
    }

    public void setVehicleInformation(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }

}

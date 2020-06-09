package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;

public class DataRepository {

    /**
     * Map of OBD Module Source Address to {@link OBDModuleInformation}
     */
    private final Map<Integer, OBDModuleInformation> obdModules = new HashMap<>();

    private VehicleInformation vehicleInformation;

    public OBDModuleInformation getObdModule(int sourceAddress) {
        return obdModules.get(sourceAddress);
    }

    public Set<Integer> getObdModuleAddresses() {
        return new HashSet<>(obdModules.keySet());
    }

    public Collection<OBDModuleInformation> getObdModules() {
        return new HashSet<>(obdModules.values());
    }

    public VehicleInformation getVehicleInformation() {
        return vehicleInformation;
    }

    public int obdModuleCount() {
        return obdModules.size();
    }

    public void putObdModule(int sourceAddress, OBDModuleInformation information) {
        obdModules.put(sourceAddress, information);
    }

    public void setVehicleInformation(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }

}

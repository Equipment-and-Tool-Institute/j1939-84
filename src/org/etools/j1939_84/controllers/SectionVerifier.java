/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;

import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.CommunicationsModule;

public class SectionVerifier {
    private final DataRepository dataRepository;
    private final CommunicationsModule communicationsModule;
    private final VehicleInformationModule vehInfoModule;
    private final int partNumber;
    private final int stepNumber;

    SectionVerifier(int partNumber, int stepNumber) {
        this(DataRepository.getInstance(),
             new CommunicationsModule(),
             new VehicleInformationModule(),
             partNumber,
             stepNumber);
    }

    protected SectionVerifier(DataRepository dataRepository,
                                      CommunicationsModule communicationsModule,
                                      VehicleInformationModule vehInfoModule,
                                      int partNumber,
                                      int stepNumber) {
        this.dataRepository = dataRepository;
        this.communicationsModule = communicationsModule;
        this.vehInfoModule = vehInfoModule;
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
    }
    public void setJ1939(J1939 j1939) {
        communicationsModule.setJ1939(j1939);
        vehInfoModule.setJ1939(j1939);
    }

    public int getPartNumber() {
        return partNumber;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }

    public CommunicationsModule getCommunicationsModule() {
        return communicationsModule;
    }

    protected void addFailure(ResultsListener listener, String section, boolean verifyIsErased, GenericPacket p) {
        addFailure(listener, section, verifyIsErased, p.getModuleName(), p.getName());
    }

    protected void addFailure(ResultsListener listener, String message) {
        listener.addOutcome(getPartNumber(), getStepNumber(), FAIL, message);
    }

    protected  <T extends GenericPacket> T getLatest(Class<T> clazz, int address) {
        return getDataRepository().getObdModule(address).getLatest(clazz);
    }

    protected boolean isEngineModuleYearLessThan2019() {
        return getDataRepository().getVehicleInformation()
                .getEngineModelYear() < 2019;
    }

    protected void addFailure(ResultsListener listener,
                            String section,
                            boolean verifyIsErased,
                            String moduleName,
                            String dataName) {
        if (verifyIsErased) {
            addFailure(listener, section + " - " + moduleName + " did not erase " + dataName + " data");
        } else {
            addFailure(listener, section + " - " + moduleName + " erased " + dataName + " data");
        }
    }

    protected static class Result {
        public final boolean isErased;
        public final boolean isMixed;

        protected Result(boolean isErased, boolean isMixed) {
            this.isErased = isErased;
            this.isMixed = isMixed;
        }
    }
}

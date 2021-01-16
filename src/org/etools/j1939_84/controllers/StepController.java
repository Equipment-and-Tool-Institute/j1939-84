/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public abstract class StepController extends Controller {

    private final int partNumber;
    private final int stepNumber;
    private final int totalSteps;

    protected StepController(Executor executor,
                             EngineSpeedModule engineSpeedModule,
                             BannerModule bannerModule,
                             VehicleInformationModule vehicleInformationModule,
                             DateTimeModule dateTimeModule,
                             int partNumber,
                             int stepNumber,
                             int totalSteps) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, dateTimeModule);
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
        this.totalSteps = totalSteps;
    }

    protected void addFailure(String message) {
        addFailure(getPartNumber(), getStepNumber(), message);
    }

    protected void addWarning(String message) {
        addWarning(getPartNumber(), getStepNumber(), message);
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
                    addFailure(section + " - Difference compared to data received during global request");
                    break; //Only report the error once
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

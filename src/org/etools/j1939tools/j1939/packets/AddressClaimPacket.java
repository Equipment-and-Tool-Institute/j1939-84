/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.Lookup;

/**
 * Parses the Address Claim Packet
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class AddressClaimPacket extends GenericPacket {

    /**
     * The Parameter Group Number for the Address Claim Packet
     */
    public static final int PGN = 0xEE00;
    /**
     * The cache of Industry Group/Vehicle System/Functional Names
     */
    private static Set<Name> names;
    /**
     * The ID of the Function
     */
    private final int functionId;
    private final String source;
    /**
     * The String that's returned by the toString method
     */
    private final String string;

    /**
     * Constructor
     *
     * @param packet
     *                   the {@link Packet} to parse
     */
    public AddressClaimPacket(Packet packet) {
        super(packet);

        source = Lookup.getAddressName(getSourceAddress());
        int identityNumber = (packet.get(0) & 0xFF) + ((packet.get(1) & 0xFF) << 8) + ((packet.get(2) & 0x1F) << 16);
        int manufacturerId = ((packet.get(2) & 0xE0) >> 5) + ((packet.get(3) & 0xFF) << 3);

        int functionInstance = (packet.get(4) & 0xF8) >> 3;
        int ecuInstance = packet.get(4) & 0x07;

        functionId = packet.get(5);

        int vehicleSystemId = (packet.get(6) & 0xFE) >> 1;

        boolean arbitraryAddressCapable = (packet.get(7) & 0x80) != 0;
        int industryGroupId = (packet.get(7) & 0x70) >> 4;
        int vehicleSystemInstance = packet.get(7) & 0x0F;

        String manufacturer = Lookup.getManufacturer(manufacturerId);
        Name name = findName(industryGroupId, vehicleSystemId, functionId);

        String result = source + " reported as: {" + NL;
        result += "  Industry Group: " + name.getIndustryGroup() + NL;
        result += "  Vehicle System: " + name.getVehicleSystem() + ", System Instance: " + vehicleSystemInstance + NL;
        result += "  Function: " + name.getFunction() + ", Functional Instance: " + functionInstance
                + ", ECU Instance: "
                + ecuInstance + NL;
        result += "  Manufactured by: " + manufacturer + ", Identity Number: " + identityNumber + NL;
        result += "  Is " + (arbitraryAddressCapable ? "" : "not ") + "arbitrary address capable." + NL;
        result += "}";
        string = result;
    }

    /**
     * Helper method to lookup the {@link Name} for the Address Claim
     *
     * @param  industryGroupId
     *                             the ID of the Industry Group
     * @param  vehicleSystemId
     *                             the ID of the Vehicle System
     * @param  functionId
     *                             the ID of the Function
     * @return                 the {@link Name} that corresponds to the Industry Group, Vehicle
     *                         System and Function
     */
    private static Name findName(int industryGroupId, int vehicleSystemId, int functionId) {
        int id = Name.getId(industryGroupId, vehicleSystemId, functionId);
        for (Name name : getNames()) {
            if (name.id == id) {
                return name;
            }
        }
        return new Name(industryGroupId,
                        vehicleSystemId,
                        "Unknown System (" + vehicleSystemId + ")",
                        functionId,
                        "Unknown Function (" + functionId + ")");
    }

    /**
     * Method to cache and return the {@link Collection} of {@link Name}s
     *
     * @return collection of Names
     */
    private synchronized static Collection<Name> getNames() {
        if (names == null) {
            names = new HashSet<>();
            names.add(new Name(0, 0, "Non-specific System", 0, "Engine"));
            names.add(new Name(0, 0, "Non-specific System", 1, "Auxiliary Power Unit (APU)"));
            names.add(new Name(0, 0, "Non-specific System", 2, "Electric Propulsion Control"));
            names.add(new Name(0, 0, "Non-specific System", 3, "Transmission"));
            names.add(new Name(0, 0, "Non-specific System", 4, "Battery Pack Monitor"));
            names.add(new Name(0, 0, "Non-specific System", 5, "Shift Control/Console"));
            names.add(new Name(0, 0, "Non-specific System", 6, "Power TakeOff - (Main or Rear)"));
            names.add(new Name(0, 0, "Non-specific System", 7, "Axle - Steering"));
            names.add(new Name(0, 0, "Non-specific System", 8, "Axle - Drive"));
            names.add(new Name(0, 0, "Non-specific System", 9, "Brakes - System Controller"));
            names.add(new Name(0, 0, "Non-specific System", 10, "Brakes - Steer Axle"));
            names.add(new Name(0, 0, "Non-specific System", 11, "Brakes - Drive axle"));
            names.add(new Name(0, 0, "Non-specific System", 12, "Retarder - Engine"));
            names.add(new Name(0, 0, "Non-specific System", 13, "Retarder - Driveline"));
            names.add(new Name(0, 0, "Non-specific System", 14, "Cruise Control"));
            names.add(new Name(0, 0, "Non-specific System", 15, "Fuel System"));
            names.add(new Name(0, 0, "Non-specific System", 16, "Steering Controller"));
            names.add(new Name(0, 0, "Non-specific System", 17, "Suspension - Steer Axle"));
            names.add(new Name(0, 0, "Non-specific System", 18, "Suspension - Drive Axle"));
            names.add(new Name(0, 0, "Non-specific System", 19, "Instrument Cluster"));
            names.add(new Name(0, 0, "Non-specific System", 20, "Trip Recorder"));
            names.add(new Name(0, 0, "Non-specific System", 21, "Cab Climate Control"));
            names.add(new Name(0, 0, "Non-specific System", 22, "Aerodynamic Control"));
            names.add(new Name(0, 0, "Non-specific System", 23, "Vehicle Navigation"));
            names.add(new Name(0, 0, "Non-specific System", 24, "Vehicle Security"));
            names.add(new Name(0, 0, "Non-specific System", 25, "Network Interconnect ECU"));
            names.add(new Name(0, 0, "Non-specific System", 26, "Body Controller"));
            names.add(new Name(0, 0, "Non-specific System", 27, "Power TakeOff (Secondary or Front)"));
            names.add(new Name(0, 0, "Non-specific System", 28, "Off Vehicle Gateway"));
            names.add(new Name(0, 0, "Non-specific System", 29, "Virtual Terminal (in cab)"));
            names.add(new Name(0, 0, "Non-specific System", 30, "Management Computer"));
            names.add(new Name(0, 0, "Non-specific System", 31, "Propulsion Battery Charger"));
            names.add(new Name(0, 0, "Non-specific System", 32, "Headway Controller"));
            names.add(new Name(0, 0, "Non-specific System", 33, "System Monitor"));
            names.add(new Name(0, 0, "Non-specific System", 34, "Hydraulic Pump Controller"));
            names.add(new Name(0, 0, "Non-specific System", 35, "Suspension - System Controller"));
            names.add(new Name(0, 0, "Non-specific System", 36, "Pneumatic - System Controller"));
            names.add(new Name(0, 0, "Non-specific System", 37, "Cab Controller"));
            names.add(new Name(0, 0, "Non-specific System", 38, "Tire Pressure Control"));
            names.add(new Name(0, 0, "Non-specific System", 39, "Ignition Control Module"));
            names.add(new Name(0, 0, "Non-specific System", 40, "Seat Control"));
            names.add(new Name(0, 0, "Non-specific System", 41, "Lighting - Operator Controls"));
            names.add(new Name(0, 0, "Non-specific System", 42, "Water Pump Control"));
            names.add(new Name(0, 0, "Non-specific System", 43, "Transmission Display"));
            names.add(new Name(0, 0, "Non-specific System", 44, "Exhaust Emission Control"));
            names.add(new Name(0, 0, "Non-specific System", 45, "Vehicle Dynamic Stability Control"));
            names.add(new Name(0, 0, "Non-specific System", 46, "Oil Sensor Unit"));
            names.add(new Name(0, 0, "Non-specific System", 47, "Information System Controller"));
            names.add(new Name(0, 0, "Non-specific System", 48, "Ramp Control"));
            names.add(new Name(0, 0, "Non-specific System", 49, "Clutch/Converter Control"));
            names.add(new Name(0, 0, "Non-specific System", 50, "Auxiliary Heater"));
            names.add(new Name(0, 0, "Non-specific System", 51, "Forward-Looking Collision Warning System"));
            names.add(new Name(0, 0, "Non-specific System", 52, "Chassis Controller"));
            names.add(new Name(0, 0, "Non-specific System", 53, "Alternator/Charging System"));
            names.add(new Name(0, 0, "Non-specific System", 54, "Communications Unit, Cellular"));
            names.add(new Name(0, 0, "Non-specific System", 55, "Communications Unit, Satellite"));
            names.add(new Name(0, 0, "Non-specific System", 56, "Communications Unit, Radio"));
            names.add(new Name(0, 0, "Non-specific System", 57, "Steering Column Unit"));
            names.add(new Name(0, 0, "Non-specific System", 58, "Fan Drive Control"));
            names.add(new Name(0, 0, "Non-specific System", 59, "Starter"));
            names.add(new Name(0, 0, "Non-specific System", 60, "Cab Display"));
            names.add(new Name(0, 0, "Non-specific System", 61, "File Server / Printer"));
            names.add(new Name(0, 0, "Non-specific System", 62, "On-Board Diagnostic Unit"));
            names.add(new Name(0, 0, "Non-specific System", 63, "Engine Valve Controller"));
            names.add(new Name(0, 0, "Non-specific System", 64, "Endurance Braking"));
            names.add(new Name(0, 0, "Non-specific System", 65, "Gas Flow Measurement"));
            names.add(new Name(0, 0, "Non-specific System", 66, "I/O Controller"));
            names.add(new Name(0, 0, "Non-specific System", 67, "Electrical System Controller"));
            names.add(new Name(0, 0, "Non-specific System", 68, "Aftertreatment system gas measurement"));
            names.add(new Name(0, 0, "Non-specific System", 69, "Engine Emission Aftertreatment System"));
            names.add(new Name(0, 0, "Non-specific System", 70, "Auxiliary Regeneration Device"));
            names.add(new Name(0, 0, "Non-specific System", 71, "Transfer Case Control"));
            names.add(new Name(0, 0, "Non-specific System", 72, "Coolant Valve Controller"));
            names.add(new Name(0, 0, "Non-specific System", 73, "Rollover Detection Control"));
            names.add(new Name(0, 0, "Non-specific System", 74, "Lubrication System"));
            names.add(new Name(0, 0, "Non-specific System", 75, "Supplemental Fan"));
            names.add(new Name(0, 0, "Non-specific System", 76, "Temperature Sensor"));
            names.add(new Name(0, 0, "Non-specific System", 77, "Fuel Properties Sensor"));
            names.add(new Name(0, 0, "Non-specific System", 78, "Fire Suppression System"));
            names.add(new Name(0, 0, "Non-specific System", 79, "Power Systems Manager"));
            names.add(new Name(0, 0, "Non-specific System", 80, "Electric Powertrain"));
            names.add(new Name(0, 0, "Non-specific System", 81, "Hydraulic Powertrain"));
            names.add(new Name(0, 0, "Non-specific System", 82, "File Server"));
            names.add(new Name(0, 0, "Non-specific System", 83, "Printer"));
            names.add(new Name(0, 0, "Non-specific System", 84, "Start Aid Device"));
            names.add(new Name(0, 0, "Non-specific System", 128, "Reserved"));
            names.add(new Name(0, 0, "Non-specific System", 129, "Off-board diagnostic-service tool"));
            names.add(new Name(0, 0, "Non-specific System", 130, "On-board data logger"));
            names.add(new Name(0, 0, "Non-specific System", 131, "PC Keyboard"));
            names.add(new Name(0, 0, "Non-specific System", 132, "Safety Restraint System"));
            names.add(new Name(0, 0, "Non-specific System", 133, "Turbocharger"));
            names.add(new Name(0, 0, "Non-specific System", 134, "Ground based speed sensor"));
            names.add(new Name(0, 0, "Non-specific System", 135, "Keypad"));
            names.add(new Name(0, 0, "Non-specific System", 136, "Humidity sensor"));
            names.add(new Name(0, 0, "Non-specific System", 137, "Thermal Management System Controller"));
            names.add(new Name(0, 0, "Non-specific System", 138, "Brake Stroke Alert"));
            names.add(new Name(0, 0, "Non-specific System", 139, "On-board axle group scale"));
            names.add(new Name(0, 0, "Non-specific System", 140, "On-board axle group display"));
            names.add(new Name(0, 0, "Non-specific System", 141, "Battery Charger"));
            names.add(new Name(0, 0, "Non-specific System", 142, "Turbocharger Compressor Bypass"));
            names.add(new Name(0, 0, "Non-specific System", 143, "Turbocharger Wastegate"));
            names.add(new Name(0, 0, "Non-specific System", 144, "Throttle"));
            names.add(new Name(0, 0, "Non-specific System", 145, "Inertial Sensor"));
            names.add(new Name(0, 0, "Non-specific System", 146, "Fuel Actuator"));
            names.add(new Name(0, 0, "Non-specific System", 147, "Engine EGR"));
            names.add(new Name(0, 0, "Non-specific System", 148, "Engine Exhaust Backpressure"));
            names.add(new Name(0, 0, "Non-specific System", 149, "On-board bin weighing scale"));
            names.add(new Name(0, 0, "Non-specific System", 150, "On-board bin weighing scale display"));
            names.add(new Name(0, 0, "Non-specific System", 151, "Engine Cylinder Pressure Monitoring System"));
            names.add(new Name(0, 0, "Non-specific System", 255, "Not Available"));
            names.add(new Name(0, 127, "Not Available", 255, "Not Available"));
            names.add(new Name(1, 0, "Non-specific System", 128, "Tachograph"));
            names.add(new Name(1, 0, "Non-specific System", 129, "Door Controller"));
            names.add(new Name(1, 0, "Non-specific System", 130, "Articulation Turntable Control"));
            names.add(new Name(1, 0, "Non-specific System", 131, "Body-to-Vehicle Interface Control"));
            names.add(new Name(1, 0, "Non-specific System", 132, "Slope Sensor"));
            names.add(new Name(1, 0, "Non-specific System", 134, "Retarder Display"));
            names.add(new Name(1, 0, "Non-specific System", 135, "Differential Lock Controller"));
            names.add(new Name(1, 0, "Non-specific System", 136, "Low-Voltage Disconnect"));
            names.add(new Name(1, 0, "Non-specific System", 137, "Roadway Information"));
            names.add(new Name(1, 0, "Non-specific System", 255, "Not Available"));
            names.add(new Name(1, 1, "Tractor", 128, "Forward Road Image Processing"));
            names.add(new Name(1, 1, "Tractor", 129, "Fifth Wheel Smart System"));
            names.add(new Name(1, 1, "Tractor", 130, "Catalyst Fluid Sensor"));
            names.add(new Name(1, 1, "Tractor", 131, "Adaptive Front Lighting System"));
            names.add(new Name(1, 1, "Tractor", 132, "Idle Control System"));
            names.add(new Name(1, 1, "Tractor", 133, "User Interface System"));
            names.add(new Name(1, 1, "Tractor", 255, "Not Available"));
            names.add(new Name(1, 2, "Trailer", 255, "Not Available"));
            names.add(new Name(1, 127, "Not Available", 255, "Not Available"));
            names.add(new Name(2, 0, "Non-specific System", 128, "Non Virtual Terminal Display"));
            names.add(new Name(2, 0, "Non-specific System", 129, "Operator Controls - Machine Specific"));
            names.add(new Name(2, 0, "Non-specific System", 130, "Task Controller (Mapping Computer)"));
            names.add(new Name(2, 0, "Non-specific System", 131, "Position Control"));
            names.add(new Name(2, 0, "Non-specific System", 132, "Machine Control"));
            names.add(new Name(2, 0, "Non-specific System", 133, "Foreign Object Detection"));
            names.add(new Name(2, 0, "Non-specific System", 134, "Tractor ECU"));
            names.add(new Name(2, 0, "Non-specific System", 135, "Sequence Control Master"));
            names.add(new Name(2, 0, "Non-specific System", 136, "Product Dosing"));
            names.add(new Name(2, 0, "Non-specific System", 137, "Product Treatment"));
            names.add(new Name(2, 0, "Non-specific System", 138, "reserved"));
            names.add(new Name(2, 0, "Non-specific System", 139, "Data Logger"));
            names.add(new Name(2, 0, "Non-specific System", 140, "Decision Support"));
            names.add(new Name(2, 0, "Non-specific System", 141, "Lighting Controller"));
            names.add(new Name(2, 0, "Non-specific System", 255, "Not Available"));
            names.add(new Name(2, 1, "Tractor", 129, "Auxiliary Valve Control"));
            names.add(new Name(2, 1, "Tractor", 130, "Rear Hitch Control"));
            names.add(new Name(2, 1, "Tractor", 131, "Front Hitch Control"));
            names.add(new Name(2, 1, "Tractor", 132, "Tractor Machine Control"));
            names.add(new Name(2, 1, "Tractor", 134, "Center Hitch Control"));
            names.add(new Name(2, 1, "Tractor", 255, "Not Available"));
            names.add(new Name(2, 2, "Tillage", 132, "Tillage Machine Control"));
            names.add(new Name(2, 2, "Tillage", 135, "Tillage Depth Control"));
            names.add(new Name(2, 2, "Tillage", 136, "Frame Control"));
            names.add(new Name(2, 2, "Tillage", 255, "Not Available"));
            names.add(new Name(2, 3, "Secondary Tillage", 132, "Secondary Tillage Machine Control"));
            names.add(new Name(2, 3, "Secondary Tillage", 135, "Secondary Tillage Depth Control"));
            names.add(new Name(2, 3, "Secondary Tillage", 136, "Frame Control"));
            names.add(new Name(2, 3, "Secondary Tillage", 255, "Not Available"));
            names.add(new Name(2, 4, "Planters/Seeders", 128, "Seed Rate Control"));
            names.add(new Name(2, 4, "Planters/Seeders", 129, "Section On/Off Control"));
            names.add(new Name(2, 4, "Planters/Seeders", 131, "Position Control"));
            names.add(new Name(2, 4, "Planters/Seeders", 132, "Planters/ Seeders Machine Control"));
            names.add(new Name(2, 4, "Planters/Seeders", 133, "Product Flow"));
            names.add(new Name(2, 4, "Planters/Seeders", 134, "Product Level"));
            names.add(new Name(2, 4, "Planters/Seeders", 135, "Depth Control"));
            names.add(new Name(2, 4, "Planters/Seeders", 136, "Frame Control"));
            names.add(new Name(2, 4, "Planters/Seeders", 137, "Down Pressure"));
            names.add(new Name(2, 4, "Planters/Seeders", 255, "Not Available"));
            names.add(new Name(2, 5, "Fertilizers", 128, "Fertilize Rate Control"));
            names.add(new Name(2, 5, "Fertilizers", 129, "Section On/Off Control"));
            names.add(new Name(2, 5, "Fertilizers", 130, "Product Pressure"));
            names.add(new Name(2, 5, "Fertilizers", 131, "Position Control"));
            names.add(new Name(2, 5, "Fertilizers", 132, "Fertilizers Machine Control"));
            names.add(new Name(2, 5, "Fertilizers", 133, "Product Flow"));
            names.add(new Name(2, 5, "Fertilizers", 134, "Product Level"));
            names.add(new Name(2, 5, "Fertilizers", 135, "Height/Depth Control"));
            names.add(new Name(2, 5, "Fertilizers", 136, "Frame Control"));
            names.add(new Name(2, 5, "Fertilizers", 255, "Not Available"));
            names.add(new Name(2, 6, "Sprayers", 128, "Spray Rate Control"));
            names.add(new Name(2, 6, "Sprayers", 129, "Section On/Off Control"));
            names.add(new Name(2, 6, "Sprayers", 130, "Product Pressure"));
            names.add(new Name(2, 6, "Sprayers", 131, "Position Control"));
            names.add(new Name(2, 6, "Sprayers", 132, "Sprayers Machine Control"));
            names.add(new Name(2, 6, "Sprayers", 133, "Product Flow"));
            names.add(new Name(2, 6, "Sprayers", 134, "Product Level"));
            names.add(new Name(2, 6, "Sprayers", 135, "Boom Height Control"));
            names.add(new Name(2, 6, "Sprayers", 136, "Frame Control"));
            names.add(new Name(2, 6, "Sprayers", 255, "Not Available"));
            names.add(new Name(2, 7, "Harvesters", 128, "Tailing Monitor"));
            names.add(new Name(2, 7, "Harvesters", 129, "Header Control"));
            names.add(new Name(2, 7, "Harvesters", 130, "Product Loss Monitor"));
            names.add(new Name(2, 7, "Harvesters", 131, "Product Moisture"));
            names.add(new Name(2, 7, "Harvesters", 132, "Harvester Machine Control"));
            names.add(new Name(2, 7, "Harvesters", 133, "Product Flow"));
            names.add(new Name(2, 7, "Harvesters", 134, "Product Level"));
            names.add(new Name(2, 7, "Harvesters", 135, "Header Height Control"));
            names.add(new Name(2, 7, "Harvesters", 255, "Not Available"));
            names.add(new Name(2, 8, "Root Harvesters", 132, "Root Harvesters Machine Control"));
            names.add(new Name(2, 8, "Root Harvesters", 133, "Product Flow"));
            names.add(new Name(2, 8, "Root Harvesters", 134, "Product Level"));
            names.add(new Name(2, 8, "Root Harvesters", 135, "Depth Control"));
            names.add(new Name(2, 8, "Root Harvesters", 255, "Not Available"));
            names.add(new Name(2, 9, "Forage", 128, "Twine Wrapper Control"));
            names.add(new Name(2, 9, "Forage", 129, "Product Packaging Control"));
            names.add(new Name(2, 9, "Forage", 131, "Product Moisture"));
            names.add(new Name(2, 9, "Forage", 132, "Forage Machine Control"));
            names.add(new Name(2, 9, "Forage", 133, "Product Flow"));
            names.add(new Name(2, 9, "Forage", 135, "Working Height Control"));
            names.add(new Name(2, 9, "Forage", 255, "Not Available"));
            names.add(new Name(2, 10, "Irrigation", 255, "Not Available"));
            names.add(new Name(2, 11, "Transport/Trailer", 132, "Transport Machine Control"));
            names.add(new Name(2, 11, "Transport/Trailer", 136, "Unload Control"));
            names.add(new Name(2, 11, "Transport/Trailer", 255, "Not Available"));
            names.add(new Name(2, 12, "Farm Yard Operations", 255, "Not Available"));
            names.add(new Name(2, 13, "Powered Auxiliary Devices", 132, "Powered Devices Machine Control"));
            names.add(new Name(2, 13, "Powered Auxiliary Devices", 255, "Not Available"));
            names.add(new Name(2, 14, "Special Crops", 132, "Special Crop Machine Control"));
            names.add(new Name(2, 14, "Special Crops", 255, "Not Available"));
            names.add(new Name(2, 15, "Earth Work", 128, "Material Rate Control"));
            names.add(new Name(2, 15, "Earth Work", 132, "Earthworks Machine Control"));
            names.add(new Name(2, 15, "Earth Work", 133, "Material Flow"));
            names.add(new Name(2, 15, "Earth Work", 134, "Material Level"));
            names.add(new Name(2, 15, "Earth Work", 135, "Depth Control"));
            names.add(new Name(2, 15, "Earth Work", 255, "Not Available"));
            names.add(new Name(2, 16, "Skidder", 132, "Skidder Machine Control"));
            names.add(new Name(2, 16, "Skidder", 255, "Not Available"));
            names.add(new Name(2, 17, "Sensor Systems", 128, "Guidance Feeler"));
            names.add(new Name(2, 17, "Sensor Systems", 129, "Camera System"));
            names.add(new Name(2, 17, "Sensor Systems", 130, "Crop Scouting"));
            names.add(new Name(2, 17, "Sensor Systems", 131, "Material Properties Sensing"));
            names.add(new Name(2, 17, "Sensor Systems", 132, "Inertial Measurement Unit (IMU)"));
            names.add(new Name(2, 17, "Sensor Systems", 133, "Product flow"));
            names.add(new Name(2, 17, "Sensor Systems", 134, "Product Level"));
            names.add(new Name(2, 17, "Sensor Systems", 135, "Product Mass"));
            names.add(new Name(2, 17, "Sensor Systems", 136, "Vibration/Knock"));
            names.add(new Name(2, 17, "Sensor Systems", 137, "Weather Instruments"));
            names.add(new Name(2, 19, "Timber Harvesters", 132, "Timber Harvestors Machine Control"));
            names.add(new Name(2, 20, "Forwarders", 132, "Forwarders Machine Control"));
            names.add(new Name(2, 21, "Timber Loaders", 132, "Timber Loaders Machine Control"));
            names.add(new Name(2, 22, "Timber Processing Machines", 132, "Timber Processing Machine Control"));
            names.add(new Name(2, 23, "Mulchers", 132, "Mulcher Machine Control"));
            names.add(new Name(2, 24, "Utility Vehicles", 132, "Utility Machine Control"));
            names.add(new Name(2, 25, "Slurry/Manure Applicators", 128, "Slurry/Manure Rate Control"));
            names.add(new Name(2, 25, "Slurry/Manure Applicators", 129, "Section On/Off Control"));
            names.add(new Name(2, 25, "Slurry/Manure Applicators", 130, "Product Pressure"));
            names.add(new Name(2, 25, "Slurry/Manure Applicators", 132, "Slurry/Manure Machine Control"));
            names.add(new Name(2, 25, "Slurry/Manure Applicators", 133, "Product Flow"));
            names.add(new Name(2, 25, "Slurry/Manure Applicators", 134, "Product Level"));
            names.add(new Name(2, 25, "Slurry/Manure Applicators", 135, "Boom Height Control"));
            names.add(new Name(2, 26, "Feeders/Mixers", 128, "Feeder/Mixer Rate Control"));
            names.add(new Name(2, 26, "Feeders/Mixers", 129, "Section On/Off Control"));
            names.add(new Name(2, 26, "Feeders/Mixers", 130, "Product Pressure"));
            names.add(new Name(2, 26, "Feeders/Mixers", 132, "Feeder/Mixer Machine Control"));
            names.add(new Name(2, 26, "Feeders/Mixers", 133, "Product Flow"));
            names.add(new Name(2, 26, "Feeders/Mixers", 134, "Product Level"));
            names.add(new Name(2, 26, "Feeders/Mixers", 135, "Boom Height Control"));
            names.add(new Name(2, 127, "Not Available", 255, "Not Available"));
            names.add(new Name(3, 0, "Non-specific system", 128, "Supplemental Engine Control Sensing"));
            names.add(new Name(3, 0, "Non-specific system", 129, "Laser Receiver"));
            names.add(new Name(3, 0, "Non-specific system", 130, "Land Leveling System Operator Interface"));
            names.add(new Name(3, 0, "Non-specific system", 131, "Land Leveling Electric Mast"));
            names.add(new Name(3, 0, "Non-specific system", 132, "Single Land Leveling System Supervisor"));
            names.add(new Name(3, 0, "Non-specific system", 133, "Land Leveling System Display"));
            names.add(new Name(3, 0, "Non-specific system", 134, "Laser Tracer"));
            names.add(new Name(3, 0, "Non-specific system", 135, "Loader Control"));
            names.add(new Name(3, 0, "Non-specific system", 136, "Slope Sensor"));
            names.add(new Name(3, 0, "Non-specific system", 137, "Liftarm Control"));
            names.add(new Name(3, 0, "Non-specific system", 138, "Supplemental Sensor Processing Units"));
            names.add(new Name(3, 0, "Non-specific system", 139, "Hydraulic System Planner"));
            names.add(new Name(3, 0, "Non-specific system", 140, "Hydraulic Valve Controller"));
            names.add(new Name(3, 0, "Non-specific system", 141, "Joystick Control"));
            names.add(new Name(3, 0, "Non-specific system", 142, "Rotation Sensor"));
            names.add(new Name(3, 0, "Non-specific system", 143, "Sonic Sensor"));
            names.add(new Name(3, 0, "Non-specific System", 144, "Survey Total Station Target"));
            names.add(new Name(3, 0, "Non-specific System", 145, "Heading Sensor"));
            names.add(new Name(3, 0, "Non-specific System", 146, "Alarm device"));
            names.add(new Name(3, 0, "Non-specific system", 255, "Not Available"));
            names.add(new Name(3, 1, "Skid Steer Loader", 128, "Main Controller"));
            names.add(new Name(3, 1, "Skid Steer Loader", 255, "Not Available"));
            names.add(new Name(3, 2, "Articulated Dump Truck", 255, "Not Available"));
            names.add(new Name(3, 3, "Backhoe", 255, "Not Available"));
            names.add(new Name(3, 4, "Crawler", 128, "Blade Controller"));
            names.add(new Name(3, 4, "Crawler", 255, "Not Available"));
            names.add(new Name(3, 5, "Excavator", 128, "Slope Sensor"));
            names.add(new Name(3, 5, "Excavator", 255, "Not Available"));
            names.add(new Name(3, 6, "Forklift", 255, "Not Available"));
            names.add(new Name(3, 7, "Four Wheel Drive Loader", 255, "Not Available"));
            names.add(new Name(3, 8, "Grader", 128, "HFWD Controller"));
            names.add(new Name(3, 8, "Grader", 255, "Not Available"));
            names.add(new Name(3, 127, "Not Available", 255, "Not Available"));
            names.add(new Name(4, 0, "Non-specific System", 128, "Alarm System Control for Marine Engines"));
            names.add(new Name(4, 0, "Non-specific System", 129, "Protection System for Marine Engines"));
            names.add(new Name(4, 0, "Non-specific System", 130, "Display for Protection System for Marine Engines"));
            names.add(new Name(4, 0, "Non-specific System", 255, "Not Available"));
            names.add(new Name(4, 10, "System tools", 255, "Not Available"));
            names.add(new Name(4, 20, "Safety systems", 255, "Not Available"));
            names.add(new Name(4, 25, "Gateway", 10, ""));
            names.add(new Name(4, 30, "Power management and lighting systems", 130, "Switch"));
            names.add(new Name(4, 30, "Power management and lighting systems", 140, "Load"));
            names.add(new Name(4, 40, "Steering systems", 130, "Follow-up Controller"));
            names.add(new Name(4, 40, "Steering systems", 140, "Mode Controller"));
            names.add(new Name(4, 40, "Steering systems", 150, "Automatic Steering Controller"));
            names.add(new Name(4, 40, "Steering systems", 160, "Heading Sensors"));
            names.add(new Name(4, 50, "Propulsion systems", 130, "Engineroom monitoring"));
            names.add(new Name(4, 50, "Propulsion systems", 140, "Engine Interface"));
            names.add(new Name(4, 50, "Propulsion systems", 150, "Engine Controller"));
            names.add(new Name(4, 50, "Propulsion systems", 160, "Engine Gateway"));
            names.add(new Name(4, 50, "Propulsion systems", 170, "Control Head"));
            names.add(new Name(4, 50, "Propulsion systems", 180, "Actuator"));
            names.add(new Name(4, 50, "Propulsion systems", 190, "Gauge Interface"));
            names.add(new Name(4, 50, "Propulsion systems", 200, "Gauge Large"));
            names.add(new Name(4, 50, "Propulsion systems", 210, "Gauge Small"));
            names.add(new Name(4, 60, "Navigation systems", 130, "Sounder, depth"));
            names.add(new Name(4, 60, "Navigation systems", 140, ""));
            names.add(new Name(4, 60, "Navigation systems", 145, "Global Navigation Satellite System (GNSS)"));
            names.add(new Name(4, 60, "Navigation systems", 150, "Loran C"));
            names.add(new Name(4, 60, "Navigation systems", 155, "Speed Sensors"));
            names.add(new Name(4, 60, "Navigation systems", 160, "Turn Rate Indicator"));
            names.add(new Name(4, 60, "Navigation systems", 170, "Integrated Navigation"));
            names.add(new Name(4, 60, "Navigation systems", 200, "Radar and/or Radar Plotting"));
            names.add(new Name(4,
                               60,
                               "Navigation systems",
                               205,
                               "Electronic Chart Display & Information System (ECDIS)"));
            names.add(new Name(4, 60, "Navigation systems", 210, "Electronic Chart System (ECS)"));
            names.add(new Name(4, 60, "Navigation systems", 220, "Direction Finder"));
            names.add(new Name(4, 70, "Communications systems", 130, "Emergency Position Indicating Beacon (EPIRB)"));
            names.add(new Name(4, 70, "Communications systems", 140, "Automatic Identification System"));
            names.add(new Name(4, 70, "Communications systems", 150, "Digital Selective Calling (DSC)"));
            names.add(new Name(4, 70, "Communications systems", 160, "Data Receiver"));
            names.add(new Name(4, 70, "Communications systems", 170, "Satellite"));
            names.add(new Name(4, 70, "Communications systems", 180, "Radio-Telephone (MF/HF)"));
            names.add(new Name(4, 70, "Communications systems", 190, "Radio-Telephone (VHF)"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 130, "Time/Date systems"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 140, "Voyage Data Recorder"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 150, "Integrated Instrumentation"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 160, "General Purpose Displays"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 170, "General Sensor Box"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 180, "Weather Instruments"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 190, "Transducer/general"));
            names.add(new Name(4, 80, "Instrumentation/general systems", 200, "NMEA 0183 Converter"));
            names.add(new Name(4, 90, "Environmental (HVAC) systems", 255, "Not Available"));
            names.add(new Name(4, 100, "Deck, cargo, and fishing equipment systems", 255, "Not Available"));
            names.add(new Name(4, 127, "Not Available", 255, "Not Available"));
            names.add(new Name(5,
                               0,
                               "Industrial-Process Control-Stationary (Gen-Sets)",
                               128,
                               "Supplemental Engine Control Sensing"));
            names.add(new Name(5,
                               0,
                               "Industrial-Process Control-Stationary (Gen-Sets)",
                               129,
                               "Generator Set Controller"));
            names.add(new Name(5,
                               0,
                               "Industrial-Process Control-Stationary (Gen-Sets)",
                               130,
                               "Generator Voltage Regulator"));
            names.add(new Name(5, 0, "Industrial-Process Control-Stationary (Gen-Sets)", 131, "Choke Actuator"));
            names.add(new Name(5, 0, "", 132, "Well Stimulation Pump"));
            names.add(new Name(5, 0, "Industrial-Process Control-Stationary (Gen-Sets)", 255, "Not Available"));
            names.add(new Name(5, 127, "Not Available", 255, "Not Available"));
        }
        return names;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * The ID of the Function
     *
     * @return the functionId
     */
    public int getFunctionId() {
        return functionId;
    }

    @Override
    public String getName() {
        return "Address Claim";
    }

    @Override
    public String toString() {
        return string;
    }

    public String getSource() {
        return source;
    }

    /**
     * Class that parses the industry group, vehicle system and function into an
     * SAE Name
     */
    private static class Name {

        /**
         * The Function Name
         */
        private final String function;
        /**
         * The unique ID of the Name
         */
        private final int id;
        /**
         * The Industry Group Name
         */
        private final String industryGroup;
        /**
         * The Vehicle System Name
         */
        private final String vehicleSystem;

        /**
         * Constructor
         *
         * @param industryGroupId
         *                            the ID of the Industry Group
         * @param vehicleSystemId
         *                            the ID of the Vehicle System
         * @param vehicleSystem
         *                            the Name of the Vehicle System
         * @param functionId
         *                            the ID of the Function
         * @param function
         *                            the Name of the Function
         */
        public Name(int industryGroupId, int vehicleSystemId, String vehicleSystem, int functionId, String function) {
            id = getId(industryGroupId, vehicleSystemId, functionId);
            industryGroup = findIndustryGroup(industryGroupId);
            this.vehicleSystem = vehicleSystem;
            this.function = function;
        }

        /**
         * Finds the name of the Industry Group given the industryGroupId
         *
         * @param  industryGroupId
         *                             the id of the Industry Group
         * @return                 the name of the Industry Group
         */
        private static String findIndustryGroup(int industryGroupId) {
            switch (industryGroupId) {
                case 0:
                    return "Global";
                case 1:
                    return "On-Highway Equipment";
                case 2:
                    return "Agricultural and Forestry Equipment";
                case 3:
                    return "Construction Equipment";
                case 4:
                    return "Marine";
                case 5:
                    return "Industrial-Process Control-Stationary (Gen-Sets)";
                default:
                    return "Unknown (" + industryGroupId + ")";
            }
        }

        /**
         * Creates a unique ID for the Name given the industry group, vehicle
         * system and function
         *
         * @param  industryGroupId
         *                             the ID of the Industry Group
         * @param  vehicleSystemId
         *                             the ID of the Vehicle System
         * @param  functionId
         *                             the ID of the Function
         * @return                 the unique ID
         */
        public static int getId(int industryGroupId, int vehicleSystemId, int functionId) {
            return industryGroupId << 16 | vehicleSystemId << 8 | functionId;
        }

        /**
         * Returns the Function Name
         *
         * @return the Function Name
         */
        public String getFunction() {
            return function;
        }

        /**
         * Returns the Industry Group Name
         *
         * @return the Industry Group Name
         */
        public String getIndustryGroup() {
            return industryGroup;
        }

        /**
         * Returns the Vehicle System Name
         *
         * @return the Vehicle System Name
         */
        public String getVehicleSystem() {
            return vehicleSystem;
        }

    }
}

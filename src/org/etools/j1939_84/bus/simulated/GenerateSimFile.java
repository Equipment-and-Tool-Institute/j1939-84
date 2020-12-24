package org.etools.j1939_84.bus.simulated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939TP;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GenerateSimFile {
    static class PacketDescriptor {
        static public PacketDescriptor sum(PacketDescriptor a, PacketDescriptor b) {
            return new PacketDescriptor(a.count + b.count, Math.min(a.min, b.min), Math.max(a.max, b.max));
        }

        final int count;

        final long min, max;

        public PacketDescriptor(int c, long min, long max) {
            count = c;
            this.min = min;
            this.max = max;
        }
    }

    final static private List<Integer> pgns = Arrays.asList(
            61444,
            65248,
            65259,
            65253,
            0xEE00,
            65260,
            54016,
            65231,
            65236,
            64949,
            64896,
            65235,
            65230,
            0xFDB8,
            0xC200,
            49408,
            64950,
            64711,
            58112, // DM7
            0xA400, // DM30
            0xFED0, // DM8
            0xFECA, // DM1
            0xFECB // DM2
    );

    static public Integer getPgn(Packet p) {
        int id = p.getId(0xFFFF);
        if (id < 0xF000) {
            id &= 0xFF00;
        }
        return id;
    }

    static public void main(String... a) throws Exception {
        new GenerateSimFile().run(new File(a[0]));
    }

    /**
     * Parse each packet from file and send through bus. Load requests with PGNs
     * requested.
     *
     * @param file
     *            File to read.
     * @param bus
     *            Bus to send packets on.
     * @param requests
     *            Out parameter of all requests in stream.
     */
    public void load(File file, EchoBus bus, Map<Integer, String> requests) {
        // 0.000953 1 14F00131x Rx d 8 FF FF FF FF FF FF FF FF Length = 0
        // BitCount = 0
        // ID = 351273265x
        LocalDateTime start = LocalDateTime.now();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            in.lines()
                    .map(line -> Packet.parseVector(start, line))
                    .filter(p -> p != null)
                    .peek(p -> {
                        if (getPgn(p) == 0xEA00) {
                            requests.put(p.get24(0), "true");
                        }
                    })
                    .forEach(p -> bus.send(p));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run(File file) throws Exception {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                run(f);
            }
            return;
        }
        if (file.getName().endsWith(".json")) {
            return;
        }
        int address = 0xF9;
        try (EchoBus bus = new EchoBus(address);
                J1939TP tp = new J1939TP(bus, bus.getAddress())) {
            Stream<Packet> out = tp.read(300, TimeUnit.DAYS);

            // load raw packets to read from TP layer
            Map<Integer, String> requests = new ConcurrentSkipListMap<>();
            // requests.put(0xE300, new ConcurrentSkipListSet<>()); // DM7
            requests.put(0xA400, "dm7"); // DM30
            requests.put(0xFED0, "dm7"); // DM8

            new Thread(() -> {
                load(file, bus, requests);
                out.close();
            }).start();

            // for each post TP packet
            int[] counter = { 0 };
            JsonArray json = out
                    .peek(p -> counter[0]++)
                    // only worry about PGNs that we use
                    .filter(p -> {
                        Integer pgn = getPgn(p);
                        // FIXM
                        return pgns.contains(pgn);// && (pgn > 0xF000 ||
                                                  // destinationAddress == 0xFF
                                                  // ||
                                                  // destinationAddress ==
                                                  // 0xF9);
                    })
                    // group sets of unique packets by PGN/DA/SA
                    .collect(Collectors.groupingBy(p -> (p.getId(0xFFFF) << 8) | p.getSource(),
                            Collectors.toCollection(() -> new LinkedHashSet<>())))
                    // only consider each of those sets
                    .values().stream()
                    // sort by PGN/SA
                    .sorted(Comparator.comparing((Set<Packet> set) -> set.iterator().next().getId(0xFFFF))
                            .thenComparing(set -> set.iterator().next().getSource()))
                    // sort each set of packets to a list to simplify manual
                    // updates and merging.
                    // .map(set ->
                    // set.stream().sorted(Comparator.comparing((Packet p) ->
                    // p.toString()))
                    // .collect(Collectors.toList()))
                    // for each set, make a JSON entry
                    .map(packets -> {
                        JsonObject o = new JsonObject();

                        // Just use first packet to gather information
                        Packet examplePacket = packets.iterator().next();

                        // Information for humans
                        int pgn = getPgn(examplePacket);
                        o.addProperty("PGN", String.format("%04X", pgn));
                        o.addProperty("SA", String.format("%02X", examplePacket.getSource()));

                        // tag as on request or broadcast
                        if (requests.containsKey(pgn)) {
                            o.addProperty("onRequest", requests.get(pgn));
                        } else {
                            o.addProperty("period", 100);
                        }
                        // add the packets as individual environs, to allow
                        // humans to tag with set,
                        // setFor, isSet, clear, isClear.
                        JsonArray packetArray = new JsonArray();
                        o.add("packets", packetArray);
                        for (Packet p : packets) {
                            JsonObject pd = new JsonObject();
                            packetArray.add(pd);
                            pd.addProperty("packet", p.toString());
                        }

                        return o;
                    })
                    // convert Stream to JsonArray
                    .collect(Collectors.reducing(new JsonArray(), a -> {
                        JsonArray array = new JsonArray();
                        array.add(a);
                        return array;
                    }, (a, b) -> {
                        a.addAll(b);
                        return a;
                    }));

            try (PrintWriter w = new PrintWriter(new FileWriter(file + ".json"))) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, w);
            }
            System.out.println("Response count: " + json.size() + " from " + counter[0] + " CAN packets.");
            System.out.println("Filtered for PGNS:");
            for (int pgn : pgns) {
                System.out.format("%8d %04X%n", pgn, pgn);
            }
        }
    }
}

/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.simulated;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class ScriptedEngine implements AutoCloseable {

    public class DM7Provider implements Supplier<Packet>, Predicate<Packet> {

        final private JsonArray array;
        private Packet next;
        final private JsonObject obj;
        private int sequence = 0;

        final private int spn;

        public DM7Provider(int spn, JsonObject obj) {
            this.obj = obj;
            this.spn = spn;
            // now let's find the right packet
            array = obj.get("packets").getAsJsonArray();
            // It's easy to leave a stray "," and get a null, so just remove them for our
            // users.
            while (array.remove(JsonNull.INSTANCE)) {
            }
        }

        @Override
        synchronized public Packet get() {
            return next;
        }

        @Override
        public boolean test(Packet packet) {
            if (packet.getId() != 0xE300) {
                return false;
            }
            // FIXME why?
            if (packet.get(0) != 247) {
                return false;
            }
            // FIXME why?
            if ((packet.get(3) & 0x1F) != 31) {
                return false;
            }
            int reqSpn = dm7Spn(packet);
            if (spn != reqSpn) {
                return false;
            }

            for (int i = 0; i < array.size(); i++) {
                JsonObject descriptor = array.get(sequence).getAsJsonObject();
                Packet p = Packet.parse(descriptor.get("packet").getAsString());
                sequence++;
                if (sequence >= array.size()) {
                    sequence = 0;
                }
                if (dm7Spn(p) != spn) {
                    continue;
                }
                if (envHandler(descriptor)) {
                    next = p;
                }
            }
            if (next == null) {
                throw new IllegalStateException(
                        "env not compatible with DM7 response:" + array + " env: " + env);
            }
            return true;
        }
    }

    public class ResponseProvider implements Supplier<Packet> {
        final private JsonObject obj;
        private int sequence = 0;

        public ResponseProvider(JsonObject o) {
            obj = o;
        }

        @Override
        synchronized public Packet get() {
            try {
                JsonArray array = obj.get("packets").getAsJsonArray();
                // It's easy to leave a stray "," and get a null, so just remove them for our
                // users.
                while (array.remove(JsonNull.INSTANCE)) {
                }
                for (int i = 0; i < array.size(); i++) {
                    JsonObject descriptor = array.get(sequence).getAsJsonObject();
                    Packet p = Packet.parse(descriptor.get("packet").getAsString());
                    sequence++;
                    if (sequence >= array.size()) {
                        sequence = 0;
                    }

                    if (envHandler(descriptor)) {
                        return p;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

    }

    private static int dm7Spn(Packet packet) {
        return (((packet.get(3) & 0xE0) << 11) & 0xFF0000) | ((packet.get(2) << 8) & 0xFF00)
                | (packet.get(1) & 0xFF);
    }

    public static void main(String... filename) throws Exception {
        try (Bus bus = new EchoBus(0xF9);
                FileInputStream in = new FileInputStream(filename[0]);
                ScriptedEngine e = new ScriptedEngine(bus, in);
                AutoCloseable log = bus.log(p -> p.getTimestamp().toString() + " : ");) {
            Thread.sleep(10 * 30 * 1000);
        }
    }

    private final Set<String> env = new HashSet<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Sim sim;

    public ScriptedEngine(Bus bus, InputStream in) throws BusException {
        sim = new Sim(bus);
        JsonArray a = new Gson().fromJson(new InputStreamReader(in), JsonArray.class);
        a.forEach(e -> {
            JsonObject o = e.getAsJsonObject();
            if (o.has("onRequest") && "dm7".equals(o.get("onRequest").getAsString())) {
                StreamSupport.stream(o.get("packets").getAsJsonArray().spliterator(), false)
                        .map(element -> element.getAsJsonObject())
                        .map(packetO -> Packet.parse(packetO.get("packet").getAsString()))
                        .map(packet -> (((packet.get(3) & 0xE0) << 11) & 0xFF0000) | ((packet.get(2) << 8) & 0xFF00)
                                | (packet.get(1) & 0xFF))
                        // we only need one DM17Provider for each SPN
                        .distinct()
                        .map(spn -> new DM7Provider(spn, o))
                        .forEach(provider -> sim.response(provider, provider));
            } else if (o.has("onRequest") && o.get("onRequest").getAsBoolean()) {
                JsonObject firstPacket = o.get("packets").getAsJsonArray().get(0).getAsJsonObject();
                Packet packet = Packet.parse(firstPacket.get("packet").getAsString());
                sim.response(isRequestForPredicate(packet), new ResponseProvider(o));
            } else {
                int period = o.get("period").getAsInt();
                if (period <= 0) {
                    System.err.println("FAIL:" + o.get("response").getAsString());
                    return;
                }
                ResponseProvider responseProvider = new ResponseProvider(o);
                sim.schedule(period,
                        period,
                        TimeUnit.MILLISECONDS,
                        () -> sim.sendNow(responseProvider.get()));
            }
        });
    }

    @Override
    public void close() {
        sim.close();
    }

    private boolean envHandler(JsonObject descriptor) {
        if (descriptor.has("isSet")
                && !env.contains(descriptor.get("isSet").getAsString())) {
            return false;
        }
        if (descriptor.has("isClear")
                && env.contains(descriptor.get("isClear").getAsString())) {
            return false;
        }
        if (descriptor.has("set")) {
            String symbol = descriptor.get("set").getAsString();
            env.add(symbol);
            if (descriptor.has("setFor")) {
                executor.schedule(() -> env.remove(symbol),
                        descriptor.get("setFor").getAsLong(),
                        TimeUnit.MILLISECONDS);
            }
        }
        if (descriptor.has("clear")) {
            env.remove(descriptor.get("clear").getAsString());
        }
        return true;
    }

    private Integer getPgn(Packet p) {
        int id = p.getId() & 0xFFFF;
        if (id < 0xF000) {
            id &= 0xFF00;
        }
        return id;
    }

    private Predicate<Packet> isRequestForPredicate(Packet responsePacket) {
        int pgn = getPgn(responsePacket);
        int address = responsePacket.getSource();
        return packet -> (packet.getId() == (0xEA00 | address) || packet.getId() == 0xEAFF)
                && packet.get24(0) == pgn;
    }

}

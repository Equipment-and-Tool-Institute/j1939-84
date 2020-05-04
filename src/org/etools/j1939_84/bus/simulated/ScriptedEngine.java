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

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class ScriptedEngine implements AutoCloseable {

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

                    if (descriptor.has("isSet")
                            && !env.contains(descriptor.get("isSet").getAsString())) {
                        continue;
                    }
                    if (descriptor.has("isClear")
                            && env.contains(descriptor.get("isClear").getAsString())) {
                        continue;
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

                    return p;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

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
            if (o.has("onRequest") && o.get("onRequest").getAsBoolean()) {
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

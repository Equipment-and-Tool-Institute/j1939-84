/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.simulated;

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
import org.etools.j1939_84.bus.Packet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class ScriptedEngine implements AutoCloseable {
    /** Handle responding to DM7 requests. */
    private class DM7Provider implements Supplier<Packet>, Predicate<Packet> {
        /**
         * Packet identified in last positive test. This avoids testing twice, but makes
         * assumptions about the process. It is fragile.
         */
        private Packet next;

        /** Possible responses for this request. */
        final private JsonArray packetDescriptors;

        /** source address */
        final private int sa;

        /** Next sequence to use in possible responses. */
        private int sequence = 0;

        final private int spn;

        DM7Provider(int spn, int sa, JsonObject messageDescriptot) {
            this.spn = spn;
            this.sa = sa;
            // now let's find the right packet
            packetDescriptors = messageDescriptot.get("packets").getAsJsonArray();
            // It's easy to leave a stray "," and get a null, so just remove them for our
            // users.
            while (packetDescriptors.remove(JsonNull.INSTANCE)) {
            }
        }

        @Override
        public Packet get() {
            return next;
        }

        @Override
        synchronized public boolean test(Packet packet) {
            // SPN DM7?
            if (packet.getId() != (0xE300 | sa)) {
                return false;
            }
            // J1939-84 6.1.12.1 TID 247
            if (packet.get(0) != 247) {
                return false;
            }
            // J1939-84 6.1.12.1 FMI 31
            if ((packet.get(3) & 0x1F) != 31) {
                return false;
            }
            // requested SPN the configured SPN?
            if (spn != dm7Spn(packet)) {
                return false;
            }

            for (int i = 0; i < packetDescriptors.size(); i++) {
                JsonObject descriptor = packetDescriptors.get(sequence).getAsJsonObject();
                Packet p = Packet.parse(descriptor.get("packet").getAsString());
                sequence++;
                if (sequence >= packetDescriptors.size()) {
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
                        "env not compatible with DM7 response:" + packetDescriptors + " env: " + env);
            }
            return true;
        }
    }

    /** Handle EA00 requests. */
    class ResponseProvider implements Supplier<Packet> {
        final private JsonObject messageDescriptor;
        /** Current sequence in the array of packet descriptors. */
        private int sequence = 0;

        private ResponseProvider(JsonObject messageDescriptor) {
            this.messageDescriptor = messageDescriptor;
        }

        @Override
        synchronized public Packet get() {
            try {
                JsonArray array = messageDescriptor.get("packets").getAsJsonArray();
                // It's easy to leave a stray "," and get a null, so just remove them for our
                // users.
                while (array.remove(JsonNull.INSTANCE)) {
                }
                for (int i = 0; i < array.size(); i++) {
                    JsonObject descriptor = array.get(sequence).getAsJsonObject();
                    Packet packet = Packet.parse(descriptor.get("packet").getAsString());
                    sequence++;
                    if (sequence >= array.size()) {
                        sequence = 0;
                    }

                    if (envHandler(descriptor)) {
                        return packet;
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

    private static int getPgn(Packet p) {
        int id = p.getId() & 0xFFFF;
        if (id < 0xF000) {
            id &= 0xFF00;
        }
        return id;
    }

    private static Predicate<Packet> isRequestForPredicate(Packet responsePacket) {
        int pgn = getPgn(responsePacket);
        int address = responsePacket.getSource();
        return packet -> (packet.getId() == (0xEA00 | address) || packet.getId() == 0xEAFF)
                && packet.get24(0) == pgn;
    }

    private final Set<String> env = new HashSet<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Sim sim;

    public ScriptedEngine(Bus bus, InputStream in) throws BusException {
        sim = new Sim(bus);
        JsonArray a = new Gson().fromJson(new InputStreamReader(in), JsonArray.class);
        a.forEach(e -> {
            JsonObject o = e.getAsJsonObject();
            JsonArray array = o.get("packets").getAsJsonArray();
            if (o.has("onRequest") && "dm7".equals(o.get("onRequest").getAsString())) {
                int sa = Packet.parse(array.get(0).getAsJsonObject().get("packet").getAsString()).getSource();
                // register a response in the simulator for each DM7 SPN request
                StreamSupport.stream(array.spliterator(), false)
                        .map(element -> Packet.parse(element.getAsJsonObject().get("packet").getAsString()))
                        .map(ScriptedEngine::dm7Spn)
                        // we only need one DM17Provider for each SPN
                        .distinct()
                        .map(spn -> new DM7Provider(spn, sa, o))
                        .forEach(provider -> sim.response(provider, provider));
            } else if (o.has("onRequest") && o.get("onRequest").getAsBoolean()) {
                // register a response in the simulator for each EA00 request
                JsonObject firstPacket = array.get(0).getAsJsonObject();
                Packet packet = Packet.parse(firstPacket.get("packet").getAsString());
                sim.response(isRequestForPredicate(packet), new ResponseProvider(o));
            } else {
                // register a periodic broadcast
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

    /**
     * Handle checking and updating the environment.
     *
     * @param descriptor The possible response to be checked. If it passes, then the
     *                   environment will be updated based on the set, setFor and
     *                   clear commands.
     * @return Should this descriptor be used?
     */
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
}
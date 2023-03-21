package org.etools.j1939tools.bus;

import java.util.concurrent.atomic.AtomicBoolean;

import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;

/**
 * Helper to ping for DM5 to alert other tools that service tool is still connected.
 *
 */
public class DM5Heartbeat {
    static public AutoCloseable run(J1939 j1939, ResultsListener listener) {
        AtomicBoolean running = new AtomicBoolean(true);
        new Thread(() -> {
            try {
                Thread.sleep(10_000);
                while (running.get()) {
                    j1939.requestGlobal("DM5 Heartbeat", DM5DiagnosticReadinessPacket.class, listener);
                    Thread.sleep(10_000);
                }
            } catch (InterruptedException e) {
                System.err.println("DM5 Heartbeat interrupted.");
            }
        }, "DM5Ping").start();
        return () -> running.set(false);
    }
}

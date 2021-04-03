/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private static final EventBus instance = new EventBus();

    public static EventBus getInstance() {
        return instance;
    }

    private final List<EventListener> listeners = new ArrayList<>();

    public EventBus() {
    }

    public void publish(Event event) {
        listeners.forEach(l -> l.onEvent(event));
    }

    public void registerListener(EventListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(EventListener listener) {
        listeners.remove(listener);
    }

}

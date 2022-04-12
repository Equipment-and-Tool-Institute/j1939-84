package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.j1939.model.Spn;

/** Value used when SPNs map to a single Value. */
final class SpnValue implements Value {
    private final Spn spn;

    protected SpnValue(Spn spn) {
        this.spn = spn;
    }

    @Override
    public String getLabel() {
        return spn.getLabel();
    }

    @Override
    public String getValue() {
        return spn.getStringValueNoUnit();
    }

    @Override
    public String getUnit() {
        return spn.getSlot().getUnit();
    }
}

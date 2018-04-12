package se.kth.id2203.multipaxos.events;

import se.sics.kompics.KompicsEvent;

public class AscDecide implements KompicsEvent {
    private Object v;

    public AscDecide(Object value) {
        this.v = value;
    }

    public Object getValue() {
        return v;
    }

}
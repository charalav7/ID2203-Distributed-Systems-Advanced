package se.kth.id2203.multipaxos.events;

import se.sics.kompics.KompicsEvent;

public class AscAbort implements KompicsEvent {
    private Object v;

    public AscAbort(Object value) {
        this.v = value;
    }

    public Object getValue() {
        return v;
    }
}
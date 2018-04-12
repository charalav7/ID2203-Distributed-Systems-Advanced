package se.kth.id2203.multipaxos.events;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class Prepare implements KompicsEvent, Serializable {
    protected final int ts;
    protected final int l;
    protected final int t;

    public Prepare(int ts, int l, int t) {
        this.ts = ts;
        this.l = l;
        this.t = t;
    }

    public int getTs() {
        return ts;
    }

    public int getLength() {
        return l;
    }

    public int getT() {
        return t;
    }
}
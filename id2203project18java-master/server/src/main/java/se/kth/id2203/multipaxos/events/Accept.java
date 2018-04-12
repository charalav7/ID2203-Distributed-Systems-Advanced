package se.kth.id2203.multipaxos.events;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.ArrayList;

public class Accept implements KompicsEvent, Serializable {
    protected final int ts;
    protected final int offs;
    protected final int t;
    protected final ArrayList<Object> s;

    public Accept(int ts, ArrayList<Object> s, int offs, int t) {
        this.ts = ts;
        this.s = s;
        this.offs = offs;
        this.t = t;
    }

    public int getTs() {
        return ts;
    }

    public ArrayList<Object> getVsuf() {
        return s;
    }

    public int getOffs() {
        return offs;
    }

    public int getT() {
        return t;
    }
}
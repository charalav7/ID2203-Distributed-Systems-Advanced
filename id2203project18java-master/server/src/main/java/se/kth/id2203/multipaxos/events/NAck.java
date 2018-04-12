package se.kth.id2203.multipaxos.events;


import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class NAck implements KompicsEvent, Serializable {
    protected final int pts;
    protected final int t;

    public NAck(int pts, int t) {
        this.pts = pts;
        this.t = t;
    }

    public int getPts() {
        return pts;
    }

    public int getT() {
        return t;
    }
}
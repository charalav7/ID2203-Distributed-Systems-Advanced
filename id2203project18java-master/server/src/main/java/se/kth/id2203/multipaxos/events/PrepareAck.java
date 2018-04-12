package se.kth.id2203.multipaxos.events;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.ArrayList;

public class PrepareAck implements KompicsEvent, Serializable {
    protected final int pts;
    protected final int ts;
    protected final ArrayList<Object> vSuf;
    protected final int l;
    protected final int t;

    public PrepareAck(int pts, int ts, ArrayList<Object> vSuf, int l, int t) {

        this.pts = pts;
        this.ts = ts;
        this.vSuf = vSuf;
        this.l = l;
        this.t = t;
    }

    public int getPts() {
        return pts;
    }

    public int getTs() {
        return ts;
    }

    public ArrayList<Object> getVsuf() {
        return vSuf;
    }

    public int getL() {
        return l;
    }

    public int getT() {
        return t;
    }
}
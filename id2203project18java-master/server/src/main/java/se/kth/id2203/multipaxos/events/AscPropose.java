package se.kth.id2203.multipaxos.events;

import se.sics.kompics.KompicsEvent;

public class AscPropose implements KompicsEvent {
    private Object p; //proposal

    public AscPropose(Object proposal) {
        this.p = proposal;
    }

    public Object getProposal() {
        return p;
    }
}
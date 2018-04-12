package se.kth.id2203.multipaxos.port;

import se.kth.id2203.multipaxos.events.AscPropose;
import se.kth.id2203.multipaxos.events.AscDecide;
import se.kth.id2203.multipaxos.events.AscAbort;
import se.sics.kompics.PortType;

public class PaxosPort extends PortType {
    {
        request(AscPropose.class);
        indication(AscDecide.class);
        indication(AscAbort.class);
    }
}

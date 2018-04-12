package se.kth.id2203.meld.port;

import se.kth.id2203.meld.events.MeldLeaderEvent;
import se.sics.kompics.PortType;

public class MeldPort extends PortType {
    {
        indication(MeldLeaderEvent.class); //the port indicates a new leader detection event
    }
}

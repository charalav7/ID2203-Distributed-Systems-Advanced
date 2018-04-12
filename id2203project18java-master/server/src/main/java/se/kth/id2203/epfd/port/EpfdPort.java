package se.kth.id2203.epfd.port;

import se.kth.id2203.epfd.events.EpfdRestoreEvent;
import se.kth.id2203.epfd.events.EpfdSuspectEvent;
import se.sics.kompics.PortType;

public class EpfdPort extends PortType {
    {
        indication(EpfdRestoreEvent.class); //the port indicates a restore event for a node
        indication(EpfdSuspectEvent.class); //the port indicates a suspect event for a node
    }
}

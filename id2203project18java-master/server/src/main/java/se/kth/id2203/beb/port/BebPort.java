package se.kth.id2203.beb.port;

import se.kth.id2203.beb.events.BebBroadcastEvent;
import se.kth.id2203.beb.events.BebDeliverEvent;
import se.sics.kompics.PortType;

public class BebPort extends PortType{
    {
        request(BebBroadcastEvent.class); //the port requires a broadcast event
        indication(BebDeliverEvent.class); //the port indicates a delivery event
    }
}
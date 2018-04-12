package se.kth.id2203.beb.events;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;
import java.util.Collection;

public class BebBroadcastEvent implements KompicsEvent{
    private final BebDeliverEvent deliverEvent;
    private final Collection<NetAddress> netAddresses;

    /**
     * BebBroadcastEvent when an event needs to be broadcasted through perfect links
     * @param deliverEvent The event to be broadcasted
     * @param netAddresses The array list of nodes to be broadcasted
     */
    public BebBroadcastEvent(BebDeliverEvent deliverEvent, Collection<NetAddress> netAddresses) {
        this.deliverEvent = deliverEvent;
        this.netAddresses = netAddresses;
    }

    public BebDeliverEvent getDeliverEvent() {
        return deliverEvent;
    }

    public Collection<NetAddress> getNetAddresses() {
        return netAddresses;
    }
}

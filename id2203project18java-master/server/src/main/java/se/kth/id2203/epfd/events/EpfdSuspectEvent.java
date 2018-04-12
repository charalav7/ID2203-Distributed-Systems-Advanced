package se.kth.id2203.epfd.events;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

public class EpfdSuspectEvent implements KompicsEvent {
    private NetAddress netAddress;

    /**
     * The suspect event
     * @param netAddress The address of the corresponding node
     */
    public EpfdSuspectEvent(NetAddress netAddress){
        this.netAddress = netAddress;
    }

    public NetAddress getNetAddress() {
        return netAddress;
    }
}


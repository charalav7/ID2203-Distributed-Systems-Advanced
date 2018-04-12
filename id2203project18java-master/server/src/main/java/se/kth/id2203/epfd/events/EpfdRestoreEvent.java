package se.kth.id2203.epfd.events;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

public class EpfdRestoreEvent implements KompicsEvent {
    private NetAddress netAddress;

    /**
     * The restore event for the epfd
     * @param netAddress The address of the corresponding node
     */
    public EpfdRestoreEvent(NetAddress netAddress){
        this.netAddress = netAddress;
    }

    public NetAddress getNetAddress() {
        return netAddress;
    }
}

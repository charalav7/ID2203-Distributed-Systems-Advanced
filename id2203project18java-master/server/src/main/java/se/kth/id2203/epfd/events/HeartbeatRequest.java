package se.kth.id2203.epfd.events;

import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.networking.NetMessage;
import se.sics.kompics.network.Transport;


public class HeartbeatRequest extends NetMessage{
    private final int seq;

    /**
     * The heartbeat request that is send from the network port
     * @param src The source address
     * @param dst The destination address
     * @param protocol The corresponding network protocol
     * @param seq The seqnum
     */
    public HeartbeatRequest(NetAddress src, NetAddress dst, Transport protocol, int seq) {
        super(src, dst, protocol);
        this.seq = seq;
    }

    public int getSeqnum() {
        return seq;
    }
}

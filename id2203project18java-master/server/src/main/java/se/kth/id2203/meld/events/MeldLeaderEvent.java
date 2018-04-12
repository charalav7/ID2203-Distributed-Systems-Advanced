package se.kth.id2203.meld.events;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

public class MeldLeaderEvent  implements KompicsEvent {
    public final NetAddress leaderAddress;

    /**
     * The leader election event
     * @param leaderAddress The specific address to be the corresponding leader
     */
    public MeldLeaderEvent(NetAddress leaderAddress) {
        this.leaderAddress = leaderAddress;
    }

    public NetAddress getLeaderAddress() {
        return leaderAddress;
    }
}

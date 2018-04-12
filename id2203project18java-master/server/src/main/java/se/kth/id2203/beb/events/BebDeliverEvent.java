package se.kth.id2203.beb.events;

import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.RouteMsg;
import se.sics.kompics.KompicsEvent;

public class BebDeliverEvent implements KompicsEvent {
    private NetAddress fromAddress;
    private RouteMsg routeMsg;

    /**
     * BebDeliverEvent when new deliver event arrives at BebPort
     * @param fromAddress The source address of the new event
     * @param routeMsg The new routeMsg kompics event
     */
    public BebDeliverEvent(NetAddress fromAddress, RouteMsg routeMsg){
        this.fromAddress = fromAddress;
        this.routeMsg = routeMsg;
    }

    public NetAddress getFromAddress() {
        return fromAddress;
    }

    public RouteMsg getRouteMsg() {
        return routeMsg;
    }

}

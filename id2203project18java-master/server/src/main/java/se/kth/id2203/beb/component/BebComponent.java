package se.kth.id2203.beb.component;

import se.kth.id2203.beb.events.BebBroadcastEvent;
import se.kth.id2203.beb.events.BebDeliverEvent;
import se.kth.id2203.beb.port.BebPort;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.ArrayList;
import java.util.Collection;

public class BebComponent extends ComponentDefinition{

    //define the ports
    private Negative<BebPort> bebPortNegative = provides(BebPort.class);
    private Positive<Network> networkPositive = requires(Network.class);

    //define self address
    private final NetAddress selfAddress = config().getValue("id2203.project.address", NetAddress.class);

    //define broadcast event handler
    private Handler<BebBroadcastEvent> broadcastEventHandler = new Handler<BebBroadcastEvent>() {
        @Override
        public void handle(BebBroadcastEvent broadcastEvent) {
            Collection<NetAddress> netAddresses = broadcastEvent.getNetAddresses();
            for (NetAddress netAddress: netAddresses){
                logger.info("BroadcastEvent -> From: " + selfAddress + ", To: " + netAddress);
                trigger(new Message(selfAddress, netAddress, broadcastEvent.getDeliverEvent()), networkPositive);
            }
        }
    };

    //define perfect link deliver event handler
    private ClassMatchedHandler<BebDeliverEvent, Message> plDeliverEventHandler = new ClassMatchedHandler<BebDeliverEvent, Message>() {
        @Override
        public void handle(BebDeliverEvent deliverEvent, Message message) {
            logger.info("PL_DeliverEvent -> To: " + selfAddress);
            trigger(deliverEvent, bebPortNegative);
        }
    };

    //subscribe event handlers to appropriate ports
    {
        subscribe(broadcastEventHandler, bebPortNegative);
        subscribe(plDeliverEventHandler, networkPositive);
    }

}
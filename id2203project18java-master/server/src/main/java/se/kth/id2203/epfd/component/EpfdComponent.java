package se.kth.id2203.epfd.component;

import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.epfd.events.*;
import se.kth.id2203.epfd.port.EpfdPort;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.ArrayList;
import java.util.Collection;

public class EpfdComponent extends ComponentDefinition {
    //define self address
    private final NetAddress selfAddress = config().getValue("id2203.project.address", NetAddress.class);

    private Collection<NetAddress> topology; //a specific partition topology of addresses
    private int delta = 100; //define the delay when we want to increase the detecting period
    private int period = 1200; //define the timeout period
    private ArrayList<NetAddress> alive = new ArrayList<>(); //the alive addresses; empty at the beginning
    private ArrayList<NetAddress> suspected = new ArrayList<>(); //the suspected addresses; empty at the beginning
    private int seqnum; //the increasing sequence number

    //define the ports
    private Negative<EpfdPort> epfdPortNegative = provides(EpfdPort.class);
    private Positive<Network> networkPositive = requires(Network.class);
    private Positive<Timer> timerPositive = requires(Timer.class);
    private Positive<Bootstrapping> bootstrappingPositive = requires(Bootstrapping.class);

    //CheckTimeout event to handle later
    private static class CheckTimeout extends Timeout{

        CheckTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    //startTimer and trigger CheckTimeout event in timerPositive port
    private void startTimer(int delay){
        ScheduleTimeout scheduleTimeout = new ScheduleTimeout(delay);
        scheduleTimeout.setTimeoutEvent(new CheckTimeout(scheduleTimeout));
        trigger(scheduleTimeout, timerPositive);
    }

    //epfd event handlers

    //Start event handler
    private Handler<Booted> epfdStartEventHandler = new Handler<Booted>() {
        @Override
        public void handle(Booted booted) {
            seqnum = 0; //initially 0
            LookupTable lookupTable = (LookupTable) booted.assignment;

            //get the partition for the corresponding self address
            for (int key: lookupTable.getAllKeys()){
                if (lookupTable.lookup(Integer.toString(key)).contains(selfAddress)){
                    topology = lookupTable.lookup(Integer.toString(key));
                    break;
                }
            }
            alive.addAll(topology);
            startTimer(period);
        }
    };

    //CheckTimeout event handler
    private Handler<CheckTimeout> checkTimeoutHandler = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout checkTimeout) {
            ArrayList<NetAddress> intersectList = new ArrayList<>(alive);
            intersectList.retainAll(suspected);
            if (!intersectList.isEmpty()){
                period += delta;
            }
            seqnum += 1;
            for (NetAddress netAddress : topology){
                if (!alive.contains(netAddress) && !suspected.contains(netAddress)){
                    suspected.add(netAddress);
                    logger.info("Suspected address added: " + netAddress);
                    trigger(new EpfdSuspectEvent(netAddress), epfdPortNegative);
                } else if (alive.contains(netAddress) && suspected.contains(netAddress)){
                    suspected.remove(netAddress);
                    logger.info("Suspected address removed: " + netAddress);
                    trigger(new EpfdRestoreEvent(netAddress), epfdPortNegative);
                }
                trigger(new HeartbeatRequest(selfAddress, netAddress, Transport.TCP, seqnum), networkPositive);
            }
            alive.clear();
            startTimer(period);
        }
    };

    //HeartbeatRequest event handler
    private Handler<HeartbeatRequest> heartbeatRequestHandler = new Handler<HeartbeatRequest>() {
        @Override
        public void handle(HeartbeatRequest heartbeatRequest) {
            trigger(new HeartbeatReply(selfAddress, heartbeatRequest.getSource(), heartbeatRequest.getProtocol(), seqnum), networkPositive);
        }
    };

    //HeartbeatReply event handler
    private Handler<HeartbeatReply> heartbeatReplyHandler = new Handler<HeartbeatReply>() {
        @Override
        public void handle(HeartbeatReply heartbeatReply) {
            if (heartbeatReply.getSeqnum() == seqnum || suspected.contains(heartbeatReply.getSource())){
                alive.add(heartbeatReply.getSource());
            }
        }
    };

    //subscribe event handlers to appropriate ports
    {
        subscribe(epfdStartEventHandler, bootstrappingPositive);
        subscribe(checkTimeoutHandler, timerPositive);
        subscribe(heartbeatRequestHandler, networkPositive);
        subscribe(heartbeatReplyHandler, networkPositive);
    }
}

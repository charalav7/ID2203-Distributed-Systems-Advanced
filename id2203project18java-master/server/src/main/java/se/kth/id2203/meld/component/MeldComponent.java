package se.kth.id2203.meld.component;

import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.epfd.events.EpfdRestoreEvent;
import se.kth.id2203.epfd.events.EpfdSuspectEvent;
import se.kth.id2203.epfd.port.EpfdPort;
import se.kth.id2203.meld.events.MeldLeaderEvent;
import se.kth.id2203.meld.port.MeldPort;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

import java.util.ArrayList;
import java.util.Collection;

public class MeldComponent extends ComponentDefinition {
    //define self address
    private final NetAddress selfAddress = config().getValue("id2203.project.address", NetAddress.class);

    private Collection<NetAddress> topology; //a specific partition topology of addresses
    private ArrayList<NetAddress> suspected = new ArrayList<>(); //the suspected list; empty at the beginning
    private NetAddress leaderAddress = null; //the leader node; null at the beginning

    //define the ports
    private Negative<MeldPort> meldPortNegative = provides(MeldPort.class);
    private Positive<EpfdPort> epfdPortPositive = requires(EpfdPort.class);
    private Positive<Bootstrapping> bootstrappingPositive = requires(Bootstrapping.class);

    //meld event handlers

    //Start event handler
    private Handler<Booted> meldStartEventHandler = new Handler<Booted>() {
        @Override
        public void handle(Booted booted) {
            LookupTable lookupTable = (LookupTable) booted.assignment;

            //get the partition for the corresponding self address
            for (int key: lookupTable.getAllKeys()){
                if (lookupTable.lookup(Integer.toString(key)).contains(selfAddress)){
                    topology = lookupTable.lookup(Integer.toString(key));
                    break;
                }
            }

            leaderAddress = selfAddress;
            for (NetAddress netAddress : topology){
                if (!suspected.contains(netAddress)){
                    if (netAddress.compareTo(leaderAddress) < 0){
                        //let the address with the lowest port be the leader
                        leaderAddress = netAddress;
                    }
                }
            }

            trigger(new MeldLeaderEvent(leaderAddress), meldPortNegative);
        }
    };

    //Epfd suspect event handler
    private Handler<EpfdSuspectEvent> epfdSuspectEventHandler = new Handler<EpfdSuspectEvent>() {
        @Override
        public void handle(EpfdSuspectEvent epfdSuspectEvent) {
            suspected.add(epfdSuspectEvent.getNetAddress());
            //check if current leader is at the suspected list; if so change leader
            if (epfdSuspectEvent.getNetAddress().equals(leaderAddress)){
                leaderAddress = selfAddress;
                for (NetAddress netAddress : topology) {
                    if (!suspected.contains(netAddress)){
                        if (netAddress.compareTo(leaderAddress) < 0) {
                            leaderAddress = netAddress;
                        }
                    }
                }
                //new leader elected
                trigger(new MeldLeaderEvent(leaderAddress), meldPortNegative);
            }
        }
    };

    //Epfd restore event handler
    private Handler<EpfdRestoreEvent> epfdRestoreEventHandler = new Handler<EpfdRestoreEvent>() {
        @Override
        public void handle(EpfdRestoreEvent epfdRestoreEvent) {
            suspected.remove(epfdRestoreEvent.getNetAddress());
            //check if restored address in the topology has higher rank than the current leader; if so, change leader
            if (epfdRestoreEvent.getNetAddress().compareTo(leaderAddress) < 0){
                leaderAddress = epfdRestoreEvent.getNetAddress();
                trigger(new MeldLeaderEvent(leaderAddress), meldPortNegative);
            }
        }
    };

    //subscribe handlers to ports
    {
        subscribe(meldStartEventHandler, bootstrappingPositive);
        subscribe(epfdSuspectEventHandler, epfdPortPositive);
        subscribe(epfdRestoreEventHandler, epfdPortPositive);
    }

}

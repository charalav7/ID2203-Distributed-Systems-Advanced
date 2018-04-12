package se.kth.id2203.multipaxos.component;

import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.multipaxos.events.AscPropose;
import se.kth.id2203.multipaxos.events.AscDecide;
import se.kth.id2203.multipaxos.events.AscAbort;
import se.kth.id2203.multipaxos.events.Prepare;
import se.kth.id2203.multipaxos.events.PrepareAck;
import se.kth.id2203.multipaxos.events.Accept;
import se.kth.id2203.multipaxos.events.AcceptAck;
import se.kth.id2203.multipaxos.events.Decide;
import se.kth.id2203.multipaxos.events.NAck;
import se.kth.id2203.multipaxos.port.PaxosPort;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class multipaxos extends ComponentDefinition {

    // Define the ports
    private Negative<PaxosPort> asc = provides(PaxosPort.class);
    private Positive<Network> net = requires(Network.class);
    private Positive<Bootstrapping> boot = requires(Bootstrapping.class);

    // Get addr
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private Collection<NetAddress> partition = null;

    // Define variables
    private int t;                                  //Logical clock
    private int prepts;                             //Prepared timestamp

    public class Seq {
        protected int ts = 0;                                 // Timestamp
        protected int l = 0;                                  // Length
        protected ArrayList<Object> s = new ArrayList<>();    // Sequence
    }

    Seq acceptor = new Seq();
    Seq proposer = new Seq();

    // Read list information
    public class RLInfo {

        protected int ts;
        protected ArrayList<Object> suf;

        public RLInfo(int ts, ArrayList<Object> suf) {
            this.ts = ts;
            this.suf = suf;
        }
    }

    protected ArrayList<Object> proposedValues = new ArrayList<>();
    protected HashMap<NetAddress, RLInfo> readlist = new HashMap<>();
    protected HashMap<NetAddress, Integer> accepted = new HashMap<>();
    protected HashMap<NetAddress, Integer> decided = new HashMap<>();

    protected ArrayList<Object> prefix(ArrayList<Object> v, int l) {
        ArrayList<Object> vPrefix = new ArrayList<>();
        for(int i = 0; i < l; i++) {
            vPrefix.add(v.get(i));
        }
        return vPrefix;
    }

    protected ArrayList<Object> suffix(ArrayList<Object> v, int l) {
        ArrayList<Object> vSuffix = new ArrayList<>();
        for(int i = l; i < v.size(); i++) {
            vSuffix.add(v.get(i));
        }
        return vSuffix;
    }

    protected final Handler<Booted> bootedHandler = new Handler<Booted>() {

        private LookupTable tab;

        @Override
        public void handle(Booted booted) {
            tab = (LookupTable) booted.assignment;
            partition = tab.lookup(tab.getNodeKey(self));

        }
    };


    /** Handle proposals. Triggered by the leader of the group */
     protected final Handler<AscPropose> proposeHandler = new Handler<AscPropose>() {

        private Object proposal;

        public void handle(AscPropose ascPropose) {
            t ++;
            proposal = ascPropose.getProposal();

            //Initial proposal
            if(proposer.ts == 0) {
                proposer.ts = (t * partition.size()) + self.getPort(); // The rank is provided by the port of the TCP session
                proposer.s = prefix(acceptor.s, acceptor.l);
                proposer.l = 0; // Set the length of the proposer to 0


                proposedValues.add(proposal);
                readlist.clear();
                accepted = new HashMap<>();
                decided = new HashMap<>();

                for(NetAddress node : partition) {
                    // Send Prepare Message to all nodes of the group
                    trigger(new Message(self, node, new Prepare(proposer.ts, proposer.l, t)), net);
                }
            }
            //If we are not in the initial case and if the majority is not met
            else if(readlist.size() <= partition.size()/2) {
                proposedValues.add(proposal);
            }
            //We have the majority and the proposal have not been suggested before
            else if(!proposer.s.contains(proposal)) {
                proposer.s.add(proposal);
                for(NetAddress node : partition) {


                    if(readlist.containsKey(node)) {
                        ArrayList<Object> prop = new ArrayList<>();
                        prop.add(proposal);
                        trigger(new Message(self, node, new Accept(proposer.ts, prop, proposer.s.size() - 1, t)), net);
                    }


                }
            }

        }
     };


     protected final ClassMatchedHandler<Prepare, Message> prepareHandler = new ClassMatchedHandler<Prepare, Message>() {
        @Override
        public void handle(Prepare content, Message m) {

            //We take the max Timestamp and add 1 (logical clock)
            t = Math.max(t, content.getT()) + 1;

            if(content.getTs() < prepts) {
                trigger(new Message(self, m.getSource(), new NAck(content.getTs(), t)), net);
            } else {
                prepts = content.getTs();
                trigger(new Message(self, m.getSource(), new PrepareAck(content.getTs(), acceptor.ts, suffix(acceptor.s, content.getLength()), acceptor.l, t)), net);
            }
        }
    };


    protected final ClassMatchedHandler<PrepareAck, Message> prepareAckHandler = new ClassMatchedHandler<PrepareAck, Message>() {

        @Override
        public void handle(PrepareAck content, Message m) {


            t = Math.max(t, content.getT()) + 1;

            if(content.getPts() == proposer.ts) {

                readlist.put(m.getSource(), new RLInfo(content.getTs(), content.getVsuf()));
                decided.put(m.getSource(), content.getL());
                if(readlist.size() == partition.size()/2 + 1) {

                    RLInfo p1 = new RLInfo(0, new ArrayList<>());

                    for(RLInfo p2 : readlist.values()) {
                        if(p1.ts < p2.ts || (p1.ts == p2.ts && p1.suf.size() < p2.suf.size())) {
                            p1.ts = p2.ts;
                            p1.suf = p2.suf;
                        }
                    }


                    proposer.s.addAll(content.getVsuf());


                    for(Object v : proposedValues) {
                        if(!proposer.s.contains(v)) {
                            proposer.s.add(v);
                        }
                    }


                    for(NetAddress p : partition) {
                        if(readlist.containsKey(p)) {
                            int l1 = decided.get(p);
                            trigger(new Message(self, p, new Accept(proposer.ts, suffix(proposer.s, l1), l1, t)), net);
                        }
                    }


                } else if(readlist.size() > partition.size()/2 + 1) {
                    trigger(new Message(self, m.getSource(), new Accept(proposer.ts, suffix(proposer.s, content.getL()), content.getL(), t)), net);
                    if(proposer.l != 0) {
                        trigger(new Message(self, m.getSource(), new Decide(proposer.ts, proposer.l, t)), net);
                    }
                }
            }
        }
    };

    // ACCEPT PART OF THE ALGO - See the report
    protected final ClassMatchedHandler<Accept, Message> acceptHandler = new ClassMatchedHandler<Accept, Message>() {

        @Override
        public void handle(Accept content, Message m) {


            t = Math.max(t, content.getT()) + 1;


            if(content.getTs() != prepts) {
                trigger(new Message(self, m.getSource(), new NAck(content.getTs(), t)), net);
            } else {
                if(content.getOffs() < acceptor.s.size()) {
                    acceptor.s = prefix(acceptor.s, content.getOffs());
                }
                acceptor.s.addAll(content.getVsuf());
                trigger(new Message(self, m.getSource(), new AcceptAck(content.getTs(), acceptor.s.size(), t)), net);
            }
        }
    };
    protected final ClassMatchedHandler<AcceptAck, Message> acceptAckHandler = new ClassMatchedHandler<AcceptAck, Message>() {

        @Override
        public void handle(AcceptAck content, Message m) {


            t = Math.max(t, content.getT()) + 1;


            if(content.getPts() == proposer.ts) {
                accepted.put(m.getSource(), content.getL());
                int num_greater = 0;

                for(NetAddress p : partition) {
                    if(accepted.get(p) != null && accepted.get(p) >= content.getL()) {
                        num_greater++;
                    }
                }
                if(proposer.l < content.getL() && num_greater > partition.size() / 2) {
                    proposer.l = content.getL();
                    for(NetAddress p : partition) {
                        if(readlist.containsKey(p)) {
                            trigger(new Message(self, p, new Decide(proposer.ts, proposer.l, t)), net);
                        }
                    }
                }
            }
        }
    };

    //If something went wrong
    protected final ClassMatchedHandler<NAck, Message> nackHandler = new ClassMatchedHandler<NAck, Message>() {

        @Override
        public void handle(NAck content, Message m) {


            t = Math.max(t, content.getT()) + 1;


            if(content.getPts() == proposer.ts) {
                proposer.ts = 0;
                trigger(new AscAbort(proposer.s), asc);
            }
        }
    };


    protected final ClassMatchedHandler<Decide, Message> decideHandler = new ClassMatchedHandler<Decide, Message>() {

        @Override
        public void handle(Decide content, Message m) {


            t = Math.max(t, content.getT()) + 1;


            if(content.getTs() == prepts) {
                while(acceptor.l < content.getL()) {
                    trigger(new AscDecide(acceptor.s.get(acceptor.l)), asc);
                    acceptor.l = acceptor.l + 1;
                }
            }
        }
    };

    {
        subscribe(bootedHandler, boot);
        subscribe(proposeHandler, asc);
        subscribe(prepareHandler, net);
        subscribe(prepareAckHandler, net);
        subscribe(acceptHandler, net);
        subscribe(acceptAckHandler, net);
        subscribe(nackHandler, net);
        subscribe(decideHandler, net);
    }
}

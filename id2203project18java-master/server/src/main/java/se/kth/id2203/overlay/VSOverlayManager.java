/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.overlay;

import com.larskroll.common.J6;
import java.util.Collection;

import se.kth.id2203.beb.events.BebBroadcastEvent;
import se.kth.id2203.beb.events.BebDeliverEvent;
import se.kth.id2203.beb.port.BebPort;
import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.bootstrapping.GetInitialAssignments;
import se.kth.id2203.bootstrapping.InitialAssignments;
import se.kth.id2203.meld.events.MeldLeaderEvent;
import se.kth.id2203.meld.port.MeldPort;
import se.kth.id2203.multipaxos.port.PaxosPort;
import se.kth.id2203.multipaxos.events.AscPropose;
import se.kth.id2203.multipaxos.events.AscAbort;
import se.kth.id2203.multipaxos.events.AscDecide;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * The V(ery)S(imple)OverlayManager.
 * <p>
 * Keeps all nodes in a single partition in one replication group.
 * <p>
 * Note: This implementation does not fulfill the project task. You have to
 * support multiple partitions!
 * <p>
 * @author Lars Kroll <lkroll@kth.se>
 */
public class VSOverlayManager extends ComponentDefinition {

    //******* Ports ******
    protected final Negative<Routing> route = provides(Routing.class);
    protected final Positive<Bootstrapping> boot = requires(Bootstrapping.class);
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<BebPort> bebPortPositive = requires(BebPort.class); //added BebPort
    protected final Positive<MeldPort> meldPortPositive = requires(MeldPort.class); //added MeldPort
    protected final Positive<PaxosPort> paxosPortPositive = requires(PaxosPort.class); //added PaxosPort
    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private LookupTable lut = null;
    private NetAddress leaderNetAddress = null;
    //******* Handlers ******
    protected final Handler<GetInitialAssignments> initialAssignmentHandler = new Handler<GetInitialAssignments>() {

        @Override
        public void handle(GetInitialAssignments event) {
            logger.info("Generating LookupTable...");
            LookupTable lut = LookupTable.generate(event.nodes);
            logger.debug("Generated assignments:\n{}", lut);
            trigger(new InitialAssignments(lut), boot);
        }
    };
    protected final Handler<Booted> bootHandler = new Handler<Booted>() {

        @Override
        public void handle(Booted event) {
            if (event.assignment instanceof LookupTable) {
                logger.info("Got NodeAssignment, overlay ready.");
                lut = (LookupTable) event.assignment;
            } else {
                logger.error("Got invalid NodeAssignment type. Expected: LookupTable; Got: {}", event.assignment.getClass());
            }
        }
    };
    //change this handler
    protected final ClassMatchedHandler<RouteMsg, Message> routeHandler = new ClassMatchedHandler<RouteMsg, Message>() {

        @Override
        public void handle(RouteMsg content, Message context) {
            /*Collection<NetAddress> partition = lut.lookup(content.key);
            NetAddress target = J6.randomElement(partition);
            logger.info("Forwarding message for key {} to {}", content.key, target);
            trigger(new Message(context.getSource(), target, content.msg), net);*/
            Collection<NetAddress> partition = lut.lookup(content.key);
            //check if self in the partition
            if (partition.contains(self)){
                //check if self is actually the leader of the partition
                if (self.equals(leaderNetAddress)) {
                    logger.info("<RouteMsg, Message> Message delivered by leader of partition");
                    trigger(new AscPropose(content.msg), paxosPortPositive);
                } else {
                    logger.info("<RouteMsg, Message> Message to be forwarded to leader");
                    trigger(new Message(self, leaderNetAddress, content), net);
                }
            } else {
                logger.info("<RouteMsg, Message> Self not contained at partition. Relay message to correct partition group.");
                //trigger a new broadcast event
                trigger(new BebBroadcastEvent(new BebDeliverEvent(context.getSource(), content), partition), bebPortPositive);
            }
        }
    };
    protected final Handler<RouteMsg> localRouteHandler = new Handler<RouteMsg>() {

        @Override
        public void handle(RouteMsg event) {
            Collection<NetAddress> partition = lut.lookup(event.key);
            NetAddress target = J6.randomElement(partition);
            logger.info("Routing message for key {} to {}", event.key, target);
            trigger(new Message(self, target, event.msg), net);
        }
    };
    //added BebDeliver event
    protected final Handler <BebDeliverEvent> bebDeliverEventHandler = new Handler<BebDeliverEvent>() {
        @Override
        public void handle(BebDeliverEvent bebDeliverEvent) {
            //check if self is the leader
            if(self.equals(leaderNetAddress)) {
                logger.info("<BebDeliverEvent> Received by leader. Starting proposing.");
                trigger(new AscPropose(bebDeliverEvent.getRouteMsg().getMsg()), paxosPortPositive);
            } else {
                //the message needs to be forwarded to the leader
                logger.info("<BebDeliverEvent> Forwarding message to leader.");
                trigger(new Message(self, leaderNetAddress, bebDeliverEvent.getRouteMsg()), net);
            }

        }
    };
    //added MeldLeader event
    protected final Handler<MeldLeaderEvent> meldLeaderEventHandler = new Handler<MeldLeaderEvent>() {
        @Override
        public void handle(MeldLeaderEvent meldLeaderEvent) {
            //check if current leader address is the one of the leader election event; if not, assign new leader
            if(meldLeaderEvent.getLeaderAddress() != leaderNetAddress) {
                logger.info("<MeldLeaderEvent> New leader elected: " + meldLeaderEvent.getLeaderAddress());
                leaderNetAddress = meldLeaderEvent.getLeaderAddress();
            }
        }
    };
    //added AscDecide event
    protected final Handler<AscDecide> ascDecideHandler = new Handler<AscDecide>() {
        @Override
        public void handle(AscDecide ascDecide) {
            logger.info("<AscDecide> A decide event was received.");
            //trigger a new message to net port
            trigger(new Message(self, self, (KompicsEvent)ascDecide.getValue()), net);
        }
    };
    //added AscAbort event
    protected final Handler<AscAbort> ascAbortHandler = new Handler<AscAbort>() {
        @Override
        public void handle(AscAbort abort) {
            logger.info("<AscAbort> The operation was aborted.");
        }
    };
    protected final ClassMatchedHandler<Connect, Message> connectHandler = new ClassMatchedHandler<Connect, Message>() {

        @Override
        public void handle(Connect content, Message context) {
            if (lut != null) {
                logger.debug("Accepting connection request from {}", context.getSource());
                int size = lut.getNodes().size();
                trigger(new Message(self, context.getSource(), content.ack(size)), net);
            } else {
                logger.info("Rejecting connection request from {}, as system is not ready, yet.", context.getSource());
            }
        }
    };

    {
        subscribe(initialAssignmentHandler, boot);
        subscribe(bootHandler, boot);
        subscribe(routeHandler, net);
        subscribe(localRouteHandler, route);
        subscribe(connectHandler, net);
        subscribe(bebDeliverEventHandler, bebPortPositive);
        subscribe(meldLeaderEventHandler, meldPortPositive);
        subscribe(ascDecideHandler, paxosPortPositive);
        subscribe(ascAbortHandler, paxosPortPositive);
    }
}

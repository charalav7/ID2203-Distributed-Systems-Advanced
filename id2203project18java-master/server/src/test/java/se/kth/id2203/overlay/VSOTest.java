/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2203.overlay;

import com.google.common.collect.ImmutableSet;
import java.net.InetAddress;
import java.util.UUID;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.bootstrapping.GetInitialAssignments;
import se.kth.id2203.bootstrapping.InitialAssignments;
import se.kth.id2203.kvstore.Operation;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.Component;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class VSOTest {

    public VSOTest() {
        se.kth.id2203.Main.prepareOptions(); // just load the class so the converters are registered
    }

    private NetAddress a1 = new NetAddress(InetAddress.getLoopbackAddress(), 12345);
    private NetAddress a2 = new NetAddress(InetAddress.getLoopbackAddress(), 12346);
    private NetAddress a3 = new NetAddress(InetAddress.getLoopbackAddress(), 12347);

    private ImmutableSet<NetAddress> nodes = ImmutableSet.of(a1, a2, a3);
    private Operation op = new Operation("test", a1);

    @Test
    public void connectTest() {
        TestContext<VSOverlayManager> tc = TestContext.newInstance(VSOverlayManager.class);
        Component vsom = tc.getComponentUnderTest();
        Negative<Bootstrapping> boot = vsom.getNegative(Bootstrapping.class);
        Positive<Routing> route = vsom.getPositive(Routing.class);
        Negative<Network> net = vsom.getNegative(Network.class);
        GetInitialAssignments gia = new GetInitialAssignments(nodes);
        InitialAssignments ia = new InitialAssignments(LookupTable.generate(nodes));
        Connect cm = new Connect(UUID.randomUUID());
        Message cmm = new Message(a1, a2, cm);
        Connect.Ack ack = cm.ack(3);
        tc.body()
                .trigger(gia, boot)
                .expect(ia, boot, Direction.OUT)
                .trigger(new Booted(ia.assignment), boot)
                .trigger(cmm, net)
                .expect(Message.class, (Message msg) -> msg.payload.equals(ack), net, Direction.OUT);
        assertTrue(tc.check());
    }

    @Test
    public void routingTest() {
        TestContext<VSOverlayManager> tc = TestContext.newInstance(VSOverlayManager.class);
        Component vsom = tc.getComponentUnderTest();
        Negative<Bootstrapping> boot = vsom.getNegative(Bootstrapping.class);
        Positive<Routing> route = vsom.getPositive(Routing.class);
        Negative<Network> net = vsom.getNegative(Network.class);
        GetInitialAssignments gia = new GetInitialAssignments(nodes);
        InitialAssignments ia = new InitialAssignments(LookupTable.generate(nodes));
        RouteMsg rm = new RouteMsg(op.key, op);
        tc.body()
                .trigger(gia, boot)
                .expect(ia, boot, Direction.OUT)
                .trigger(new Booted(ia.assignment), boot)
                .trigger(rm, route)
                .expect(Message.class, (Message msg) -> msg.payload.equals(rm.msg), net, Direction.OUT);
        assertTrue(tc.check());
    }

    @Test
    public void assignmentTest() {
        TestContext<VSOverlayManager> tc = TestContext.newInstance(VSOverlayManager.class);
        Component vsom = tc.getComponentUnderTest();
        Negative<Bootstrapping> boot = vsom.getNegative(Bootstrapping.class);
        GetInitialAssignments gia = new GetInitialAssignments(nodes);
        InitialAssignments ia = new InitialAssignments(LookupTable.generate(nodes));
        tc.body()
                .trigger(gia, boot)
                .expect(ia, boot, Direction.OUT);
        assertTrue(tc.check());
    }
}

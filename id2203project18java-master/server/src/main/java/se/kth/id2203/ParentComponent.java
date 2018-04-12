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
package se.kth.id2203;

import com.google.common.base.Optional;
import se.kth.id2203.beb.component.BebComponent;
import se.kth.id2203.beb.port.BebPort;
import se.kth.id2203.bootstrapping.BootstrapClient;
import se.kth.id2203.bootstrapping.BootstrapServer;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.epfd.component.EpfdComponent;
import se.kth.id2203.epfd.port.EpfdPort;
import se.kth.id2203.kvstore.KVService;
import se.kth.id2203.meld.component.MeldComponent;
import se.kth.id2203.meld.port.MeldPort;
import se.kth.id2203.multipaxos.component.multipaxos;
import se.kth.id2203.multipaxos.port.PaxosPort;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.kth.id2203.overlay.VSOverlayManager;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class ParentComponent
        extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    //******* Children ******
    protected final Component overlay = create(VSOverlayManager.class, Init.NONE);
    protected final Component kv = create(KVService.class, Init.NONE);
    protected final Component boot;
    protected final Component bebComponent = create(BebComponent.class, Init.NONE); //added bebComponent
    protected final Component epfdComponent = create(EpfdComponent.class, Init.NONE); //added epfdComponent
    protected final Component meldComponent = create(MeldComponent.class, Init.NONE); //added meldComponent
    protected final Component paxosComponent = create(multipaxos.class, Init.NONE); //added paxosComponent

    {

        Optional<NetAddress> serverO = config().readValue("id2203.project.bootstrap-address", NetAddress.class);
        if (serverO.isPresent()) { // start in client mode
            boot = create(BootstrapClient.class, Init.NONE);
        } else { // start in server mode
            boot = create(BootstrapServer.class, Init.NONE);
        }
        connect(timer, boot.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, boot.getNegative(Network.class), Channel.TWO_WAY);
        // Overlay
        connect(boot.getPositive(Bootstrapping.class), overlay.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(net, overlay.getNegative(Network.class), Channel.TWO_WAY);
        // KV
        //connect(overlay.getPositive(Routing.class), kv.getNegative(Routing.class), Channel.TWO_WAY);
        connect(kv.getNegative(Network.class), net,  Channel.TWO_WAY);

        //.............ADD NEW CONNECTIONS............................

        // Beb
        connect(overlay.getNegative(BebPort.class), bebComponent.getPositive(BebPort.class), Channel.TWO_WAY);
        connect(bebComponent.getNegative(Network.class), net,  Channel.TWO_WAY);
        // Epfd
        connect(epfdComponent.getNegative(Bootstrapping.class), boot.getPositive(Bootstrapping.class),  Channel.TWO_WAY);
        connect(epfdComponent.getNegative(Timer.class), timer,  Channel.TWO_WAY);
        connect(epfdComponent.getNegative(Network.class), net,  Channel.TWO_WAY);
        // Meld
        connect(meldComponent.getNegative(Bootstrapping.class), boot.getPositive(Bootstrapping.class),  Channel.TWO_WAY);
        connect(overlay.getNegative(MeldPort.class), meldComponent.getPositive(MeldPort.class), Channel.TWO_WAY);
        connect(meldComponent.getNegative(EpfdPort.class), epfdComponent.getPositive(EpfdPort.class), Channel.TWO_WAY);
        // Paxos
        connect(paxosComponent.getNegative(Bootstrapping.class), boot.getPositive(Bootstrapping.class),  Channel.TWO_WAY);
        connect(overlay.getNegative(PaxosPort.class), paxosComponent.getPositive(PaxosPort.class), Channel.TWO_WAY);
        connect(paxosComponent.getNegative(Network.class), net,  Channel.TWO_WAY);
    }
}

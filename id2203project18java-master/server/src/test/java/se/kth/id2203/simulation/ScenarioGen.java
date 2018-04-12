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
package se.kth.id2203.simulation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import se.kth.id2203.ParentComponent;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.events.system.KillNodeEvent;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public abstract class ScenarioGen {

    private static final Operation1 startServerOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                final NetAddress bsAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.0." + self), 45678);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ParentComponent.class;
                }

                @Override
                public String toString() {
                    return "StartNode <" + selfAdr.toString() + "> ";
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    if (self != 1) { // don't put this at the bootstrap server, or it will act as a bootstrap client
                        config.put("id2203.project.bootstrap-address", bsAdr);
                    }
                    return config;
                }
            };
        }
    };

    private static final Operation1 startClientOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                final NetAddress bsAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.1." + self), 45678);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ScenarioClient.class;
                }

                @Override
                public String toString() {
                    return "StartClient<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    config.put("id2203.project.bootstrap-address", bsAdr);
                    return config;
                }
            };
        }
    };

    private static final Operation1 startClient2Op = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                final NetAddress bsAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.1." + self), 45678);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ScenarioClient2.class;
                }

                @Override
                public String toString() {
                    return "StartClient<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    config.put("id2203.project.bootstrap-address", bsAdr);
                    return config;
                }
            };
        }
    };


    /* We kill a node bu changing his self address */

    private static final Operation1 killServerOp = new Operation1<KillNodeEvent, Integer>() {

        @Override
        public KillNodeEvent generate(final Integer killPort) {
            return new KillNodeEvent() {
                final NetAddress selfAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.0.2"), killPort);
                    } catch (UnknownHostException ex) {

                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }
            };
        }
    };

    public static SimulationScenario simpleOps(final int servers) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startCluster = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startClients = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startClients2 = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClient2Op, new BasicIntSequentialDistribution(1));
                    }
                };

                startCluster.start();
                startClients2.startAfterTerminationOf(20000,startCluster);
                startClients.startAfterTerminationOf(20000,startClients2);
                terminateAfterTerminationOf(100000, startClients);
            }
        };
    }


    /* --Sim--Perfect Link and Best-Effort Broadcast--
    * We aim here to demonstrate the properties of PPPL (Perfect-Point-to-Point-Link) and BE Broadcast
    * The sim is simple, we send an operation request to all servers.
    * All correct servers must deliver the message exactly once.
    */

    public static SimulationScenario BestEffortBroadcastScenario(final int servers) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startCluster = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                //Kill node with a specified port
                SimulationScenario.StochasticProcess killServer = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, killServerOp, new ConstantDistribution<>(Integer.class,45678));
                    }
                };

                SimulationScenario.StochasticProcess startClients = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };

                startCluster.start();
                killServer.startAfterTerminationOf(10000, startCluster);
                startClients.startAfterTerminationOf(10000, killServer);
                terminateAfterTerminationOf(100000, startClients);
            }
        };
    }


    /* --Sim--Eventually Perfect Failure Detector-- */
    public static SimulationScenario EventuallyPerfectFailureDetectorScenario(final int servers) {
        return new SimulationScenario() {
            {

                SimulationScenario.StochasticProcess startCluster = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                //
                SimulationScenario.StochasticProcess startServer = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(2, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                //Kill node with a specified port
                SimulationScenario.StochasticProcess killServer = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, killServerOp, new ConstantDistribution<>(Integer.class,45678));
                    }
                };

                SimulationScenario.StochasticProcess startClients = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };

                startCluster.start();
                startClients.startAfterTerminationOf(10000, startCluster);
                killServer.startAfterTerminationOf(10000, startClients);
                startServer.startAfterTerminationOf(20000, killServer);
                terminateAfterTerminationOf(100000, killServer);
            }
        };
    }

    /* --Sim--Monarchical Eventual Leader Detector-- */
    public static SimulationScenario MonarchicalEventualLeaderDetectorScenario(final int servers) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startCluster = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                //Kill node with a specified port
                SimulationScenario.StochasticProcess killServer = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, killServerOp, new ConstantDistribution<>(Integer.class,45678));
                    }
                };

                SimulationScenario.StochasticProcess startClients = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };

                startCluster.start();
                killServer.startAfterTerminationOf(10000, startCluster);
                startClients.startAfterTerminationOf(20000, killServer);
                terminateAfterTerminationOf(100000, startClients);
            }
        };
    }

    /* --Sim--Multipaxos : Abortable Sequence Consensus-- */
    public static SimulationScenario AbortableSequenceConsensusScenario(final int servers) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startCluster = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startClients1PUT = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startClients2PUT = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startClientsCAS = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClient2Op, new BasicIntSequentialDistribution(1));
                    }
                };

                startCluster.start();
                startClients1PUT.startAfterTerminationOf(10000, startCluster);
                startClients2PUT.startAfterTerminationOf(20000, startClients1PUT);
                startClientsCAS.startAfterTerminationOf(20000, startClients1PUT); //Start at the same time !
                terminateAfterTerminationOf(100000, startClientsCAS);
            }
        };
    }

}

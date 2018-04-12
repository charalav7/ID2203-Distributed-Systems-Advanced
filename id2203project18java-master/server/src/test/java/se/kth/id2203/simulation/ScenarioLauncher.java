package se.kth.id2203.simulation;

import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class ScenarioLauncher {
    public static void main(String[] args) {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        /*
            For each simu, we create 3 replication groups of 3 nodes : (So the cluster contains 9 servers in total)
         */

        // Best-Effort Broadcast
        SimulationScenario simple = ScenarioGen.simpleOps(9);
        SimulationScenario beb = ScenarioGen.BestEffortBroadcastScenario(9);
        SimulationScenario epfd = ScenarioGen.EventuallyPerfectFailureDetectorScenario(9);
        SimulationScenario meld = ScenarioGen.MonarchicalEventualLeaderDetectorScenario(9);
        SimulationScenario asc = ScenarioGen.AbortableSequenceConsensusScenario(9);

        asc.simulate(LauncherComp.class);
    }
}

package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.PingPong.testing.TestScenarios;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class ProtocolComparisonPingPong extends ApplicationLayer {

	@ShoXParameter(description = "Número de transmissões que a aplicação irá realizar", defaultValue = "10")
	private int transmissions;

	private TestScenarios scenarios = TestScenarios.getInstance();
	
	private static final int INITIAL_NODE = 1;

	protected void processEvent(StartSimulation start) {
		if (node.getId().asInt() == INITIAL_NODE) {
			int nextId = scenarios.next();
			SimulationManager.logNodeState(node.getId(), "Destino", "int", String.valueOf(10));
			SimulationManager.logNodeState(NodeId.get(nextId), "Destino", "int", String.valueOf(10));
			SingletonTestResult.getInstance().setStartingTime(SimulationManager.getInstance().getCurrentTime());
			SingletonTestResult.getInstance().mark();
			ScenarioTestPacket pack = new ScenarioTestPacket(sender, nextId, 1);
			sendPacket(pack);
		}
	}

	@Override
	public void processEvent(TrafficGeneration tg) {
	}

	@Override
	public int getPacketTypeCount() {
		return 1;
	}

	@Override
	public void lowerSAP(Packet packet) throws LayerException {
		if (packet instanceof ScenarioTestPacket) {
			SingletonTestResult.getInstance().mark();
			ScenarioTestPacket rcvdPack = (ScenarioTestPacket) packet;
			ScenarioTestPacket pack;
			int pingpong = rcvdPack.getPingPongCounter();
			if (node.getId().asInt() == INITIAL_NODE) {
				SingletonTestResult.getInstance().setEndingTime(SimulationManager.getInstance().getCurrentTime());
				System.out.println(SingletonTestResult.getInstance().getAccumulatedTime());
				System.out.println("BB: " + (SingletonTestResult.getInstance().getBackboneCount() - 8));
				return;
			} else if (pingpong == transmissions - 1) {
				pack = new ScenarioTestPacket(sender, INITIAL_NODE, pingpong + 1);
			} else {
				pack = new ScenarioTestPacket(sender, scenarios.next(), pingpong + 1);
			}
			SimulationManager.logNodeState(pack.getReceiver(), "Destino", "int", String.valueOf(10));
			SingletonTestResult.getInstance().mark();
			sendPacket(pack);
		}
	}
}

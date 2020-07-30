package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class ProtocolComparisonList extends ApplicationLayer {

	@ShoXParameter(description = "Número de voltas que a aplicação irá realizar", defaultValue = "2")
	private int laps;

	private static final int INITIAL_NODE = 1;
	private static final int FINAL_NODE = 2;

	protected void processEvent(StartSimulation start) {
		if (node.getId().asInt() == INITIAL_NODE) {
			SimulationManager.logNodeState(node.getId(), "Destino", "int", String.valueOf(10));
			SimulationManager.logNodeState(NodeId.get(INITIAL_NODE + 1), "Destino", "int", String.valueOf(10));
			SingletonTestResult.getInstance().setStartingTime(SimulationManager.getInstance().getCurrentTime());
			SingletonTestResult.getInstance().mark();
			ChainTestPacket pack = new ChainTestPacket(sender, 1);
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
		if (packet instanceof ChainTestPacket) {
			SingletonTestResult.getInstance().mark();
			ChainTestPacket chainPack = (ChainTestPacket) packet;
			ChainTestPacket pack;
			int lap = chainPack.getLapNumber();
			if (node.getId().asInt() == INITIAL_NODE && lap > laps) {
				SingletonTestResult.getInstance().setEndingTime(SimulationManager.getInstance().getCurrentTime());
				System.out.println(SingletonTestResult.getInstance().getTime());
				return;
			} else if (node.getId().asInt() == FINAL_NODE) {
				pack = new ChainTestPacket(sender, NodeId.get(INITIAL_NODE), lap + 1);
			} else {
				pack = new ChainTestPacket(sender, lap);
			}
			SimulationManager.logNodeState(pack.getReceiver(), "Destino", "int", String.valueOf(10));
			SingletonTestResult.getInstance().mark();
			sendPacket(pack);
		}
	}
}

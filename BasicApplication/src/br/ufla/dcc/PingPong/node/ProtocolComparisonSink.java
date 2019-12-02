package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class ProtocolComparisonSink extends ApplicationLayer {

	@ShoXParameter(description = "Número de voltas que a aplicação irá realizar", defaultValue = "2")
	private int laps;

	private static final int COMM_NODES = 10;
	private static final NodeId SINK = NodeId.get(5);

	protected void processEvent(StartSimulation start) {
		if (node.getId().asInt() <= COMM_NODES) {
			SimulationManager.logNodeState(node.getId(), "Origens", "int", String.valueOf(10));
			PingPongWakeUpCall ppwuc = new PingPongWakeUpCall(sender, 12000 * (node.getId().asInt() - 1));
			sendEventSelf(ppwuc);
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
		if (packet instanceof AppPacket) {
			AppPacket pack = (AppPacket) packet;
			SingletonTestResult.getInstance().mark();
			if (pack.getSender().getId().asInt() == COMM_NODES) {
				System.out.println(SingletonTestResult.getInstance().getAccumulatedTime());
			}
		}
	}
	
	@Override
	public void processWakeUpCall(WakeUpCall wuc) {
		if (wuc instanceof PingPongWakeUpCall) {
			SingletonTestResult.getInstance().mark();
			AppPacket pack = new AppPacket(sender, NodeId.get(5));
			sendPacket(pack);
		}
	}
}

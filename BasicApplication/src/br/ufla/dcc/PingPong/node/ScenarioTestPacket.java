package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;

public class ScenarioTestPacket extends AppPacket {

	private int pingPongCounter;
	
	public ScenarioTestPacket(Address sender, int receiverId, int pingPongCount) {
		super(sender, NodeId.get(receiverId));
		pingPongCounter = pingPongCount;
		// TODO Auto-generated constructor stub
	}

	public int getPingPongCounter() {
		return pingPongCounter;
	}
}

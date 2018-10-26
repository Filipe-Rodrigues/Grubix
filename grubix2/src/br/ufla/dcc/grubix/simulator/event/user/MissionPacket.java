package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;
import br.ufla.dcc.grubix.simulator.node.Node;

public class MissionPacket extends ApplicationPacket
{
	private Mission mission;
	
	private double goodness = 0;
	
	public MissionPacket(Address sender, NodeId receiver, Mission mission, double goodness) {
		super(sender,receiver);
		this.mission = mission;
		this.goodness = goodness;
	}

	public Mission getMission() {
		return mission;
	}

	public double getGoodness() {
		return goodness;
	}
}
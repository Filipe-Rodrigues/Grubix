package br.ufla.dcc.PingPong.routing.USAMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class USAMacRoutingControlPacket extends NetworkPacket {

	private Position growthDirection;
	private Position backboneLineRoot;
	private NodeId nextSelectedBackbone;
	
	public USAMacRoutingControlPacket(Address sender, NodeId receiver, Position direction, 
			Position backboneRoot, NodeId nextBackbone) {
		super(sender, receiver);
		growthDirection = direction;
		backboneLineRoot = backboneRoot;
		nextSelectedBackbone = nextBackbone;
	}
	
	public USAMacRoutingControlPacket(Address sender, NodeId receiver, Position direction, 
			Position backboneRoot, NodeId nextBackbone, NodeId prevBackbone) {
		super(sender, receiver);
		growthDirection = direction;
		backboneLineRoot = backboneRoot;
		nextSelectedBackbone = nextBackbone;
	}
	
	public Position getGrowthDirection() {
		return growthDirection;
	}

	public Position getBackboneLineRoot() {
		return backboneLineRoot;
	}

	public NodeId getNextSelectedBackbone() {
		return nextSelectedBackbone;
	}

}

package br.ufla.dcc.PingPong.routing.MXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;

public class MXMacRoutingControlPacket extends NetworkPacket {

	private Position growthDirection;
	private Position backboneLineRoot;
	private NodeId nextSelectedBackbone;
	private int backboneType;
	
	public MXMacRoutingControlPacket(Address sender, NodeId receiver, Position direction, 
			Position backboneRoot, NodeId nextBackbone, int bbType) {
		super(sender, receiver);
		growthDirection = direction;
		backboneLineRoot = backboneRoot;
		nextSelectedBackbone = nextBackbone;
		backboneType = bbType;
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

	public int getBackboneType() {
		return backboneType;
	}

}

package br.ufla.dcc.PingPong.routing.EXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class EXMacRoutingControlPacket extends NetworkPacket {

	private Position growthDirection;
	
	public EXMacRoutingControlPacket(Address sender, NodeId receiver, Packet packet, Position direction) {
		super(sender, receiver, packet);
		growthDirection = direction;
	}
	
	public Position getGrowthDirection() {
		return growthDirection;
	}

}

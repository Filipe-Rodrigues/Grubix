package br.ufla.dcc.PingPong.routing;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class GeoRoutingPacket extends NetworkPacket {

	public GeoRoutingPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
		// TODO Auto-generated constructor stub
	}

	public GeoRoutingPacket(Address sender, NodeId receiver, Packet packet) {
		super(sender, receiver, packet);
		// TODO Auto-generated constructor stub
	}
	
	public GeoRoutingPacket(Address sender, Packet packet) {
		super(sender, packet);
		// TODO Auto-generated constructor stub
	}

}


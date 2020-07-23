package br.ufla.dcc.PingPong.routing;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class GeoRoutingPacket extends NetworkPacket {

	private int hopCounter;
	
	public GeoRoutingPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
		setHopCount(0);
		// TODO Auto-generated constructor stub
	}

	public GeoRoutingPacket(Address sender, NodeId receiver, Packet packet) {
		super(sender, receiver, packet);
		setHopCount(0);
		// TODO Auto-generated constructor stub
	}
	
	public GeoRoutingPacket(Address sender, Packet packet) {
		super(sender, packet);
		setHopCount(0);
		// TODO Auto-generated constructor stub
	}

	public int getHopCounter() {
		return hopCounter;
	}

	public void setHopCount(int hops) {
		hopCounter = hops;
	}

}


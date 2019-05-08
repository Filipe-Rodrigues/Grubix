package br.ufla.dcc.PingPong.routing.EXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class EXMacRoutingControlPacket extends NetworkPacket {

	public EXMacRoutingControlPacket(Address sender, NodeId receiver, Packet packet) {
		super(sender, receiver, packet);
	}
	
	

}

package br.ufla.dcc.PingPong.routing.EXMac;

import java.util.Queue;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class EXMacRoutingPacket extends NetworkPacket {

	private Queue<Byte> backboneSegmentPath;
	
	public EXMacRoutingPacket(Address sender, NodeId receiver, Packet packet, Queue<Byte> backbonePath) {
		super(sender, receiver, packet);
		backboneSegmentPath = backbonePath;
	}

	public Queue<Byte> getBackboneSegmentPath() {
		return backboneSegmentPath;
	}

}

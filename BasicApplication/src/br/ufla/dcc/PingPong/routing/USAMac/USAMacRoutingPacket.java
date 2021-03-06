package br.ufla.dcc.PingPong.routing.USAMac;

import java.util.Queue;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class USAMacRoutingPacket extends NetworkPacket {

	private Queue<Byte> backboneSegmentPath;
	private int hopCounter;
	
	public USAMacRoutingPacket(Address sender, NodeId receiver, Packet packet, Queue<Byte> backbonePath) {
		super(sender, receiver, packet);
		backboneSegmentPath = backbonePath;
	}

	public Queue<Byte> getBackboneSegmentPath() {
		return backboneSegmentPath;
	}
	
	public int getHopCount() {
		return hopCounter;
	}
	
	public void setHopCount(int hops) {
		hopCounter = hops;
	}

}

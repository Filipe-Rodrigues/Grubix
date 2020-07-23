package br.ufla.dcc.PingPong.routing.MXMac;

import java.util.Queue;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.util.Pair;

public class MXMacRoutingPacket extends NetworkPacket {

	private Queue<Pair<Integer, Position>> backbonePath;
	private Position vTarget;
	private int hopCounter;
	
	public MXMacRoutingPacket(Address sender, NodeId receiver, Packet packet, Queue<Pair<Integer, Position>> path, Position virtualTarget) {
		super(sender, receiver, packet);
		backbonePath = path;
		vTarget = virtualTarget;
		hopCounter = 0;
	}

	public Queue<Pair<Integer, Position>> getBackbonePath() {
		return backbonePath;
	}

	public Position getVirtualTarget() {
		return vTarget;
	}
	
	public int getHopCounter () {
		return hopCounter;
	}
	
	public void setHopCount(int hops) {
		hopCounter = hops;
	}
	
}

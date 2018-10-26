package br.ufla.dcc.PingPong.routing;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class BackboneRoutingPacket extends NetworkPacket {
	
	private BackboneRoutingPacketType type;
	private Position backboneRoot;
	
	public BackboneRoutingPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
		// TODO Auto-generated constructor stub
	}

	public BackboneRoutingPacket(Address sender, NodeId receiver, Packet packet) {
		super(sender, receiver, packet);
		// TODO Auto-generated constructor stub
	}
	
	public BackboneRoutingPacket(Address sender, Packet packet) {
		super(sender, packet);
		// TODO Auto-generated constructor stub
	}
	
	public BackboneRoutingPacket(Address sender, NodeId receiver, BackboneRoutingPacketType type) {
		super(sender, receiver);
		this.type = type;
		this.backboneRoot = null;
	}
	
	public BackboneRoutingPacket(Address sender, NodeId receiver, BackboneRoutingPacketType type, Position root) {
		super(sender, receiver);
		this.type = type;
		this.backboneRoot = root;
	}

	public BackboneRoutingPacketType getType() {
		return type;
	}
	
	
	public Position getRootPosition() {
		return backboneRoot;
	}
}


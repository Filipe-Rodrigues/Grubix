package br.ufla.dcc.PingPong.physicalMX;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;

public class PhysicalMXPacket extends PhysicalPacket {

	private int channel;
	
	public PhysicalMXPacket(Address sender, Packet packet, int channel) {
		super(sender, packet);
		this.channel = channel;
	}
	
	public PhysicalMXPacket(NodeId receiver, Address sender, int channel) {
		super(receiver, sender);
		this.channel = channel;
	}

	public int getPacketChannel() {
		return channel;
	}
	
}

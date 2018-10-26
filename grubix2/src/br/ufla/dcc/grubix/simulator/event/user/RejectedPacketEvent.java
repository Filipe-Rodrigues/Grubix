package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.CrossLayerResult;
import br.ufla.dcc.grubix.simulator.event.Packet;

/**
 * This event is used for a botch of the 802.11bg DCF MAC, which sometimes
 * tries to send a packet, when currently one is sent.
 * 
 * @author Dirk Held
 */
public class RejectedPacketEvent extends CrossLayerEvent {

	/** If the air rejects a packet, its because of this packet. */
	private Packet currentPacket;
	
	/**
	 * @param sender the sender of the packet, which is dropped before sending.
	 * @param packet the dropped packet.
	 */
	public RejectedPacketEvent(Address sender, Packet packet, Packet currentPacket) {
		super(sender, packet);
		
		this.currentPacket = currentPacket;
	}

	/** @return the current sent packet, if any. */
	public final Packet getCurrentPacket() {
		return currentPacket;
	}
}

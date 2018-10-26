package br.ufla.dcc.grubix.simulator.node.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.user.RejectedPacketEvent;


/**
 * This event is sent from the Air, whenever the MAC sent 
 * a packet during an uncompleted carrier sense cycle. 
 * 
 * @author Dirk Held
 */
public class CarrierSenseInterruptedEvent extends RejectedPacketEvent {

	/** the time, the current transmission still needs, to complete. */
	private double rest;
	
	/** the position index, which called the free carrier method. */
	private int pos;
	
	/**
	 * default constructor.
	 * 
	 * @param sender        The Air, as current only sender of this event.
	 * @param currentPacket The current outgoing packet.
	 * @param rest          The time, this packet still needs to be transmitted.
	 * @param pos           The position index, from which a free carrier was assumed.
	 */
	public CarrierSenseInterruptedEvent(Address sender, PhysicalPacket currentPacket, double rest, int pos) {
		super(sender, null, currentPacket);
		
		this.rest = rest;
		this.pos  = pos;
	}

	/** @return the time, the current transmission still needs. */
	public final double getRest() {
		return rest;
	}

	/** @return the position index, from which the free carrier event was called. */
	public final int getPos() {
		return pos;
	}
}

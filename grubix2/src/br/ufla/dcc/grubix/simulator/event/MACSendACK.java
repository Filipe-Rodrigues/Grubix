/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;

/**
 * this event is used to signal a MAC layer, that an ACK is to be sent *now* (TM 24).
 * @author Dirk Held
 */
public class MACSendACK extends MACEvent {

	/** the ack packet to sent, when this event comes. */
	private Packet ack;
	
	/**
	 * constructor for immediate sending. 
	 * @param sender the sender of this event. 
	 */
	public MACSendACK(Address sender) {
		super(sender);
	}

	/**
	 * default constructor for delayed sending.
	 * @param sender the sender of this event.
	 * @param time   the delay to wait before processing.
	 * @param ack    the ack packet to send.
	 */
	public MACSendACK(Address sender, double time, Packet ack) {
		super(sender, time);
		this.ack = ack;
	}

	/** @return the ack packet. */
	public Packet getAck() {
		return ack;
	}
}

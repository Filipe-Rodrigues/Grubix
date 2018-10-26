package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

/**
 * This wakeup call event is fired when the next packet in the MAC queue is to be sent.
 * @author jlsx
 */
public class MACAdvanceQueue extends WakeUpCall {
	
	/** The packet that is first in the queue (i.e. to be sent when the wakeup call fires). */
	private Packet packet;

	/**
	 * Default constructor of the class MACAdvanceQueue.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param delay Wanted delay until callback.
	 * @param packet The packet that is now to be sent to the physical layer
	 */
	public MACAdvanceQueue(Address sender, Double delay, Packet packet) {
		super(sender, delay);
		this.packet = packet;
	}

	/**
	 * Constructor of the class MACAdvanceQueue.
	 * Creates WakeUpCalls with delay 0 (immediate callback).
	 * Receiver is set to nodeId of the sender.
	 * 
	 * @param sender sendernode for the wakeUpEvent
	 * @param packet The packet that is now to be sent to the physical layer
	 */
	public MACAdvanceQueue(Address sender, Packet packet) {
		super(sender);
		this.packet = packet;
	}

	/**
	 * @return The packet that is first in the queue (i.e. to be sent when the wakeup call fires).
	 */
	public final Packet getPacket() {
		return this.packet;
	}
}

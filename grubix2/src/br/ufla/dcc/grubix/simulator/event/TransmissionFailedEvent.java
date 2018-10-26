package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;

/**
 * event to signal a failed transmission to a higher layer.
 * 
 * @author Dirk Held
 */
public class TransmissionFailedEvent extends CrossLayerEvent {

	/**
	 * @param sender the origin of this event.
	 * @param packet the packet, which was not sent.
	 */
	public TransmissionFailedEvent(Address sender, Packet packet) {
		super(sender, packet, CrossLayerResult.FAIL);
	}
	
	/**
	 * @param sender   The origin of this event.
	 * @param receiver The layer to which this event has to be forwarded at least.
	 * @param packet   The packet, which was not sent.
	 */
	public TransmissionFailedEvent(Address sender, LayerType receiver, Packet packet) {
		super(sender, receiver, packet, CrossLayerResult.FAIL);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "TransmissionFailedEvent from " + getSender().getId() + " to " + getReceiver() + " packet: " + getPacket();
	}
}

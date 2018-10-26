package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * If a node expects to receive a packet, but there is silence on the air, he may sent this event.
 * 
 * @author Dirk Held
 */
public class MissingReceptionEvent extends CrossLayerEvent {

	/** The node, which a packet was expected. */
	private NodeId senderId;
		
	/**
	 * @param receiver      The node, who didn't got the expected packet
	 * @param terminalLayer The highest layer, this event should be sent to.
	 * @param senderId      The expected node, from which a packet was not received.
	 */
	public MissingReceptionEvent(Address receiver, LayerType terminalLayer, NodeId senderId) {
		super(receiver, terminalLayer, null);
		
		this.senderId    = senderId;
	}

	/** @return the NodeId of the Node, from which a packet was expected, but not received. */
	public final NodeId getSenderId() {
		return senderId;
	}
}

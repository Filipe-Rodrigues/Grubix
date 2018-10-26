package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.NetworkEvent;

/**
 * This event is used, to trigger the establishing of a connection to another node.
 * 
 * @author Dirk Held
 */
public class TriggerConnectionStartEvent extends NetworkEvent {

	/** The id of the stream at this node. */
	private int sourceId;
	
	/** The destination of the to be established connection. */
	private NodeId destination;
	
	/**
	 * Default constructor of this class.
	 * 
	 * @param sender      The node from which the connection starts.
	 * @param sourceId    The id of the stream at this node.
	 * @param destination The destination of the connection.
	 */
	public TriggerConnectionStartEvent(Address sender, int sourceId, NodeId destination) {
		super(sender);
		
		this.sourceId    = sourceId;
		this.destination = destination;
	}

	/** @return the sourceId. */
	public final int getSourceId() {
		return sourceId;
	}

	/** @return the destination of the connection. */
	public NodeId getDestination() {
		return destination;
	}
}

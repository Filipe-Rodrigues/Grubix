package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.AppEvent;

/**
 * This event signals the application layer terminated connection process.
 *  
 * @author Dirk Held
 */
public class ConnectionEstablishedEvent extends AppEvent {

	/** the destination of the just established connection. */
	private NodeId destination;
	
	/** the sourceId of the stream. */
	private double connectionEstablishingTime;
	
	/** if true, the connection was established. */
	private boolean success;
	
	/**
	 * @param sender      the node, which has started the connection.
	 * @param destination the destination of the just established connection.
	 * @param conEstTime  the time needed, to establish the connection.
	 * @param success     if true, the connection was established.
	 */
	public ConnectionEstablishedEvent(Address sender, NodeId destination, 
									  double conEstTime, boolean success) {
		super(sender, 0.0, -1);
		
		this.destination 				= destination;
		this.connectionEstablishingTime = conEstTime;
		this.success     				= success;
	}

	/** @return the destination. */
	public final NodeId getDestination() {
		return destination;
	}

	/** @return the sourceId. */
	public final double getConnectionEstablishingTime() {
		return connectionEstablishingTime;
	}

	/** @return the success. */
	public final boolean isSuccess() {
		return success;
	}
}

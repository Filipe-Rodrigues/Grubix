/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event;

/** 
 * SimulatonEvent to call the NodeStartupManager to startup new nodes after a certain period.
 * 
 * @author Florian Rittmeier
 */

public class NodeStartupManagerEvent extends SimulationEvent {

	/**
	 * Specifies when the NodeStartupManagerEnvelope of SimulationManager has
	 * to request new nodes to startup from the NodeStartupManager.
	 */
	protected final double delay;

	/**
	 * @param delay delay for when to startup new nodes
	 */
	public NodeStartupManagerEvent(double delay) {
		this.delay = delay;
	}

	/**
	 * @return delay for when to startup new nodes
	 */
	public final double getDelay() {
		return this.delay;
	}
	
	/**
	 * @return String for logging.
	 */
	public final String toString() {
		return  this.getClass().getName() + " to startup new nodes.";
	}

}

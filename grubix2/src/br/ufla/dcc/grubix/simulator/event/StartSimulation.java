/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * @author Dirk Held
 *
 */
public class StartSimulation extends SimulationState {

	/**
	 * @param receiver the node which has to start this layer.
	 * @param delay the time to wait, before this event is processed.
	 */
	public StartSimulation(NodeId receiver, double delay) {
		super(receiver, delay);
	}
}

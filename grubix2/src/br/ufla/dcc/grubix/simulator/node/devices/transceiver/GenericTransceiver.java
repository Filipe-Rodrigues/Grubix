/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.transceiver;

import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.node.energy.PowerConsumer;

/**
 * Base class for all transceivers.
 * 
 * @author Florian Rittmeier
 */
public abstract class GenericTransceiver implements PowerConsumer {

	/**
	 * this is called from deep inside the simulator when the radio state
	 * of a node changes.
	 * @param newstate the new radio state
	 */
	public abstract void switchToRadioState(RadioState newstate);
}

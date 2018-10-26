/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices;

import br.ufla.dcc.grubix.simulator.node.energy.PowerScavengingSupplier;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;

/**
 * This is just an example for a PowerScavengingSupplier,
 * which uses very low energy values, which can be checked
 * by hand.
 * 
 * @author Florian Rittmeier
 */
public class ExampleVerySmallPowerSocket extends PowerScavengingSupplier {
	
	/**
	 * This is the constructor.
	 */
	public ExampleVerySmallPowerSocket() {
		maximumOutput = new Watt(0, 100, 0, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}
	
}

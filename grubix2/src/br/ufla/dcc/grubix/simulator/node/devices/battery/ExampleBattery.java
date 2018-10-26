/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.battery;

import br.ufla.dcc.grubix.simulator.node.energy.EnergyReservoirSupplier;
import br.ufla.dcc.grubix.simulator.node.energy.units.Joule;

/**
 * This is an example battery, just for having something
 * with an inital value which is small enough to be checked by hand.
 * 
 * @author Florian Rittmeier
 */
public class ExampleBattery extends EnergyReservoirSupplier {

	/**
	 * 
	 */
	public ExampleBattery() {
		capacity = new Joule(1, 0, 0, 0);
		currentLevel = (Joule) capacity.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}

}

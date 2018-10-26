/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.battery;

import br.ufla.dcc.grubix.simulator.node.energy.EnergyReservoirSupplier;
import br.ufla.dcc.grubix.simulator.node.energy.units.Joule;

/**
 * This is an implementation for a Mono battery (ANSI type D).
 * approx. 20.000 mAh @ 1.5V = 108000J
 * see http://de.wikipedia.org/w/index.php?title=Batterie&oldid=40157019
 * 
 * @author Florian Rittmeier
 */
public class MonoBattery extends EnergyReservoirSupplier {

	/**
	 * This is the constructor.
	 */
	MonoBattery() {
		capacity = new Joule(108000, 0, 0, 0);
		currentLevel = (Joule) capacity.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}
	
}

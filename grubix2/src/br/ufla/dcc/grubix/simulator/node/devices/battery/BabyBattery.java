/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.battery;

import br.ufla.dcc.grubix.simulator.node.energy.EnergyReservoirSupplier;
import br.ufla.dcc.grubix.simulator.node.energy.units.Joule;

/**
 * This is an implementation for a Baby battery (ANSI type C).
 * approx. 8.000 mAh @ 1.5V = 43.200J
 * see http://de.wikipedia.org/w/index.php?title=Batterie&oldid=40157019
 * 
 * @author Florian Rittmeier
 */
public class BabyBattery extends EnergyReservoirSupplier {

	/**
	 * This is the constructor.
	 */
	BabyBattery() {
		capacity = new Joule(43200, 0, 0, 0);
		currentLevel = (Joule) capacity.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}
	
}

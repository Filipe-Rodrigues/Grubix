/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.devices.battery;

import br.ufla.dcc.grubix.simulator.node.energy.EnergyReservoirSupplier;
import br.ufla.dcc.grubix.simulator.node.energy.units.Joule;

/**
 * This is an implementation for a Lady battery (ANSI type N).
 * approx. 800 mAh @ 1.5V = 4320J
 * see http://de.wikipedia.org/w/index.php?title=Spezial:Cite&page=Batterie&id=40157019
 * 
 * @author Florian Rittmeier
 */
public class LadyBattery extends EnergyReservoirSupplier {

	/**
	 * This is the constructor.
	 */
	LadyBattery() {
		capacity = new Joule(4320, 0, 0, 0);
		currentLevel = (Joule) capacity.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}
	
}

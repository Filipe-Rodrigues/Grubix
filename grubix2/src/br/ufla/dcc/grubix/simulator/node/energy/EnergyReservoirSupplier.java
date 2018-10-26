/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.energy.units.Joule;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * This is the base class for all energy reservoir suppliers.
 * 
 * A energy reservoir supplier is characterized by two values.
 * Its capacity and its current level.
 * 
 * The current level fulfills the following condition:
 * 0 <= current level <= capacity
 * 
 * Examples are all kinds of batteries.
 * 
 * @author Florian Rittmeier
 */
public abstract class EnergyReservoirSupplier implements PowerSupplier {

	/**
	 * Internal field for the maximum capacity.
	 */
	protected Joule capacity;
	
	/**
	 * Internal field for the fill level.
	 */
	protected Joule currentLevel;
	
	/**
	 * @return the maximum capacity
	 */
	public Joule getCapacity() {
		return (Joule) this.capacity.clone();
	}
	
	/**
	 * @return the fill level
	 */
	public Joule getCurrentLevel() {
		return (Joule) this.currentLevel.clone();
	}

	/**
	 * Reduces the amount of energy this energy reservoir can provide.
	 * 
	 * @param amountOfEnergy how much energy should be subtracted / used
	 * @return how much of the request could not be satisfied because the reservoir collapsed
	 */
	public Joule reduceEnergy(Joule amountOfEnergy) {
		if (currentLevel.getValue() > amountOfEnergy.getValue()) {
			currentLevel.substractJoule(amountOfEnergy);
			return new Joule();
		} else {
			/* this energy reservoir collapse,
			 * some amount of Energy cannot be satisfied
			 */
			Joule delta = (Joule) amountOfEnergy.clone();
			delta.substractJoule(currentLevel);
			currentLevel = new Joule();
			return delta;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
	}
	
}

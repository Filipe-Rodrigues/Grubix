package br.ufla.dcc.grubix.simulator.random;

import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * deterministic distribution.
 * 
 * @author dmeister
 *
 */
public class DeterministicDistribution extends RandomDistribution {
	
	/**
	 * value.
	 */
	@ShoXParameter(description = "value")
	private double value;
	
	/**
	 * Constructor for use in a factory.
	 * Cannot be public since this method assumes that init is called.
	 */
	@SuppressWarnings("unused")
	private DeterministicDistribution() {
		//nothing to do
	}
	

	/**
	 * returns the configured value.
	 * 
	 * @return the configured value.
	 * @param generator a random generator
	 * @see br.ufla.dcc.grubix.simulator.random.RandomDistribution#nextDouble()
	 */
	@Override
	public double nextDouble(RandomGenerator generator) {
		return value;
	}
}

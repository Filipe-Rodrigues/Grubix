package br.ufla.dcc.grubix.simulator.random;

import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * Uniform distribution.
 * 
 * @author dmeister
 *
 */
public class UniformRandomDistribution extends RandomDistribution {
	
	/**
	 * maximal value.
	 */
	@ShoXParameter(description = "maximal value", defaultValue = "1.0")
	private double maximalValue;
	
	/**
	 * minimal value.
	 */
	@ShoXParameter(description = "minimal value", defaultValue = "0.0")
	private double minimalValue;
	
	/**
	 * Constructor for use in a factory.
	 * Cannot be public since this method assumes that init is called.
	 */
	@SuppressWarnings("unused")
	private UniformRandomDistribution() {
		//nothing to do
	}

	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.random.RandomDistribution#init()
	 */
	@Override
	public void init() throws ConfigurationException {
		super.init();
		if(maximalValue < minimalValue) {
			throw new ConfigurationException("maximal value is less than minimal value");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "UniformDistribution(" + minimalValue + ", " + maximalValue + ")";
	}

	/**
	 * generates the next double using a uniform distribution.
	 * 
	 * @return a new double value generated using a uniform distribution.
	 * @param generator a random generator
	 * @see br.ufla.dcc.grubix.simulator.random.RandomDistribution#nextDouble()
	 */
	@Override
	public double nextDouble(RandomGenerator generator) {
		return minimalValue + (generator.nextDouble() * (maximalValue - minimalValue));
	}


}

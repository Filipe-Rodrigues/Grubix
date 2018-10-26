package br.ufla.dcc.grubix.simulator.random;

import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * Abstract class for various random distrubtions.
 * 
 * @author dmeister
 *
 */
public abstract class RandomDistribution implements Configurable {

	/**
	 * Returns the next pseudorandom  double value between 0.0 and 1.0
	 * from this random number generator's sequence.
	 * The values are distributed according the the distribution specified by the class.
	 * 
	 * Implementations should use getGenerator() to access to underlying random source.
	 * 
	 * @return random double
	 */
	public abstract double nextDouble(RandomGenerator randomGenerator);
	
	/**
	 * inits the random distribution
	 * @throws ConfigurationException thrown if configuration is invalid
	 */
	public void init() throws ConfigurationException {
		//nothing to do
	}
}

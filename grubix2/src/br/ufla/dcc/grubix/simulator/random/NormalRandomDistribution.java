package br.ufla.dcc.grubix.simulator.random;

import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * Normal or gaussian distribution.
 * The calculation is done with the Polar or Box-Mueller method.
 * For details, so Knuth v2 or http://en.wikipedia.org/wiki/Box-Muller_transform.
 * 
 * @author dmeister
 *
 */
public class NormalRandomDistribution extends RandomDistribution {

	/**
	 * sigma.
	 */
	@ShoXParameter(description = "sigma parameter", defaultValue = "1.0")
	private double sigma;
	
	/**
	 * flag if a second value is available.
	 */
	private boolean hasNext = false;
	
	/**
	 * the next value if hasNext is true.
	 */
	private double nextValue = 0.0;
	
	/**
	 * Constructor for use in a factory.
	 * Cannot be public since this method assumes that init is called.
	 */
	@SuppressWarnings("unused")
	private NormalRandomDistribution() {
		//nothing to do
	}
	
	/**
	 * Constructor.
	 * 
	 * @param sigma sigma
	 */
	public NormalRandomDistribution(double sigma) {
		this.sigma = sigma;
		try {
			init();
		} catch (ConfigurationException e) {
			throw new IllegalArgumentException("sigma");
		}
	}

	/**
	 * generates the next double using a gaussian distribution.
	 * 
	 * @param generator a random generator
	 * @return a double generated using a gaussian distribution.
	 * @see br.ufla.dcc.grubix.simulator.random.RandomDistribution#nextDouble()
	 */
	@Override
	public double nextDouble(RandomGenerator generator) {
		if (hasNext) {
			hasNext = false;
            return nextValue;
        }
		hasNext = true;
		double thisValue = 0.0;
		double v1, v2, s;
		do {
		    v1 = -1 + 2 * generator.nextDouble(); // between -1 and 1
		    v2 = -1 + 2 * generator.nextDouble(); // between -1 and 1
		    s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
		nextValue = sigma * v2 * multiplier;
		thisValue = sigma * v1 * multiplier;
		return thisValue;
	}

	/**
	 * gets sigma.
	 * @return sigma parameter of the distribution.
	 */
	public double getSigma() {
		return sigma;
	}

}

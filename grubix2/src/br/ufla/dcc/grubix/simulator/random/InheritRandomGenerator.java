/**
 * 
 */
package br.ufla.dcc.grubix.simulator.random;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;

/**
 * A random generator referencing to the global random generator stored in the Configuration.
 * 
 * This class is for usage in classes where by default the global random generator can be used, but
 * an user can configure an own source of randomness if he liked to.
 * 
 * @author dmeister
 *
 */
public class InheritRandomGenerator extends RandomGenerator {

	/**
	 * Constructor.
	 */
	public InheritRandomGenerator() {
	}

	/**
	 * returns a reference to the global random generator.
	 * @return the global random generator.
	 */
	private RandomGenerator getGlobalRandom() {
		return Configuration.getInstance().getRandomGenerator();
	}
	
	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextInt(int)
	 */
	@Override
	public long getSeed() {
		return getGlobalRandom().getSeed();
	}

	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextInt(int)
	 */
	@Override
	public boolean nextBoolean() {
		return getGlobalRandom().nextBoolean();
	}

	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextInt(int)
	 */
	@Override
	public double nextDouble() {
		return getGlobalRandom().nextDouble();
	}

	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextInt(int)
	 */
	@Override
	public int nextInt(int n) {
		return getGlobalRandom().nextInt(n);
	}

}

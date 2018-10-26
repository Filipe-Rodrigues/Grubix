/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.grubix.simulator.random;

import java.util.Random;

import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * RandomGenerator that used the {@link Random} generator from Java.
 * @author dmeister
 *
 */
public class JavaRandomGenerator extends RandomGenerator {

	/**
	 * Java's own random generator.
	 */
	private final Random random;
	
	/**
	 * seed of the random generator.
	 * if not set (or set to 0) the random seed (likely to be unique) is used.
	 */
	@ShoXParameter(defaultValue = "0", description = "seed of the random generator")
	private long seed;

	/**
	 * Constructor with a given random generator.
	 * @param random random generator
	 */
	public JavaRandomGenerator(Random random) {
		this.random = random;
	}

	/**
	 * Constructor without a given random generator.
	 */
	public JavaRandomGenerator() {
		this.random = new Random();			
	}

	/**
	 * Constructor with a seed.
	 * @param seed random seed
	 */
	public JavaRandomGenerator(long seed) {
		this.seed = seed;
		this.random = new Random(seed);
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed boolean value from this random number generator's sequence.
	 * @return random boolean
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextBoolean()
	 */
	@Override
	public boolean nextBoolean() {
		return random.nextBoolean();
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0
	 * from this random number generator's sequence.
	 * @return random double
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextDouble()
	 */
	@Override
	public double nextDouble() {
		double value = random.nextDouble();
		return value;
	}

	/**
	 * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the
	 * specified value (exclusive), drawn from this random number generator's sequence.
	 * @param n upper (exclusive) limit
	 * @return random int (from 0 to n - 1)
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextInt(int)
	 */
	@Override
	public int nextInt(int n) {
		return random.nextInt(n);
	}

	/**
	 * inits the random generator.
	 * It sets the seed, if it is seed to a non-default value.
	 * 
	 * @throws ConfigurationException thrown if base class throws the exception.
	 * @see br.ufla.dcc.grubix.xml.Configurable#init()
	 */
	@Override
	public void init() throws ConfigurationException {
		super.init();
		
		while (seed == 0) {
			seed = this.random.nextLong();
		}
		
		this.random.setSeed(seed);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSeed() {
		return seed;
	}

}

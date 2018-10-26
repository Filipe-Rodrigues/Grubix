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
 
Copyright 1998 B. Narasimhan, adapted by the ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
 ********************************************************************************/
package br.ufla.dcc.grubix.simulator.random;

import java.util.Random;

import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

 
/**
 * Implementation of the Mersenne Twister Random Generator, coded after
 * the 1998 paper "Mersenne Twister in Java" of B. Narasimhan
 * (citeseer.ist.psu.edu/narasimhan98mersenne.html).
 * 
 * Note: In contrast to the Java Random class this class is not
 * threadsafe.
 * 
 * @author dmeister
 *
 */
public class MersenneTwisterRandomGenerator extends RandomGenerator {
	// Period parameters
	/**
	 * The N-lag in the recurrence.
	 */
	private static final int N = 624;
	
	/**
	 * m-lag in the recurrence.
	 */
	private static final int M = 397;
	
	/**
	 * The most significant w-r bits, where w = 32
	 * is the word length and r = 31.
	 */
	private static final int UPPER_MASK = 0x80000000;
	
	/**
	 * The least significant r = 31 bits.
	 */
	private static final int LOWER_MASK = 0x7fffffff; // least significant r bits
	
	/**
	 * The tempering mask B.
	 */
	private static final int TEMPERING_MASK_B = 0x9d2c5680;
	
	/**
	 * The tempering mask C.
	 */
	private static final int TEMPERING_MASK_C = 0xefc60000;
	
	/**
	 * Here mag01[x] = x * MATRIX_A for x=0,1.
	 */
	private static final int[] MAG01 = {0x0, 0x9908b0df};
	
	/**
	 * Mask for zeroing the leading 11 bits.
	 */
	private static final int MASK_LEADING_11_BITS = 0x1FFFFF;
	
	/**
	 * Mask for zeroing the leading 18 bits.
	 */
	private static final int MASK_LEADING_18_BITS = 0x3FFF;

	/**
	 * The array for the state vector.
	 */
	private int[] mt = new int[624];
	
	/**
	 * The index into the array.
	 * mti==N+1 means mt[N] is not initialized
	 */
	private int mti = 625;
	
	/**
	 * seed.
	 * Must be set to a non-zero value.
	 */
	@ShoXParameter(defaultValue = "0", description = "seed of the random generator")
	private long seed;
	
	/**
	 * Private constructor for use in a factory only.
	 */
	@SuppressWarnings("unused")
	private MersenneTwisterRandomGenerator() {
		// do nothing since init will be called by the factoy 
	}
	
	/**
	 * Constructor outside a factory.
	 * @param seed a non-zero seed
	 */
	public MersenneTwisterRandomGenerator(long seed) {
		this.seed = seed;
		try {
			init();
		} catch (ConfigurationException e) {
			throw new IllegalArgumentException("seed");
		}
	}
	
	/**
	 * Generates a single double value from the generator.
	 * 
	 * @return double uniform-distributed double value
	 */
	@Override
	public double nextDouble() {
		int y;
		if (mti >= N) { // generate N words at one time
			int kk;
			for (kk = 0; kk < N - M; kk++) {
				y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
				mt[kk] = mt[kk + M] ^ ((y >> 1) & LOWER_MASK) ^ MAG01[(y & 0x1)];
			}
			for ( ; kk < N - 1; kk++) {
				y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
				mt[kk] = mt[kk + (M - N)] ^ ((y >> 1) & LOWER_MASK) ^ MAG01[(y & 0x1)];
			}
			y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
			mt[N - 1] = mt[M - 1] ^ ((y >> 1) & LOWER_MASK) ^ MAG01[(y & 0x1)];
			mti = 0;
		}
		y = mt[mti++];
		y ^= ((y >> 11) & MASK_LEADING_11_BITS);
		y ^= (y << 7) & TEMPERING_MASK_B;
		y ^= (y << 15) & TEMPERING_MASK_C;
		y ^= ((y >> 18) & MASK_LEADING_18_BITS);
		return ((y & 0xFFFFFFFFL) / 4294967296.0); /* reals */
	}
	
	/**
	 * Generates a single integer value from the generator.
	 * @param bound upper-bound
	 * @return integer uniform-distributed integer value.
	 */
	@Override
	public int nextInt(int bound) {
		return (int) (nextDouble() * bound);
	}
	
	/**
	 * seed of the random generator.
	 * 
	 * @return the seed
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#getSeed()
	 */
	@Override
	public long getSeed() {
		return seed;
	}

	/**
	 * returns a boolean value.
	 * 
	 * @return a random boolean value
	 * @see br.ufla.dcc.grubix.simulator.random.RandomGenerator#nextBoolean()
	 */
	@Override
	public boolean nextBoolean() {
		if (nextDouble() < 0.5) {
			return true;
		}
		return false;
	}

	/**
	 * inits the mersenne twister random generator.
	 * It checks if the seed is set the a non-zero value.
	 * 
	 * @throws ConfigurationException thrown if the seed is zero.
	 * @see br.ufla.dcc.grubix.xml.Configurable#init()
	 */
	@Override
	public void init() throws ConfigurationException {
		if (seed == 0) {
			Random random = new Random();
			
			do {
				seed = random.nextLong();
			} while (seed == 0);
		}
		
		mt[0] = (int) (seed & 0xffffffff);
		
		for (mti = 1; mti < N; mti++) {
			mt[mti] = (int) (69069 * (long) mt[mti - 1]) & 0xffffffff;
		}
	}
}

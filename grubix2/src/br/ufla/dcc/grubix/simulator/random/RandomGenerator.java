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

import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * Random generator interface.
 * @author dmeister
 *
 */
public abstract class RandomGenerator implements Configurable {

	/**
	 * Returns the next pseudorandom, uniformly distributed boolean value from this random number generator's sequence.
	 * @return random boolean
	 */
	public abstract boolean nextBoolean();

	/**
	 * Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0
	 * from this random number generator's sequence.
	 * @return random double
	 */
	public abstract double nextDouble();

	/**
	 * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the
	 * specified value (exclusive), drawn from this random number generator's sequence.
	 * @param n upper (exclusive) limit
	 * @return random int (from 0 to n - 1)
	 */
	public abstract int nextInt(int n);
	
	/**
	 * Returns the seed of the random generator.
	 * Used for logging purposed.
	 * 
	 * @return seed.
	 */
	public abstract long getSeed();
	
	/**
	 * 
	 * @return
	 * @deprecated Better use the {@link NormalRandomDistribution} class
	 */
	@Deprecated
	public double getGaussian() {
		NormalRandomDistribution normalDist = new NormalRandomDistribution(1.0);
		return normalDist.nextDouble(this);
	}
	

	/**
	 * @throws ConfigurationException 
	 * @see br.ufla.dcc.grubix.xml.Configurable#init()
	 */
	public void init() throws ConfigurationException {
		// nothing to do. 
	}
}

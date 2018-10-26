/********************************************************************************
This file is part of ShoX.
It is created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net and
the Orcos developers as defined unter http://orcos.cs.upb.de

********************************************************************************/
package br.ufla.dcc.grubix.simulator.random;

import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * Exponential random distribution.
 * 
 * @author dmeister
 *
 */
public class ExponentialDistribution extends RandomDistribution {

	/**
	 * mean of the distribution.
	 */
	@ShoXParameter(description = "mean", required = true)
	private double mean;
	
	/**
	 * Constructor for use in a factory.
	 * Cannot be public since this method assumes that init is called.
	 */
	@SuppressWarnings("unused")
	private ExponentialDistribution() {
		//nothing to do
	}
	
	/**
	 * Constructor.
	 * 
	 * @param mean mean of the distribution
	 */
	public ExponentialDistribution(double mean) {
		this.mean = mean;
		try {
			init();
		} catch (ConfigurationException e) {
			throw new IllegalArgumentException("mean");
		}
	}

	/**
	 * generates the next double value with an exponential distribution.
	 * 
	 * @return an double with an exponential distribution
	 * @param generator a base random generator
	 * @see br.ufla.dcc.grubix.simulator.random.RandomDistribution#nextDouble()
	 */
	@Override
	public double nextDouble(RandomGenerator generator) {
		return -mean * StrictMath.log(generator.nextDouble());
	}

	/**
	 * inits the distribution object.
	 * Tests if the mean is positive, otherwise it throwns an exception.
	 * 
	 * @throws ConfigurationException thrown if the mean value is not positive.
	 */
	@Override
	public void init() throws ConfigurationException {
		if (mean <= 0.0) {
			throw new ConfigurationException("mean must be positive.");
		}
	}
}

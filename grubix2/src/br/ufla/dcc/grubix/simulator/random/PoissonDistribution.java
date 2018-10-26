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
 * Poisson random distribution.
 * 
 * @author Dirk Held
 *
 */
public class PoissonDistribution extends RandomDistribution {

	// see http://rkb.home.cern.ch/rkb/AN16pp/node208.html
	
	/** intensity of the distribution. */
	@ShoXParameter(description = "lamda", required = true)
	private double lamda;
	
	/** precomputed result of 1 / intensity to be able to use a multiplication, instead of a division. */
	private double eExpNegLamda;
	
	/**
	 * Constructor for use in a factory.
	 * Cannot be public since this method assumes that init is called.
	 */
	@SuppressWarnings("unused")
	private PoissonDistribution() {
		//nothing to do
	}
	
	/**
	 * Constructor.
	 * 
	 * @param lamda intensity of the distribution
	 */
	public PoissonDistribution(double lamda) {
		this.lamda = lamda;
		
		try {
			init();
		} catch (ConfigurationException e) {
			throw new IllegalArgumentException("intensity");
		}
	}

	/**
	 * generates the next double value with a poisson distribution.
	 * Although its a double value, only integer values >= 0 are returned.
	 * 
	 * @return an double with an exponential distribution
	 * @param generator a base random generator
	 * @see br.ufla.dcc.grubix.simulator.random.RandomDistribution#nextDouble()
	 */
	@Override
	public double nextDouble(RandomGenerator generator) {
		double x = 0.0, t = 1.0;
		   
		for (;;) {
			t *= generator.nextDouble();
		      
			if (t < eExpNegLamda) {
				return x;
			}
	      
			x++;
	   }
	}

	/**
	 * inits the distribution object.
	 * Tests if the intensity is positive, otherwise it throwns an exception.
	 * 
	 * @throws ConfigurationException thrown if the mean value is not positive.
	 */
	@Override
	public void init() throws ConfigurationException {
		if (lamda <= 0.0) {
			throw new ConfigurationException("lamda must be positive.");
		}
		
		eExpNegLamda = Math.exp(-lamda); 
	}
}

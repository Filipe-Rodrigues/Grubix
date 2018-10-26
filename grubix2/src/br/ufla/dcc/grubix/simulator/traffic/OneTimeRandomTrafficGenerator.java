/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.traffic;

import java.util.Collection;
import java.util.SortedSet;

import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * This class generates random application-level network traffic. That is, for each node, it 
 * generates one traffic event at the beginning.
 * BEWARE: this class only operates correctly with certain inner random generator. you need to test the
 * correctness for your self, for example it does not operate correctly with ExponentialRandomGenerator but should
 * work correctly with RandomTraffic
 * @author Dirk Meister
 */
public class OneTimeRandomTrafficGenerator extends TrafficGenerator {
	
	/**
	 * source for randomness for the traffic generator.
	 */
	@ShoXParameter(description = "traffic generator", defaultClass = ExponentialRandomTraffic.class)
	private TrafficGenerator generator;
	
	/**
	 * flag is the generator has already been used.
	 */
	private boolean used = false;
	
	/**
	 * @param configuration configuration
	 * @throws ConfigurationException throws if the generation configuration fails
	 */
	@Override
	public void initConfiguration(Configuration configuration)
			throws ConfigurationException {
		super.initConfiguration(configuration);
		generator.initConfiguration(configuration);
	}



	/**
	 * Generates random traffic for all nodes contained in the specified collection for the coming
	 * trafficTimeInterval.
	 * @see br.ufla.dcc.grubix.simulator.kernel.Configuration#trafficTimeInterval
	 * @param allNodes Collection of nodes for which new traffic is to be generated (usually
	 * all nodes in the network)
	 * @return Sorted collection of TrafficGeneration events specifying the new traffic per node.
	 * The collection is expected to be sorted in ascending simulation time order.
	 */
	@Override
	public final SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes) {
		used = true;
		return generator.generateTraffic(allNodes);
		
	}
	
	/**
	 * Avoid generator traffic anymore.
	 */
	@Override
	public double getDelayToNextQuery() {
		if (!used) {
			return generator.getDelayToNextQuery();
		}
		return TrafficGenerator.NO_TRAFFIC_ANYMORE;
	}
}

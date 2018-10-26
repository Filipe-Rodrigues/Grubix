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
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * This class generates random application-level network traffic. That is, for each node, it 
 * waits for a random period p of time 0 < p <= currentPeriod <= maxWaitingPeriod between two
 * rounds of traffic. 
 * The constant maxWaitingPeriod is passed to this class by the ShoX configuration file
 * and is the total upper bound for all waiting periods. The variable currentPeriod 
 * depends on the current traffic percentile (amount of traffic to be generated).
 * @author jlsx
 */
public class RandomTraffic extends TrafficGenerator {
	
	/** Used to store the delay per node for the next traffic generation. */
	private HashMap<NodeId, Double> delays;
	
	/**
	 * The maximum number of simulation steps between two rounds of traffic per node.
	 */
	@ShoXParameter(description = "The maximum number of simulation steps between "
			+ "two rounds of traffic per node", required = true)
	protected double maxWaitingPeriod;
	
	/**
	 * random generator.
	 * By the default the global random generator is used.
	 */
	@ShoXParameter(description = "random generator", defaultClass = InheritRandomGenerator.class)
	private RandomGenerator random;
	
	/**
	 * This constructor is only used internally.
	 */
	public RandomTraffic() {
		this.delays = new HashMap<NodeId, Double>();
	}
	
	/**
	 * Can be invoked to directly initialize this class from within ShoX.
	 * @param maxWaitingPeriod The maximum number of simulation steps between two rounds of traffic per node
	 */
	public RandomTraffic(double maxWaitingPeriod) {
		this.maxWaitingPeriod = maxWaitingPeriod;
		this.delays = new HashMap<NodeId, Double>();
	}
	
	/**
	 * Called at the beginning of simulation to initialize traffic generator. This is useful
	 * since while the constructor is executed, the parameters are not yet set.
	 */
	public final void initConfiguration() {
		// currently nothing to do
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
	public final SortedSet<TrafficGeneration> generateTraffic(Collection<Node> allNodes) {
		Configuration configuration = Configuration.getInstance();
		TreeSet<TrafficGeneration> traffic = new TreeSet<TrafficGeneration>();
		int count = allNodes.size();
		NodeId[] ids = new NodeId[count];
		int i = 0;
		for (Node n : allNodes) {
			ids[i] = n.getId();
			i++;
		}
		
		for (Node n : allNodes) {
			double delay;
			double offset = 0.0;
			do {
				if (this.delays.get(n.getId()) != null) {
					delay = this.delays.get(n.getId()) - configuration.getTrafficTimeInterval();
				} else {
					delay = Math.round(random.nextDouble() * this.getCurrentPeriod()) + offset;
				}
				NodeId recipient;
				do {
					int randomIndex = Math.round((float) random.nextDouble() * (count - 1));
					recipient = ids[randomIndex];
				} while (recipient.equals(n.getId()));
				int packetType = Math.round((float) random.nextDouble() * (packetTypeCount - 1));
				if (delay < configuration.getTrafficTimeInterval()) {
					TrafficGeneration tg = new TrafficGeneration(n.getId(), recipient, delay, packetType);
					traffic.add(tg);
					this.delays.remove(n.getId());
					offset += delay;
				} else {
					this.delays.put(n.getId(), delay);
				}
			} while (delay < configuration.getTrafficTimeInterval());
		}
		return traffic; 
	}

	
	/**
	 * @return The current upper bound (in simulation steps) for the waiting period between two traffic
	 * generation events. Defined as (1 - trafficPercentile) * maxWaitingPeriod.
	 */
	private double getCurrentPeriod() {
		return (1 - this.trafficIntensity) * this.maxWaitingPeriod;
	}

	/**
	 * This method sets a new traffic intensity value. This is a value between 0 and 1
	 * indicating the current amount of traffic in the network. 0 means no traffic, 1 means
	 * permanent traffic (possibly overloading the network). Note that the exact semantics of
	 * these values depend on the network designer. It is just meant as an easy means to test
	 * the network design under different loads.
	 * @param params Parameters for this traffic generator which directly imply
	 * the new traffic intensity value
	 * @return The new traffic intensity value
	 */
	public double setTrafficIntensity(HashMap<String, String> params) {
		return 0;
	}
}

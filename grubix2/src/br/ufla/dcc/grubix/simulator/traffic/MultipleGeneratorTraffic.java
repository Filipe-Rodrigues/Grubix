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
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

import com.google.common.collect.Maps;


/**
 * This meta traffic generator can be used to generate traffic by more than one "real"
 * traffic generator. 
 * 
 * It is configured with a list of the generator.
 * This meta generator managed the delay times of the child generators and at each time, it calls
 * the generator with the next minimal delay time.
 * 
 * The traffic generators for the
 * individual traffic types can be implemented independently and reused within a combined
 * scenario. 
 * @author jlsx
 */
public class MultipleGeneratorTraffic extends TrafficGenerator {

	/**
	 * child generators.
	 */
	@ShoXParameter()
	private TrafficGenerator[] generators;
	
	/**
	 * flag indicating of the packet types should be overwritten.
	 */
	@ShoXParameter(defaultValue = "true")
	private boolean overwriteGeneratorPacketTypes; 
	
	/**
	 * mapping of child generator packet types to external packet types.
	 * if overwriteGeneratorPacketTypes is true, each child generator is responsible for generation one packet type
	 * if overwriteGeneratorPacketTypes is false, no type mapping is done.
	 */
	private Map<TrafficGenerator, Integer> packetTypeMapping = Maps.newLinkedHashMap();
	
	/**
	 * next check map.
	 */
	private Map<TrafficGenerator, Double> nextChecks = Maps.newLinkedHashMap();
	
	/**
	 * the last delay.
	 */
	private Map.Entry<TrafficGenerator, Double> currentDelayEntry;
	
	
	/** 
	 * Constructs a meta traffic generator. The individual sub-generators from which this
	 * meta generator draws are configured through the ShoX configuration file (not yet
	 * working!)
	 */
	public MultipleGeneratorTraffic() {
		super();
	}

	/**
	 * Generates aggregated traffic for all nodes contained in the specified 
	 * collection for the coming trafficTimeInterval. These should generally be all nodes.
	 * @see br.ufla.dcc.grubix.simulator.kernel.Configuration#trafficTimeInterval
	 * @param allNodes Collection of nodes for which new traffic is to be generated,
	 * i.e. all nodes in the network
	 * @return Sorted collection of TrafficGeneration events specifying the new traffic per node.
	 * The collection is expected to be sorted in ascending simulation time order.
	 */
	protected SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes) {
		TrafficGenerator generator = currentDelayEntry.getKey();
		double delay = currentDelayEntry.getValue();
		
		updateCheckDelay(delay);
		
		SortedSet<TrafficGeneration> traffic = generator.generateTraffic(allNodes);
		
		// rewrite traffic
		SortedSet<TrafficGeneration> externalVisibleTraffic = new TreeSet<TrafficGeneration>();
		for (TrafficGeneration trafficItem : traffic) {
			int packetType = trafficItem.getPacketType() + packetTypeMapping.get(generator);
			TrafficGeneration newItem = new TrafficGeneration(trafficItem.getSource(), 
					trafficItem.getRecipient(), 
					trafficItem.getDelay(), packetType);
			externalVisibleTraffic.add(newItem);
		}
		
		nextChecks.put(generator, generator.getDelayToNextQuery());
		currentDelayEntry = getMinimalCheckDelay();
		
		return externalVisibleTraffic;
	}

	/**
	 * The TrafficGenEnvelope of SimulationManager calls this method to determine
	 * when to check for new traffic by the TrafficGenerator.
	 *  
	 * @return -1.0 if TrafficGenEnvelope should not check again,
	 * 		   a value greater or equal to zero is a valid delay in simulation steps,
	 * 		   all other (negative) values are invalid. 
	 */
	public double getDelayToNextQuery() {
		return currentDelayEntry.getValue();
	}
	
	/**
	 * Called at the beginning of simulation to initialize traffic generator. This is useful
	 * since while the constructor is executed, the parameters are not yet set.
	 * 
	 * @param configuration configuration instance
	 * @throws ConfigurationException thrown if configuration is invalid e.g. if no generator is configured.
	 */
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		
		if (generators.length == 0) {
			throw new ConfigurationException("No valid generators");
		}
		
		if (overwriteGeneratorPacketTypes) {
			for (int i = 0; i < generators.length; i++) {
				packetTypeMapping.put(generators[i], i);
			}		
		} else {
			for (int i = 0; i < generators.length; i++) {
				packetTypeMapping.put(generators[i], 0);
			}	
		}
		
		for (TrafficGenerator generator : generators) {
			generator.initConfiguration(configuration);
			nextChecks.put(generator, generator.getDelayToNextQuery());
		}
		currentDelayEntry = getMinimalCheckDelay();
	}

	/**
	 * update the next check dalay of all traffic generator.
	 * 
	 * @param delay delay to substract.
	 */
	private void updateCheckDelay(double delay) {
		for (TrafficGenerator generator : generators) {
			double nextCheck = nextChecks.get(generator);
			nextCheck -= delay;
			
			// can only happen by rounding errors
			if (nextCheck < 0) {
				nextCheck = 0;
			}
			nextChecks.put(generator, nextCheck);
		}
	}
	
	/**
	 * returns that map entry that has the minimal next check delay.
	 * 
	 * @return map entry that has the minimal next check delay
	 */
	private Map.Entry<TrafficGenerator, Double> getMinimalCheckDelay() {
		Map.Entry<TrafficGenerator, Double> minimal = null;
		for (Map.Entry<TrafficGenerator, Double> entry : nextChecks.entrySet()) {
			if (minimal == null
					|| (rewriteNoTrafficAnymoreValue(entry.getValue())
						< rewriteNoTrafficAnymoreValue(minimal.getValue()))) {
				minimal = entry;
			}
		}
		return minimal;
	}
	
	/**
	 * rewrits the NO_TRAFFIC_ANYMORE constant so that it is larger than any other normal value.
	 * 
	 * @param value delay value
	 * @return rewritten delay value
	 */
	public double rewriteNoTrafficAnymoreValue(double value) {
		if (value == TrafficGenerator.NO_TRAFFIC_ANYMORE) {
			return Double.MAX_VALUE;
		}
		return value;
	}

}

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

package br.ufla.dcc.grubix.simulator.traffic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * Provides a more simplified API to create traffic events. It reduces
 * the code that is needed to implement specific traffic generation
 * strategies.
 * 
 * This class should be subclassed. Subclasses must
 * implement the new generateTraffic method and submit new traffic
 * events by calling the submit method.
 *
 * @see ExponentialRandomTraffic
 * @author dmeister
 *
 */
public abstract class SimplifiedTrafficGenerator extends TrafficGenerator {
	
	/**
	 * temporary set of traffic during the creation.
	 * Is overwritten during next call of generateTraffic.
	 */
	private TreeSet<TrafficGeneration> traffic = null;

	/**
	 * flag the the generator has already been used.
	 */
	private boolean used = false;
	
	/**
	 * list of all node ids.
	 */
	private List<NodeId> nodeIdList;
	
	/**
	 * generation interval as part of the traffic generator.
	 */
	@ShoXParameter(description = "size of traffic generating batches in seconds", defaultValue="0")
	private double generationInterval;
	
	/**
	 * map containing the last traffic delays of the nodes.
	 */
	private Map<NodeId, Double> lastTrafficDelay = new HashMap<NodeId, Double>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes) {
		double intervalInSteps = getConfig().getSimulationSteps(generationInterval);
		if (generationInterval == 0.0) {
			intervalInSteps = getConfig().getSimulationTime();
		}
		traffic = new TreeSet<TrafficGeneration>();
		
		// cache node id list
		if (nodeIdList == null) {
			nodeIdList = new ArrayList<NodeId>(allNodes.size());
			for (Node node : allNodes) {
				nodeIdList.add(node.getId());
				if (!lastTrafficDelay.containsKey(node.getId())) {
					lastTrafficDelay.put(node.getId(), 0.0);
				}
			}
		}
		
		for (NodeId node : nodeIdList) {
			final double lastTime = lastTrafficDelay.get(node);
			final double currentTime = SimulationManager.getInstance().getCurrentTime();
			double currentDelayTime = lastTime - currentTime;
			if (currentDelayTime < 0) {
				currentDelayTime = 0;
			}
			while (currentDelayTime < intervalInSteps) {
				TrafficGeneration tg = generateNextTraffic(node, currentDelayTime);
				traffic.add(tg);
				currentDelayTime += tg.getDelay();
			}
			lastTrafficDelay.put(node, currentDelayTime);
		}
		used = true;
		return traffic;
	}
	
	/**
	 * template method to fill be subclasses.
	 * 
	 * @param nodeId source node
	 * @param lastArrival last arrival time
	 * @return a new traffic generation event
	 */
	public abstract TrafficGeneration generateNextTraffic(NodeId nodeId, double lastArrival);

	/**
	 * returns a unmodifiable list of node ids.
	 * @return a unmodifiable list of node ids.
	 */
	public List<NodeId> getNodeIdList() {
		return Collections.unmodifiableList(nodeIdList);
	}

	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator#getDelayToNextQuery()
	 */
	@Override
	public double getDelayToNextQuery() {
		if (!used) {
			return 0;
		}
		return getConfig().getSimulationSteps(generationInterval);
	}

}

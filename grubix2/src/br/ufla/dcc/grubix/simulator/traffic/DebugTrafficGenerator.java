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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomDistribution;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomUtil;
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
public class DebugTrafficGenerator extends TrafficGenerator {
	
	/**
	 * random distribution of interarrival times.
	 */
	@ShoXParameter(description = "distribution of interarrival times")
	private RandomDistribution distribution;
	
	
	/**
	 * random generator.
	 */
	@ShoXParameter(description = "random generator", defaultClass = InheritRandomGenerator.class)
	private RandomGenerator random;

	private double lastTrafficEvent = 0;
	
	@Override
	protected SortedSet<TrafficGeneration> generateTraffic(
			Collection<Node> allNodes) {
		TreeSet<TrafficGeneration> traffic = new TreeSet<TrafficGeneration>();
		
		// cache node id list
			ArrayList<NodeId> nodeIdList = new ArrayList<NodeId>(allNodes.size());
			for (Node node : allNodes) {
				nodeIdList.add(node.getId());
			}
			
		RandomUtil.shuffle(nodeIdList, random);
		
		double currentTime = 0;
		for (NodeId node : nodeIdList) {
				double delay = distribution.nextDouble(random);
				TrafficGeneration tg
					= new TrafficGeneration(node, generateReceiver(nodeIdList), delay, generatePacketType());
				traffic.add(tg);
				currentTime += tg.getDelay();
		}
		lastTrafficEvent = currentTime;
		return traffic;
	}
	

	/**
	 * get a new (uniform and independent of the sender) random node id as receiver.
	 * 
	 * @param nodeList list of all node ids
	 * @return a node id as receiver
	 */
	private NodeId generateReceiver(List<NodeId> nodeList) {
		int nodeIndex = random.nextInt(nodeList.size());
		return nodeList.get(nodeIndex);
	}

	/**
	 * gets a uniform and independent random packet type.
	 * @return a random packet type.
	 */
	private int generatePacketType() {
		return random.nextInt(packetTypeCount);
	}


	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator#getDelayToNextQuery()
	 */
	@Override
	public double getDelayToNextQuery() {
		return lastTrafficEvent;
	}
}

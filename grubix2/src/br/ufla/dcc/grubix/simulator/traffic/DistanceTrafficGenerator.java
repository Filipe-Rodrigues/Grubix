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

import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.util.Pair;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * Generates traffic only between nodes whose distance is greater or equal than a specified threshold.
 * 
 * @author Thomas Kemmerich
 */
public class DistanceTrafficGenerator extends TrafficGenerator {
	
	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(DistanceTrafficGenerator.class.getName());
	
	/** Total number of generated traffic. */
	private static int generated = 0;

	/** The distance threshold. */
	@ShoXParameter(description = "Distance threshold", defaultValue = "30.0")
	private double distanceThreshold;
	
	/** The traffic generation interval. */
	@ShoXParameter(description = "Traffic generation interval", defaultValue = "10000.0")
	private double trafficGenerationInterval;

	/** Maximum number of generated traffic generation events (=max number of initiated packets). */
	@ShoXParameter(description = "Maximum number of generated traffic generation events (=max number of initiated packets).", defaultValue = "30")
	private int maxTraffic;
	
	/** Minimum number of generated traffic generation events (=min number of initiated packets). */
	@ShoXParameter(description = "Minimum number of generated traffic generation events"
			+ " (=min number of initiated packets).", defaultValue = "30")
	private int minTraffic;
	
	/** Start the generation of traffic at this time. */
	@ShoXParameter(description = "Start the generation of traffic at this time.", defaultValue = "100000.0")
	private double generateTrafficBegin;
	
	/** End the generation of traffic at (max. simulationtime in simulationsteps - this value). */
	@ShoXParameter(description = "End the generation of traffic at (max. simulationtime in simulationsteps"
			+ " - this value).", defaultValue = "100000.0")
	private double generateTrafficEnd;

	/** The allowed communication partners. */
	private static LinkedList<Pair<NodeId, NodeId>> allowedCommunicationPartners = null;
		
	/** Flag. */
	private boolean firstCall = true;
	

	/**
	 * Calculates the euclidean distance between two {@link Position}s.
	 * 
	 * @param position1
	 *            the first position
	 * @param position2
	 *            the second position
	 * @return the distance between position1 and position2.
	 */
	private double getDistanceBetweenNodes(Position position1, Position position2) {
		return Math.sqrt(
					Math.pow((position2.getXCoord() - position1.getXCoord()), 2)
				+ 	Math.pow((position2.getYCoord() - position1.getYCoord()), 2)
				);
	}

	/**
	 * Creates a set of nodes that are allowed to communicate with respect to the {@link this#distanceThreshold}.
	 */
	private void createAllowedCommunicationPartners() {
		DistanceTrafficGenerator.allowedCommunicationPartners = new LinkedList<Pair<NodeId, NodeId>>();
		for (Node sender : SimulationManager.getAllNodes().values()) {
			for (Node receiver : SimulationManager.getAllNodes().values()) {
				double distance = getDistanceBetweenNodes(sender.getPosition(), receiver.getPosition());
				if (distance >= distanceThreshold) {
					DistanceTrafficGenerator.allowedCommunicationPartners
					.add(new Pair<NodeId, NodeId>(sender.getId(), receiver.getId()));
				}				
			}
		}	
	}
	
	/**
	 * Generates traffic only between allowed communication partners and asures that
	 * the number of generated traffic x is in [minTraffic, maxTraffic].
	 * @param allNodes All nodes.
	 * @return The traffic.
	 * @see br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator#generateTraffic(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected SortedSet<TrafficGeneration> generateTraffic(Collection<Node> allNodes) {
		LinkedList<TrafficGeneration> traffic = new LinkedList<TrafficGeneration>();
		
		if (firstCall) {
			return new TreeSet<TrafficGeneration>();
		}
		
		// generate allowed communication partners
		if (DistanceTrafficGenerator.allowedCommunicationPartners == null) {
			createAllowedCommunicationPartners();
		}
		
		// determine how many traffic is to be generated
		int border = maxTraffic - minTraffic;
		int count;
		if (border == 0) {
			count = minTraffic;
		} else {
			 count = Configuration.getInstance().getRandomGenerator().nextInt(border) + minTraffic;
		}
		
		 
		// create the traffic
		for (int i = 0; i < count; i++) {
			int index = Configuration.getInstance().getRandomGenerator().nextInt(allowedCommunicationPartners.size());
			Pair<NodeId, NodeId> pair = allowedCommunicationPartners.get(index);
			TrafficGeneration tg = new TrafficGeneration(pair.first, pair.second, 
					Configuration.getInstance().getRandomGenerator().nextDouble() * this.trafficGenerationInterval,
					0);
			traffic.add(tg);
		}
		TreeSet<TrafficGeneration> t = new TreeSet<TrafficGeneration>(traffic);
		generated += t.size();
		LOGGER.debug(SimulationManager.getInstance().getCurrentTime() 
				+ ": Total generated traffic so far = " + generated);
		return t;
	}
	
	/**
	 * Assures that traffic is generated only between {@link this#generateTrafficBegin} 
	 * and {@link this#generateTrafficEnd}. Furthermore traffic is generated in the
	 * interval {@link this#trafficGenerationInterval}.
	 * @return the next time to be asked for new traffic.
	 */
	@Override
	public double getDelayToNextQuery() {
		if (firstCall) {
			firstCall = false;
			return generateTrafficBegin;
		}
	
		double currentTime = SimulationManager.getInstance().getCurrentTime();
		if (currentTime + this.generateTrafficEnd - 2 // -2 is for adjustment, since simulation starts at time '2.0' 
			>= Configuration.getInstance().getSimulationTime()) {
			return TrafficGenerator.NO_TRAFFIC_ANYMORE;
		}
		return trafficGenerationInterval;
	}


	/**
	 * gets generated.
	 * @return current generated value.
	 */
	public static int getGenerated() {
		return generated;
	}


	/**
	 * gets distanceThreshold.
	 * @return current distanceThreshold
	 */
	public double getDistanceThreshold() {
		return distanceThreshold;
	}


	/**
	 * gets trafficGenerationInterval.
	 * @return current trafficGenerationInterval
	 */
	public double getTrafficGenerationInterval() {
		return trafficGenerationInterval;
	}


	/**
	 * gets maxTraffic.
	 * @return current maxTraffic
	 */
	public int getMaxTraffic() {
		return maxTraffic;
	}


	/**
	 * gets minTraffic.
	 * @return current minTraffic
	 */
	public int getMinTraffic() {
		return minTraffic;
	}

}

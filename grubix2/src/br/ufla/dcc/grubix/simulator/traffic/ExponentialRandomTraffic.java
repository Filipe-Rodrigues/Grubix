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

import java.util.List;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.random.ExponentialDistribution;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * Uses a traffic generator with
 * - an exponential interarrival time distribution
 * - an uniform distribution of the packet types
 * and an uniform distribution of the packet recipients.
 * 
 * It used its on random generator.
 * 
 * @author dmeister
 *
 */
public class ExponentialRandomTraffic extends SimplifiedTrafficGenerator {
	/**
	 * mean parameter of the exponential distribution.
	 */
	@ShoXParameter(description = "mean", required = true)
	private double mean;
	
	@ShoXParameter(description = "time type (steps, seconds)", defaultValue="steps")
	private String timeType;

	/**
	 * random generator.
	 */
	@ShoXParameter(description = "random generator", defaultClass = InheritRandomGenerator.class)
	private RandomGenerator random;

	/**
	 * distribution of the interarrival times of packets.
	 */
	private ExponentialDistribution distribution;

	/**
	 * inits the traffic generator.
	 * 
	 * @throws ConfigurationException throws if distribution mean is invalid
	 * @see br.ufla.dcc.grubix.simulator.traffic.SimplifiedTrafficGenerator#initialize()
	 */
	@Override
	public void init() throws ConfigurationException {
		super.init();
		if (!timeType.equals("steps") && !timeType.equals("seconds")) {
			throw new ConfigurationException("Invalid time type");
		}
	}
	
	/**
	 * inits the traffic generator (using configuraiton values).
	 * 
	 * @see br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator#initConfiguration(br.ufla.dcc.grubix.simulator.kernel.Configuration)
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		
		double stepMean = mean; // if steps
		if (timeType.equals("seconds")) {
			stepMean = mean * configuration.getStepsPerSecond();
		}
		
		distribution = new ExponentialDistribution(stepMean);
	}
	/**
	 * generates traffic for the nodeId.
	 * 
	 * @param nodeId nodeId
	 * @param lastArrival time of the last arrival.
	 * @return a new traffic generation event
	 * @see br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator#generateTraffic(java.util.Collection)
	 */
	@Override
	public TrafficGeneration generateNextTraffic(NodeId nodeId, double lastArrival) {
		double interarrivalTime = distribution.nextDouble(random);
		double delay = lastArrival + interarrivalTime;
		int packetType = generatePacketType();
		NodeId receiver = generateReceiver(getNodeIdList());
		
		TrafficGeneration tg
			= new TrafficGeneration(nodeId, receiver, delay, packetType);
		return tg;
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


}

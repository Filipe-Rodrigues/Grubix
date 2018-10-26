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

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.simulator.traffic.RandomTraffic;
import br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * This class models traffic produced by phone conversations. A phone conversation is a
 * set of voice streams of length (0...maxContributionLength) where the two involved nodes take
 * turns in sending and receiving. In the current form, the voice streams do not overlap,
 * i.e. both parties "talk" strictly sequentially, they do not "cut each other off". For
 * this traffic generator to work properly, {@link br.ufla.dcc.grubix.simulator.kernel.Configuration.trafficTimeInterval}
 * must be a divisor of the number of simulation steps corresponding to VOICE_SAMPLE_PERIOD (e.g. 25ms).
 * @author jlsx
 */
public class PhoneConversationsTraffic extends TrafficGenerator {

	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(PhoneConversationsTraffic.class.getName());

	/** The voice sample period, i.e. every VOICE_SAMPLE_PERIOD seconds, a sample must be generated. */ 
	public static final double VOICE_SAMPLE_PERIOD = 0.025;
	
	/** 
	 * The minimum / maximum number of contributions per call, 
	 * i.e. alternations in the sender - receiver constellation. 
	 */
	private static final int minContributionCount = 10, maxContributionCount = 100;
	
	/** The minimum / maximum number of simulation steps for a single contribution within a call. */
	private static double minContributionLength, maxContributionLength;
	
	/** The number of conversations taking place at any given time. */
	private int conversationsPerTime;
	
	/** The currently running phone conversations. */ 
	private ArrayList<PhoneConversation> runningConversations;
	
	/** List of nodes which are currently (not) in a conversation. */
	private ArrayList<NodeId> conversatingNodes, nonConversatingNodes;
	
	/**
	 * random generator.
	 * By the default the global random generator is used.
	 */
	@ShoXParameter(description = "random generator", defaultClass = InheritRandomGenerator.class)
	private RandomGenerator random;
	
	/** Reference to the random traffic generator which is used for the non-RT traffic. */
	private RandomTraffic randomTraffic;
	
	/** Constructor of this class. */
	public PhoneConversationsTraffic() {
		this.runningConversations = new ArrayList<PhoneConversation>();
		this.conversatingNodes = new ArrayList<NodeId>(); 
	}

	/**
	 * Generates phone conversation traffic for all nodes contained in the specified 
	 * collection for the coming trafficTimeInterval.
	 * @see br.ufla.dcc.grubix.simulator.kernel.Configuration#trafficTimeInterval
	 * @param allNodes Collection of nodes for which new traffic is to be generated,
	 * i.e. all nodes in the network
	 * @return Sorted collection of TrafficGeneration events specifying the new traffic per node.
	 * The collection is expected to be sorted in ascending simulation time order.
	 */
	protected SortedSet<TrafficGeneration> generateTraffic(Collection<Node> allNodes) {
		if (this.nonConversatingNodes == null) {
			// we initialize nonConversatingNodes with allNodes for the first time
			this.nonConversatingNodes = new ArrayList<NodeId>();
			for (Node n : allNodes) {
				this.nonConversatingNodes.add(n.getId());
			}
		}
		
		SortedSet<TrafficGeneration> tgs = new TreeSet<TrafficGeneration>();
		ArrayList<PhoneConversation> finishedCalls = new ArrayList<PhoneConversation>();
		double currentTime = SimulationManager.getInstance().getCurrentTime();
		
		// first, we generate samples for existing / running calls
		for (PhoneConversation pc : this.runningConversations) {
			pc.updateGenerationRound();
			if (pc.isSampleDue()) {
				TrafficGeneration t = new TrafficGeneration(pc.getCurrentSender(), 
						pc.getCurrentReceiver(), pc.getDelay(), 0);
				tgs.add(t);
			} else if (pc.isContributionFinished(currentTime)) {
				if (pc.getPerformedContributions() >= pc.getContributionCount()) {
					// the conversation is done, remove it and its two nodes from the lists
					finishedCalls.add(pc);
				} else {
					// the previous listener is now talking
					pc.addContribution(currentTime, random.nextDouble() * maxContributionLength);
				}
			}
		}
		
		// update the lists according to finished calls
		for (PhoneConversation pc : finishedCalls) {
			this.runningConversations.remove(pc);
			this.conversatingNodes.remove(pc.getCurrentSender());
			this.conversatingNodes.remove(pc.getCurrentReceiver());
			this.nonConversatingNodes.add(pc.getCurrentSender());
			this.nonConversatingNodes.add(pc.getCurrentReceiver());
		}
		
		// generate new conversations
		while (this.conversationsPerTime > this.runningConversations.size()) {
			int l = this.nonConversatingNodes.size();
			int n1 = this.random.nextInt(l);
			NodeId nid1 = this.nonConversatingNodes.remove(n1);
			int n2 = this.random.nextInt(l - 1);
			NodeId nid2 = this.nonConversatingNodes.remove(n2);
			int contributionCount = this.random.nextInt(maxContributionCount - minContributionCount) 
				+ minContributionCount;
			double delay = this.random.nextDouble() * Configuration.getInstance().getTrafficTimeInterval();
			PhoneConversation pc = new PhoneConversation(nid1, nid2, contributionCount, delay);
			double length = this.random.nextDouble() * (maxContributionLength - minContributionLength) 
				+ minContributionLength;
			pc.addContribution(currentTime, length);
			
			this.conversatingNodes.add(nid1);
			this.conversatingNodes.add(nid2);
			this.runningConversations.add(pc);
			
			LOGGER.info("Added conversation between node " + nid1 + " and " + nid2 
					+ " with contribution count " + contributionCount + " and delay " + delay);
		}
		
		tgs.addAll(this.generateNonRTTraffic(allNodes));
		return tgs;
	}

	/**
	 * Method to generate some additional non-RT traffic.
	 * @param allNodes All nodes in the network
	 * @return List of traffic generation events for the current round
	 */
	private SortedSet<TrafficGeneration> generateNonRTTraffic(Collection<Node> allNodes) {
		return this.randomTraffic.generateTraffic(allNodes);
	}
	
	/**
	 * Called at the beginning of simulation to initialize traffic generator. This is useful
	 * since while the constructor is executed, the parameters are not yet set.
	 */
	public void initConfiguration() {
		Configuration configuration = Configuration.getInstance();
		int maxConversations = (int) Math.floor(configuration.getNodeCount() / 2);
		this.conversationsPerTime = (int) Math.floor(this.trafficIntensity * maxConversations);
		minContributionLength = configuration.getSimulationSteps(1);
		maxContributionLength = configuration.getSimulationSteps(15);
		this.randomTraffic = new RandomTraffic(configuration.getSimulationSteps(8));
	}

}

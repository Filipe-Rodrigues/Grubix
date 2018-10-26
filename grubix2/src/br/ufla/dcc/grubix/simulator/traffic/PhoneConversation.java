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

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

/**
 * This class represents a single phone conversation between two nodes.
 * @author jlsx
 */
public class PhoneConversation {

	/** 
	 * The number of traffic generation rounds corresponding to 
	 * {@link PhoneConversationsTraffic#VOICE_SAMPLE_PERIOD}. 
	 */
	private int sampleRoundCount;
	
	/** The IDs of the two nodes having this conversation. */
	private NodeId node1, node2;
	
	/** 
	 * The number of alternations of the sender-receiver constellation, i.e. how often
	 * the two conversating parties take turns in talking.
	 */
	private int contributionCount;
	
	/** 
	 * A number of simulation steps that is always added for sample generation
	 * (to diversify sample generation times).
	 */
	private double delay;
	
	/** The number of contributions that have already occured during the conversation. */
	private int performedContributions;
	
	/** The ID of the node which is currently talking (contributing). */
	private NodeId currentSender;
	
	/** The start simulation time and duration of the current contribution. */
	private double currentStartTime, currentDuration;
	
	/** Counts the number of traffic generation rounds since the start of the current contribution. */
	private int currentGenerationRound;
	
	/**
	 * Constructs a PhoneConversation object.
	 * @param node1 One of the two conversating nodes
	 * @param node2 The other conversating node
	 * @param contributionCount The number of alternations of the sender-receiver constellation, i.e. how often
	 * the two conversating parties take turns in talking.
	 * @param delay A number of simulation steps that is always added for sample generation
	 * (to diversify sample generation times)
	 */
	public PhoneConversation(NodeId node1, NodeId node2, int contributionCount, double delay) {
		this.sampleRoundCount = (int) 
			Math.floor(Configuration.getInstance().getSimulationSteps(
				PhoneConversationsTraffic.VOICE_SAMPLE_PERIOD) 
				/  Configuration.getInstance().getTrafficTimeInterval());
		this.node1 = node1;
		this.node2 = node2;
		this.contributionCount = contributionCount;
		this.delay = delay;
		this.performedContributions = 0;
		this.currentSender = node2;
	}
	
	/**
	 * @return The number of alternations of the sender-receiver constellation, i.e. 
	 * how often the two conversating parties take turns in talking.
	 */
	public int getContributionCount() {
		return this.contributionCount;
	}
	
	/**
	 * @return The number of simulation steps that is always added for sample generation
	 * (to diversify sample generation times)
	 */
	public double getDelay() {
		return this.delay;
	}
	
	/** 
	 * Adds a contribution of the given duration to this conversation. The first contribution
	 * is always made by node1, after that node1 and node2 take turns.
	 * @param currentTime The current simulation time
	 * @param duration The number of simulation steps which this contribution is to last
	 */ 
	public void addContribution(double currentTime, double duration) {
		this.performedContributions++;
		this.currentGenerationRound = -1;
		this.currentStartTime = currentTime;
		this.currentDuration = duration;
		
		// flip sender - receiver constellation
		if (this.currentSender.equals(this.node1)) {
			this.currentSender = this.node2;
		} else {
			this.currentSender = this.node1;
		}
	}
	
	/**
	 * @return The number of contributions that have already occured during the conversation.
	 */
	public int getPerformedContributions() {
		return this.performedContributions;
	}
	
	/**
	 * @return The ID of the node which is currently talking (contributing).
	 */
	public NodeId getCurrentSender() {
		return this.currentSender;
	}
	
	/**
	 * @return The ID of the node which is currently listening (not contributing).
	 */
	public NodeId getCurrentReceiver() {
		if (this.currentSender.equals(this.node1)) {
			return this.node2;
		} 
		return this.node1;
	} 
	
	/**
	 * Checks whether the current contribution's time is up and a new one should be started
	 * or the conversation should be finished.
	 * @param currentTime The current simulation time
	 * @return True, if the current contribution is finished, false otherwise
	 */
	public boolean isContributionFinished(double currentTime) {
		return (this.currentStartTime + this.currentDuration <= currentTime);
	}
	
	/** 
	 * Increases the currentGenerationRound counter by one. Called in each traffic
	 * generation round of the PhoneConversationsTraffic generator to allow this conversation
	 * to calculate whether in the current round a new sample must be generated. 
	 */
	public void updateGenerationRound() {
		this.currentGenerationRound++;
	}
	
	/**
	 * A new sample is due (to be generated) when 20ms have passed since the generation
	 * of the previous sample.
	 * @return True, if a new sample must be generated, false otherwise
	 */
	public boolean isSampleDue() {
		return (this.currentGenerationRound % this.sampleRoundCount == 0);
	}
}

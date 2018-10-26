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

Copyright 2008 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.nodestartup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.simulator.util.Pair;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * This node startup manager starts all nodes randomly in a specified
 * time interval (lower bound including, upper bound excluding).
 * 
 * @author Florian Rittmeier
 */
public class RandomInRangeNodeStartupManager extends NodeStartupManager {

	/**
	 * The time in simulation seconds which is the lower bound of the
	 * time interval in which the nodes are started. 
	 */
	@ShoXParameter (required = true)
	private double startTimeInSeconds;

	/**
	 * The time in simulation steps which is the lower bound of the
	 * time interval in which the nodes are started.
	 * 
	 * This field is computed of the field startTimeInSeconds.
	 */
	private double startTimeInSteps;

	/**
	 * The time in simulation seconds which is the upper bound of the
	 * time interval in which the nodes are started. 
	 */	
	@ShoXParameter (required = true)
	private double endTimeInSeconds;
	
	/**
	 * The time in simulation seconds which is the upper bound of the
	 * time interval in which the nodes are started.
	 * 
	 * This field is computed of the field endTimeInSeconds.
	 */		
	private double endTimeInSteps;
	
	/**
	 * Used to manage nodes with node start times.
	 */
	private List<Pair<Double, NodeId>> nodeStartupTimes; 
	
	/**
	 * Constructs a new RandomInRangeNodeStartupMananger.
	 */
	public RandomInRangeNodeStartupManager() {
		this.nodeStartupTimes = new ArrayList<Pair<Double, NodeId>>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws ConfigurationException {
		super.init();
		
		/*
		 * Multiple checks for valid configuration parameters
		 */
		if (this.startTimeInSeconds >= this.endTimeInSeconds) {
			throw new ConfigurationException("end time must be after start time");
		}
		if (this.startTimeInSeconds < 0.0) {
			throw new ConfigurationException("start time cannot be negative");
		}
		if (this.startTimeInSeconds == 0.0) {
			throw new ConfigurationException("start time cannot be zero as"
					+ " this would yield to simulation step 0.0 which is"
					+ " before the first query to the NodeStartManager");
		}
	}

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public void initConfiguration(Configuration config)
			throws ConfigurationException {
		super.initConfiguration(config);
		
		if (config.getSimulationSteps(this.startTimeInSeconds) < 1.0) {
			throw new ConfigurationException("setting of simulation steps per "
					+ "second and start time yield to start time beeing before"
					+ " simulation step 1.0 which is before the first query to"
					+ " the NodeStartManager");
		}
		
		this.startTimeInSteps = config.getSimulationSteps(this.startTimeInSeconds);
		this.endTimeInSteps = config.getSimulationSteps(this.endTimeInSeconds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<NodeId> getStartupNodes() {
		
		double currentTime = SimulationManager.getInstance().getCurrentTime();
		
		if (currentTime < this.startTimeInSteps) {
			return new ArrayList<NodeId>();
		}
		
		if (this.nodeStartupTimes.size() > 0) {
			Collection<NodeId> nextNodes = new ArrayList<NodeId>();
			
			while (this.nodeStartupTimes.size() > 0) {
				Pair<Double, NodeId> nextNode = this.nodeStartupTimes.get(0);
				
				if (nextNode.first < currentTime) {
					throw new IllegalStateException("found node which had to "
							+ "be started earlier");
				} else if (nextNode.first == currentTime) {
					// remove from own list
					this.nodeStartupTimes.remove(0);
					// save for return value
					nextNodes.add(nextNode.second);					
				} else {
					return nextNodes;
				}
			}
			
			return nextNodes;
		} else {
			return new ArrayList<NodeId>();
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDelayToNextQuery() {
		double currentTime = SimulationManager.getInstance().getCurrentTime();
		
		if (this.nodeStartupTimes.size() > 0) {
			double delay = this.nodeStartupTimes.get(0).first - currentTime;
			
			if (delay < 0.0) {
				throw new IllegalStateException("requested time for next query"
						+ " has passed already");
			}
	
			return delay;
		} else {
			// every node started
			return -1.0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAvailableNodes(Set<NodeId> availableNodes) {		
		RandomGenerator random =
			Configuration.getInstance().getRandomGenerator();
		
		for (NodeId nodeId : availableNodes) {
			double multiplier = this.endTimeInSteps - this.startTimeInSteps;
			double nodeStartTime = this.startTimeInSteps + multiplier * random.nextDouble();
			this.nodeStartupTimes.add(new Pair<Double, NodeId>(nodeStartTime, nodeId));
		}
		
		// sort by startup time
		Comparator<Pair<Double, NodeId>> comp =
			new Comparator<Pair<Double, NodeId>>() {
				public int compare(Pair<Double, NodeId> o1,
						Pair<Double, NodeId> o2) {
					return o1.first.compareTo(o2.first);
				}
		};		
		java.util.Collections.sort(this.nodeStartupTimes, comp);
	}
}

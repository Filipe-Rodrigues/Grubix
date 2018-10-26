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
import java.util.List;
import java.util.Set;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * This node startup manager starts all nodes node by node with a
 * fixed step length beginning from a given start time.
 * The nodes are started in order, first node 1, then node 2, ...
 * at last node n. 
 * 
 * @author Florian Rittmeier
 */
public class FixedStepNodeStartupManager extends NodeStartupManager {

	/**
	 * The time in simulation seconds which is the time the
	 * FixedStepNodeStartupManager starts starting the nodes node by
	 * node.  
	 */
	@ShoXParameter (required = true)
	private double startTimeInSeconds;

	/**
	 * The time in simulation steps which is the time the
	 * FixedStepNodeStartupManager starts starting the nodes node by
	 * node.  
	 * 
	 * This field is computed of the field startTimeInSeconds.
	 */
	private double startTimeInSteps;

	/**
	 * The time in simulation seconds which is the time the
	 * FixedStepNodeStartupManager starts starting the nodes node by
	 * node.  
	 */
	@ShoXParameter (required = true)
	private double stepLengthInSeconds;

	/**
	 * The time in simulation steps which is the time the
	 * FixedStepNodeStartupManager starts starting the nodes node by
	 * node.
	 *   
	 * This field is computed of the field stepLengthInSeconds.
	 */
	private double stepLengthInSteps;
	
	/**
	 * The remaining not yet started nodes.
	 * <br><br>
	 * The list is sorted after initialization. The sort uses ascending order,
	 * according to the natural ordering of {@link NodeId}s. 
	 */
	private List<NodeId> remainingNodes;
	
	/**
	 * Constructs a FixedStepNodeStartupManager.
	 */
	public FixedStepNodeStartupManager() {
		this.remainingNodes = new ArrayList<NodeId>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws ConfigurationException {
		super.init();
		
		if (this.startTimeInSeconds < 0.0) {
			throw new ConfigurationException("start time cannot be negative");
		}
		if (this.startTimeInSeconds == 0.0) {
			throw new ConfigurationException("start time cannot be zero as"
					+ " this would yield to simulation step 0.0 which is"
					+ " before the first query to the NodeStartManager");
		}
		if (this.stepLengthInSeconds <= 0.0) {
			throw new ConfigurationException("step length must be positive");
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
		
		this.startTimeInSteps = config.getSimulationSteps(
				this.startTimeInSeconds);
		this.stepLengthInSteps = config.getSimulationSteps(
				this.stepLengthInSeconds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<NodeId> getStartupNodes() {
		double currentTime = SimulationManager.getInstance().getCurrentTime();
		
		if (currentTime < this.startTimeInSteps) {
			return new ArrayList<NodeId>();
		} else if (this.remainingNodes.size() > 0) {
			ArrayList<NodeId> nextNode = new ArrayList<NodeId>();
			nextNode.add(this.remainingNodes.remove(0));
			return nextNode;
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
		
		if (currentTime < this.startTimeInSteps) {
			return this.startTimeInSteps - currentTime;
		} else if (this.remainingNodes.size() > 0) {
			return this.stepLengthInSteps;
		} else {
			return -1.0;
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAvailableNodes(Set<NodeId> availableNodes) {
		this.remainingNodes.addAll(availableNodes);
		java.util.Collections.sort(this.remainingNodes);		
	}

}

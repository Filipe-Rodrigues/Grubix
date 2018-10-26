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
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;




/**
 * This NodeStartupManager imitates the behavior of Shox before the
 * introduction of the NodeStartupManager concept.
 * 
 * All nodes are started at simulation time 2.0 (in simulation steps).
 * The first node started is node 1, second node started is node 2,
 * nth node started is node n. 
 * 
 * @author Florian Rittmeier
 */
public class ShoxClassicNodeStartupManager extends NodeStartupManager {

	/**
	 * The remaining not yet started nodes.
	 * <br><br>
	 * The list is sorted after initialization. The sort uses ascending order,
	 * according to the natural ordering of {@link NodeId}s. 
	 */
	private List<NodeId> remainingNodes;

	/**
	 * Constructs a ShoxClassicNodeStartupManager. 
	 */
	public ShoxClassicNodeStartupManager() {
		this.remainingNodes = new ArrayList<NodeId>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<NodeId> getStartupNodes() {
		if (SimulationManager.getInstance().getCurrentTime() == 2.0) {
			ArrayList<NodeId> allNodes = new ArrayList<NodeId>(this.remainingNodes);
			this.remainingNodes.clear();
			
			return allNodes;
		} else {
			return new ArrayList<NodeId>();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDelayToNextQuery() {
		/**
		 * We only want to be queried at simulation time 2.0
		 */
		double currentTime = SimulationManager.getInstance().getCurrentTime(); 
		if (currentTime < 2.0) {
			return 2.0 - currentTime;
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

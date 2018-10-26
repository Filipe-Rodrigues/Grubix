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

import java.util.Collection;
import java.util.Set;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;



/**
 * @author Florian Rittmeier
 *
 */
public abstract class NodeStartupManager implements Configurable {

	/**
	 * method to initialize the configuration of the object.
	 * It is assured by the runtime system that the <code>Configuration</code>
	 * is valid, when this method is called.
	 * 
	 * @param config configuration of the simulation run
	 * @throws ConfigurationException thrown when the object cannot run with
	 * the configured values.
	 */
	public void initConfiguration(Configuration config) throws ConfigurationException {
		// do nothing
	}

	
	/**
	 * Called by the ConfigurableFactory after setting the configured
	 * parameter.
	 * @throws ConfigurationException thrown if configuration is invalid.
	 */
	public void init() throws ConfigurationException {
		// nothing to do
	}
	
	
	/**
	 * Called by the NodeStartupManagerEnvelope in the
	 * SimulationManager to get the NodeId of each Node which shall
	 * start at the current simulation time.
	 * <br><br>
	 * Return empty collection for no nodes to be started at this
	 * simulation time.
	 * 
	 * @return a collection containing NodeIds of yet not started nodes
	 */
	public abstract Collection<NodeId> getStartupNodes();
	
	/**
	 * The NodeStartupManagerEnvelope of SimulationManager calls this
	 * method to determine when to check for new nodes to be started
	 * by the NodeStartupManager.
	 * 
	 * This method is first called at simulation time 1.0.
	 * See {@link SimulationManager#runSimulation()} for this.
	 * 
	 * @return -1.0 if NodeStartupManagerEnvelope should not check
	 * 			again,<br>
	 * 		    a value greater or equal to zero is a valid delay in
	 * 			simulation steps,<br>
	 * 		   	all other (negative) values are invalid. 
	 */
	public double getDelayToNextQuery() {
		return -1.0;
	}

	/**
	 * Initialize the list of to be started nodes.
	 * 
	 * @param availableNodes the to be started nodes
	 */
	public abstract void setAvailableNodes(Set<NodeId> availableNodes);
	
}

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
package br.ufla.dcc.grubix.simulator.node;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.xml.Configurable;

/**
 * Observer of the simulation.
 * Used to evaluate and monitor a simulation run without to dirty the layer implementations/algorithms.
 * 
 * The goal is to (as far as possible) seperate the evaluation logic from the algorithm (Seperation of Concerns).
 * 
 * The observer are configured in the "observer" tag in the configuration. 
 * There are exactly one instance per configured observer type.
 * All nodes share the observers. This also means that an observer has global knowledge.
 * 
 * Remarks:
 * - No Algorithm should depend on a configured observer. Never. 
 *   If the algorithm depend on a information, it has to get it in-band using
 *   the normal methods as meta data or layer states.
 * - The Observer should never change a packet or event. Never.
 *
 * @author dmeister
 *
 */
public abstract class SimulationObserver implements Configurable {
	
	/**
	 * called by the node before(!) the delivery of a packet that runs up the network stack.
	 * 
	 * @param nodeId id of the current node
	 * @param layer current layer
	 * @param packet packet to be delivered
	 */
	public void observeLowerSAP(NodeId nodeId, Layer layer, Packet packet) {
		// do nothing
	}
	
	/**
	 * called by the node before(!) the delivery of a packet that runs down the network stack.
	 * 
	 * @param nodeId id of the current node
	 * @param layer current layer
	 * @param packet packet to be delivered
	 */
	public void observerUpperSAP(NodeId nodeId, Layer layer, Packet packet) {
		// do nothing
	}
	
	/**
	 * called by the node before(!) the delivery of an non-packet event to a layer.
	 * 
	 * @param nodeId id of the current node
	 * @param layer current layer
	 * @param event event to be delivered
	 */
	public void observerEvent(NodeId nodeId, Layer layer, ToLayer event) {
		// do nothing	
	}
	
	/**
	 * called directly after the creation of the object and the setting of @ShoxParameters.
	 */
	public void init() {
		// do nothing
	}

	/**
	 * called after the complete configuration has be initialized.
	 * @param configuration current run configuration
	 */
	public void initConfiguration(Configuration configuration) {
		// do nothing
	}
	
	/**
	 * Called when the simulation finishes.
	 * The method is called before the FinalizeEvent is dispatched to the nodes.
	 * 
	 * @param nodeId id of the current node
	 */
	public void simulationBeforeFinish(NodeId nodeId) {
		// do nothing
	}

	/**
	 * Called when the simulation finishes.
	 * The method is called after the FinalizeEvent is dispatched to the nodes.
	 * 
	 * @param nodeId id of the current node
	 */
	public void simulationFinished(NodeId nodeId) {
		// do nothing
	}


}

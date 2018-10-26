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

package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.NodeId;


/** 
 * Class for the initialization of a node after creation.
 * Sent to every node at the beginning of the SIMULATION
 * to initialize it.
 * 
 * Subclass of ToNode because all LAYERS should be initialized.
 * 
 * @author Andreas Kumlehn
 */
public class Initialize extends SimulationState {
	
	/** contains the total number of nodes in the test. */
	private int nodeCount;
	
	/**
	 * Constructor of the class InitEvent.
	 * 
	 * @param receiver ReceiverId of the InitEvent.
	 * @param delay time to wait before this event ist processed.
	 */
	public Initialize(NodeId receiver, double delay, int nodeCount) {
		super(receiver, delay);
		this.nodeCount = nodeCount;
	}
	
	/** @return  String for logging. */
	public final String toString() {
		return "Initialize for node " + this.getReceiver().asInt() + ".";
	}

	/** @return the nodeCount */
	public final int getNodeCount() {
		return nodeCount;
	}
}

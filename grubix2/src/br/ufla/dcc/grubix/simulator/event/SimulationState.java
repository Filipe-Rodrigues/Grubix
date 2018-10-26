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
 * Class for notifying nodes of the beginning or end of the simulation. At the beginning of the
 * simulation, each node is sent an Initialize event, at the end, each node receives a Finalize
 * event. 
 * Subclass of ToNode because all LAYERS should be initialized.
 * @author jlsx
 */
public class SimulationState extends ToNode {

	/** the delay to wait before the event is procdessed. */
	protected double delay;

	/**
	 * Constructor of the class SimulationState.
	 * @param receiver ReceiverId of the SimulationState event.
	 * @param delay time to wait before this event ist processed.
	 */
	public SimulationState(NodeId receiver, double delay) {
		super(receiver);
		this.delay = delay;
	}

	/** @return  String for logging. */
	public String toString() {
		return "Simulation state notification for node " + this.getReceiver().asInt() + ".";
	}

	/** @return the delay for this event. */
	public double getDelay() {
		return delay;
	}
}

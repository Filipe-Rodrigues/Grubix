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

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.node.Node;


/** 
 * Simulation internal class representing movement of nodes.
 * Created by MovementManager, not by the nodes themselves.
 * After Movement is finished (dequeued in the SIMULATION)
 * the Topology is updated and a Moved event is sent to the node.
 * 
 * If the nodeFailure property of is true, than this means that
 * the node fails. If the property is set to "false", this means a recovery of the node.
 * If a node is able to cover depends from the error model, e.g. in a fail stop model this isn't possible at all.
 * 
 * @author Andreas Kumlehn
 */
public class Movement extends SimulationEvent {

	/**
	 * The node to MOVE.
	 */
	private final Node node;
	
	/** The time interval between inserting into the queue and sending
	 * the corresponding MOVE event to the node. */
	private final double delay;
	
	/**
	 * The new POSITION after the movement.
	 */
	private final Position newPosition;
	
	/**
	 * if set to true, the node fails at the "movement" time.
	 * if set to false, the node recovery from a failure.
	 * 
	 */
	private final boolean nodeFailure;
		
	/**
	 * Constructor of the class Movement.
	 * 
	 * @param node The node to MOVE.
	 * @param newPos The new POSITION of the node.
	 * @param delay The time interval between inserting into the queue and sending
	 * the corresponding MOVE event to the node 
	 */
	public Movement(Node node, Position newPos, double delay) {
		this(node, newPos, delay, false);
	}

	/**
	 * Constructor of the class Movement.
	 * 
	 * @param node The node to MOVE.
	 * @param newPos The new POSITION of the node.
	 * @param delay The time interval between inserting into the queue and sending
	 * the corresponding MOVE event to the node 
	 * @param nodeFailure if set to true, the node failes at the "movement" time.
	 */
	public Movement(Node node, Position newPos, double delay, boolean nodeFailure) {
		super();
		if (node == null) {
			throw new IllegalArgumentException("node");
		}
		if (newPos == null) {
			throw new IllegalArgumentException("newPos");
		}
		if (delay < 0.0) {
			throw new IllegalArgumentException("delay");
		}
		this.node = node;
		this.newPosition = newPos;
		this.delay = delay;
		this.nodeFailure = nodeFailure;
	}	
	
	/**
	 * @return Returns the newPosition.
	 */
	public final Position getNewPosition() {
		return newPosition;
	}
	
	/**
	 * @return The time interval between inserting into the queue and sending
	 * the corresponding MOVE event to the node.
	 */
	public final double getDelay() {
		return this.delay;
	}	
	
	/**
	 * @return Returns the node.
	 */
	public final Node getNode() {
		return node;
	}
	
	/**
	 * @return Sring for logging.
	 */
	public final String toString() {
		return "Movement[Of: " + getNode().getId() + ", To:"
		  + this.getNewPosition() + "].";
	}

	/**
	 * @return the nodeFailure
	 */
	public boolean isNodeFailure() {
		return nodeFailure;
	}
}

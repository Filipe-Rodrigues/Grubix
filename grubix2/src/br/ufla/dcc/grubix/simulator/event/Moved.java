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
import br.ufla.dcc.grubix.simulator.Position;

/**
 * Class representing the notification that a node has moved.
 * Send to the whole node and handled internally.
 * 
 * @author Andreas
 */
public class Moved extends ToNode {
	
	/**
	 * The new POSITION of the node.
	 */
	private final Position newPosition;
		
	/**
	 * Constructor of the class Moved.
	 * 
	 * @param node The moved node.
	 * @param newPosition The new POSITION of the node.
	 */
	public Moved(NodeId node, Position newPosition) {
		super(node);
		this.newPosition = newPosition;
	}

	/**
	 * @return  Returns the newPos.
	 */
	public final Position getNewPosition() {
		return newPosition;
	}
	
	/**
	 * @return  String for logging.
	 */
	public final String toString() {
		return "Moved node " + this.getReceiver().asInt()
			+ " to Position (" + this.getNewPosition().getXCoord()
			+ "," + this.getNewPosition().getYCoord() + ").";
	}
}

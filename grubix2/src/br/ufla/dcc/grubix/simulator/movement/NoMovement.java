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

package br.ufla.dcc.grubix.simulator.movement;

import java.util.Collection;
import java.util.LinkedList;

import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.node.Node;





/** 
 * Class to generate no movement at all. All nodes are stationary.
 * No Movement events are generated.
 * 
 * @author Andreas Kumlehn
 */
public class NoMovement extends MovementManager {

	/**
	 * @see br.ufla.dcc.grubix.simulator.movement.MovementManager#createMoves(java.util.Collection)
	 * Returns always an empty list of Movements.
	 * 
	 * @param allNodes allNodes Collection containing all nodes to create moves for.
	 * @return Empty collection containing no moves.
	 */
	public final Collection<Movement> createMoves(Collection<Node> allNodes) {
		return new LinkedList<Movement>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public double getDelayToNextQuery() {
		/* 
		 * as this MovementManager will never generate any movements
		 * the MoveManaEnvelope shouldn`t request new moves anymore.
		 */
		return -1.0;
	}
}

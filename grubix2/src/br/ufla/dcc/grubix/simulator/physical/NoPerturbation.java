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

package br.ufla.dcc.grubix.simulator.physical;

import br.ufla.dcc.grubix.simulator.Position;

/** 
 * Simple implementation of a position perturber which does not perturb at all.
 * 
 * @author Andreas Kumlehn
 */
public class NoPerturbation extends PositionPerturber {

	/** constructor with a special NoParameter param object. */
	public NoPerturbation() {
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.physical.PositionPerturber#perturbPosition(br.ufla.dcc.grubix.simulator.Position)
	 * @param realPosition The exact and real position of a node to perturb.
	 * @return Perturbed position of the node as new Position object.
	 */
	@Override
	public final Position perturbPosition(Position realPosition) {
		return new Position(realPosition.getXCoord(), realPosition.getYCoord());
	}
}

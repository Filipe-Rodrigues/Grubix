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
import br.ufla.dcc.grubix.simulator.node.Node;

/**
 * Simple physical model for debugging. Global transmission range.
 * 
 * @author Andreas Kumlehn
 */
public class SimplePhysics extends PhysicalModel {

	/** Constructor of the class SimplePhysics. */
	public SimplePhysics() {
	}
	
	/**
	 * use the simple physics model, to calculate a reachability object for the
	 * supplied sender and receiver and a given signal strength.
	 * @see br.ufla.dcc.grubix.simulator.physical.PhysicalModel#apply(
	 *      br.ufla.dcc.grubix.simulator.Position, br.ufla.dcc.grubix.simulator.Position, double)
	 * @param receiver
	 *            Position of the potential receiver.
	 * @param sender
	 *            Position of the sender.
	 * @param signalStrength Signal strength whith which the sender sends
     * @return the resulting attenuation and whether the receiver is reachable at all 
	 */
	@Override
	public final Reachability apply(Node re, Node se, double signalStrength) {
		
		// MUITO BUGADO!!!!! ARRUMAR!!!
		Position receiver = new Position(0,0); Position sender = new Position(0,0); //Arrumar!!!!!!!
		Reachability reachability = new Reachability();
		
		reachability.setPositions(receiver, sender);
		reachability.setSignalStrength(signalStrength);
		
		reachability.setReachable(Boolean.TRUE);       // TODO calculate the following values accordingly
		reachability.setInterfering(Boolean.TRUE);
		reachability.setAttenuation(0.0);
		reachability.setSsAtReceiver(signalStrength);
		
		return reachability;
	}
}

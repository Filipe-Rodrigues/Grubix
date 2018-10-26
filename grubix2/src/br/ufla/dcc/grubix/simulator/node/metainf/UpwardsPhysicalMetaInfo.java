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
package br.ufla.dcc.grubix.simulator.node.metainf;

import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;

/**
 * This class implements the MetaInformation which are attached from the MAC
 *  layer to a packet proceeding upwards in the network stack.
 *  
 * @author hannes
 * */
public class UpwardsPhysicalMetaInfo implements MetaInformation {

	/**
	 * the signalStrength this packet was received with.
	 */
	private final double signalStrength;
	
	/** 
	 * simple constructor.
	 * @param signalStrength the signalStrength this packet was received with
	 */
	public UpwardsPhysicalMetaInfo(double signalStrength) {
		this.signalStrength = signalStrength;
	}
	
	/**
	 * returns the signalStrength this packet was received with.
	 * @return the signalStrength this packet was received with
	 */
	public double geSignalStrength() {
		return signalStrength;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public LayerType getCorrespondingLayerType() {
		return LayerType.PHYSICAL;
	}

	/**
	 * {@inheritDoc}
	 */
	public Direction getDirection() {
		return Direction.UPWARDS;
	}

}

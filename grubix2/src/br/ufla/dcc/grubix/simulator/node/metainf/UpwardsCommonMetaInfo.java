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
 * This class implements the MetaInformation which are attached from the LLC
 *  layer to a packet proceeding upwards in the network stack.
 *  
 * @author hannes
 * */
abstract class UpwardsCommonMetaInfo implements MetaInformation {

	/**
	 * indicating of packet was transmitted at all.
	 * this flag has to be false if and only if under following circumstances
	 * - this packet originally was intended to be send from this node 
	 * - therefore the packet was proceeding downwards
	 * - the lower layers were for some reason unable to transmit it
	 * - therefore the packet now proceeds upwards to inform upper the layers
	 *  that it could not be transmitted.
	 *  
	 * In every other case delivered has to be true
	 *  
	 */
	private final boolean delivered;
	
	/**
	 * number of retries due to delivery problems.
	 */
	private final int numRetransmits;
	
	
	/** 
	 * simple constructor.
	 * 
	 * @param delivered delivery state
	 * @param numRetransmits number of retries due to delivery problems.
	 */
	public UpwardsCommonMetaInfo(
			boolean delivered, int numRetransmits) {
		this.numRetransmits = numRetransmits;
		this.delivered = delivered;
		
	}
	
	/**
	 * returns the number of retries due to delivery problems.
	 * @return number of retries due to delivery problems.
	 */
	public int getNumRetransmits() {
		return numRetransmits;
	}

	/**
	 * indicating of packet was transmitted at all.
	 * @return delivery state
	 */
	public boolean isDelivered() {
		return delivered;
	}

	/**
	 * {@inheritDoc}
	 */
	public LayerType getCorrespondingLayerType() {
		return LayerType.LOGLINK;
	}

	/**
	 * {@inheritDoc}
	 */
	public Direction getDirection() {
		return Direction.UPWARDS;
	}

}


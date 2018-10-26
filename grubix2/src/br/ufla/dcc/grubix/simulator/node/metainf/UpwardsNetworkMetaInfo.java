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
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * This class implements the MetaInformation which are attached from the network
 *  layer to a packet proceeding upwards in the network stack.
 *  
 * @author hannes
 * */
public class UpwardsNetworkMetaInfo extends UpwardsCommonMetaInfo {

	/**
	 * the node from which this packet was received.
	 * Be aware in most cases this will NOT be the source of the packet 
	 */
	private final NodeId prevHop;
	
	/** 
	 * simple constructor.
	 * 
	 * @param prevHop the node from which this packet was received.
	 * @param delivered delivery state
	 * @param numRetransmits number of retries due to delivery problems.
	 */
	public UpwardsNetworkMetaInfo(NodeId prevHop, boolean delivered, int numRetransmits) {
		super(delivered, numRetransmits);
		this.prevHop = prevHop;
	}
	
	/**
	 * returns the previous hop.
	 * @return previous hop
	 */
	public NodeId getPrevHop() {
		return prevHop;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LayerType getCorrespondingLayerType() {
		return LayerType.NETWORK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Direction getDirection() {
		return Direction.UPWARDS;
	}

}

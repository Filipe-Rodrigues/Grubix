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

import java.util.Arrays;
import java.util.List;

import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.NodeId;




/**
 * This class implements the MetaInformation which are attached from 
 * more than one
 *  layer to a packet proceeding downwards in the network stack.
 *  
 * @author hannes
 * */
abstract class DownwardsCommonMetaInfo implements MetaInformation {

	/**
	 * The nexthop, the packet has to sent to.
	 */
	private final List<NodeId> nexthop;
	
	/**
	 * This variable holds the castType of the packet, 
	 * possible values are the CASTTYPE constants defined in this class.
	 */
	private final CastType castType;
	
	
	/**
	 * simple constructor.
	 * 
	 * @param nexthop List of NodeIds containing the nexthop the lower layers 
	 * are supposed to send the packet to. Number of elements must fit to castType.
	 * castType = Broadcast: no element in nexthop
	 * castType = Unicast : one element in nexthop
	 * casttype = Multicast: one or more elements in nexthop
	 * @param castType must be one of MetaInformation_CASTTYPE contants
	 */
	protected DownwardsCommonMetaInfo(CastType castType, NodeId... nexthop) {
		this.nexthop = Arrays.asList(nexthop);
		this.castType = castType;
		
		switch(castType) {
		case BROADCAST:
			if (this.nexthop.size() != 0) {
				throw new IllegalArgumentException("Broadcast packet cannot have nexthop");
			}
			break;
		case UNICAST:
			if (this.nexthop.size() != 1) {
				throw new IllegalArgumentException("Unicast packet must have exactly one nexthop");
			}
			break;
		case MULTICAST:
			if (this.nexthop.size() < 1) {
				throw new IllegalArgumentException("Multicast packet must have at least one nexthop");
			}
			break;
		default:
			throw new IllegalArgumentException("Illegal cast type");
		}

	}

	/**
	 * returns the next hop or the next hops depending on the cast type.
	 * @return a list of next hops
	 */
	public List<NodeId> getNexthop() {
		return nexthop;
	}

	/**
	 * returns the cast type.
	 * @return cast type
	 */
	public CastType getCastType() {
		return castType;
	}

	/**
	 * {@inheritDoc}
	 */
	public Direction getDirection() {
		return Direction.DOWNWARDS;
	}
	
	

}

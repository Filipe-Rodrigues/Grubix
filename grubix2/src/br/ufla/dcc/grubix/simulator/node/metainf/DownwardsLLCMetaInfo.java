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

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.node.Link;

/**
 * This class implements the MetaInformation which are attached from the LLC
 *  layer to a packet proceeding downwards in the network stack.
 *  
 * @author hannes
 * */
public class DownwardsLLCMetaInfo extends DownwardsCommonMetaInfo {

	/**
	 * link between the sender and the receiver (optional).
	 */
	private final Link link;
	
	/**
	 * simple constructor.
	 * 
	 * @param link link between the sender and the receiver (optional)
	 * @param nexthop List of NodeIds containing the nexthop the lower layers 
	 * are supposed to send the packet to. Number of elements must fit to castType.
	 * castType = Broadcast: no element in nexthop
	 * castType = Unicast : one element in nexthop
	 * casttype = Multicast: one or more elements in nexthop
	 * @param castType must be one of MetaInformation_CASTTYPE contants
	 */
	public DownwardsLLCMetaInfo(Link link, CastType castType, NodeId... nexthop) {
		super(castType, nexthop);
		this.link = link;
	}

	/**
	 * {@inheritDoc}
	 */
	public LayerType getCorrespondingLayerType() {
		return LayerType.LOGLINK;
	}

	/**
	 * @return the link
	 */
	public Link getLink() {
		return link;
	}
}

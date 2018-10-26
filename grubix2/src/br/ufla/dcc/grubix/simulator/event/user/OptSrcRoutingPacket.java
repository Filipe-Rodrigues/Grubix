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

package br.ufla.dcc.grubix.simulator.event.user;

import java.util.ArrayList;
import java.util.List;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;




/**
 * This class implements the the packets needed for the
 * optimal source routing network layer.
 * 
 * @author Dirk Held
 */
public class OptSrcRoutingPacket extends NetworkPacket {

	/** the computed path for this packet. */
	protected List<NodeId> path;
	/** the number of hops, this packet has to travel. */
	private int hopCount;
	
	/**
	 * This is the constructor for terminal packets.
	 * This ist just for the sake of completeness, since
	 * this constructor is not used. 
	 * @param sender   the origin of the packet.
	 * @param receiver the receiver of this packet.
	 * @param path     the precomputed path, this packet has to travel.
	 */
	public OptSrcRoutingPacket(Address sender, NodeId receiver, List<NodeId> path) {
		super(sender, receiver);
		this.path = path;
		hopCount = path.size();
	}

	/**
	 * This is the constructor for nonterminal packets.
	 * @param sender the origin of the packet.
	 * @param packet the to be enclosed packet.
	 * @param path   the precomputed path for this packet, ending with the final destination.
	 */
	public OptSrcRoutingPacket(Address sender, Packet packet, List<NodeId> path) {
		super(sender, packet);
		this.path = path;
	}
	
	/**
	 * Extracts the next hop from the path contained in this packet and sets it to be the next receiver. 
	 * @return true, if a next hop exists and was successfully set. 
	 */
	public boolean popNextHop() {
		if (path.size() > 0) {
			setReceiver(path.get(0));
			path.remove(0);
			
			return true;
		}
		
		return false;
	}

	/** @return the computed path for this packet. */
	public final List<NodeId> getPath() {
		return path;
	}

	/** @return the number of hops, this packet has to travel. */
	public final int getTotalHopCount() {
		return hopCount;
	}

	/**
	 * Creates a clone of the routing packet.
	 * In addition to the base class cloning,
	 * a deep copy of the path is created.
	 * @see br.ufla.dcc.grubix.simulator.event.Packet#clone()
	 * @return a cloned packet.
	 */
	@Override
	public Object clone() {
		OptSrcRoutingPacket packet =  (OptSrcRoutingPacket) super.clone();
		packet.path = new ArrayList<NodeId>(path);
		return packet;
	}
}

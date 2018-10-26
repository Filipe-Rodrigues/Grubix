/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * A network layer that implements {@link VisitingRoutingSupported} has to
 * check for packets that are instances of this packet type and start the 
 * visiting mechanism (sending this packet to the upper layer and routing it 
 * further).
 * 
 * @author Thomas Kemmerich 
 */
public abstract class VisitingPacket extends OperatingSystemPacket {
	
	/**
	 * flag that indicates that (if true) the visiting packet should be sended towards the receiver node it should
	 * take only one hop. if false the packet is send to the final receiver node.
	 */
	private final boolean oneHopVisitor;
	
	/**
	 * number of hops this packets has been forwarded.
	 * Since this is a usual operation in VisitingPacket, it is contained in the base class. All
	 * Visiting-enabled network layers have to update this counter on each forwarding.
	 * 
	 * The hop count is by default one and the sender id.
	 * The method increaseHopCount should be called after visiting a node,
	 * so that the node is preparred for the next node.
	 */
	private int hopCounter;
	
	/**
	 * the last hop this packet has visited at last.
	 */
	private NodeId lastVisitedHop;
	
	/**
	 * Constructor.
	 * 
	 * @param sender sender address of the packet
	 * @param senderPort port of the sender (used for reply, can be null)
	 * @param receiver ReceiverId of the packet
	 * @param receiverPort port of the receiver (can be null)
	 * @param oneHopVisitor flag that indicates that (if true) the visiting packet should be sended towards 
	 * the receiver node it should take only one hop.
	 * @param packet The packet to enclose inside the new packet.
	 */
	public VisitingPacket(Address sender, Port senderPort, NodeId receiver, Port receiverPort, 
			boolean oneHopVisitor, Packet packet) {
		super(sender, senderPort, receiver, receiverPort, packet); 
		this.oneHopVisitor = oneHopVisitor;
		this.hopCounter = 1;
		this.lastVisitedHop = sender.getId();
	}

	/**
	 * A simplyfied constructor with the oneHopVisiting option set to false.
	 * 
	 * @param sender sender address of the packet
	 * @param senderPort port of the sender (used for reply, can be null)
	 * @param receiver ReceiverId of the packet
	 * @param receiverPort port of the receiver (can be null)
	 * @param packet The packet to enclose inside the new packet.
	 */
	public VisitingPacket(Address sender, Port senderPort, NodeId receiver, Port receiverPort, Packet packet) {
		this(sender, senderPort, receiver, receiverPort, false, packet); 
	}
	
	/**
	 * gets oneHopVisitor.
	 * @return current oneHopVisitor
	 */
	public boolean isOneHopVisitor() {
		return oneHopVisitor;
	}
	
	/**
	 * increases the hop counter.
	 * Should only be called by a network layer. 
	 * 
	 * @param lastVisitedNode last visited node
	 */
	public void increaseHopCount(NodeId lastVisitedNode) {
		this.hopCounter++;
		this.lastVisitedHop = lastVisitedNode;
	}
	
	/**
	 * returns the hop count.
	 * @return hop count.
	 */
	public int getHopCount() {
		return this.hopCounter;
	}

	/**
	 * @return the last visited hop
	 */
	public NodeId getLastVisitedHop() {
		return lastVisitedHop;
	}
}

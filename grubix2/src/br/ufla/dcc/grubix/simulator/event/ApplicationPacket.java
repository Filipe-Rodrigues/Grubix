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

package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * this class is the super class for all Application Packets. Since this is the innermost layer,
 * no packets can be embedded here, and please set the header length or insert the headerstring,
 * since this is considered as the payload of the application.
 * 
 * @author Dirk Held
 */
public class ApplicationPacket extends Packet {

	/** the number of hops to the receiver. */
	@NoHeaderData
	protected int hopCount;
	
	public int PacketId;
	private static int counter = 0;

	/**
	 * Default constructor of class Packet to create a terminal packet
	 * with no enclosed packet.
	 * 
	 * @param sender Senderaddress of the packet
	 * @param receiver ReceiverId of the packet
	 */
	public ApplicationPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
		
		hopCount = -1;
		PacketId = counter++;
	}

	/** @return the hopCount. */
	public final int getHopCount() {
		return hopCount;
	}

	/** @param hopCount the hopCount to set. */
	public final void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}
}

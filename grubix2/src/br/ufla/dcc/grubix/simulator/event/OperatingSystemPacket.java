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
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * Super-class of all packets generated by the OperatingSystemLayer. This is to ensure some
 * basic information to be present in all operating system packets regardless of the particular
 * implementation.
 * This is also an element to structure the source code
 * 
 * @author dmeister
 */
public class OperatingSystemPacket extends Packet {
	
	/**
	 * receiver port.
	 */
	private final Port receiverPort;
	
	/**
	 * sender port.
	 */
	private final Port senderPort;
	
	/**
	 * Overloaded constructor to create non-terminal packets by
	 * specifying a packet to enclose.
	 * ReceiverID of the new packet may not be the same as stated in the enclosedPacket.
	 * 
	 * @param sender sender address of the packet
	 * @param senderPort port of the sender (used for reply, can be null)
	 * @param receiver ReceiverId of the packet
	 * @param receiverPort port of the receiver (can be null)
	 * @param packet The packet to enclose inside the new packet.
	 */
	public OperatingSystemPacket(Address sender, Port senderPort, NodeId receiver, Port receiverPort, Packet packet) {
		super(sender, receiver, packet);
		if (sender.getFromLayer() != LayerType.OPERATINGSYSTEM) {
			throw new IllegalArgumentException("sender");
		}
		this.senderPort = senderPort;
		this.receiverPort = receiverPort;
	}

	/**
	 * Returns the port of the receiver, can be null.
	 * @return port of the receiver
	 */
	public final Port getReceiverPort() {
		return receiverPort;
	}

	/**
	 * Returns the port of the sender.
	 * Can be used to send a reply packet, can be null.
	 * @return port of the sender
	 */
	public final Port getSenderPort() {
		return senderPort;
	}

}

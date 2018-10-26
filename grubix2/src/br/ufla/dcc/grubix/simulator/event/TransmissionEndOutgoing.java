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
import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.NodeId;

/** 
 * WakeUpCall indicating that the sending of a packet is finished.
 * 
 * @author Andreas Kumlehn
 */
public final class TransmissionEndOutgoing extends TransmissionEnd {
	
	/** The ID of the node which is the receiver of the transmission. */
	private NodeId receiver;

	/**
	 * Constructor of the class TransmissionEndOutgoing.
	 * 
	 * @param sender Sender of the WUC as Address.
	 * @param time SendingDelay of the packet.
	 * @param transmissionId EventId of the transmission
	 * @param receiver The receiver of the transmission
	 * @param packetId EventId of the actual packet.
	 */
	public TransmissionEndOutgoing(Address sender, double time, 
			EventId transmissionId, NodeId receiver, EventId packetId) {
		super(sender, time, transmissionId, packetId);
		this.receiver = receiver;
	}

	/**
	 * @return The IDs of the two conversating nodes in the transmission. This is helpful for
	 * the ShoX Monitor later on.
	 */
	public String getData() {
		return "receiver id: " + this.receiver.asInt() + ", packet id: " + this.packetId;		
	}
}

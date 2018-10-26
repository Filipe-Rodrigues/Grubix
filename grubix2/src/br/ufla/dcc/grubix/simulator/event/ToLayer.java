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
 * Abstract superclass for all events that should be delivered
 * to a specific node AND a specific layer in the node.
 * The receiving layer is defined by the TYPE of the specific object
 * (for example LogLinkPacket). WakeUpCalls are addressed via enqueuing.
 * See SimulationManager for further details. 
 * 
 * @author Andreas Kumlehn
 */
public abstract class ToLayer extends ToNode {
	
	/**
	 * The Address of the sending node.
	 */
	private Address sender;

	/**
	 * Constructor of the class ToLayer.
	 * @param sender Address of the sendernode.
	 * @param receiver NodeId of the receiver.
	 */
	protected ToLayer(Address sender, NodeId receiver) {
		super(receiver);
		if (sender == null) {
			throw new IllegalArgumentException("sender");
		}
		this.sender = sender;
	}

	/**
	 * @return Returns the sender.
	 */
	public final Address getSender() {
		return sender;
	}
	
	/**
	 * method to change the sender.
	 * @param sender the new destination for this packet.
	 */
	public final void setSender(Address sender) {
		this.sender = sender;
	}
	
	/**
	 * @return String for logging.
	 */
	@Override
	public String toString() {
		return "Event[from=" + sender.getId() + ",to=" + getReceiver() + "]";
	}
}

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

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;

/** 
 * Simple packet used for testing. Contains only a String.
 * 
 * @author Andreas Kumlehn
 */
@SuppressWarnings("deprecation")
public class HelloWorldPacket extends ApplicationPacket {
	
	/** If set then this hello world packet is regarded as an answer to a previous packet. */
	private boolean isAnswer = false;
	
	/**
	 * Constructor of the class AppMessagePacket.
	 * 
	 * @param receiver NodeId of the receiver.
	 * @param sender Address of the sender.
	 * @param message Message to send as String.
	 */
	public HelloWorldPacket(Address sender, NodeId receiver, String message) {
		super(sender, receiver);
		setHeader(message);
	}

	/**
	 * Returns true if this packet is regarded as an answer to a prevoius packet.
	 * @return the isAnswer
	 */
	public boolean isAnswer() {
		return isAnswer;
	}

	/**
	 * Set to true iff this packet should be regarded as an answer to a previous packet.
	 * @param isAnswer the isAnswer to set
	 */
	public void setAnswer(boolean isAnswer) {
		this.isAnswer = isAnswer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "HelloWorldPacket from " + getSender().getId() + " to " + getReceiver() + ":" + this.getHeader();
	}
}

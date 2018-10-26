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
import br.ufla.dcc.grubix.simulator.event.LoggableData;

/** 
 * Simple packet used for testing. Contains only a String.
 * 
 * @author Andreas Kumlehn
 * @deprecated bad naming
 */
@Deprecated
public class AppMessagePacket extends ApplicationPacket implements LoggableData, Cloneable {
	
	/**
	 * Constructor of the class AppMessagePacket.
	 * 
	 * @param receiver NodeId of the receiver.
	 * @param sender Address of the sender.
	 * @param message Message to send as String.
	 */
	public AppMessagePacket(NodeId receiver, Address sender, String message) {
		super(sender, receiver);
		setHeader(message);
	}

	/**
	 * Adds the appendix to the existing message.
	 * @param appendix The appendix to add.
	 */
	public final void appendMessage(String appendix) {
		setHeader(getHeader() + appendix);
	}
	
	/** @return Returns the message. */
	@Deprecated
	public final String getMessage() {
		return getHeader();
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.event.LoggableData#getData()
	 * @return Enclosed message as String for logging.
	 */
	public final String getData() {
		return getHeader();
	}
	
	/**
	 *  method for ptinting the object as string.
	 *  @return the object as string for logging.  
	 */
	@Override
	public final String toString() {
		return "AppMessagePacket. " + getHeader();
	}
}

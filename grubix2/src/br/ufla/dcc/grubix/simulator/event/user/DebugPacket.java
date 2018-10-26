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
import br.ufla.dcc.grubix.simulator.event.NetworkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

/** 
 * Simple packet without further data for testing.
 * 
 * @author Andreas Kumlehn
 */
public class DebugPacket extends NetworkPacket {
	
	/**
	 * Constructor of the class DebugPacket.
	 * 
	 * @param receiver NodeId of the receiver.
	 * @param sender Address of the sender.
	 */
	public DebugPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
	}
	
	/**
	 * Constructor of the class DebugPacket.
	 * 
	 * @param receiver NodeId of the receiver.
	 * @param sender Address of the sender.
	 * @deprecated Don't use this since the default order in Shox isn't receiver, sender.
	 */
	@Deprecated
	public DebugPacket(NodeId receiver, Address sender) {
		super(sender, receiver);
	}

	/**
	 * Constructor of the class DebugPacket.
	 * 
	 * @param sender Address of the sender.
	 * @param packet Enclosed packet.
	 */
	public DebugPacket(Address sender, Packet packet) {
		super(sender, packet);
	}
}

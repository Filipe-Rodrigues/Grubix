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
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Packet;

/** 
 * Class to represent a beacon for the simple log link metric based on euclidean distance between nodes.
 * 
 * @author Andreas Kumlehn
 */
public class LogLinkBeaconPosition extends Packet implements Cloneable {
	
	/** Position of the sender of this beacon. */
	private final Position position;
	
	/**
	 * Constructor to create a terminal LogLinkBeaconObject.
	 * 
	 * @param receiver NodeId of the receiver of the terminal packet.
	 * @param sender Address of the sender.
	 * @param position Position of the sending node.
	 */
	public LogLinkBeaconPosition(NodeId receiver, Address sender, Position position) {
		super(sender, receiver);
		this.position = position;
	}
	
	/**
	 * Constructor to create a non-terminal LogLinkBeaconObject.
	 * 
	 * @param sender Address of the sender.
	 * @param packet Packet to enclose.
	 * @param position Position of the sending node.
	 */
	public LogLinkBeaconPosition(Address sender, Packet packet, Position position) {
		super(sender, packet);
		this.position = position;
	}

	/** @return Returns the position. */
	public final Position getPosition() {
		return position;
	}
}

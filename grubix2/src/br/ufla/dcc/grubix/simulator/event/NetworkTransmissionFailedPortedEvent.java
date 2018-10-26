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

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.node.user.os.PortedEvent;

/**
 * This event should be thrown by a NetworkLayer if and only if an ported packet could
 * not be transmitted. The event contains the failed ported packet.
 * 
 * @author Thomas Kemmerich
 */
public class NetworkTransmissionFailedPortedEvent extends PortedEvent {
	

	/** The packet whose transmission failed. */
	private OperatingSystemPacket packet;
	
	/**
	 * Constructor of this class.
	 * @param sender The node which constructs this wake up call
	 * @param packet The packet that could not be sent
	 */
	public NetworkTransmissionFailedPortedEvent(NodeId sender, OperatingSystemPacket packet) {
		super(sender, packet.getSenderPort());
		this.packet = packet;
	}

	/**
	 * Returns the packet whose transmission failed.
	 * @return the packet.
	 */
	public OperatingSystemPacket getPacket() {
		return packet;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "NetworkTransmissionFailedPortedEvent on Port " + packet.getSenderPort() + " for " + packet;
	}

}

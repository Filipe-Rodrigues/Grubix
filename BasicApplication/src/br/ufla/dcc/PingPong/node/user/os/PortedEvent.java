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
package br.ufla.dcc.PingPong.node.user.os;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Port;
import br.ufla.dcc.grubix.simulator.event.ToLayer;

/**
 * Ported event.
 * @author dmeister
 *
 */
public class PortedEvent extends ToLayer {

	/**
	 * destination port.
	 */
	private final Port port;

	/**
	 *
	 * @param senderId sender id
	 * @param port destination port.
	 */
	public PortedEvent(NodeId senderId, Port port) {
		super(new Address(senderId, LayerType.OPERATINGSYSTEM), senderId);
		if (port == null) {
			throw new IllegalArgumentException("port");
		}
		this.port = port;
	}

	/**
	 * gets port.
	 * @return current port
	 */
	public Port getPort() {
		return port;
	}

}

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
package br.ufla.dcc.grubix.simulator.node.user.os;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Port;
import br.ufla.dcc.grubix.simulator.event.ToLayer;

/**
 * Class to wrap all {@link LowerLayerEvent}s into {@link PortedEvent}s, which
 * could be sent to running {@link Service}s.
 * 
 * @author wolff
 * 
 */
public class LowerLayerPortedEvent extends PortedEvent {

	/**
	 * the wrapped Event. 
	 */
	private ToLayer lowerEvent;
	
	/**
	 * constructor.
	 * @param senderId the sender
	 * @param port the receiver port
	 * @param event the event forwarded to the {@link Service} identified by the {@link Port}.
	 */
	public LowerLayerPortedEvent(NodeId senderId, Port port, ToLayer event) {
		super(senderId, port);
		this.lowerEvent = event;
	}
	
	/**
	 * Gets the enclosed {@link ToLayer} event.
	 * @return the enclosed {@link ToLayer} event.
	 */
	public ToLayer getEnclosedEvent() {
		return lowerEvent;
	}
}

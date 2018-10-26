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
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

/**
 * this event is signaled from the Air, whenever a packet was sent.
 *
 * @author Dirk Held
 */
public class ReceiverInformationEvent extends MACEvent {

	/** the packet, which just has been Terminated. */
	private Packet p;
	
	/** the time, when this packet has left the antenna. */
	private double endTime;
	
	/**
	 * default constructor.
	 * 
	 * @param sender  the node which packet was sent.
	 * @param p       the packet which just has left the antenna.
	 * @param endTime the time, when the transmission has left the antenna.
	 */
	public ReceiverInformationEvent(Address sender, Packet p, double endTime) {
		super(sender, Configuration.getInstance().getPropagationDelay() * 2);
		
		this.p       = p;
		this.endTime = endTime;
	}

	/** @return the packet which just has bee transmitted. */
	public final Packet getPacket() {
		return p;
	}

	/** @return the time, when the packethas left the antenna. */
	public final double getEndTime() {
		return endTime;
	}
}

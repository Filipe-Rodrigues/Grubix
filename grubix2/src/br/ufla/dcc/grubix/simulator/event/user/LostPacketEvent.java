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
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.CrossLayerResult;
import br.ufla.dcc.grubix.simulator.event.Packet;

/**
 * This event is sent from the 802.11 MAC, when a packet was lost. Although this
 * event seems to be identical to the FailedTransmissionEvent, this one differs,
 * since it is only issued, if the receiver information was requested and the
 * packet really did't reach the receiver. When the FailedTransmissionEvent is 
 * issued, it could happen, that the packet has reached the receiver, but the ack  
 * of the receiver didn't reach the sender, thus the sender sends it again, if
 * the resend counter allows to.  
 * 
 * WARNING: This event is created by using informations, which actually are not
 * known to the sending node, thus such events should only be used for statistics
 * or monitor code, which doesn't influence the normal behavior of the node.
 * 
 * @author Dirk Held
 */
public class LostPacketEvent extends CrossLayerEvent {

	/**
	 * default constructor of this class.
	 * The result is set to CrossLayerResult.FAIL.
	 *  
	 * @param sender the layer, where the transmission failed.
	 * @param packet the failed packet.
	 */
	public LostPacketEvent(Address sender, Packet packet) {
		super(sender, packet);
	}
}

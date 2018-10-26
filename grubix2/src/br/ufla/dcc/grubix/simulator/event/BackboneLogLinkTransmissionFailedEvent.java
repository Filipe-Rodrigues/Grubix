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

import br.ufla.dcc.grubix.simulator.Address;

/**
 * Event that notifies its recipient (the network layer) that a network-level packet
 * could not be sent successfully on a backbone-based loglink layer. 
 * @author Thomas Kemmerich
 */
public class BackboneLogLinkTransmissionFailedEvent extends TransmissionFailedEvent {

	/** Indicates whether or not the backbone was established when sending this event. */
	private boolean backboneEstablished;
	
	/**
	 * Constructor of this class.
	 * @param sender The node which constructs this wake up call
	 * @param packet The packet that could not be sent
	 * @param backboneEstablished Indicates whether or not the backbone was established when sending this event. 
	 */
	public BackboneLogLinkTransmissionFailedEvent(Address sender, NetworkPacket packet, 
			boolean backboneEstablished) {
		super(sender, packet);
		this.backboneEstablished = backboneEstablished;
	}

	/**
	 * Returns the backbone established stated at the moment when the event was sent.
	 * @return the status.
	 */
	public boolean isBackboneEstablished() {
		return backboneEstablished;
	}

}

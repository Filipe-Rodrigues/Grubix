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

/**
 * This event is used, to inform the sending node, that the sending of a packet has been completed.
 * 
 * @author Dirk Held
 */
public class SendingTerminated extends CrossLayerEvent {

	/**
	 * default constructor of this class.
	 * @param sender the sending node
	 * @param packet the packet, which was just completely sent.
	 */
	public SendingTerminated(Address sender, Packet packet) {
		super(sender, packet);
		// TODO Auto-generated constructor stub
	}

}

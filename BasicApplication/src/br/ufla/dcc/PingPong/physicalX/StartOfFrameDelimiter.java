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

package br.ufla.dcc.PingPong.physicalX;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.Packet;

/**
 * Event used by PHY to inform the MAC that a packet started to come in.
 * SFD is a byte signal present in the beginning of a IEEE802.15.4 message.
 * 
 * @author João Giacomin 
 * @version 18/03/2019
 *  
 */
public class StartOfFrameDelimiter extends CrossLayerEvent {

	/**
	 * default constructor of this class.
	 * @param sender the sending node
	 * @param packet the packet, which was just completely sent.
	 */
	public StartOfFrameDelimiter(Address sender, Packet packet) {
		super(sender, packet);
	}

}

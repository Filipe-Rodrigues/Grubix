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

package br.ufla.dcc.grubix.simulator.physical;

import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.Transmission;

/**
 * @author Dirk Held
 *
 * simple implementation of a bit mangling model, by invalidating, 
 * packets whenever a collision was detected.
 * 
 */
public class CollisionPacketMangler extends BitManglingModel {

	/** default constructor of this class. */
	public CollisionPacketMangler() {
	}

	/**
	 * simple implementation of a packet mangler, who invalidates the packet on the slightest collission.
	 * 
	 * @see BitManglingModel#getResultingDataPacket(double, Transmission, Reachability)
	 * @param transStartTime the start time of the data transmission.
	 * @param trans          the affected transmission.
	 * @param inter          the affecting interferences.
	 * @return packet, setting valid depending on the SNR. 
	 */
	@Override
	public PhysicalPacket getResultingDataPacket(double transStartTime, Transmission trans, InterferenceQueue inter) {
		Packet p = (Packet) trans.getPacket().clone();
		p.setValid(inter.getSize() == 0);
		return (PhysicalPacket) p;
	}
}

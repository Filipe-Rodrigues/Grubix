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

import br.ufla.dcc.grubix.simulator.event.Interference;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.physical.Reachability;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * Simple implementation of a bit mangling model, where packets get dropped, 
 * when a disturbance is found, where the ratio of the received signal to the
 * disturbance is below the given minimal SNR level.
 * 
 * @author Dirk Held
 */
public final class PeakNoiseMangler extends BitManglingModel {

	@ShoXParameter(description = "good signals must be larger by this factor, to be accepted.", defaultValue = "2.0")
	protected double minSNRlevel;
	
	/** default constructor of this class. */
	public PeakNoiseMangler() {
	}

	/**
	 * simple implementation of a packet mangler, which just doesn't returns the packet,
	 * if the snr is below a certain threshold.
	 * @see BitManglingModel#getResultingDataPacket(double, Transmission, Reachability)
	 * @param transStartTime the start time of the data transmission.
	 * @param trans          the affected transmission.
	 * @param inter          the affecting interferences.
	 * @return packet, setting valid depending on the SNR. 
	 */
	@Override
	public PhysicalPacket getResultingDataPacket(double transStartTime, Transmission trans, 
			                             		InterferenceQueue inter) {
		double r2 = transStartTime + trans.getSendingTime();
		
		Packet         p1   = (Packet) trans.getPacket().clone();
		PhysicalPacket p    = (PhysicalPacket) p1;
		double         rssi = inter.getSelfReachability().getSsAtReceiver();
		
		p.setValid(true);
		
		for (Interference i : inter.getInterferences()) {
			double l = i.getSimStartTime();
			
			if (r2 < l) {
				break;
			}
			
			if ((l + i.getSendingTime()) < transStartTime) {
				break;
			}
			
			double snr = rssi / i.getReachability().getSsAtReceiver();
			
			if (snr < minSNRlevel) {
				p.setValid(false);
				
				return p;
			}
		}
		
		return p;
	}
}

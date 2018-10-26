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
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * @author Dirk Held
 *
 * simple implementation of a bit mangling model, by dropping packets, where the
 * averaged SNR is below a certain level. If a packet is returned, it's intact.
 * 
 * This is work in (non-)progress. Since the current state is not complete, it was
 * stripped down, until a more sophisticated solution would be found. You better use
 * the CollisionPacketMangler instead, until this class is complete. Feel free, to
 * implement it, if you need something better than the CollisionPacketMangler.  
 * 
 * currently use BitManglingModel.getInstance();
 */
@Deprecated
public final class ThresholdPacketMangler extends BitManglingModel {

	@ShoXParameter(description = "signals must be above this value, to be accepted.")
	protected double minSNRlevel;
	
	/** default constructor of this class. */
	public ThresholdPacketMangler() {
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
		double l, r, r2; //, t, noise = 0.0, snr;
		//Reachability reachability = inter.getSelfReachability();
		
		Packet p1 = (Packet) trans.getPacket().clone();
		
		if (!(p1 instanceof PhysicalPacket)) {
			return null;
		}
		
		PhysicalPacket p = (PhysicalPacket) p1; 
		
		p.setValid(true);
		
		for (Transmission i : inter.getInterferences()) {
			l = i.getSimStartTime();
			r = l + i.getSendingTime();
			r2 = transStartTime + trans.getSendingTime();
				
			// the lower bound of the intersection;
			
			if (l <= transStartTime) {
				l = transStartTime;
			}

			// the upper bound of the intersection
			
			if (r2 < r) {
				r = r2;
			}
			
			l = r - l;  // the resulting length of the intersecting interval.
			r = trans.getSendingTime();
			
			if (r > 0.0) {
				p.setValid(false); // harshe decission on slightest interference
			/*	
				l /= r;  // the ratio of the affected transmission length.
				l *= i.getReachability().getSsAtReceiver();
			*/	 
			} else {
				l = 0;
			}
			
			// l contains the averaged noise of this interference.
			//noise += l;
		}
		/*
		l = reachability.getSsAtReceiver();
		
		t = l + noise;
		
		// TODO check, if this makes sense !!!
		
		if (t != 0.0) {
			snr = l / t;
		} else {
			snr = 1.0;
		}
		
		// p.setValid(param.isParamSetValid() && (snr >= param.asDouble(MIN_SNR_LEVEL)));
		*/
		return p;
	}
}

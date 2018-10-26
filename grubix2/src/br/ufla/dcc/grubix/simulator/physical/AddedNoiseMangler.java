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
import br.ufla.dcc.grubix.simulator.physical.Reachability;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * Simple implementation of a bit mangling model, where packets get dropped, when at any time the
 * combined noise multiplied with the minSNRlevel overwhelms the received signal. Although a full
 * evaluation of the current noise is possible, it makes no sense to do so, unless the data is
 * actually mangled to stress test some error correction mechanism.
 * 
 * Warning: This class is rather untested yet, so feel free to check especially 
 * 			the class NoiseVector, where most of the actual calculation is done. 
 * 
 * @author Dirk Held
 */
public final class AddedNoiseMangler extends BitManglingModel {

	@ShoXParameter(description = "Good signals must be larger by this factor, to be accepted.", defaultValue = "2.0")
	protected double minSNRlevel;
	@ShoXParameter(description = "Stop on the first aggregated noise, which is too intense.", defaultValue = "true")
	private boolean quickEvaluation;
	
	/** default constructor of this class. */
	public AddedNoiseMangler() {
	}

	/**
	 * simple implementation of a packet mangler, which just doesn't returns the packet,
	 * if the snr is below a certain threshold.
	 * 
	 * @see BitManglingModel#getResultingDataPacket(double, Transmission, Reachability)
	 * @param transStartTime the start time of the data transmission.
	 * @param trans          the affected transmission.
	 * @param inter          the affecting interferences.
	 * @return packet, setting valid depending on the SNR. 
	 */
	@Override
	public PhysicalPacket getResultingDataPacket(double transStartTime, Transmission trans, 
			                             		InterferenceQueue inter) {
		
		Packet         p1 = (Packet) trans.getPacket().clone();
		PhysicalPacket p  = (PhysicalPacket) p1; 

		p.setValid(true);
		
		if (inter.getSize() > 0) {
			NoiseVector nv;
			Noise  n        = null;		
			double maxNoise = inter.getSelfReachability().getSsAtReceiver() / minSNRlevel;
			double duration = trans.getSendingTime();
			
			if (quickEvaluation) {
				nv = new NoiseVector(transStartTime, duration, inter, maxNoise);
			} else {
				nv = new NoiseVector(transStartTime, duration, inter, 0.0);
			}
			
			n = nv.getMostSignificantNoise();
			
			if ((n != null) && (n.getRssi() > maxNoise)) {
				p.setValid(false);
			}
		}
		
		return p;
	}
}

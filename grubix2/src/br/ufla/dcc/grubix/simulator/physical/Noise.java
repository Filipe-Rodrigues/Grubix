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

import br.ufla.dcc.grubix.simulator.Interval;
import br.ufla.dcc.grubix.simulator.event.Interference;

/**
 * This class represents a single noise event as 
 * interval plus strength of the disturbing signal.
 * 
 * @author Dirk Held
 */
public class Noise extends Interval {

	/** The strength of the noise. */
	private double rssi;
	
	/**
	 * Constructor, where all values are extracted from an interference.
	 * 
	 * @param i The interference to get all values from.
	 */
	public Noise(Interference i) {		
		super(i.getSimStartTime(), i.getSendingTime());
		
		rssi = i.getReachability().getSsAtReceiver();
	}
	
	/**
	 * Constructor, where all values are supplied directly.
	 * 
	 * @param start    The starting time of the noise.
	 * @param duration The duration of the noise.
	 * @param rssi     The signal strength of the noise at the receiver.
	 */
	public Noise(double start, double duration, double rssi) {
		super(start, duration);
		
		this.rssi = rssi;
	}

	/**
	 * @param i    The interval to create the noise from.
	 * @param rssi The signal strength of the noise at the receiver.
	 */
	public Noise(Interval i, double rssi) {
		super(i.getStart(), i.getDuration());
		
		this.rssi = rssi;
	}

	/** @return the rssi. */
	public final double getRssi() {
		return rssi;
	}

	/**
	 * Method to intersect this noise interval with another one. If they 
	 * don't intersect, return null. The resulting noise intervals are
	 * trimmed, to not intersect with the result afterwards and still
	 * represent the join of the two intervals. The duration of the
	 * resulting noise intervals may become zero and if an interval is a 
	 * real subset of the other, it is modified, to represent the part of
	 * the larger interval after the intersection.
	 *  
	 * @param noise The second noise interval, to intersect this with.
	 * @return the intersection, if existent, with added rssi.
	 */
	public Noise intersect(Noise noise) {
		double end = getEnd(), end2 = noise.getEnd();
		Interval i = intersect(noise, true);
		
		Noise res = null;
		
		if (i != null) {
			res = new Noise(i, rssi + noise.rssi);
		}
		
		if (end == getStart()) {
			rssi = noise.getRssi();
		} else if (end2 == noise.getStart()) {
			noise.rssi = rssi;
		}
		
		return res;
	}

	/* (non-Javadoc)
	 * @see br.ufla.dcc.grubix.simulator.Interval#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "@" + rssi;
	}
}

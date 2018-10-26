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

import br.ufla.dcc.grubix.simulator.physical.Reachability;

/**
 * encapsulate an interference vector as a converted transmission extended by the 
 * computed signal strength and the absolute time, when the interfering 
 * transmission started.
 * 
 * @author Dirk Held
 */
public class Interference extends Transmission implements Comparable<Object> {

	/** signals strength and distance information. */
	protected Reachability reachability;
	
	/**
	 * Combines a transmission with the computed reachability information for a single node.
	 * 
	 * @param trans the transmission, to create an interference from.
	 * @param reachability the computed information of ss. at receiver and distance.
	 */
	public Interference(Transmission trans, Reachability reachability) {
		super(trans.getPacket(), trans.getSignalStrength());
		
		this.reachability = reachability;
	}
	
	/**
	 * returns true, if the given interval intersects with this interference.
	 * @param time     the starting time of the interval
	 * @param duration the length of the interval
	 * @return true, if an intersection occurred.
	 */
	public final boolean intersects(double time, double duration) {
		if (time + duration <= getSimStartTime()) {
			return false;
		} else if (getSimStartTime() + getSendingTime() <= time) {
			return false;
		}
		return true;
	}
	
	/**
	 * implements the comparing method, to include objects of this class into a sorted ArrayList.
	 * WARNING: the result for non-disjoint interferences is not correct.
	 * @param o the other object to compare this object to.
	 * @return the standard result of the compare operation one of {-1,0,1}
	 */
	public final int compareTo(Object o) {
		if ((o != null) && (o instanceof Interference)) {
			Interference i = (Interference) o;
			
			if (getPacket().getId() == i.getPacket().getId()) {
				return 0;
			} else {
				return compareTo(i.getSimStartTime(), i.getSendingTime());
			}
		} else {
			throw new NullPointerException("other interference missing or not of type Interference.");
		}
	}

	/**
	 * core compare method, to be able to compare versus a time interval
	 * and not just versus an object, as needed by java.
	 * @param startingTime the start time.
	 * @param duration     the duration of the interval.
	 * @return the standard compare result out of {-1,0,1}
	 */
	public final int compareTo(@SuppressWarnings("hiding") double startingTime, double duration) {
		if (getSimStartTime() < startingTime) {
			return -1;  
		} else {
			if (getSimStartTime() == startingTime) {
				if (getSendingTime() < duration) {
					return -1;
				} else {
					if (getSendingTime() == duration) {
						return 0;
					} else {
						return 1;
					}
				}
			} else {
				return 1;
			}
		}
	}
	
	/** @return the reachability */
	public final Reachability getReachability() {
		return reachability;
	}
}

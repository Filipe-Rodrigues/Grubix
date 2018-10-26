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
 * event to request carrier sensing from the AirModule. 
 * The result is sent back to the MacLayer.
 *  
 * @author dirk
 *
 */
public class AirPerformCarrierSense extends WakeUpCall {

	/** if set to true, a carrier was sensed and the cs is prolongued. */
	private boolean retry;
	
	/** minimal time, the carrier has to be free before the request returns. */
	private double minTime;
	
	/**
	 * additional variable time, the carrier has to be free after the minTime, 
	 * before the request returns. If returns earlier, the carrier wasn't free.
	 */
	private double varTime;
	
	/**
	 * perform carrier sensing for a given time and inform the Mac on a carrier or 
	 * the absence of a carrier after the given time.
	 * 
	 * @param sender the airlayer, where the garbage collection is to be performed.
	 * @param time    the delay, to wait before cs starts. 
	 * @param minTime the minimal time, the carrier has to be free. 
	 * @param varTime the duration of the variable carrier sensing after time has passed.
	 * @param retry   set to true, if the carrier sensing has to be retried.
	 */
	public AirPerformCarrierSense(Address sender, double time, double minTime, double varTime, boolean retry) {
		super(sender, time);
		this.minTime = minTime;
		this.varTime = varTime;
		this.retry = retry;
	}

	/**
	 * I wait in this place, where the sun never shines.
	 * @param sender the affected airlayer.
	 */
	public AirPerformCarrierSense(Address sender) {
		super(sender);
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return String for logging.
	 */
	public final String toString() {
		return "AirPerformCarrierSense " + super.toString();
	}

	/** @return the varTime */
	public final double getVarTime() {
		return varTime;
	}

	/** @param varTime the varTime to set */
	public final void setVarTime(double varTime) {
		this.varTime = varTime;
	}

	/** @return the minTime */
	public double getMinTime() {
		return minTime;
	}

	/** @return the retry */
	public final boolean isRetry() {
		return retry;
	}
}

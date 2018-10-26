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
 * this event is issued from the Airlayer, when a certain waiting period has
 * passed, without a carrier sensed or a carrier was sensed prior that time.
 * @author Dirk Held
 */
public class MACCarrierSensing extends MACEvent {

	/** the start of the variable carrier sense phase. */
	private double csStart;
	
	/** contains the result of the carrier sensing phase. */
	private boolean noCarrier;

	/**
	 * Constructor of the class MACCarrierSensing.
	 * Creates WakeUpCalls with delay for internal transmission (immediate callback).
	 * Receiver is set to nodeId of the sender. Contains the result of a cs-phase.
	 * 
	 * @param sender     sender node for the wakeUpEvent
	 * @param delay      the time to wait, before this event is processed.
	 * @param noCarrier  is true, if no carrier was detected for a requested period.
	 */
	public MACCarrierSensing(Address sender, double delay, boolean noCarrier) {
		super(sender, delay);
		this.noCarrier = noCarrier;
	}

	/**
	 * Constructor of the class MACCarrierSensing.
	 * Creates WakeUpCalls with delay for internal transmission (immediate callback).
	 * Receiver is set to nodeId of the sender.
	 * 
	 * @param sender sendernode for the wakeUpEvent
	 */
	public MACCarrierSensing(Address sender) {
		super(sender);
	}

	/**
	 * Default constructor of the class MACCarrierSensing.
	 * Time value is set to delay for internal transmission if value to small.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param time Wanted delay until callback.
	 */
	public MACCarrierSensing(Address sender, double time) {
		super(sender, time);
	}

	/** @return the noCarrier */
	public final boolean isNoCarrier() {
		return noCarrier;
	}

	/** @return the csStart */
	public final double getCsStart() {
		return csStart;
	}

	/** @param csStart the csStart to set */
	public final void setCsStart(double csStart) {
		this.csStart = csStart;
	}
}

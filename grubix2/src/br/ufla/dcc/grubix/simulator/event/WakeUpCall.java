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
 * Class for WakeUpEvents (also called Timers).
 * A sendernode can place this event to wake up the receiver at the specified
 * time wakeUpTime.
 * If several wakeups are used in parallel by a layer, define subtypes of this class for proper handling.
 * 
 * @author Andreas Kumlehn
 */
public class WakeUpCall extends ToLayer {
	
	/**
	 * The delay time for the wakeUpCall.
	 */
	private double delay;

	/**
	 * Default constructor of the class WakeUpCall.
	 * Time value is set to delay for internal transmission if value to small.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param delay Wanted delay until callback in simulation steps
	 */
	public WakeUpCall(Address sender, double delay) {
		//receiver = sender for WUCs
		super(sender, sender.getId());
		this.delay = delay;
	}
	
	/**
	 * Constructor of the class WakeUpCall.
	 * Creates WakeUpCalls with delay 0 (immediate callback).
	 * Receiver is set to nodeId of the sender.
	 * 
	 * @param sender sendernode for the wakeUpEvent
	 */
	public WakeUpCall(Address sender) {
		//receiver = sender for WUCs
		super(sender, sender.getId());
		this.delay = 0.0;
	}
	
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.event.Event#getDelay()
	 * @return Delay of the WUC as Double.
	 */
	public final double getDelay() {
		return this.delay;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * @return String for logging.
	 */
	public String toString() {
		return "WakeUpCall " + super.toString();
	}

	/** @param delay the new delay for an reused event. */
	public final void setDelay(double delay) {
		if (delay < 0.0) {
			delay = 0.0;
		}
		this.delay = delay;
	}
}

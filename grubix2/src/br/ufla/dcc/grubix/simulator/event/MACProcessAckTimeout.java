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
 * This wakeup-call is issued by the Mac itself, to check if
 * an Ack hasn't arrived yet and to take the approbiate action,
 * like resending or informing the upper layer of this problem.
 * 
 * @author Dirk Held
 */
public class MACProcessAckTimeout extends MACEvent {

	/**
	 * Constructor of the class MACprocessAckTimeout.
	 * Creates WakeUpCalls with delay for internal transmission (immediate callback).
	 * Receiver is set to nodeId of the sender.
	 * 
	 * @param sender sendernode for the wakeUpEvent
	 */
	public MACProcessAckTimeout(Address sender) {
		super(sender);
	}

	/**
	 * Default constructor of the class MACprocessAckTimeout.
	 * Time value is set to delay for internal transmission if value to small.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param time Wanted delay until callback.
	 */
	public MACProcessAckTimeout(Address sender, double time) {
		super(sender, time);
	}
}

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
 * marker super-class for all events to be processed by the LogLink-Layer.
 * @author dirk
 *
 */
public class LogLinkEvent extends WakeUpCall {

	/**
	 * Constructor of the class LogLinkEvent.
	 * Creates WakeUpCalls with delay 0 (immediate callback).
	 * Receiver is set to nodeId of the sender.
	 * 
	 * @param sender sendernode for the LogLinkEvent
	 */
	public LogLinkEvent(Address sender) {
		super(sender);
	}

	/**
	 * Default constructor of the class LogLinkEvent.
	 * 
	 * @param sender Sender of the LogLinkEvent.
	 * @param time Wanted delay until callback.
	 */
	public LogLinkEvent(Address sender, double time) {
		super(sender, time);
	}
}

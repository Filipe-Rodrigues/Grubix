/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net 
and the Orcos developers as defined unter http://orcos.cs.upb.de

********************************************************************************/
package br.ufla.dcc.grubix.simulator.node.user.os;



/**
 * A ported version of the normal WakeupCall used in services which cannot receive normal calls.
 * 
 * @author dmeister
 *
 */
public class PortedWakeUpCall extends PortedEvent {

	/**
	 * delay of the wakeup call.
	 */
	private final double delay;

	/**
	 * Constructor of the wakeup call.
	 * 
	 * @param endpoint endpoint of the wakeup call. This is the receiver and the sender of the call
	 * @param delay delay of the call
	 */
	public PortedWakeUpCall(ServiceEndpoint endpoint, double delay) {
		super(endpoint.getAddress().getId(), endpoint.getPort());
		if (delay < 0.0) {
			throw new IllegalArgumentException("delay");
		}
		this.delay = delay;
	}
	
	
	/**
	 * returns the call specific delay.
	 * @return a call specific delay
	 * @see br.ufla.dcc.grubix.simulator.event.ToNode#getDelay()
	 */
	@Override
	public double getDelay() {
		return delay;
	}
	
	
}

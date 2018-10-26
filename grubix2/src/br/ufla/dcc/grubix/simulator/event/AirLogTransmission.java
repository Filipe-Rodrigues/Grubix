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
import br.ufla.dcc.grubix.simulator.Interval;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/**
 * This event is issued, after all receiving nodes got this packet,tolog the transmission.
 * 
 * @author Dirk Held
 */
public class AirLogTransmission extends WakeUpCall {

	/** the packet, containing the receiver lists. */
	private PhysicalPacket p;
	/** the time, when this event was created = end of the transmission at the sender. */
	private double creationTime;
	/** contains the intervall of the transmission. */
	private Interval out;
	
	/**
	 * defaultconstructor of the log transmission event of the air module.
	 * @param sender the sender of this event.
	 * @param p      the packet belonging to this message.
	 * @param out    the interval, when the transmission happened.
	 */
	public AirLogTransmission(Address sender, PhysicalPacket p, Interval out) {
		super(sender, Configuration.getInstance().getPropagationDelay() * 2);
		
		creationTime = SimulationManager.getInstance().getCurrentTime();
		this.p   = p;
		this.out = out;
	}
	
	/** @return the packet of this event. */
	public PhysicalPacket getPacket() {
		return p;
	}

	/** @return the time, when this event was created. */
	public final double getCreationTime() {
		return creationTime;
	}

	/** @return the interval, whenn the transmission happened. */
	public final Interval getOut() {
		return out;
	}
}

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
import br.ufla.dcc.grubix.simulator.EventId;

/** 
 * Abstract WakeUpCall indicating that the sending/receiving of a packet
 * via radio (or the corresponding AirModule) has finished.
 * 
 * @author Andreas Kumlehn
 */
public abstract class TransmissionEnd extends WakeUpCall implements LoggableData {
	
	/** Id of the current transmission. */
	protected EventId transmissionId;	
	
	/** Id of the current packet in transmission. */
	protected EventId packetId;	

	/**
	 * Constructor of the class TransmissionEnd.
	 * 
	 * @param sender Sender of the WUC as Address.
	 * @param time SendingDelay of the packet.
	 * @param transmissionId EventId of the transmission.
	 * @param packetId EventId of the transmitted packet
	 */
	public TransmissionEnd(Address sender, double time, EventId transmissionId, EventId packetId) {
		super(sender, time);
		this.transmissionId = transmissionId;
		this.packetId = packetId;
	}

	/** @return The transmission ID. */
	public final EventId getTransmissionId() {
		return transmissionId;
	}

	/** @return The ID of the transmitted packet. */
	public final EventId getPacketId() {
		return packetId;
	}

	/**
	 * @return The IDs of the two conversating nodes in the transmission. This is helpful for
	 * the ShoX Monitor later on.
	 */
	public abstract String getData();
}

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

import br.ufla.dcc.grubix.simulator.SimulationFailedException;


/** 
 * Class to encapsulate transmitted packages during sending.
 * Used for proper handling in AirModule and storing data related
 * to the communication (for example signalStrength).
 * 
 * @author Andreas Kumlehn
 */
public class Transmission extends ToLayer implements Cloneable {
	
	/** Packet transmitted via this Transmission. */
	private PhysicalPacket packet;
	
	/**
	 * Reference to the Packet instance the sender uses.
	 * Used for attaching debug information in
	 * {@link br.ufla.dcc.grubix.simulator.node.AirModule#processTransmissionEndOutgoing}
	 */
	private PhysicalPacket senderPacket;
	
	/** Signal strength with which the sender sends. */
	private double signalStrength;

	/**
	 * Constructor of the class Transmission.
	 * 
	 * TODO add params here to store all data.
	 * 
	 * @param packet transmitted packet.
	 * @param signalStrength Signal strength with which the sender sends.
	 */
	public Transmission(PhysicalPacket packet, double signalStrength) {
		super(packet.getSender(), packet.getReceiver());
		this.packet = packet;
		this.signalStrength = signalStrength;
	}

	/** @return Returns the packet. */
	public final PhysicalPacket getPacket() {
		return packet;
	}

	/** @return Returns the signalStrength. */
	public final double getSignalStrength() {
		return signalStrength;
	}

	/** @return Returns the sendingTime in simulation steps!. */
	public final double getSendingTime() {
		return packet.getDuration();
	}

	/** @return the Simulation StartTime */
	public final double getSimStartTime() {
		return packet.getTime();
	}

	/**
	 * Sets the reference to the packet instance which is used by
	 * the sender of the transmission.
	 * 
	 * @param senderPacket instance of PhysicalPacket return by the
	 * 					   transmission of the sender
	 */
	public void setSenderPacket(PhysicalPacket senderPacket) {
		this.senderPacket = senderPacket;
		
	}

	/**
	 * Gets the reference to the packet instance which is used by
	 * the sender of the transmission.
	 * 
	 * @return instance of PhysicalPacket if this transmission is
	 * 		   processed at receiver side, else null
	 */
	public PhysicalPacket getSenderPacket() {
		return this.senderPacket;
	}
	
	/**
	 * Clones the transmission. 
	 * @return cloned transmission
	 */
	public Object clone() {
		/*
		 * INFO At the time this method was implemented all parent
		 * 		classes only use shallow copy for clone and as they
		 * 		all use immutable objects in their instance fields
		 * 		this is ok. If you one day detect strange behaviour
		 * 		regarding this clone the chance is high, that
		 * 		somebody added mutable fields to a class in the parent
		 * 		hierarchy. 
		 */
		Transmission theClone;
		
		try {
			theClone = (Transmission) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new SimulationFailedException("Could not clone Transmission.");
		}
		
		theClone.packet = (PhysicalPacket) this.packet.clone();
		
		return theClone;
	}
}

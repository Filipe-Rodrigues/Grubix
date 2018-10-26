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
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.event.user.WlanFramePacket;

/**
 * This class represents a dummy packet with delay 0.0 which is used by the physical layer
 * to encapsulate packets it has to send via the AirModule. The main purpose of this packet
 * is to allow physical-to-physical packets to be logged without introducing the internal
 * transmission delay.
 * @author jlsx
 */
@StaticPacketSize(0)
public class PhysicalPacket extends Packet implements Cloneable {
	
	/** set this flag, to set the receiver's radio state to WILL_SEND after reception. */
//	@NoHeaderData
	private boolean transitToWillSend;
	
	/**
	 * Flag indicating an unset BPS (bit per simulation step) value.
	 */
	private static final int BPSUNSET = -1;
	
	/** defines bits per STEP for the header only. */
	@NoHeaderData
	private double bps = BPSUNSET;
		
	/**
	 *  an additional time in simulation steps may be set here to describe 
	 *  the overhead used for example for sync.
	 */
//	@NoHeaderData
	private double syncDuration = 0.0;
	
	/**
	 * Constructor of the class PhysicalPacket.
	 * @param receiver NodeId of the receiver.
	 * @param sender Address of the sender.
	 */
	public PhysicalPacket(NodeId receiver, Address sender) {
		super(sender, receiver);
		
		transitToWillSend = false;
	}

	/**
	 * Constructor of the class PhysicalPacket.
	 * @param sender Address of the sender.
	 * @param packet Enclosed packet.
	 */
	public PhysicalPacket(Address sender, Packet packet) {
		super(sender, packet);
		
		transitToWillSend = false;
	}

	/**
	 * @see br.ufla.dcc.grubix.simulator.event.Event#getDelay()
	 * @return 0.0, since packets from the physical layer are transmitted immediately
	 */
	@Override
	public double getDelay() {
		return 0.0;
	}

	/** @return the sync duration in simulation steps. */
	public final double getSyncDuration() {
		return syncDuration;
	}

	/** @param headerDuration the headerDuration in simulation steps to set. */
	public final void setSyncDuration(double headerDuration) {
		if (changeable) {
			this.syncDuration = headerDuration;
		}
	}
	
	/**
	 * Returns the duration in simulation steps needed to transmit this packet.
	 * If the bits per STEP parameter is not set then it is first tried
	 * to obtain the BPS from the enclosed WlanFramePacket (this means
	 * that this Packet is not a general physical packet as it should be, but it
	 * is a PHY802-specific packet! A lot of refactoring and even redesigning would be
	 * needed if you intent to change this!). If this fails then a
	 * {@link SimulationFailedException} is thrown.
	 * 
	 * @return The number of simulation steps it takes to transmit this packet.
	 */
	public final double getDuration() {
		if (bps == BPSUNSET) {
			// due to some hierarchy problems concerning the WlanFramePacket, the PhysicalPacket and 
			// the implementation of the PHY802 and MAC802 this hack was required.
			// The physical layer should set the BPS. The current MAC802 implementation however does
			// set the BPS for WlanFramePackets only. Due to the refactoring of this getDuration-method
			// we have to get the BPS value from the wlanframe.
			if (getEnclosedPacket() != null && getEnclosedPacket() instanceof WlanFramePacket) {
				this.setBPS(((WlanFramePacket) getEnclosedPacket()).getBPS());
			}
			if (bps == BPSUNSET) {
				throw new SimulationFailedException("Bits per second not set for " + this);
			}
		}
		return syncDuration + this.getTotalPacketSizeInBit() / bps;
	}
	
	/**
	 * Sets the bits per simulation step.
	 * @param bps    the bits per simulation step.
	 */
	public void setBPS(double bps) {
		this.bps = bps;
	}
	
	/** 
	 * Returns the bit per simulation step value.
	 * @return the bit per simulation step value. 
	 */
	public double getBPS() {
		return bps;
	}

	/** @return true, if the receiver has to send an ack 
	 *  immediately and his radio state changes to WILL_SEND.
	 */
	public final boolean isTransitToWillSend() {
		return transitToWillSend;
	}

	/**
	 * Method to set the transitToWillSend in the physical layer, if
	 * the receiver of a unicast has to send an ack after reception.
	 * @param transitToWillSend true triggers the change of the radio state of the receiver.
	 */
	public final void setTransitToWillSend(boolean transitToWillSend) {
		this.transitToWillSend = transitToWillSend;
	}
}

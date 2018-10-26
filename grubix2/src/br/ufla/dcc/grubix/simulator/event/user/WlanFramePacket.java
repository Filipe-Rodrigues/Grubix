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

package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.NoHeaderData;
import br.ufla.dcc.grubix.simulator.node.BitrateAdaptationPolicy;
import br.ufla.dcc.grubix.simulator.node.PhysicalTimingParameters;
import br.ufla.dcc.grubix.simulator.node.user.IEEE_802_11_TimingParameters;

/** 
 * the 802.11.b wlan frame packet.
 * 
 * @author Dirk Held
 */
public class WlanFramePacket extends MACPacket implements Cloneable {
	
	/** Flag indicating an unset BPS (bit per simulation step) value. */
	private static final int BPSUNSET = -1;
	
	/** defines bits per STEP for the header only. */
	@NoHeaderData
	private double bps = BPSUNSET;
	
	/** defines bitrate index, if >= 0. */
	@NoHeaderData
	private int bitrateIdx;
	
	/**
	 *  an additional time may be set here to describe 
	 *  the overhead used for example for sync.
	 */
	//NoHeaderData
	//private double syncDuration = 0.0;
	
	/** the to be used rate adaptation policy for this packet. */
	@NoHeaderData
	private BitrateAdaptationPolicy raPolicy;
	
	/** is set to true, if the packet can be, but was not yet transmitted. */
	private boolean readyForTransmission;
	
	/**
	 * method to initialise some values upon creation.
	 * @param type the type of this packet.
	 */
	private void init(PacketType type) {
		readyForTransmission = true;
		bitrateIdx = -1;
		setType(type);
	}
	
	/*
	 * PLCP:    (16 Byte) 0xff x 16 preamble for sync, CS, freq. drift
	 * SFD :    ( 2 Byte) 0xf3a0 start frame delimiter
	 * Signal:  ( 1 Byte) ?????? DBPSK or DQPSK (1 or 2 MBit ???)
	 * Service: ( 1 Byte) 0
	 * Length:  ( 2 Byte) payload length in musec.
	 * HEC:     ( 2 Byte) CRC of the header.
	 *
	 */	
	/**
	 * Constructor of the class WlanFramePacket.
	 * 
	 * @param sender Address of the sender.
	 * @param receiver NodeId of the receiver.
	 * @param type of the packet.
	 * @param signalStrength The strength of the signal to transmit in mW
	 */
	public WlanFramePacket(Address sender, NodeId receiver, PacketType type, double signalStrength) {
		super(sender, receiver, signalStrength);
		
		switch (type) {
			case DATA : setHeaderLength(34); break;  
			case ACK  : setHeaderLength(14); break;  
			case RTS  : setHeaderLength(20); break;  
			case CTS  : setHeaderLength(14); break; 
			default : setHeaderLength(34);
		}
		init(type);
	}

	/**
	 * Constructor of the class WlanFramePacket.
	 * 
	 * @param sender Address of the sender.
	 * @param packet Enclosed packet.
	 */
	public WlanFramePacket(Address sender, LogLinkPacket packet) {
		super(sender, packet);
		
		setHeaderLength(24);
		init(PacketType.DATA);
	}

	/** @return the used rate adaptation policy. */
	public BitrateAdaptationPolicy getRaPolicy() {
		return raPolicy;
	}

	/** @param raPolicy the to be used rate adaptation policy. */
	public void setRaPolicy(BitrateAdaptationPolicy raPolicy) {
		this.raPolicy = raPolicy;
	}

	/** @return the current bitrate index. */ 
	public int getBitrateIdx() {
		if (bitrateIdx >= 0) {
			return bitrateIdx;
		} else {
			if (raPolicy == null) {
				return 0;
			} else {
				return raPolicy.getBitrateIdx();
			}
		}
	}
	
	/**
	 * setter of the bitrate index, used in apply bitrate.
	 * @param bitrateIdx the bitrate index to use.
	 */
	public final void setBitrateIdx(int bitrateIdx) {
		this.bitrateIdx = bitrateIdx;
	}
	
	/**
	 * implements the clone method to duplicate packets.
	 * @return a clone of the object.
	 */
	public WlanFramePacket clone() {
		WlanFramePacket obj = (WlanFramePacket) super.clone();
		
		obj.bitrateIdx = bitrateIdx;
		
		if (raPolicy == null) {
			obj.raPolicy = null;
		} else {
			obj.raPolicy = (BitrateAdaptationPolicy) raPolicy.clone();
		}
		return obj;
	}
	
	/**
	 * Returns the duration in simulation steps needed to transmit this packet.
	 * If the bits per step parameter is not set then a {@link SimulationFailedException} is thrown.
	 * 
	 * @return The number of simulation steps it takes to transmit this packet.
	 */
	public final double getDuration() {
		if (bps == BPSUNSET) {
			throw new SimulationFailedException("Bits per second not set for " + this);
		}
	
		return /*syncDuration +*/ this.getTotalPacketSizeInBit() / bps;
	}

	/**
	 * Sets the bits per simulation step from a specific bitrate index.
	 * @param timings the table of supported bitrates (per simulation step).
	 * @param bitrateIdx  the bitrate to use.
	 */
	public void setBPS(PhysicalTimingParameters timings, int bitrateIdx) {
		bps = timings.getBPS(bitrateIdx);
		this.bitrateIdx = bitrateIdx;
	}
	
	/**
	 * Sets the bits per simulation step from a supplied rate adaptation policy.
	 * @param timings the table of supported bitrates (per simulation step).
	 * @param raPolicy the to be used rate adaptation policy.
	 */
	public void setBPS(PhysicalTimingParameters timings, BitrateAdaptationPolicy raPolicy) {
		bitrateIdx = raPolicy.getBitrateIdx();
		bps = timings.getBPS(bitrateIdx);
		this.raPolicy = raPolicy;
	}

	/**
	 * Sets the bits per simulation step.
	 * @param bps    the bits per simulation step.
	 *
	public void setBPS(double bps) {
		this.bps = bps;
	}*/
	
	/** 
	 * Returns the bit per simulation step value.
	 * @return the bit per simulation step value. 
	 */
	public double getBPS() {
		return bps;
	}
		
	/** @return the sync duration. *
	public final double getSyncDuration() {
		return syncDuration;
	}*/

	/** @param headerDuration the headerDuration to set. *
	public final void setSyncDuration(double headerDuration) {
		if (changeable) {
			this.syncDuration = headerDuration;
		}
	}*/

	/** @return true, if this packet is ready to be transmitted. */
	public final boolean isReadyForTransmission() {
		return readyForTransmission;
	}

	/**
	 * method to set the read for transmission flag.
	 * @param readyForTransmission the new value for the ready for transmission flag.
	 */
	public final void setReadyForTransmission(boolean readyForTransmission) {
		this.readyForTransmission = readyForTransmission;
	}
}

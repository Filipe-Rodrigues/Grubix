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

package br.ufla.dcc.grubix.simulator.node;

import java.util.ArrayList;

import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.user.WlanFramePacket;




/**
 * This class represents a status for any MAC layer.
 * @author jlsx
 */
public class MACState extends LayerState {
	
	/** stores the current used BPS as index into payloadBPS. */
	private BitrateAdaptationPolicy raDefaultPolicy;
	/** the possibly on the bitrate dependent maximum transmission power. */ 
	private double maxTransmissionPower;
	/** contains the number of to be transmitted packets. */
	private int outQueueSize;
	/** true if MAC is in promiscuous and delivers ALL received data packet to upperLayer. */ 
	private boolean promiscuous;
	/** This queue is only set, if the parameter "includeQueueInState" of the 802.11 MAC is set to true. */
	private ArrayList<WlanFramePacket> queue;
	
	/**
	 * Default constructor to initialize the used BPS values. The use
	 * outside of the MAC_IEEE802_11bg_AdHoc.class is prohibited.
	 * 
	 * @param raDefaultPolicy the current used rate adaption policy.
	 * @param maxPower The maximum transmission power
	 * @param outQueueSize The number of packets to be transmitted in the queue
	 */
	public MACState(BitrateAdaptationPolicy raDefaultPolicy, double maxPower, int outQueueSize) {
		this.raDefaultPolicy = raDefaultPolicy;
		maxTransmissionPower = maxPower;
		this.outQueueSize = outQueueSize;
		this.promiscuous = false;
		queue = null;
	}
	
	/**
	 * Overloaded constructor to initialize the used BPS values. The use
	 * outside of the MAC_IEEE802_11bg_* classes is prohibited.
	 * @param raDefaultPolicy the current used rate adaption policy.
	 * @param maxPower The maximum transmission power
	 * @param outQueueSize The number of packets to be transmitted in the queue
	 * @param promniscous indicates whether or not the MAC shall forward all received 
	 * 	DATA packets to LLC and not just the ones sent to this node
	 */
	public MACState(BitrateAdaptationPolicy raDefaultPolicy, double maxPower, int outQueueSize, boolean promniscous) {
		this.raDefaultPolicy = raDefaultPolicy;
		maxTransmissionPower = maxPower;
		this.outQueueSize = outQueueSize;
		this.promiscuous = promniscous;
		queue = null;
	}
	
	/** @return the raDefaultPolicy */
	public final BitrateAdaptationPolicy getRaDefaultPolicy() {
		return raDefaultPolicy;
	}

	/** @return the maxTransmissionPower */
	public final double getMaxTransmissionPower() {
		return maxTransmissionPower;
	}

	/**
	 * @return the outQueueSize
	 */
	public final int getOutQueueSize() {
		return outQueueSize;
	}
	
	/**
	 * Sets the number of packets currently in the MAC's out queue scheduled for transmission.
	 * @param size The number of packets with pending transmission
	 */
	public void setOutQueueSize(int size) {
		this.outQueueSize = size;
	}
	
	/** @return true if promiscuous is true. */
	public final boolean getPromiscuous() {
		return promiscuous;
	}

	/** @return the queue, if includeQueueInState in the 802.11 MAC is set to true. */
	public final ArrayList<WlanFramePacket> getQueue() {
		return queue;
	}

	/** @param queue the queue to set. */
	public final void setQueue(ArrayList<WlanFramePacket> queue) {
		this.queue = queue;
	}
}

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

import java.util.Map;

import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.Interval;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.physical.InterferenceQueue;




/**
 * This class represents the current state of the artificial air layer.
 * 
 * @author Dirk Held
 */
public class AirState extends LayerState {

	/** the current variable carrier sensing interval. */
	private final Interval csInterval;
	
	/** the interval, when the AirModule has to perform carrier sense. */
	private final Interval lastInterference;
	
	/** the interval, when the AirModule has to perform carrier sense. */
	private final Interval lastIncoming;
	
	/** the queue to collect current interferences. */
	private final InterferenceQueue interQueue;
	
	/**MAC_IEEE802_11bg_AdHoc The currently transmitted packet. Null if nothing on air. */
	private final Interval outgoingI;
	
	/** Map of current incoming transmission. */
	private final Map<EventId, Transmission> currentTransmission;
	
	/**
	 * This is used to be able to check the channel for activity
	 * without receiving the incoming packets.
	 * Therefore if this is activated, the AirModule will not switch to
	 * RadioState.RECEIVING and will stay in RadioState.LISTENING, when
	 * an incoming transmission is detected.
	 */
	private boolean isOnlyCarrierSensing;
	
	/**
	 * default constructor for the AirState class.
	 * 
	 * @param outgoingI           the current outgoing transmission, if any.
	 * @param csInterval          the current period for carrier sensing.
	 * @param lastInterference    the latest occurred interference.
	 * @param interQueue          the history of all happened interferences after the last garbage collection.
	 * @param lastIncoming        the interval of a current sensed carrier coming from one or multiple incoming 
	 *                            packets, which may collide but are coming from another node in transmission range.
	 * @param currentTransmission the transmission currently happening.
	 * @param isOnlyCarrierSensing whether the channel is only checked
	 * 							   for activity (=true) or really receives
	 * 							   incoming packets (=false)
	 */
	public AirState(Interval outgoingI, Interval csInterval, Interval lastInterference, 
				    InterferenceQueue interQueue, Interval lastIncoming, 
				    Map<EventId, Transmission> currentTransmission, boolean isOnlyCarrierSensing) {
		
		this.outgoingI = outgoingI;
		this.csInterval = csInterval; 
		this.lastInterference = lastInterference;
		this.interQueue = interQueue;
		this.lastIncoming = lastIncoming;
		this.currentTransmission = currentTransmission;
		this.isOnlyCarrierSensing = isOnlyCarrierSensing;
	}

	/** @return the current carrier sensing interval. */
	public final Interval getCsInterval() {
		return csInterval;
	}

	/** @return the current incoming transmissions. */
	public final Map<EventId, Transmission> getCurrentTransmission() {
		return currentTransmission;
	}

	/** @return the interference queue. */
	public final InterferenceQueue getInterQueue() {
		return interQueue;
	}

	/** @return the last interference. */
	public final Interval getLastInterference() {
		return lastInterference;
	}

	/** @return the currently outgoing packet. */
	public final Interval getOutgoing() {
		return outgoingI;
	}

	/** @return the lastIncoming. */
	public final Interval getLastIncoming() {
		return lastIncoming;
	}

	/**
	 * Gets whether the channel is only checked for activity without
	 * receiving the incoming packets.<br>
	 * See field {@link AirState.isOnlyCarrierSensing} for a full
	 * description.
	 * 
	 * @return true if channel is checked only for activity<br>
	 * 		   false if channel will also receive incoming packets
	 */
	public final boolean isOnlyCarrierSensing() {
		return this.isOnlyCarrierSensing;
	}

	/**
	 * Sets whether the channel is only checked for activity without
	 * receiving the incoming packets.<br>
	 * See field {@link AirState.isOnlyCarrierSensing} for a full
	 * description.
	 * 
	 * @param isOnlyCarrierSensing use true if channel shall only be
	 * 							   checked for activity<br>
	 * 		   					   use false if channel shall also
	 * 							   receive incoming packets
	 */
	public void setOnlyCarrierSensing(boolean isOnlyCarrierSensing) {
		this.isOnlyCarrierSensing = isOnlyCarrierSensing;
	}
}

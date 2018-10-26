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

import br.ufla.dcc.grubix.simulator.node.PhysicalLayerParameter;
import br.ufla.dcc.grubix.simulator.node.PhysicalTimingParameters;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.node.devices.transceiver.GenericTransceiver;
import br.ufla.dcc.grubix.simulator.node.energy.BasicEnergyManager;
import br.ufla.dcc.grubix.simulator.node.energy.EnergyManager;

/**
 * Contains the dynamic parameters of the Physical Layer.
 * 
 * @author Dirk Held
 */
public class PhysicalLayerState extends LayerState {

	/** the current signal strength, when a packet is sent. */
	private double currentSignalStrength;
	/** the minimal used signal strength. */
	private double minimumSignalStrength;
	/** the maximal possible signal strength. */
	private double maximumSignalStrength;
	/** the current state of the radio. */
	private RadioState radioState = RadioState.LISTENING;
	/** the current Frequency of the radio. */
	private double currentFrequency;
	/** the current tuned channel. */
	private int    currentChannel = 0;
	/** the number of supported Channels. */
	private int channelCount;
	/** bandwidth of each channel. */
	private double bandwidth;
	/** the minimal Frequency. */
	private double minFrequency;
	/** the maximal Frequency. */
	private double maxFrequency;
	/** the timings of the 802.11 Phy. */
	private PhysicalTimingParameters timings;
	
	/**
	 * Default constructor for the internal state class of the PhysicalDebug Layer.
	 * @param timings				TODO
	 * @param param					The static parameters for the 802.11bg MAC Layer
	 * @param radioState            The current state of the radio.
	 * @param currentChannel        The current used channel.
	 * @param currentSignalStrength The current signal strength.
	 * @param dropInvalid       	the current or new policy of dropping/forwarding invalid packets.
	 */
	public PhysicalLayerState(PhysicalTimingParameters timings, PhysicalLayerParameter param, 
							RadioState radioState, int currentChannel, double currentSignalStrength) {
		if (param != null) {
			channelCount = param.getChannelCount();
			minFrequency = param.getMinFrequency();
			maxFrequency = param.getMaxFrequency();
		
			minimumSignalStrength = param.getMinSignalStrength();
			maximumSignalStrength = param.getMaxSignalStrength();
		}
		
		if (channelCount < 1) {
			channelCount = 1;
		}
		
		if (minFrequency < 1.0) {
			minFrequency = 1.0;
		}
		
		if (maxFrequency < minFrequency) {
			maxFrequency = minFrequency;
		}
		
		if (minimumSignalStrength < 1.0) {
			minimumSignalStrength = 1.0;
		}
		
		if (maximumSignalStrength < minimumSignalStrength) {
			maximumSignalStrength = minimumSignalStrength;
		}
		
		bandwidth     = (maxFrequency - minFrequency) / channelCount;
		minFrequency += 0.5 * bandwidth;
		
		if ((currentChannel < 0) || (currentChannel >= channelCount)) {
			currentChannel = 0;
		}
		
		if (currentSignalStrength < minimumSignalStrength) {
			currentSignalStrength = minimumSignalStrength;
		}
		
		if (currentSignalStrength < maximumSignalStrength) {
			currentSignalStrength = maximumSignalStrength;
		}
		
		this.radioState            = radioState;
		this.currentChannel        = currentChannel;
		this.currentSignalStrength = currentSignalStrength;
		this.currentFrequency      = minFrequency + bandwidth * this.currentChannel;    
		this.timings     		   = timings;
	}
	
	/** @return the currentChannel */
	public final int getCurrentChannel() {
		return currentChannel;
	}

	/** @param currentChannel the currentChannel to set */
	public final void setCurrentChannel(int currentChannel) {
		if ((currentChannel >= 0) && (currentChannel < channelCount)) {
			this.currentChannel   = currentChannel;
			this.currentFrequency = this.minFrequency + this.bandwidth * this.currentChannel;    
		}
	}

	/** @return the currentSignalStrength */
	public final double getCurrentSignalStrength() {
		return currentSignalStrength;
	}

	/** @param currentSignalStrength the currentSignalStrength to set */
	public final void setCurrentSignalStrength(double currentSignalStrength) {
		if ((currentSignalStrength >= minimumSignalStrength) 
			&& (currentSignalStrength <= maximumSignalStrength)) {
			this.currentSignalStrength = currentSignalStrength;
		}
	}

	/** @return the radioState */
	public final RadioState getRadioState() {
		return radioState;
	}

	/** @param radioState the radioState to set */
	public final void setRadioState(RadioState radioState) {
		this.radioState = radioState;
	}

	/** @return the channelCount */
	public final int getChannelCount() {
		return channelCount;
	}

	/** @return the currentFrequency */
	public final double getCurrentFrequency() {
		return currentFrequency;
	}

	/** @return the maximalSignalStrength */
	public final double getMaximumSignalStrength() {
		return maximumSignalStrength;
	}

	/** @return the minimalSignalStrength */
	public final double getMinimumSignalStrength() {
		return minimumSignalStrength;
	}

	/**
	 * @return the timings
	 */
	public final PhysicalTimingParameters getTimings() {
		return timings;
	}
}

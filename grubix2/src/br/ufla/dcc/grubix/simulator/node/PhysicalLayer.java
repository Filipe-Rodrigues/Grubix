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

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.SimulationState;
import br.ufla.dcc.grubix.simulator.node.devices.transceiver.GenericTransceiver;
import br.ufla.dcc.grubix.simulator.node.energy.BasicEnergyManager;
import br.ufla.dcc.grubix.simulator.node.energy.EnergyManager;
import br.ufla.dcc.grubix.simulator.node.user.PhysicalDebug;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/** 
 * Abstract superclass for the physical layer of a network stack.
 * 
 * @author Andreas Kumlehn 
 */
public abstract class PhysicalLayer extends Layer {
	
	/** contains the supported bitrates. */
	protected PhysicalTimingParameters timings;

	/**
	 * The current state of the radio.
	 * 
	 * You may only change the current state using the setState()
	 * method, as the use of this method ensures that the EnergyManager
	 * is properly informed about the change.  
	 */
	protected RadioState radioState;

	/**
	 * If neighbor detection happens at a time, when the (later) sending nodes radio is off
	 * and similar for the receiver, the collected neighborhood information is useless/invalid.
	 * Although nodes radio may be off, it may be still considered part of a neighborhood, since
	 * a possibly (later) sending node knows (hopefully), when to send. This is particularly the
	 * case for TDMA-MACs.
	 */
	@ShoXParameter(description = "ignore the radio state of the sending or receiving node", defaultValue="false")
	private boolean ignoreRadioStateOnNeighborhoodDetection;

	/**
	 * The default signal strength to use in mW.
	 */
	@ShoXParameter(description = "The default signal strength to use in mW.", defaultValue = "16.0")
	protected double signalStrength;
	
	/**
	 * The minimal choosable signal strength value in mW.
	 */
	@ShoXParameter(description = "The minimal choosable signal strength value in mW.", defaultValue = "10.0")
	protected double minSignalStrength;
	
	/**
	 * The maximal choosable signal strength value in mW.
	 */
	@ShoXParameter(description = "The maximal choosable signal strength value in mW.", defaultValue = "100.0")
	protected double maxSignalStrength;
	
	/**
	 * The number of nonintersecting usable channels.
	 */
	@ShoXParameter(description = "The number of nonintersecting usable channels.", defaultValue = "1")
	protected int channelCount;
	
	/**
	 * The number of nonintersecting usable channels.
	 */
	@ShoXParameter(description = "The lower end of the frequency band.", defaultValue = "2.4e9")
	protected double minFrequency;
	
	/**
	 * The upper end of the frequency band.
	 */
	@ShoXParameter(description = "The upper end of the frequency band.", defaultValue = "2.4e9")
	protected double maxFrequency;

	/** Constructor. */
	public PhysicalLayer() {
		super(LayerType.PHYSICAL);
	}
	
	/**
	 * Method to initialize/finalize this layer.
	 * @param simState Initialize/Finalize event to start up / terminate the layer.
	 */
	@Override
	public final void processEvent(SimulationState simState) {
		if (simState instanceof Initialize) {
			standardPhyInit();
		}
		super.processEvent(simState);
	}

	/**
	 * Used to obtain information about the current state / settings of the PhysicalLayer.
	 * This information include radio state, signal strength, and some more
	 * 
	 * @see PhysicalLayerParameter
	 * @see PhysicalLayerState
	 * @return an instance of PhysicalLayerState
	 */
	public LayerState getState() {
		PhysicalLayerParameter param = new PhysicalLayerParameter(
				this.signalStrength, this.minSignalStrength, this.maxSignalStrength,
				this.channelCount, this.minFrequency, this.maxFrequency);
		
		return new PhysicalLayerState(this.timings, param, this.radioState, 0,
									  this.signalStrength);
	}
	
	/**
	 * Sets physical layer state properties
	 * like radio state, signal strength and if invalid packets should be dropped.
	 * 
	 * @param state the new desired state of this layer.
	 * @return true if the state change was accepted, false if state change was not accepted
	 */
	public boolean setState(LayerState state) {
		if (state instanceof PhysicalLayerState) {
			PhysicalLayerState theState = (PhysicalLayerState) state;
		
			if (this.radioState != theState.getRadioState()) {
				this.radioState = theState.getRadioState();
			
				EnergyManager energyManager = node.getEnergyManager();
				if (energyManager instanceof BasicEnergyManager) {
					GenericTransceiver transceiver =
						((BasicEnergyManager) energyManager).getConsumerTransceiver();
					transceiver.switchToRadioState(this.radioState);
				}
			}
			
			this.signalStrength = theState.getCurrentSignalStrength();
			
			return true;
		} else {
			return false;
		}
	}

	/** standard method to read the parameters common for all physical layers. */
	protected final void standardPhyInit() {
		PhysicalLayerState theState = (PhysicalLayerState) this.getState();
		theState.setRadioState(RadioState.LISTENING);
		this.setState(theState);
	}

	/** @return the ignoreRadioStateOnNeighborhoodDetection. */
	public final boolean isIgnoreRadioStateOnNeighborhoodDetection() {
		return ignoreRadioStateOnNeighborhoodDetection;
	}
}

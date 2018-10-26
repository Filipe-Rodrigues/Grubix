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

package br.ufla.dcc.PingPong.node.user;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.node.PhysicalLayer;
import br.ufla.dcc.grubix.simulator.node.PhysicalTimingParameters;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.user.PhysicalLayerDebugState;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/** 
 * Simple layer for testing. Sends with a default modulation rate of 1Mbps.
 * 
 * @author Andreas Kumlehn
 */
public class PhysicalDebug extends PhysicalLayer {

	/** set to "false" from the configuration, to even upward invalid packets. */
	@ShoXParameter(description = "set to false, to even upward invalid packets.", defaultValue = "true")
	private boolean dropInvalidPackets;
	
	
	/** constructor with a special NoParameter param object. */
	public PhysicalDebug() {
		super();
	}

	/** Logger of the class. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(PhysicalDebug.class.getName());

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#lowerSAP(br.ufla.dcc.PingPong.event.Packet)
	 * 
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public void lowerSAP(Packet packet) {
		if (!packet.isTerminal()) {
			// radioState = PhysicalLayerState.LISTENING; already done in AirModule
			// SimulationManager.enqueue(packet.getEnclosedPacket(), id, getThisLayer().getUpperLayer());
			
			if (packet.isValid() || !dropInvalidPackets) {
				if (!packet.isValid()) {
					LOGGER.warn("forwarding invalid packet up.");
				}
				sendPacket(packet.getEnclosedPacket());
			}
		}
	}

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#upperSAP(br.ufla.dcc.PingPong.event.Packet)
	 * @param packet to process coming from higher layer.
	 */
	@Override
	public void upperSAP(Packet packet) {
		if (!(packet instanceof MACPacket)) {
			LOGGER.debug("Physical layer cannot handle packets of type "
					+ packet.getClass().getName());
			return;
		}

		MACPacket mp = (MACPacket) packet;
		PhysicalPacket nextPacket = new PhysicalPacket(getSender(), mp);
		if (nextPacket.isTransitToWillSend())
		   System.out.println("Retransmission required");
		
		//if (radioState != PhysicalLayerState.SENDING) {
		// TODO what todo on OFF or RECEIVING ?
		if (radioState == RadioState.WILL_SEND)
		{
		  this.signalStrength = mp.getSignalStrength();
		  nextPacket.setBPS(1000000 / this.getConfig().getSimulationSteps(1.0));

		  //radioState = PhysicalLayerState.SENDING;
		  this.getNode().transmit(nextPacket, signalStrength);
		} else
		{
			LOGGER.warn("Node " + id + " currently sending and discarding events!" + " Radio in the state" + radioState.name());
		}
		/*} else {
		 //TODO implement queue in the MAC
		 LOGGER.warn("Node " + id + " currently sending and discarding events!");
		 }*/
	}
	
	/**
	 * method to process the wakeup calls for this layer.
	 * @param wuc the to be processed wakeup call.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		if (wuc instanceof CrossLayerEvent) {
			((CrossLayerEvent) wuc).forwardUp(this);
		} else {
			super.processWakeUpCall(wuc);
		}
	}

	/**
	 * currently no states supported, thus no statechanges possible.
	 * usefull changeable states: - sendingPower: <dBm>
	 * 							  - frequency/channel
	 * 							  - encoding
	 *							  - BPS,BER,younameit
	 *                            - radio-state: busySending,busyReceiving,listening,idle
	 * @return the current state of the PhysicalDebug Layer.
	 */
	public LayerState getState() {
		return new PhysicalLayerDebugState(timings, null, radioState, 0, signalStrength, dropInvalidPackets);
	}

	/* (non-Javadoc)
	 * @see br.ufla.dcc.PingPong.node.PhysicalLayer#setState(br.ufla.dcc.PingPong.event.LayerState)
	 */
	@Override
	public boolean setState(LayerState state) {
		if (state instanceof PhysicalLayerDebugState) {
			dropInvalidPackets = ((PhysicalLayerDebugState) state).isDropInvalidPackets();
		}
		
		return super.setState(state);
	}

	/** 
	 * method to initialize the configuration of the object. 
	 * It is assured by the runtime system that the <code>Configuration</code> 
	 * is valid, when this method is called. 
	 *  
	 * @throws ConfigurationException thrown when the object cannot run with 
	 * the configured values. 
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);

		double bps = 1000000.0 / configuration.getSimulationSteps(1.0);
		double[] bitsPerSimulationStep = { bps };
		timings = new PhysicalTimingParameters(bitsPerSimulationStep);
		
		signalStrength = 16.0;
		
		PhysicalLayerState theState = (PhysicalLayerState) this.getState();
		theState.setRadioState(RadioState.LISTENING);
		this.setState(theState);
	}
}

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

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.TransmissionFailedEvent;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.traffic.TrafficGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;


/** 
 * Abstract superclass for all implementations of ApplicationLayers.
 * 
 * @author Andreas Kumlehn
 */
public abstract class ApplicationLayer extends Layer {

	/** Logger of this class. */
	private static final Logger LOGGER = Logger.getLogger(ApplicationLayer.class.getName()); 
	
	/** the current state of the application. */
	protected ApplicationState appState = ApplicationState.CREATED;
	
	/** Constructor. */
	public ApplicationLayer() {
		super(LayerType.APPLICATION);
	}
	
	/**
	 * inits the application layer.
	 * This implementation checks the packet type count of the layer and
	 * throws an exception if the packet type count is 0. In addition to that
	 * the layer sets the packet type count of the traffic generator.
	 * 
	 * @param configuration reference to the configuration of the current
	 * simulation run.
	 * @see TrafficGenerator
	 * @throws ConfigurationException thrown if configuration is invlaid
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		int packetTypeCount = getPacketTypeCount();
		
		if (packetTypeCount == 0) {
			throw new ConfigurationException("The application instance " + getClass().getName()
						+ " must support at least 1 type of packets to be generated.");
		}
		TrafficGenerator.setPacketTypeCount(packetTypeCount);
	}
	
	/**
	 * ApplicationLayers have no upperSAP, throw exception.
	 * @param packet Packet to process.
	 * @throws LayerException because upperSAP should never be used.
	 */
	public final void upperSAP(Packet packet) throws LayerException {
		throw  new LayerException(
				"ApplicationLayer " + this.getClass().getName()
				+ " received " + packet.getClass().getName()
				+ " on upperSAP!");
	}
		

	
	
	/**
	 * Requests this application to generate application-level traffic according to the paramters
	 * specified in <code>tg</code>.
	 * @param tg Traffic generation request with all necessary details
	 */
	public abstract void processEvent(TrafficGeneration tg);
	
	/**
	 * The traffic generator in ShoX can generate different kinds of packets if the application
	 * supports that. In an application, there might be all sorts of packets that would be sent
	 * across the network following some user input. To model that, the number of packet types
	 * supported by the particular instance of the application must be specified.
	 * @return The number of packet types which the application supports
	 */
	public abstract int getPacketTypeCount();
	
	/**
	 * Handles {@link TransmissionFailedEvent}.
	 * @param wuc The received {@link WakeUpCall}.
	 * @throws LayerException if an unhandled {@link WakeUpCall} occurs.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		if (wuc instanceof TransmissionFailedEvent) {
			TransmissionFailedEvent ostfe = (TransmissionFailedEvent) wuc;
			
			LOGGER.info("Failed to transmit packet: " + ostfe.getPacket());
		} else {
			super.processWakeUpCall(wuc);
		}
	}
}

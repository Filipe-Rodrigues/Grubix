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

package br.ufla.dcc.grubix.simulator.node.user;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.MACProcessAckTimeout;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.TransmissionFailedEvent;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.user.MACAdvanceQueue;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.BitrateAdaptationPolicy;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.MACState;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.xml.ConfigurationException;



/** 
 * Simple layer for testing.
 * 
 * @author Andreas Kumlehn
 */
public class MACDebug extends MACLayer {

	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(MACDebug.class.getName());
	
	/** Polling time to retry transmission. */
	public double RETRY;

	@Override
	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);
		
		RETRY = config.getSimulationSteps(0.0004);
	}

	/** The internal state of the MAC that can be modified from outside. */
	private MACState state;
	
	/** The simulation time when the last transmission in the queue is finished. */
	private double endOfLastTransmission;
	
	/** The packet that this MAC currently tries to transmit. */
	private MACPacket currentPacket;
	
	/** constructor with a special NoParameter param object. */
	public MACDebug() {
		this.raDefaultPolicy = new BitrateAdaptationPolicy(0, 0);
		this.endOfLastTransmission = 0.0;
	}

	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * 
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public final void lowerSAP(Packet packet) {
		if (!(packet instanceof MACPacket)) {
			LOGGER.error("MAC received a packet of type " + packet.getClass().getName()
					+ ", but expected a MACPacket.");
			return;
		}
		
		MACPacket mp = (MACPacket) packet;
		NodeId senderId = packet.getSender().getId();
		boolean isTerminal = packet.isTerminal();
		boolean ownPacket = senderId == this.getNode().getId();
		boolean forThisNode = (packet.getReceiver().asInt() == id.asInt());
		boolean forAllNodes = (packet.getReceiver() == NodeId.ALLNODES);
		
		if (!isTerminal) {
			if (!ownPacket && (forThisNode || forAllNodes)) {
				sendPacket(packet.getEnclosedPacket());
			}
		}		
	}

	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#upperSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * @param packet to process coming from higher layer.
	 */
	@Override
	public final void upperSAP(Packet packet) {
		if (!(packet instanceof LogLinkPacket)) {
			LOGGER.error("MAC received packet from the logical link layer "
					+ "that is not a logical link packet, but rather of type " + packet.getClass().getName());
			return;
		}
		boolean ownPacket = packet.getReceiver() == id;
		if (ownPacket) {
			packet.flipDirection();
			sendPacket(packet);
			return;
		}

		LogLinkPacket lp = (LogLinkPacket) packet;
		MACPacket nextPacket = new MACPacket(sender, lp);
		
		double curTime = this.getNode().getCurrentTime();
		double transmissionTime = this.endOfLastTransmission;
		double sendingTime = 1.0;

		if (this.endOfLastTransmission > curTime) {
			this.endOfLastTransmission += sendingTime;
		} else {
			this.endOfLastTransmission = curTime + sendingTime;
			transmissionTime = curTime;
		}
		
		MACAdvanceQueue maq = new MACAdvanceQueue(sender, transmissionTime - curTime, nextPacket);
		sendEventSelf(maq);
	}


	/**
	 * Is invoked because a new packet is to be sent to the physical layer.
	 * @param wuc The MACAdvanceQueue event that contains the next packet in the MAC queue
	 */
	public final void processWakeUpCall(WakeUpCall wuc) {
		if (wuc instanceof MACAdvanceQueue) {
			
			PhysicalLayerState phyState = (PhysicalLayerState) getNode().getLayerState(LayerType.PHYSICAL);
			RadioState radioState = phyState.getRadioState();
			boolean radioFree = radioState == RadioState.LISTENING;
			MACAdvanceQueue maq = (MACAdvanceQueue) wuc;
			currentPacket = (MACPacket) maq.getPacket();
			if (radioFree)
			{
			  sendPacket(currentPacket);
			  this.currentPacket = null;
			}
			else
			{
				MACAdvanceQueue z = new MACAdvanceQueue(sender, RETRY, currentPacket);
				sendEventSelf(z);
			}
		}
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#processEvent(br.ufla.dcc.grubix.simulator.event.Initialize)
	 * @param init Initialize event to start up the layer.
	 */
	@Override
	protected final void processEvent(Initialize init) {
		int size = 0;
		
		if (currentPacket != null) {
			size++;
		}
		
		state = new MACState(raDefaultPolicy, 16.0, size);
	}

	/** @return the state. */
	@Override
	public MACState getState() {
		return state;
	}

	/**
	 * currently no states needed, thus no statechanges possible.
	 * @param state the new desired state of this layer.
	 * @return true if the statechange was accepted.
	 */
	public boolean setState(LayerState state) {
		if (!(state instanceof MACState)) {
			return false;
		}
		this.state = (MACState) state;
		return true;
	}
}
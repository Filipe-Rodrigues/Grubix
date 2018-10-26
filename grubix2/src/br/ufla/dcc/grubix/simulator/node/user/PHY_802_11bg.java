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

import br.ufla.dcc.grubix.simulator.Interval;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.user.RejectedPacketEvent;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayer;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayerParameter;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * 802.11 physical layer.
 * 
 * @author Andreas Kumlehn, Dirk Held
 */
public class PHY_802_11bg extends PhysicalLayer {

	/**
	 * object to hold all needed timing parameters,
	 * generated from the 802.11 Phy.
	 * 
	 * WARN PhysicalLayer contains a instance field timings, too!
	 */
	private static IEEE_802_11_TimingParameters timings;

	/**
	 * use b for 802.11b and g for 802.11g.
	 */
	@ShoXParameter(description = "use b for 802.11b and g for 802.11g.", defaultValue = "g")
	private String standard;

	/**
	 * use long or short to set the preamble. 802.11g implies a short preamble.
	 */
	@ShoXParameter(description = "use long or short to set the preamble. "
			+ "802.11g implies a short preamble", defaultValue = "short")
	private String preamble;

	/** set to "false" from the configuration, if no exception should be thrown if two valid intersecting packets arrive. */
	@ShoXParameter(description = "throw exception, if two intersecting and valid incoming packets are detected.", defaultValue = "true")
	private boolean exceptionOnInterferingValidPackets;
	
	/** count the number of dropped packets. */
	private int droppedPacket;
	/** count the number of discarded packets. */
	private int discardedPackets;
	/** the interval occupied by the last received packet. */
	private Interval lastReceivedPacketInterval;
	
	private Packet lastSentPacket;
	
	/** default constructor, which non recurring sets the parameter object. */
	public PHY_802_11bg() {
		super();
	}

	/** @throws ConfigurationException if the supplied configuration is erroneous. */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		
		boolean isB = standard.equals("b");
		if (!isB && !standard.equals("g")) {
			throw new ConfigurationException("MacModule of Node " + id
					+ " unsupported 802.11 standard " + standard);
		}

		boolean isLongPreamble = preamble.equals("long");
		if (!isLongPreamble && !preamble.equals("short")) {
			throw new ConfigurationException("MacModule of Node " + id
					+ " illegal preamble qualifier " + preamble);
		}

		timings = new IEEE_802_11_TimingParameters(isB, isLongPreamble);
		droppedPacket = 0;
		discardedPackets = 0;
		lastReceivedPacketInterval = null;
	}

	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(PHY_802_11bg.class);

	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * 
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public final void lowerSAP(Packet packet) {
		PhysicalPacket pPacket = (PhysicalPacket) packet;

		if (!pPacket.isTerminal()) {
			if (pPacket.isValid()) {
				double start    = packet.getTime();
				double duration = node.getCurrentTime() - start; 
				
				Interval receivedI = new Interval(start, duration);
				
				if ((lastReceivedPacketInterval == null) || !lastReceivedPacketInterval.intersects(receivedI)) {
					sendPacket(pPacket.getEnclosedPacket());
					lastReceivedPacketInterval = receivedI;
				} else {
					String msg = id + " got two valid intersecting packets.";
					
					if (exceptionOnInterferingValidPackets) {
						throw new SimulationFailedException(msg);
					} else {
						LOGGER.error(msg);
					}
				}
			} else {
				if (packet.isExpectedReceiver(id)) {
					LOGGER.warn(id + " dropped important packet from " + packet.getSender().getId());
				}
				
				droppedPacket++;
			}
		}
	}

	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#upperSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * @param packet
	 *            to process coming from higher layer.
	 */
	@Override
	public final void upperSAP(Packet packet) {
		MACPacket frame = (MACPacket) packet;
		PhysicalPacket pPacket = new PhysicalPacket(sender, frame);

		if (radioState != RadioState.SENDING) {
			double ss = frame.getSignalStrength();

			if ((ss < 0.0) || (Double.isNaN(ss))) {
				LOGGER.warn("(PHY.upperSAP) illegal signal strength:" + ss);
			}

			pPacket.setTransitToWillSend(frame.isAckRequested());
			pPacket.setSyncDuration(timings.getSyncDuration());
			
			lastSentPacket = pPacket;
			
			// radioState = PhysicalLayerState.SENDING; obsolete and done in the Air
			getNode().transmit(pPacket, ss);
		} else {
			sendEventUp(new RejectedPacketEvent(sender, packet, lastSentPacket));
			
			discardedPackets++;
		}
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
	 * Used to obtain information about the current state / settings of the 802.11bg Physical Layer
	 * This information includes radio state, signal strength, and some more.
	 * 
	 * @see PhysicalLayerParameter
	 * @see PhysicalLayerState
	 * @return an instance of PhysicalLayerState
	 */
	public LayerState getState() {
		PhysicalLayerParameter param = new PhysicalLayerParameter(
				this.signalStrength, this.minSignalStrength, this.maxSignalStrength,
				this.channelCount, this.minFrequency, this.maxFrequency);
		
		return new PhysicalLayerState(PHY_802_11bg.timings, param, this.radioState, 0,
									  this.signalStrength);
	}
}

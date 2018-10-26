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

import java.util.HashMap;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.node.LogLinkLayer;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.user.LogLinkBeaconPosition;
import br.ufla.dcc.grubix.simulator.event.user.LogLinkLinkMetric;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.xml.ConfigurationException;


/** 
 * Simple LogLinkMetric based on the square root of distance between nodes.
 * 
 * @author Andreas Kumlehn
 * @deprecated No Mac layer works with this layer.
 * 
 */
@Deprecated
public class LogLinkPosition extends LogLinkLayer {
	
	/**
	 * Logger of the class LogLinkPosition.
	 */
	private static final Logger LOGGER = Logger.getLogger(LogLinkPosition.class.getName());
	
	/**
	 * Hashmap to store the link metric values.
	 */
	private HashMap<NodeId, Double> linkMetric;
	
	/**
	 * Value to normalize the link metric values.
	 */
	private Double normalizer;

	/**
	 * Constructor of the class LogLinkPosition.
	 */
	public LogLinkPosition() {
		this.linkMetric = new HashMap<NodeId, Double>();
	}

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#lowerSAP(br.ufla.dcc.PingPong.event.Packet)
	 * 
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public final void lowerSAP(Packet packet) {
		if (packet instanceof LogLinkBeaconPosition) {
			updateMetric((LogLinkBeaconPosition) packet);
		}
		if (!packet.isTerminal()) {
			sendPacket(packet.getEnclosedPacket());
		}
	}
	
	/**
	 * Private method to maintain the LinkMetric values.
	 * 
	 * @param beacon Received beacon with a POSITION ot update the link metric values.
	 */
	private void updateMetric(LogLinkBeaconPosition beacon) {
		int other = beacon.getSender().getId().asInt();
		
		if (other != getId().asInt()) {
			double newValue = Position.getDistance(beacon.getPosition(), this.getNode().getPosition());
			newValue = newValue / this.normalizer;
			Double oldValue = this.linkMetric.put(beacon.getSender().getId(), newValue); // Note: D(!)ouble here important
			if (oldValue == null) {
				oldValue = -1.0;
			}
			LOGGER.debug("LinkMetric between Node " + getId() + " and Node "
					+ beacon.getSender().getId().asInt() + ": " + oldValue + " " + newValue);
		}
	}

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#upperSAP(br.ufla.dcc.PingPong.event.Packet)
	 * @param packet to process coming from higher layer.
	 */
	@Override
	public final void upperSAP(Packet packet) {
		Position position = this.getNode().getPosition();
		Packet nextPacket = new LogLinkBeaconPosition(sender, packet, position);
		sendPacket(nextPacket);
	}

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#processEvent(br.ufla.dcc.PingPong.event.Initialize)
	 * @param init Initialize event to start up the layer.
	 */
	@Override
	protected final void processEvent(Initialize init) {
		this.linkMetric.put(getId(), 0.0);
		Position position = this.getNode().getPosition();
		Packet beacon = new LogLinkBeaconPosition(NodeId.ALLNODES, sender, position);
		sendPacket(beacon);
		LOGGER.debug("Node " + getId() + " POSITION (" + position.getXCoord()
						+ "," + position.getYCoord() + ")!");
		
		Packet referenceToContainer = new LogLinkLinkMetric(sender, getId(), this.linkMetric);
		referenceToContainer.flipDirection();
		sendPacket(referenceToContainer);
	}

	/**
	 * currently no states needed, thus no statechanges possible.
	 */
	public LayerState getState() {
		// TODO generate LogLinkPositionLayerState class from LayerState, if necessary.
		return null;
	}

	/**
	 * currently no states needed, thus no statechanges possible.
	 * @param state the new desired state of this layer.
	 * @return true if the statechange was accepted.
	 */
	public boolean setState(LayerState state) {
		// TODO implement the needed state-changes for the former LayerState subclass.
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);
		this.normalizer = Math.sqrt(Math.pow(config.getXSize(), 2) + Math.pow(config.getYSize(), 2));
	}
	
	/**
	 * Handles {@link CrossLayerEvent} by sending it upwards.
	 * @param wuc The received {@link WakeUpCall}.
	 * @throws LayerException if an unhandled {@link WakeUpCall} occurs.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		if (wuc instanceof CrossLayerEvent) {
			((CrossLayerEvent) wuc).forwardUp(this);
		} else {
			super.processWakeUpCall(wuc);
		}
	}
}
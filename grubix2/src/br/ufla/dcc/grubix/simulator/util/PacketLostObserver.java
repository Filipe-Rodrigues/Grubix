/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/

package br.ufla.dcc.grubix.simulator.util;

import java.util.Map;


import org.apache.log4j.Logger;


import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.node.SimulationObserver;

import com.google.common.collect.Maps;


/**
 * @author dmeister
 *
 */
public class PacketLostObserver extends SimulationObserver {
	
	/** Logger of the class Node. */
	private static final Logger LOGGER = Logger.getLogger(PacketLostObserver.class);
	
	/**
	 * map that contains every packet that is not yet received by at least one destination.
	 * Note: using only a set of packets doesn't work, because hashCode and equals of packets are not defined. Due
	 * to the cloning (in the BitManglingModel) a received event isn't identical (==) with the event sent.
	 */
	private Map<EventId, Packet> openPacketSet = Maps.newHashMap();
	
	/**
	 * counter for the total of packets send.
	 */
	private long counter = 0;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void observeLowerSAP(NodeId nodeId, Layer layer, Packet packet) {
		if (layer.getLayerType().equals(LayerType.MAC)) {
			openPacketSet.remove(packet.getId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void observerUpperSAP(NodeId nodeId, Layer layer, Packet packet) {
		if (layer.getLayerType().equals(LayerType.MAC.getLowerLayer())) {
			openPacketSet.put(packet.getId(), packet);
			counter++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void simulationFinished(NodeId nodeId) {
		if (nodeId.asInt() == 1) { // only one time
			LOGGER.warn("Lost packets: " + openPacketSet.size() + " (" + 100.0 * openPacketSet.size() / counter + "%)");
		}
	}

}

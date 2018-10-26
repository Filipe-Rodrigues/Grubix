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

import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.node.metainf.CastType;
import br.ufla.dcc.grubix.simulator.node.metainf.DownwardsLLCMetaInfo;

/** 
 * Abstract superclass for all implementations of LogicalLinkLayers.
 * 
 * A LogLink layer can set link specific information (signal strength, etc) for each packet
 * (MetaInfos). If the loglink implementation doesn't set it, this base class
 * ensures that the link information is set.
 * This is done by setting to values to:
 * - the default bitrate adaption policy of the mac
 * - the maximal transmission power of the physical layer
 * - the highest possible bitrate
 * 
 * @author Andreas Kumlehn
 */
public abstract class LogLinkLayer extends Layer {

	/**
	 * helper method, to add a link to a packet.
	 * @param packet the packet to add a link to.
	 * @param link   the to be added link.
	 */
	public void addLinkToPacket(Packet packet, Link link) {
		CastType castType = CastType.getCastType(packet.getReceiver());
		DownwardsLLCMetaInfo mi = null;
		
		if (castType == CastType.BROADCAST) {
			mi = new DownwardsLLCMetaInfo(link, castType);
		} else {
			mi = new DownwardsLLCMetaInfo(link, castType, packet.getReceiver());
		}
		packet.getMetaInfos().addMetaInfo(mi);
	}
	
	/**
	 * called by subclasses when sending a packet.
	 * This implementation uses the Layer.sendPacket implementation, but
	 * adds a default link to the meta infos, if no link metainfo is set.
	 * 
	 * @param packet packet to send
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#sendPacket(br.ufla.dcc.grubix.simulator.event.Packet)
	 */
	@Override
	public void sendPacket(Packet packet) {
		// If not link infos are provided, the layer base class set them.
		if (packet.getDirection() == Direction.DOWNWARDS
				&& packet.getMetaInfos().getDownwardsLLCMetaInfo() == null) {
			
			addLinkToPacket(packet, Link.createLink(getNode(), packet.getReceiver()));
		}
		super.sendPacket(packet);
	}

	/**
	 * Constructor.
	 *
	 */
	public LogLinkLayer() {
		super(LayerType.LOGLINK);
	}	

	/**
	 * shortcut to create new links originating from this node.
	 * 
	 * @param v           destination node id (needed :).
	 * @param raPolicy    use null for a cloned MAC default policy or use a own policy.
	 * @param bitrateIdx  use -1 for the maximum possible bitrate index supported by the MAC.
	 * @param power       use 0.0 for the maximum possible transmission power.
	 * @return the created link.
	 * @deprecated Better use the createLink factory method of Link
	 */
	@Deprecated
	public Link createLink(NodeId v, BitrateAdaptationPolicy raPolicy, int bitrateIdx, double power) {
		return Link.createLink(getNode(), v, raPolicy, bitrateIdx, power);
	}
}

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

import java.util.List;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.user.OptSrcRoutingPacket;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.NetworkLayer;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * This implementation of the network layer implements
 * positionbased optimal source routing and thus depends
 * on the precomputed neighbourhood of each node. This 
 * only works, if NoMovement is chosen as MovementManager.
 * 
 * @author Dirk Held
 */
public class OptimalSourceRouting extends NetworkLayer {

	/** set to true, if the mac should include the current queue into the state. */
	@ShoXParameter(description = "set to true, if infos should be printed for all packets.", defaultValue = "false")
	private boolean showAll;
	
	/** Logger of this class. */
	private static final Logger LOGGER = Logger.getLogger(OptimalSourceRouting.class.getName());

	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public void lowerSAP(Packet packet) {
		OptSrcRoutingPacket osrPacket = (OptSrcRoutingPacket) packet;
		
		SimulationManager.logLinkState(id, packet.getSender().getId(), "Used", "int", "1");
		if (osrPacket.popNextHop()) {
			osrPacket.flipDirection();
			osrPacket.setSender(sender);
			sendPacket(osrPacket);
		} else {
			if (showAll) {
				LOGGER.info("Node " + id + ": Received packet " + packet.getHighestEnclosedPacket().getId());
			}
			
			sendPacket(osrPacket.getEnclosedPacket());
		}
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#upperSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * @param packet to process coming from higher layer.
	 */
	@Override
	public void upperSAP(Packet packet) {
		List<NodeId> path = SimulationManager.getInstance().resolvePath(id, packet.getReceiver());

		if (path != null) {
			if (showAll) {
				StringBuilder sb = new StringBuilder("(");
				
				sb.append("Routing packet ");
				sb.append(packet.getId());
				sb.append(" from ");
				sb.append(id);
				sb.append(" to ");
				sb.append(packet.getReceiver());
				sb.append(" over ");
				sb.append(NodeId.getNodeIdList(path));
				
				LOGGER.info(sb.toString());
			}

			SimulationManager.logStatistic(id, LayerType.NETWORK, "time", "Hop Count", 
					Double.toString(SimulationManager.getInstance().getCurrentTime()), Double.toString(path.size()));
			
			OptSrcRoutingPacket osrPacket = new OptSrcRoutingPacket(sender, packet, path);
			osrPacket.popNextHop();
			
			sendPacket(osrPacket);
		} else {
			LOGGER.info("no route to " + packet.getReceiver() + " found. *ploink*");
		}
	}
	
	/**
	 * process the incoming events.
	 * 
	 * @param wuc contains the to be processed wakeup-call.
	 * @throws LayerException if the wakeup call could not be processed.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		if (wuc instanceof CrossLayerEvent) {
			((CrossLayerEvent) wuc).forwardUp(this);
		} else {
			throw new LayerException("MacModule of Node " + id + " received wakeup call " + wuc.getClass().getName());
		}
	}
		
	/** @see br.ufla.dcc.grubix.simulator.node.StateIO#getState().
	 *  @return 's nothing. */
	public LayerState getState() {
		return null;
	}

	/** @see br.ufla.dcc.grubix.simulator.node.StateIO#setState(br.ufla.dcc.grubix.simulator.event.LayerState). 
	 *  @param status which is ignored.  
	 *  @return 's false. */
	public boolean setState(LayerState status) {
		return false;
	}
}

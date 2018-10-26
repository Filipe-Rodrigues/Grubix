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
package br.ufla.dcc.PingPong.node.user.os;

import java.util.Set;

import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.OperatingSystemPacket;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.user.os.PortMapper;
import br.ufla.dcc.grubix.simulator.node.user.os.ServiceManager;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;




/**
 * Fasade interface for a operating system.
 * @author dmeister
 *
 */
public interface OperatingSystemServiceFasade {

	/**
	 * sends a event to the same layer. 
	 * If this is a ported event the event is dispatched to the given port.
	 * @param event event.
	 */
	void sendEventSelf(ToLayer event);
	
	/**
	 * sends a event to the upper layer. 
	 * If this is a ported event the event is dispatched to the given port.
	 * @param event event.
	 */
	void sendEventUp(ToLayer event);
	
	/**
	 * sends a event to the lower layer. 
	 * If this is a ported event the event is dispatched to the given port.
	 * @param event event.
	 */
	void sendEventDown(ToLayer event);

	/**
	 * sends a packet.
	 * @param packet packet.
	 */
	void sendPacket(OperatingSystemPacket packet);

	/**
	 * returns a list of neighbors node ids.
	 * @return a list of neighbors node ids.
	 */
	Set<NodeId> getNeighbors();

	/**
	 * returns the node Id.
	 * @return the node Id.
	 */
	NodeId getNodeId();
	
	/**
	 * returns a reference to the node.
	 * @return a reference to the node.
	 */
	Node getNode();
	
	/**
	 * returns a reference to the configuration.
	 * 
	 * @return a reference to the configuration.
	 */
	Configuration getConfig();

	/**
	 * gets a random generator.
	 * @return random generator
	 */
	RandomGenerator getRandomGenerator();
	
	/**
	 * gets the current service manager.
	 * @return service manager.
	 */
	ServiceManager getServiceManager();
	
	/**
	 * returns the port mapper of an operating system.
	 * @return a port mapper
	 */
	PortMapper getPortMapper();

	/**
	 * returns the layer state of the given layer type on the current node.
	 * 
	 * @param layerType layer type.
	 * @return the layer state
	 */
	LayerState getLayerState(LayerType layerType);
	
	/**
	 * returns the current simulated time.
	 * 
	 * @return the current simulated time.
	 */
	double getCurrentSimulatedTime();
}

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
package br.ufla.dcc.grubix.simulator.node.user.os;

import java.util.HashSet;
import java.util.List;
import java.util.Set;



import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.NetworkTransmissionFailedPortedEvent;
import br.ufla.dcc.grubix.simulator.event.OperatingSystemPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.TransmissionFailedEvent;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.node.NeighborhoodProvider;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;


/**
 * Abstract superclass for all implementations of operating systems.
 * @author dmeister
 *
 */
public abstract class OperatingSystemLayer extends Layer {

	/**
	 * Logger for the operating system.
	 */
	private static final Logger LOGGER = Logger.getLogger(OperatingSystemLayer.class);
	
	/**
	 * Constructor.
	 *
	 */
	public OperatingSystemLayer() {
		super(LayerType.OPERATINGSYSTEM);
	}

	/**
	 * {@inheritDoc}
	 */
	public OperatingSystemServiceFasade createFasade() {
		return new OperatingSystemFasadeLayerState();
	}

	/**
	 * returns a new layer state object that implementations the 
	 * {@link OperatingSystemServiceFasade} interface.
	 * 
	 * @return a reference to a layer state that implements the operting system fasade.
	 * @see br.ufla.dcc.grubix.simulator.node.StateIO#getState()
	 */
	public LayerState getState() {
		return new OperatingSystemFasadeLayerState();
	}

	/**
	 * not supported.
	 * 
	 * @param status a new status
	 * @return false (in 
	 * @see br.ufla.dcc.grubix.simulator.node.StateIO#setState(br.ufla.dcc.grubix.simulator.event.LayerState)
	 */
	public boolean setState(LayerState status) {
		return false;
	}
	
	/**
	 * returns the portmapper.
	 * In this implementation a {@link UnsupportedOperationException} is thrown, but
	 * subclasses that provide a port mapper, should overwrite this method.
	 * @return a port mapper.
	 */
	public PortMapper getPortMapper() {
		throw new UnsupportedOperationException("This operating system doesn't provide a port mapping");
	}
	
	/**
	 * returns the service manager.
	 * In this implementation a {@link UnsupportedOperationException} is thrown, but
	 * subclasses that provide a service manager, should overwrite this method.
	 * @return a service manager.
	 */
	public ServiceManager getServiceManager() {
		throw new UnsupportedOperationException("This operating system doesn't provide a service manager");
	}
	
	/**
	 * Returns a set of node ids of all node neighbors.
	 * If the loglink layer provides neighborhood infos, these information
	 * is used.
	 * The the loglink layer doesn't provide this, the neighborhood infos
	 * from the simulation kernel is used, but a warning is printed out, since this
	 * is "cheating".
	 * 
	 * @return node ids of neighbors of the node
	 */
	public Set<NodeId> getNeighbors() {
		LayerState ls = node.getLayerState(LayerType.LOGLINK);
		if (ls instanceof NeighborhoodProvider) {
			NeighborhoodProvider np = (NeighborhoodProvider) ls;
			return np.getNodeNeighbors();
		} else {
			LOGGER.warn("Use of cheating neighborhood set");
			List<Node> nodeList = getNode().getNeighbors();
			Set<NodeId> idList = new HashSet<NodeId>();
			for (Node node : nodeList) {
				idList.add(node.getId());
			}
			return idList;
		}
	}
	
	/**
	 * Overrides the normal sendPacket method to flip the direction directly
	 * if it is sended to a service on the same node.
	 * 
	 * @param packet packet to send
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#sendPacket(br.ufla.dcc.grubix.simulator.event.Packet)
	 */
	@Override
	public void sendPacket(Packet packet) {
		if (packet instanceof OperatingSystemPacket) {
			OperatingSystemPacket osp = (OperatingSystemPacket) packet;
			boolean isOwn = osp.getReceiver().equals(getId());
			if (isOwn) {
				osp.flipDirection();
				sendEventSelf(packet);
				return;
			}
		}
		super.sendPacket(packet);
	}
	
	/**
	 * Layer state implementation that provides access to the {@link OperatingSystemServiceFasade}.
	 * Used mainly the application layer that used a port system.
	 * @author dmeister
	 *
	 */
	private class OperatingSystemFasadeLayerState extends LayerState implements OperatingSystemServiceFasade {

		/**
		 * {@inheritDoc}
		 */
		public void sendPacket(OperatingSystemPacket packet) {
			OperatingSystemLayer.this.sendPacket(packet);
		}

		/**
		 * {@inheritDoc}
		 */
		public void sendEventSelf(ToLayer event) {
			OperatingSystemLayer.this.sendEventSelf(event);
		}
		
		/**
		 * {@inheritDoc}
		 */		
		public void sendEventDown(ToLayer event) {
			OperatingSystemLayer.this.sendEventDown(event);
		}

		/**
		 * {@inheritDoc}
		 */
		public void sendEventUp(ToLayer event) {
			OperatingSystemLayer.this.sendEventUp(event);
		}

		/**
		 * {@inheritDoc}
		 */
		public RandomGenerator getRandomGenerator() {
			return getRandom();
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<NodeId> getNeighbors() {
			return OperatingSystemLayer.this.getNeighbors();
		}

		/**
		 * returns a new layer state object that implementations the 
		 * {@link OperatingSystemServiceFasade} interface.
		 * 
		 * @return a reference to a layer state that implements the operting system fasade.
		 * @see br.ufla.dcc.grubix.simulator.node.StateIO#getState()
		 */
		public LayerState getState() {
			return new OperatingSystemFasadeLayerState();
		}

		/**
		 * not supported.
		 * 
		 * @param status a new status
		 * @return false (in 
		 * @see br.ufla.dcc.grubix.simulator.node.StateIO#setState(br.ufla.dcc.grubix.simulator.event.LayerState)
		 */
		public boolean setState(LayerState status) {
			return false;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ServiceManager getServiceManager() {
			return OperatingSystemLayer.this.getServiceManager();
		}

		/**
		 * {@inheritDoc}
		 */
		public PortMapper getPortMapper() {
			return OperatingSystemLayer.this.getPortMapper();
		}

		/**
		 * {@inheritDoc}
		 */
		public LayerState getLayerState(LayerType layerType) {
			return OperatingSystemLayer.this.node.getLayerState(layerType);
		}

		/**
		 * {@inheritDoc}
		 */
		public Configuration getConfig() {
			return OperatingSystemLayer.this.getConfig();
		}

		/**
		 * {@inheritDoc}
		 */
		public Node getNode() {
			return OperatingSystemLayer.this.getNode();
		}

		/**
		 * {@inheritDoc}
		 */
		public NodeId getNodeId() {
			return OperatingSystemLayer.this.getId();
		}

		/**
		 * {@inheritDoc}
		 */
		public double getCurrentSimulatedTime() {
			return OperatingSystemLayer.this.getKernel().getCurrentTime();
		}		
	}

	/**
	 * {@inheritDoc}.
	 * Handles {@link TransmissionFailedEvent} events: If the failed packet has been sent
	 * by the application layer then the event is send upwards. If the failed packet
	 * has been sent by any service then a {@link NetworkTransmissionFailedPortedEvent} is created
	 * and processed by the port mapper.
	 */
	@Override
	public void processEvent(ToLayer event) throws LayerException {
		if (event instanceof TransmissionFailedEvent) {
			TransmissionFailedEvent ntfe = (TransmissionFailedEvent) event;
			OperatingSystemPacket packet = (OperatingSystemPacket) ntfe.getPacket();
			
			if (packet.getSenderPort() != null) {
				NetworkTransmissionFailedPortedEvent ntfpe = 
					new NetworkTransmissionFailedPortedEvent(ntfe.getSender().getId(), packet);
				
				getPortMapper().processPortedEvent(ntfpe);
				return;
			}
			
			ntfe.forwardUp(this);
			return;
		} else if (event instanceof CrossLayerEvent) {
			((CrossLayerEvent) event).forwardUp(this);
		} else {
			super.processEvent(event);
		}
	}
	
}

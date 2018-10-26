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

import java.util.Set;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.OperatingSystemPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.Port;
import br.ufla.dcc.grubix.simulator.event.ServiceMultiplexEvent;
import br.ufla.dcc.grubix.simulator.event.SimulationState;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.LayerStateChangedProvider;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * A basic operating system that uses services in addition to the normal application layer.
 * Most real processing is done inside the service manager.
 * 
 * @author dmeister
 *
 */
public class BasicOperatingSystemLayer extends OperatingSystemLayer {

	/**
	 * Logger for the operating system.
	 */
	private static final Logger LOGGER = Logger.getLogger(BasicOperatingSystemLayer.class);

	/**
	 * port used for the application layer.
	 */
	private final Port applicationPort = new Port(1);

	/**
	 * port mapper.
	 */
	private final PortMapper portmapper;

	/**
	 * Configurable service manager (contains service descriptions).
	 */
	@ShoXParameter(description = "service manager", defaultClass = LocalServiceManager.class)
	private ServiceManager serviceManager;

	/**
	 * Default constructor.
	 */
	public BasicOperatingSystemLayer() {
		super();
		portmapper = new PortMapper();
		try {
			portmapper.bind(applicationPort, new ApplicationPortReceiver());
		} catch (BindingException e) {
			// can not happen
			throw new AssertionError("port mapping of application port failed");
		}
	}

	/**
	 * Initializes the operating system, checks basic dependencies and registers for events.
	 * 
	 * @param config configuration
	 * @throws ConfigurationException thrown if the configuration is invalid
	 * @see br.ufla.dcc.grubix.simulator.node.user.os.OperatingSystemLayer#initConfiguration()
	 */
	@Override
	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);

		for (LayerType layerType : LayerType.values()) {
			try {
				LayerState ls = this.getNode().getLayerState(layerType);
				if (ls instanceof LayerStateChangedProvider) {
					LayerStateChangedProvider lscp = (LayerStateChangedProvider) ls;
					lscp.registerForLayerStateChangedEvent(LayerType.OPERATINGSYSTEM);
				}
			} catch (UnsupportedOperationException e) {
				//ignore
			}
		}
	}
	
	/**
	 * lower access point on any layer.
	 * delegates the packet to the port mapper if it is a operating system packet.
	 *
	 * @param packet Packet to process in the layer coming from a lower layer.
	 * @throws LayerException if something goes wrong (e.g. type not handled properly).
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 */
	@Override
	public void lowerSAP(Packet packet) throws LayerException {
		if (packet instanceof OperatingSystemPacket) {
			portmapper.processPortedPacket((OperatingSystemPacket) packet);	
			return;
		}
		LOGGER.error("Cannot handle packet " + packet);
	}

	/**
	 * higher access point of the operating system. Used by the application to send packets.
	 * 
	 * Sends a new packet with the port 0 to the lower layer.
	 *
	 * @param packet Packet to process in the layer coming from a higher layer.
	 * @throws LayerException if something goes wrong (e.g. type not handled properly).
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#upperSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 */
	@Override
	public void upperSAP(Packet packet) throws LayerException {
		Packet nextPacket = new OperatingSystemPacket(getSender(),
				applicationPort, packet.getReceiver(), applicationPort, packet);
		sendPacket(nextPacket);

	}
	
	/**
	 * processes simulation state events. This method calls the base implementation and
	 * starts the service manager after the {@link StartSimulation} event.
	 * And it ends the service manager after {@link Finalize} event.
	 * {@inheritDoc}
	 */
	@Override
	public void processEvent(SimulationState simState) {
		super.processEvent(simState);
		if (simState instanceof StartSimulation) {
			try {
				serviceManager.start(this);
			} catch (ConfigurationException e) {
				LOGGER.warn("Some services failed to start", e);
			}
		} else if (simState instanceof Finalize) {
			serviceManager.stop();
		}

	}
	
	/**
	 * {@inheritDoc}.
	 * redirects {@link PortedEvent} events to the port mapper.
	 */
	@Override
	public void processEvent(ToLayer event) throws LayerException {
		if (event instanceof PortedEvent) {
			getPortMapper().processPortedEvent((PortedEvent) event);
		} else if (event instanceof ServiceMultiplexEvent) {
			Set<Port> portList = serviceManager.getUsedPorts();
			for (Port port : portList) {
				LowerLayerPortedEvent lowerPortedEvent = new LowerLayerPortedEvent(event.getSender().getId(), port,
						event);
				sendEventSelf(lowerPortedEvent);
			}

			// TODO implement a dispatching mechanism in order to send the event to the registered services.
			return;
		} else {
			super.processEvent(event);
		}
	}
	


	/**
	 * Receives packets on port 0 and unpacks it and sends the enclosed
	 * packet to the application.
	 * @author dmeister
	 *
	 */
	private class ApplicationPortReceiver implements PortedPacketReceiver {

		/**
		 * handles a packet on port 1.
		 * @param packet a packet on port 1
		 * @see br.ufla.dcc.grubix.simulator.node.user.os.PortedPacketReceiver#
		 * processPortedPacket(br.ufla.dcc.grubix.simulator.event.OperatingSystemPacket)
		 */
		public void processPortedPacket(OperatingSystemPacket packet) {
			sendPacket(packet.getEnclosedPacket());
		}

		/**
		 * handles a ported event on port 1.
		 * It ignores the packet since the applicaiton cannot send or receive ported events.
		 * @param event event
		 * @see br.ufla.dcc.grubix.simulator.node.user.os.PortedPacketReceiver#
		 * processPortedEvent(br.ufla.dcc.grubix.simulator.node.user.os.PortedEvent)
		 */
		public void processPortedEvent(PortedEvent event) {
            // send event to application.
            sendEventUp(event);
		}
	}

	/**
	 * returns the port mapper.
	 * @return the port mapper
	 */
	@Override
	public PortMapper getPortMapper() {
		return this.portmapper;
	}	

	/**
	 * returns the service manager.
	 * @return service manager
	 */
	@Override
	public ServiceManager getServiceManager() {
		return this.serviceManager;
	}
}

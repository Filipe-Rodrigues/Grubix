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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.event.OperatingSystemPacket;
import br.ufla.dcc.grubix.simulator.event.Port;




/**
 * Handles the management of ports and delegates packets to the
 * correct destinations.
 *
 * @author dmeister
 *
 */
public class PortMapper {

	/**
	 * maps ports to port packet receivers.
	 */
	private final Map<Port, PortedPacketReceiver> portMapping = new HashMap<Port, PortedPacketReceiver>();

	/**
	 * next free port for binding without a predefined port.
	 */
	private int nextAdhocPort = 1024;

	/**
	 * Binds a receiver on a given port.
	 * 
	 * @param port a port (not null)
	 * @param receiver receiver of an packet (not null)
	 * @throws BindingException thrown when already a receiver is listening on that port
	 */
	public void bind(Port port, PortedPacketReceiver receiver) throws BindingException {
		if (port.getNumber() >= 1024) {
			throw new BindingException("Cannot bind a port above 1023");
		}
		if (portMapping.containsKey(port)) {
			throw new BindingException(port + "already bound");
		}
		portMapping.put(port, receiver);
	}

	/**
	 * Binds a receiver on a port that is not given.
	 * @param receiver receiver of an packet (not null)
	 * @return a bounded port above 1024
	 * @throws BindingException thrown when already a receiver is listening on that port
	 */
	public Port bind(PortedPacketReceiver receiver) throws BindingException {
		Port port = new Port(nextAdhocPort++);
		portMapping.put(port, receiver);
		return port;
	}

	/**
	 * tests if a port is bound.
	 * @param port a port (not null)
	 * @return true if the port is bound
	 */
	public boolean isBound(Port port) {
		return portMapping.containsKey(port);
	}

	/**
	 * tests if a port is bound to the receiver.
	 * @param port a port (not null)
	 * @param receiver a packet receiver
	 * @return true if the port is bound
	 */
	public boolean isBound(Port port, PortedPacketReceiver receiver) {
		return portMapping.containsKey(port) && portMapping.get(port).equals(receiver);
	}

	/**import br.ufla.dcc.grubix.simulator.event.Event;
	 * unbinds a receiver from a port.
	 * @param port a port (not null)
	 * @param receiver receiver of packets (not null)
	 * @throws BindingException thrown when the receiver is not bind to the port
	 */
	public void unbind(Port port, PortedPacketReceiver receiver) throws BindingException {
		if (!isBound(port, receiver)) {
			throw new BindingException("Cannot unbind " + port);
		}
		portMapping.remove(port);
	}

	/**
	 * unbinds a receiver from all ports.
	 * @param receiver receiver of packets (not null)
	 */
	public void unbind(PortedPacketReceiver receiver) {
		Set<Entry<Port, PortedPacketReceiver>> entrySet = portMapping.entrySet();
		Iterator<Entry<Port, PortedPacketReceiver>> i = entrySet.iterator();
		while (i.hasNext()) {
			Entry<Port, PortedPacketReceiver> entry = i.next();
			if (entry.getValue().equals(receiver)) {
				i.remove();
			}
		}
	}

	/**
	 * delegates the packet to the correct receiver.
	 * @param packet a packet (not null)
	 * @throws LayerException thrown by receiver or when no one listen on that port
	 */
	public void processPortedPacket(OperatingSystemPacket packet) throws LayerException {
		Port port = packet.getReceiverPort();

		PortedPacketReceiver receiver = portMapping.get(port);
		if (receiver == null) {
			throw new LayerException("No listener on port " + port);
		}
		
		receiver.processPortedPacket(packet);		
	}

	/**
	 * delegates a event to the correct receiver.
	 * @param event a event (not null)
	 * @throws LayerException thrown by receiver or when no one listen on that port
	 */
	public void processPortedEvent(PortedEvent event) throws LayerException {
		Port port = event.getPort();

		PortedPacketReceiver receiver = portMapping.get(port);
		if (receiver == null) {
			throw new LayerException("No listener on port " + port);
		}
		receiver.processPortedEvent(event);
	}
	
	/**
	 * Gets all mapped {@link Port}s excluding the {@link Port} for the
	 * operating system.
	 * 
	 * @return Set<Port> all mapped {@link Port}s excluding the {@link Port}
	 *         for the operating system.
	 */
	public Set<Port> getUsedPorts() {
		Set<Port> portList = new HashSet<Port>();
		for (Entry<Port, PortedPacketReceiver> entry : portMapping.entrySet()) {
			// exclude OS Port
			if (entry.getKey().getNumber() != 1) {
				portList.add(entry.getKey());
			}
		}
		return portList;
	}
}

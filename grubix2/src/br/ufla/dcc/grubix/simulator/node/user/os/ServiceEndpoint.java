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

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Port;

/**
 * @author dmeister
 *
 */
public class ServiceEndpoint {

	/**
	 * address of the service.
	 */
	private final Address address;

	/**
	 * port of the service.
	 */
	private final Port port;

	/**
	 * returns the address of the service.
	 * @return address of the service
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * returns the port of the service.
	 * @return port of the service
	 */
	public Port getPort() {
		return port;
	}
	
	/**
	 * returns the node id of the endpoint.
	 * @return nodeId of the endpoint.
	 */
	public NodeId getNodeId() {
		return address.getId();
	}

	/**
	 * Constructor for the service endpoint.
	 * @param address address of the service (not null)
	 * @param port port of the service (not null)
	 * @thrown IllegalArgumentException thrown if address or port are null
	 */
	public ServiceEndpoint(Address address, Port port) {
		super();
		if (address == null) {
			throw new IllegalArgumentException("address");
		}
		if (port == null) {
			throw new IllegalArgumentException("port");
		}
		this.address = address;
		this.port = port;
	}
	
	/**
	 * creates a new endpoint for the same service on a different node.
	 * @param newNodeId node id
	 * @return a new service endpoint for the same layer, the same port, but on a different node.
	 */
	public ServiceEndpoint create(NodeId newNodeId) {
		Address newAddress = new Address(newNodeId, this.address.getFromLayer());
		return new ServiceEndpoint(newAddress, this.port);
	}

	/**
	 * calculates the hash code.
	 * @return a hash code
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address.hashCode();
		result = prime * result + port.hashCode();
		return result;
	}

	/**
	 * tests if two objects are equal.
	 * @param obj object to compare
	 * @return true if address and the port are equal, otherwise false
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ServiceEndpoint)) {
			return false;
		}
		final ServiceEndpoint other = (ServiceEndpoint) obj;
		if (!address.equals(other.address)) {
			return false;
		}
		return port.equals(other.port);
	}

	/**
	 * generated a string representation of the event.
	 * @return string representation
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[ServiceEndpoint:");
		buffer.append(" address=");
		buffer.append(address);
		buffer.append(", port=");
		buffer.append(port);
		buffer.append("]");
		return buffer.toString();
	}


}

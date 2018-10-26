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

package br.ufla.dcc.grubix.simulator;

/** 
 * Class representing an Address in the SIMULATION.
 * An Address consists of a NodeId and a specific Layer of the node.
 * Used in the SIMULATION to identify senders of packets.
 * 
 * @author Andreas Kumlehn
 */
public class Address {
	
	/**
	 * The NodeId of the sender.
	 */
	private final NodeId id;
	
	/**
	 * The sending layer of the sender.
	 */
	private final LayerType fromLayer;
	
	/**
	 * Constructor of the class Address.
	 * 
	 * @param id NodeId of the sender.
	 * @param fromLayer Sending layer of the sender.
	 */
	public Address(NodeId id, LayerType fromLayer) {
		if (id == null) {
			throw new IllegalArgumentException("id");
		}
		if (fromLayer == null) {
			throw new IllegalArgumentException("fromLayer");
		}
		this.id = id;
		this.fromLayer = fromLayer;
	}

	/**
	 * @return  Returns the fromLayer.
	 */
	public final LayerType getFromLayer() {
		return fromLayer;
	}

	/**
	 * @return  Returns the sender.
	 */
	public final NodeId getId() {
		return id;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ fromLayer.hashCode();
		result = prime * result + id.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Address)) {
			return false;			
		}
		final Address other = (Address) obj;
		if (!fromLayer.equals(other.fromLayer)) {
			return false;			
		}
		return (id.equals(other.id));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[Address:");
		buffer.append(" node=");
		buffer.append(id);
		buffer.append(", layer=");
		buffer.append(fromLayer);
		buffer.append("]");
		return buffer.toString();
	}
}

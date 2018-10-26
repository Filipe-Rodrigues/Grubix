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

package br.ufla.dcc.grubix.simulator.event.user;

import java.util.HashMap;
import java.util.Map;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;




/** 
 * Packet to send a reference on the linkMetric container of a
 * LogLinkLayer to another layer.
 * 
 * Used in the LogLinkPosition layer implementation.
 * @author Andreas Kumlehn
 */
public class LogLinkLinkMetric extends Packet implements Cloneable {

	/** Reference on the linkMetricContainer of the sendernode.	*/
	private HashMap<NodeId, Double> linkMetric;

	/**
	 * Constructor of the class LogLinkLinkMetric.
	 * 
	 * @param sender Address of the sender.
	 * @param receiver NodeId of the receiver.
	 * @param linkMetric Reference to the linkMetricContainer of the sendernode.
	 */
	public LogLinkLinkMetric(Address sender, NodeId receiver, HashMap<NodeId, Double> linkMetric) {
		super(sender, receiver);
		this.linkMetric = linkMetric;
	}

	/** @return Returns the linkMetric. */
	public final Map<NodeId, Double> getLinkMetric() {
		return linkMetric;
	}
	
	/**
	 * implements the clone method to duplicate packets.
	 * 
	 * It uses the base class clone method and creates a clone of the linkMetric
	 * HashMap. It is not neseccary to dublicate the contents of the hash map, since
	 * NodeIds and Doubles are inmutable anyway.
	 * 
	 * @return a clone of the packet.
	 */
	@SuppressWarnings("unchecked")
	public Object clone() {
		LogLinkLinkMetric obj = (LogLinkLinkMetric) super.clone();
		obj.linkMetric = (HashMap<NodeId, Double>) linkMetric.clone();
		return obj;
	}
}

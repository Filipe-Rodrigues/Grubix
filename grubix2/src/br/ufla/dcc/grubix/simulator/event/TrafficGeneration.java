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

package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * This class represents an event which is created by the traffic generator and delivered to the
 * application layer instances of the individual nodes. It requests the nodes' applications
 * to send a single application-level data packet to the specified receiving node. To be able to
 * distinguish between different types of packets that an application might be able to create, a
 * packet type identifier must be specified in a TrafficGeneration request.
 * @author jlsx
 */
public class TrafficGeneration extends SimulationEvent implements Comparable<TrafficGeneration> {

	/** Stores the NodeId of the node which generates the traffic. */
	private NodeId source;
	
	/** Period of time to wait before this event is delivered to the application. */
	private double delay;
	
	/** The node which will receive the generated traffic. */
	private NodeId recipient;
	
	/** An identifier for the particular type of packet that is to be generated. */
	private int packetType;
	
	/**
	 * This constructor represents a request for traffic generation which is started <code>delay</code>
	 * simulation steps after being enqueued. It initiates the generation of an (application-level) packet
	 * which is sent from node <code>node</code> to the <code>recipient</code> node.
	 * @param source The node which is to generate the traffic (the receiver of this event)
	 * @param recipient The node which will receive the generated traffic
	 * @param delay Period of time to wait before this event is delivered to the application
	 * @param packetType An identifier for the particular type of packet that is to be generated.
	 * This identifier must be interpreted by the application
	 */
	public TrafficGeneration(NodeId source, NodeId recipient, double delay, int packetType) {
		super();
		this.source = source;
		this.delay = delay;
		this.recipient = recipient;
		this.packetType = packetType;
	}
	
	/**
	 * @return The node which is to generate traffic, i.e. the node to which this event
	 * is delivered.
	 */
	public final NodeId getSource() {
		return this.source;
	}
	
	/**
	 * @return The period of time to wait before this event is delivered to the application.
	 */
	public final double getDelay() {
		return this.delay;
	}
	
	/**
	 * @return The node which will receive the generated traffic.
	 */
	public final NodeId getRecipient() {
		return this.recipient;
	}
	
	/**
	 * @return An identifier for the particular type of packet that is to be generated.
	 */
	public final int getPacketType() {
		return this.packetType;
	}

	/**
	 * @return String for logging.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer("[Traffic: from=");
		s.append(this.getSource().asInt());
		s.append(", to=");
		s.append(this.recipient);
		s.append(", type=");
		s.append(packetType);
		s.append(", delay=");
		s.append(delay);
		s.append("]");
		return s.toString();
	}
	
	/**
	 * Compares this object with <code>obj</code> in terms of their delay. Note that this does not
	 * give a total ordering of all possible TrafficGeneration objects during the whole simulation.
	 * It is, however, useful to compare objects created within one round (trafficTimeInterval).
	 * If the delay is equal than the result depends on the comparision of the sources and the recipient.
	 * @see java.lang.Comparable#compareTo(Object)
	 * @param tgn The object to compare with
	 * @return A value < 0, 0, > 0 if this object's delay is less than, equal to, greater than 
	 * <code>obj</code>'s delay
	 */
	public int compareTo(TrafficGeneration tgn) {
		int result = Math.round((float) (this.delay - tgn.getDelay()) * 20);
		if (result != 0) {
			return result;
		}
		
		result = this.getSource().compareTo(tgn.getSource());
		if (result != 0) {
			return result;
		}
		
		result = this.getRecipient().compareTo(tgn.getRecipient());
		
		return result;
	}
}

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

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;

/**
 * This class represents a bidirectional link between between two nodes. A link is defined 
 * by the IDs of the two nodes it connects, and optionally, a bitrate and a transmission power.
 * Note that the fact that there is a link object with two endpoints u and v does not imply at
 * all that there exists a real physical link between u and v (i.e. that v would be reachable
 * by u with the given bitrate and power, or vice versa). Depending on the scenario where
 * it is used, an instance of this class might also represent a link that previously existed,
 * etc. Whether or not two nodes can actually reach each other is determined by the physical
 * model.
 * @author jlsx
 */
public class Link {
	
	/**
	 * indicates that the maximal bitrate should be used.
	 */
	public static final int MAX_BITRATE = -1;
	
	/**
	 * indicates that the maximal signal strength (or transmission power) should be used.
	 */
	public static final double MAX_SIGNAL_STRENGTH = -1.0;
	
	/** the originator of this link. */
	private final NodeId u;
	
	/** the destination of this link. */
	private final NodeId v;
	
	/** The transmission power used along this link in mW. */
	private final double transmissionPower;
	
	/** Reference to the bitrate adaptation policy used for this link. */
	private final BitrateAdaptationPolicy policy;

	/**
	 * factory for a link with default values.
	 * 
	 * @param u originator of this link
	 * @param v destination of this link
	 * @return a new link
	 */
	public static Link createLink(Node u, NodeId v) {
		return createLink(u, v, null, MAX_BITRATE, MAX_SIGNAL_STRENGTH);
	}
	
	/**
	 * factory for a new link.
	 * 
	 * @param node originator of this link
	 * @param v destination of this link
	 * @param raPolicy bitrate policy
	 * @param bitrateIndex bitrate index
	 * @param transmissionPower power
	 * @return a new link
	 */
	public static Link createLink(Node node, NodeId v, 
			BitrateAdaptationPolicy raPolicy, int bitrateIndex, double transmissionPower) {
		NodeId u = node.getId();
		BitrateAdaptationPolicy policy = raPolicy;
		MACState ms = (MACState) node.getLayerState(LayerType.MAC);
		if (policy == null && ms != null) {
			policy = (BitrateAdaptationPolicy) ms.getRaDefaultPolicy();
		}	

		PhysicalLayerState ps = (PhysicalLayerState) node.getLayerState(LayerType.PHYSICAL);
		double maxTransmissionPower = ps.getMaximumSignalStrength();

		PhysicalTimingParameters timings = ps.getTimings();
		int maxBitrateIdx = timings.getMaxBitrateIDX();

		double power = maxTransmissionPower;
		if (transmissionPower > 0 && transmissionPower < maxTransmissionPower) {
			power = transmissionPower;
		}
		if (policy != null) {
			int index = maxBitrateIdx;
			if (bitrateIndex > 0 && bitrateIndex < maxBitrateIdx) {
				index = bitrateIndex;
			}
			policy.setBitrateIdx(index);
		}
		return new Link(u, v, policy, power);
	}
	
	/**
	 * Constructs a link representation between nodes u and v.
	 * @param u Endpoint 1 of this link
	 * @param v Endpoint 2 of this link
	 * @param raPolicy bitrate adaptation policy used for this link.
	 * @param transmissionPower transmission power used along this link in mW. 
	 */
	private Link(NodeId u, NodeId v, 
			BitrateAdaptationPolicy raPolicy, 
			double transmissionPower) {
		this.u = u;
		this.v = v;
		this.policy = raPolicy;
		this.transmissionPower = transmissionPower;
	}
	
	/** @return Endpoint 1 of this (bidirectional) link. */
	public NodeId getU() {
		return u;
	}

	/** @return Endpoint 2 of this (bidirectional) link. */
	public NodeId getV() {
		return v;
	}

	/** @return Index into the MAC bitrate array => bitrate (to be) used along this link. */
	public int getBitrateIdx() {
		if (policy == null) {
			return 0;
		} else {
			return policy.getBitrateIdx();
		}
	}

	/** @return The transmission power used along this link in mW. */
	public double getTransmissionPower() {
		return transmissionPower;
	}
	
	/** @return The bitrate adaptation policy to be used for transmissions along this link.	*/
	public BitrateAdaptationPolicy getBitrateAdaptationPolicy() {
		return this.policy;
	}
}

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

package br.ufla.dcc.grubix.simulator.kernel;

import br.ufla.dcc.grubix.simulator.node.Node;

/**
 * Helper class that stores (node, distance) pairs which can be compared as needed
 * by a priority queue. The distance is the hop count between the given node and a
 * particular source node used in Dijkstra's shortest path algorithm.
 * @author jlsx
 */
public class OSRDistance implements Comparable<OSRDistance> {

	/** The node in the (node, distance) pair. */
	private Node node;
	
	/** The distance (hop count) of the node to the source node in the (node, distance) pair. */
	private int distance;
	
	/**
	 * Stores a (node, distance) pair.
	 * @param n The node in the (node, distance) pair
	 * @param distance The distance (hop count) of the node to the source node in the (node, distance) pair
	 */
	public OSRDistance(Node n, int distance) {
		this.node = n;
		this.distance = distance;
	}
	
	/**
	 * @return The node in the (node, distance) pair.
	 */
	public Node getNode() {
		return this.node;
	}
	
	/**
	 * @return The distance (hop count) of the node to the source node in the (node, distance) pair.
	 */
	public int getDistance() {
		return this.distance;
	}
	
	/**
	 * Compares this OSRDistance to another one.
	 * @param dist The OSRDistance to compare with
	 * @return -1, 0, 1, if this distance is less than, equal to, or greater than
	 * <code>dist</code>'s distance
	 */
	public int compareTo(OSRDistance dist) {
		//1. order: distance
		if (this.distance < dist.distance) {
			return -1;
		} else if (this.distance > dist.distance) {
			return 1;
		}
		
		//2. order: node
		if (this.node.getId().asInt() < dist.node.getId().asInt()) {
			return -1;
		}
		if (this.node.getId().asInt() > dist.node.getId().asInt()) {
			return 1;
		}		
		return 0;
	}

	/**
	 * Checks whether this OSRDistance and the one given in <code>obj</code> are equal
	 * in terms of their nodes' IDs.
	 * @param obj Hopefully, another OSRDistance to compare with
	 * @return True, if both objects' node IDs are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OSRDistance)) {
			return false;
		}
		OSRDistance dist = (OSRDistance) obj;
		return (this.node.equals(dist.node)
				&& this.getDistance() == dist.getDistance());
	}
	
	/**
	 * calculates the hashCode.
	 * @return hashcode of the OSRDistance object
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + node.hashCode();
		result = 37 * result + distance;
		return result;
	}
}

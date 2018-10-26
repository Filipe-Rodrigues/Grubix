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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.node.Node;




/**
 * This class computes a shortest path between two nodes using Dijkstra's Shortest Path
 * algorithm. As such, it needs global knowledge about the whole network topology at a
 * given time which can be obtained from the SimulationManager. While the All-Pairs-Shortest-Path
 * Algorithm in StaticOSROracle is more efficient for static scenarios, this class is
 * more efficient for dynamic cases.
 * @author jlsx
 */
public class DynamicOSROracle extends OSROracle {

	/**
	 * Method to resolve the shortest path between two nodes according to Dijkstra.
	 * @param from The starting node.
	 * @param to   The end node.
	 * @return The complete path, if it exists or null.
	 */
	@Override
	public final List<NodeId> resolvePath(NodeId from, NodeId to) {
		if (to.equals(NodeId.ALLNODES)) { //special case
			ArrayList<NodeId> path = new ArrayList<NodeId>();
			path.add(NodeId.ALLNODES);
			return path;
		}
		
		Node source = SimulationManager.getInstance().queryNodeById(from);
		Node dest = SimulationManager.getInstance().queryNodeById(to);
		
		if (source.getNeighbors().contains(dest)) {
			ArrayList<NodeId> path = new ArrayList<NodeId>();
			path.add(to);
			return path;
		}
		
		PriorityQueue<OSRDistance> pq = new PriorityQueue<OSRDistance>();
		HashMap<NodeId, Integer> distMap = new HashMap<NodeId, Integer>();
		HashMap<NodeId, NodeId> previous = new HashMap<NodeId, NodeId>();
		for (Node n : SimulationManager.getAllNodes().values()) {
			if (!n.equals(source)) {
				OSRDistance osrDist = new OSRDistance(n, Integer.MAX_VALUE - 1);
				pq.add(osrDist);
				distMap.put(n.getId(), Integer.MAX_VALUE - 1);
			}
		}
		pq.add(new OSRDistance(source, 0));
		distMap.put(source.getId(), 0);
		
		while (!pq.isEmpty()) {
			OSRDistance dist = pq.poll();
			Node u = dist.getNode();
			if (u.getId().equals(to)) {
				// we can terminate
				ArrayList<NodeId> path = new ArrayList<NodeId>();
				path.add(0, to);
				NodeId currentNode = previous.get(to);
				while (!(currentNode == null) && !currentNode.equals(from)) {
					path.add(0, currentNode);
					currentNode = previous.get(currentNode);
				}
				if (currentNode == null) {
					// we do not have a connected path between from and to
					return null;
				}
				return path;
			} else {
				for (Node v : u.getNeighbors()) {
					int test = dist.getDistance() + 1;
					int old = distMap.get(v.getId());
					if (test < old) {
						pq.remove(new OSRDistance(v, old));
						pq.add(new OSRDistance(v, test));
						distMap.put(v.getId(), test);
						previous.put(v.getId(), u.getId());
					}
				}
			}
		}
		return null;
	}
}

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
import java.util.List;
import java.util.SortedMap;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.node.Node;




/**
 * Class to to store all routes between two nodes.
 * This only makes sense in stationary setups.
 * 
 * This class needs the precomputed neighbourhood of each node to
 * compute all shortest path with the Floyd-Warshall algorithm.
 * 
 * The case, where the destination is the source or the broadcast ID
 * is now handled seperately, like in DynamicOSROracle.
 * 
 * @author Dirk Held
 */
public class StaticOSROracle extends OSROracle {
	
	/** number nodes. */
	private final int count;
	/** infinity number of hops resolfe to.... */
	private final int inf;
	/** array to store the current shortest path between node i and node j. */
	private final int[][] hc;
	/** array to store the next hop on the path between node i and node j. */
	private final int[][] p;
	
	/**
	 * default constructor using the given nodes with precomputed 1-hop neighbourhood.
	 * @param allNodes all nodes of this setup as sorted map.
	 */
	public StaticOSROracle(SortedMap<NodeId, Node> allNodes) {
		count = allNodes.size();
		inf = count * count + 1;
		
		hc = new int[count][count];
		p = new int[count][count];
		
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				if (i == j) {
					hc[i][j] = 0; // I'm already there
				} else {
					hc[i][j] = inf; // infinite hop count
				}
				p[i][j] = count;  // no path known yet
			}
		}
		
		for (Node node : allNodes.values()) {
			int i = node.getId().asInt() - 1;
			
			for (Node other : node.getNeighbors()) {
				int j = other.getId().asInt() - 1;
				
				hc[i][j] = 1;
				p[i][j] = j;
			}
		}
		
		for (int k = 0; k < count; k++) {
			for (int j = 0; j < count; j++) {
				for (int i = 0; i < count; i++) {
					int s = hc[i][k] + hc[k][j];

					if (s < hc[i][j]) {
						hc[i][j] = s;
		                p[i][j] = p[i][k];
					}
				}
			}
		}
	}
	
	/**
	 * method to return the number of needed hops between node from and node to.
	 * 
	 * @param from the starting node.
	 * @param to   the end node.
	 * @return the number of needed hops, or -1, if no route exists.
	 */
	public final int getHopCount(NodeId from, NodeId to) {
		int i = from.asInt() - 1, j = to.asInt() - 1;
		int res = hc[i][j];
		
		if (res == inf) {
			res = -1;
		}
		return res;
	}
	
	/**
	 * method, to get the next hop of the path between the from and the to node.
	 * 
	 * @param from the starting node.
	 * @param to   the end node.
	 * @return the NodeId of the next hop, or null if no hop exists.
	 */
	public final NodeId getNextHop(NodeId from, NodeId to) {
		int i = from.asInt() - 1, j = to.asInt() - 1;
		int k = p[i][j] + 1;
		
		if (k <= count) {
			return NodeId.get(k);
		} else {
			return null;
		}
	}
	
	/**
	 * method to resolve the complete path between two nodes.
	 * 
	 * @param from the starting node.
	 * @param to   the end node.
	 * @return the complete path, if it exists or null.
	 */
	@Override
	public final List<NodeId> resolvePath(NodeId from, NodeId to) {
		if (from.equals(to) || to.equals(NodeId.ALLNODES)) {
			ArrayList<NodeId> path = new ArrayList<NodeId>();
			
			path.add(to);
			
			return path;
		} else {
			int hopCount = getHopCount(from, to);
			if (hopCount > 0) {
				ArrayList<NodeId> path = new ArrayList<NodeId>();
				NodeId nextHop = getNextHop(from, to);
			
				while (nextHop.asInt() != to.asInt()) {
					path.add(nextHop);
					nextHop = getNextHop(nextHop, to);
				}
			
				path.add(nextHop);
			
				return path;
			}
			if (hopCount == 0) {
				 List<NodeId> path = new ArrayList<NodeId>(); 
				 path.add(to); 
				 return path; 
			}
			return null; //may be an exception would be a better idea.
		}
	}
}

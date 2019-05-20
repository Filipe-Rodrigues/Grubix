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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Factory class for NodeIds. Produces NodeId objects starting with ID 1.
 * @author Andreas Kumlehn
 */
public class NodeId extends UniqueId implements Serializable {

	/**
	 * int value of the next free NodeId.
	 */
	private static int nextFreeId = 1;
	
	/** 
	 * NodeId used for broadcasting messages to all receiving nodes.
	 */
	public static final NodeId ALLNODES = new NodeId(-1);
	
	/** 
	 * NodeId to use in a schedule for an free node. 
	 */
	public static final NodeId FREE = new NodeId(-2);
	
	/** 
	 * NodeId to use in a schedule for an unknown used node. 
	 */
	public static final NodeId USED = new NodeId(-3);
	
	/**
	 * NodeId used to send packets to a not further specified backbone node.
	 * Used e.g. by network layer implementations 
	 */
	public static final NodeId BACKBONE = new NodeId(-4);

	/**
	 * NodeId used for broadcasting messages only on a backbone.
	 */
	public static final NodeId BACKBONEBROADCAST = new NodeId(-5);
	
	/**
	 * NodeId used to send packets to a not further specified center node.
	 * Used e.g. by network layer implementations 
	 */
	public static final NodeId CENTER = new NodeId(-6);
	
	/** 
	 * Stores references to all (unique) NodeId objects ever created. This is needed to retrieve
	 * nodes from hash tables which have NodeId as their key, since it is prohibited to create
	 * NodeId objects with explicit IDs. Besides, even if it were possible, two NodeId objects
	 * with the same Integer id value do not necessarily have the same hash code in Java.	 *  
	 */
	private static final HashMap<Integer, NodeId> ID_POOL = initIdPool();
	 
	/**
	 * This method is executed only once! It ensures that the ID_POOL does also contain
	 * the constants defined above. It becomes necessary to include this NodeId-Objects into the pool
	 * because people might call {@link NodeId#get(int)} using the keys defined for the constants.
	 * 
	 * @return a newly created empty id pool.
	 */
	private static HashMap<Integer, NodeId> initIdPool() {
		HashMap<Integer, NodeId> newPool = new HashMap<Integer, NodeId>();
		newPool.put(ALLNODES.asInt(), ALLNODES);
		newPool.put(FREE.asInt(), FREE);
		newPool.put(USED.asInt(), USED);
		newPool.put(BACKBONE.asInt(), BACKBONE);
		newPool.put(BACKBONEBROADCAST.asInt(), BACKBONEBROADCAST);
		return newPool;
	}
		
	/**
	 * Generates a String version of the given path.
	 * @param path The list of nodes forming the path
	 * @return A String in the form (a, b, c, d)
	 */
	public static String getNodeIdList(List<NodeId> path) {
		return getNodeIdListSB(null, path).toString();
	}

	/**
	 * Generates a String version of the given path.
	 * @param sb supply an generated StringBuilder,or use null to generate one.
	 * @param path The list of nodes forming the path
	 * @return A StringBuilder with added (a, b, c, d)
	 */
	public static StringBuilder getNodeIdListSB(StringBuilder sb, List<NodeId> path) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		
		int l = path.size() - 1;
		
		if (l >= 0) {
			sb.append(("("));
		
			for (int i = 0; i < l; i++) {
				sb.append(path.get(i));
				sb.append(", ");
			}
		
			sb.append(path.get(l));
			sb.append(")");
		}
		
		return sb;
	}
	
	/**
	 * Constructor of the class NodeId.
	 */
	public NodeId() {
		this(nextFreeId);
		ID_POOL.put(nextFreeId, this);
		nextFreeId++;
		
	}
	
	/**
	 * constructor for initializing ALLNODES ID.
	 * @param i Value of the new NodeId.
	 */
	protected NodeId(int i) {
		super(i);
	}
	
	/**
	 * Retrieves a node id from the pool of already existing node ids.
	 * @param i The integer representation of the desired node id
	 * @return The corresponding NodeId object, or null, if node with id <code>i</code> is
	 * not available
	 */
	public static NodeId get(int i) {
		return ID_POOL.get(i);
	}
}


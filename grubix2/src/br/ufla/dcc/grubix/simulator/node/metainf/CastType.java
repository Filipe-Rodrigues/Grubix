package br.ufla.dcc.grubix.simulator.node.metainf;

import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * Constants which are used by some but not all MetaInfo classes.
 */ 
public enum CastType {
	/**
	 * Constant for casttype, saying that this packet is a broadcast packet.
	 */
	BROADCAST,

	/**
	 * Constant for casttype, saying that this packet is a broadcast packet. 
	 */
	UNICAST,

	/**
	 * Constant for casttype, saying that this packet is a broadcast packet. 
	 */
	MULTICAST;
	
	/**
	 * returns the cast type of a given node id.
	 * 
	 * @param nodeId node id 
	 * @return cast type
	 */
	public static CastType getCastType(NodeId nodeId) {
		if (nodeId.equals(NodeId.ALLNODES)) {
			return CastType.BROADCAST;
		}
		return CastType.UNICAST;
	}
}

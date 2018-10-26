package br.ufla.dcc.grubix.simulator.node.metainf;

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * Downwards Meta infos for Application.
 * 
 * @author dmeister
 *
 */
public class DownwardsApplicationMetaInfo extends DownwardsCommonMetaInfo {

	/**
	 * NodeId of the initial source (normally the creator) of the packet.
	 */
	private final NodeId source;
	
	/**
	 * simple constructor.
	 * 
	 * @param src NodeId of the initial source (normally the creator) of the packet
	 * @param nexthop List of NodeIds containing the nexthop the lower layers 
	 * are supposed to send the packet to. Number of elements must fit to castType.
	 * castType = Broadcast: no element in nexthop
	 * castType = Unicast : one element in nexthop
	 * casttype = Multicast: one or more elements in nexthop
	 * @param castType must be one of MetaInformation_CASTTYPE contants
	 */
	public DownwardsApplicationMetaInfo(NodeId src, CastType castType, NodeId... nexthop) {
		super(castType, nexthop);
		if (src == null) {
			throw new IllegalArgumentException("Source of a packet may not be null");			
		}
		this.source = src;
		
	}
	
	/**
	 * NodeId of the initial source (normally the creator) of the packet.
	 * @return source
	 */
	public NodeId getSource() {
		return source;
	}

	/**
	 * {@inheritDoc}
	 */
	public LayerType getCorrespondingLayerType() {
		return LayerType.APPLICATION;
	}
}

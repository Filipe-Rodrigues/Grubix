package br.ufla.dcc.grubix.simulator.node.metainf;

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * Downwards Meta infos for Operating system..
 * 
 * @author dmeister
 *
 */
public class DownwardsOperatingSystemMetaInfo extends DownwardsCommonMetaInfo {

	
	/**
	 * simple constructor.
	 * 
	 * @param nexthop List of NodeIds containing the nexthop the lower layers 
	 * are supposed to send the packet to. Number of elements must fit to castType.
	 * castType = Broadcast: no element in nexthop
	 * castType = Unicast : one element in nexthop
	 * casttype = Multicast: one or more elements in nexthop
	 * @param castType must be one of MetaInformation_CASTTYPE contants
	 */
	public DownwardsOperatingSystemMetaInfo(CastType castType, NodeId... nexthop) {
		super(castType, nexthop);
		
	}

	/**
	 * {@inheritDoc}
	 */
	public LayerType getCorrespondingLayerType() {
		return LayerType.OPERATINGSYSTEM;
	}
}

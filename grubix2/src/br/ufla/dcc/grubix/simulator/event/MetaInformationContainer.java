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

import java.util.List;

import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.node.metainf.DownwardsApplicationMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.DownwardsLLCMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.DownwardsMACMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.DownwardsNetworkMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.DownwardsOperatingSystemMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.MetaInformation;
import br.ufla.dcc.grubix.simulator.node.metainf.UpwardsLLCMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.UpwardsMACMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.UpwardsNetworkMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.UpwardsOperatingSystemMetaInfo;
import br.ufla.dcc.grubix.simulator.node.metainf.UpwardsPhysicalMetaInfo;




/**
 * This class is used as container for all meta informations.
 * 
 * @author Hannes Riechmann, Dirk Held
 */
public class MetaInformationContainer {

	/** a list of MetaInformation which are currently attached to the packet. */
	private List<MetaInformation> metaInformation;
	
	/**
	 * default constructor for the class MetaInformationContainer.
	 */
	public MetaInformationContainer() {
		metaInformation = new java.util.LinkedList<MetaInformation>();
	}
	
	/**
	 * attach additional MetaInfo.
	 * @param toAdd the new MetaInfo.
	 * @return true, if the MetaInfo was added.
	 */ 
	public boolean addMetaInfo(MetaInformation toAdd) {
		return metaInformation.add(toAdd);
	}

    /**
     * @param toGet     which layer is requested.
     * @param direction which direction is requested from this layer.
     * @return first MetaInformation that matches LayerType toGet and Direction
     * direction must be one of Metainformation UPWARDS or DOWNWARDS
     */
	private MetaInformation getMetaInfo(LayerType toGet, Direction direction) {
		if (metaInformation == null) {
			return null;
		}
		
		for (MetaInformation mi : metaInformation) {
			if  (mi.getCorrespondingLayerType() == toGet
			  && mi.getDirection() == direction) {
				return mi;
			}
		}
		return null;
	}
	
	/** @return the PhysicalMetaInfo, null if no PhysicalmetaInfo is attached */
	public UpwardsPhysicalMetaInfo getUpwardsPhysicalMetaInfo() {
		return (UpwardsPhysicalMetaInfo) getMetaInfo(LayerType.PHYSICAL,
				Direction.UPWARDS);
	}
	
	
	/** @return the MACMetaInfo, null if no MACmetaInfo is attached */
	public UpwardsMACMetaInfo getUpwardsMACMetaInfo() {
			return (UpwardsMACMetaInfo) getMetaInfo(LayerType.MAC,
					Direction.UPWARDS);	
	}
	
	/** @return the MACMetaInfo, null if no MACmetaInfo is attached */
	public UpwardsLLCMetaInfo getUpwardsLLCMetaInfo() {
			return (UpwardsLLCMetaInfo) getMetaInfo(LayerType.LOGLINK,
					Direction.UPWARDS);	
	}
	
	/** @return the NetworkMetaInfo, null if no NetworkmetaInfo is attached */
	public UpwardsNetworkMetaInfo getUpwardsNetworkMetaInfo() {
			return (UpwardsNetworkMetaInfo) getMetaInfo(LayerType.NETWORK,
					Direction.UPWARDS);	
	}
	
	/** @return the UpwardsOperatingSystemMetaInfo, null if no OperatingSystemmetaInfo is attached */
	public UpwardsOperatingSystemMetaInfo getUpwardsOperatingSystemMetaInfo() {
			return (UpwardsOperatingSystemMetaInfo) getMetaInfo(LayerType.OPERATINGSYSTEM,
					Direction.UPWARDS);	
	}
	
	/** @return the ApplicationMetaInfo, null if no Application
	 * metaInfo is attached */
	public DownwardsApplicationMetaInfo getDownwardsApplicationMetaInfo() {
			return (DownwardsApplicationMetaInfo) getMetaInfo(LayerType.APPLICATION,
					Direction.DOWNWARDS);	
	}
	
	/** @return the DownwardsOperatingSystemMetaInfo, null if no DownwardsOperatingSystemMetaInfo
	 * is attached */
	public DownwardsOperatingSystemMetaInfo getDownwardsOperatingSystemMetaInfo() {
			return (DownwardsOperatingSystemMetaInfo) getMetaInfo(LayerType.OPERATINGSYSTEM,
					Direction.DOWNWARDS);	
	}
	
	/** @return the NetworkMetaInfo, null if no  NetworkmetaInfo is attached */
	public DownwardsNetworkMetaInfo getDownwardsNetworkMetaInfo() {
			return (DownwardsNetworkMetaInfo) getMetaInfo(LayerType.NETWORK,
					Direction.DOWNWARDS);	
	}
	
	/** @return the LLCMetaInfo, null if no  LLCmetaInfo is attached */
	public DownwardsLLCMetaInfo getDownwardsLLCMetaInfo() {
			return (DownwardsLLCMetaInfo) getMetaInfo(LayerType.LOGLINK,
					Direction.DOWNWARDS);	
	}
	
	/** @return the MACMetaInfo, null if no  MACmetaInfo is attached */
	public DownwardsMACMetaInfo getDownwardsMACMetaInfo() {
			return (DownwardsMACMetaInfo) getMetaInfo(LayerType.MAC,
					Direction.DOWNWARDS);	
	}
}

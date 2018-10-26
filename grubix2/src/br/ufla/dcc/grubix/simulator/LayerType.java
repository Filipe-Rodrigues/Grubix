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

/** 
 * Class representing types of a layer.
 * Packets should not contain pointers to real layers to identify sending layers.
 * Therefore the class LayerType is introduced do anonymize this process.
 * For every layertype inside a node the class LayerType has a fixed constant representing
 * the layertype.
 * 
 * Note: Layer shouldn't make assumptions about the ordering, number or existence of layer if not it can be avoided.
 * Also layer implementation should not use indices of layer.
 * 
 * @author Andreas Kumlehn, Dirk Meister
 */
public enum LayerType implements Comparable<LayerType> {
	/**
	 * Air Layer.
	 */
	AIR(0, "Air", "air"),
	
	/**
	 * Physical layer.
	 */
	PHYSICAL(1, "Physical", "phy"),
	
	/**
	 * Mac layer.
	 */
	MAC(2, "MAC", "mac"),
	
	/**
	 * Log Link Layer.
	 */
	LOGLINK(3, "LogLink", "ll"),
	
	/**
	 * Network layer.
	 */
	NETWORK(4, "Network", "br.ufla"),
	
	/**
	 * Operating System layer.
	 */
	OPERATINGSYSTEM(5, "Operating System", "os"),
	
	/**
	 * Application layer.
	 */
	APPLICATION(6, "Application", "app");
	
	/** 
	 * String describing a layer. 
	 */
	private final String description;
	
	/**
	 * Index of a layer type.
	 * Should not be used outside this class.
	 */
	private final int index;
	
	/**
	 * Short name of the layer type.
	 */
	private final String shortName;
	
	/**
	 * Returns the short name of the layer type.
	 * @return a short name
	 */
	public String getShortName() {
		return shortName;
	}
    
    /**
     * Private constructor of the class LayerType.
     * 
     * @param idx The index of the created layertype.
     * @param desc String description of the new layer type.
     * @param shortName a short name of the layer type
     */
    private LayerType(int idx, String desc, String shortName) {
    	this.index = idx;
    	this.description = desc;
    	this.shortName = shortName;
    }
    
    /**
     * Private method to get the layer by the index.
     * This method should be private to prevent other to rely on index
     * numbers
     * @param index an index between 0 and 6
     * @return a layer type or null
     */
    private LayerType getLayerByIndex(int index) {
    	switch(index) {
    	case 0: return AIR;
    	case 1: return PHYSICAL;
    	case 2: return MAC;
    	case 3: return LOGLINK;
    	case 4: return NETWORK;
    	case 5: return OPERATINGSYSTEM;
    	case 6: return APPLICATION;
    	default:
    		assert false;
    	}
    	return null;
    }
	
    /**
     * Returns the next upper layer .
     * @return the next upper layer or null
     */
	public LayerType getUpperLayer() {
		if (this == APPLICATION) {
			return null;
		}
		return getLayerByIndex(index + 1);
	}

	/**
	 * Returns the next lower layer.
	 * @return the next lower layer or null
	 */
	public LayerType getLowerLayer() {
		if (this == AIR) {
			return null;
		}
		return getLayerByIndex(index - 1);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * @return Description of the layertype as String.
	 */
	@Override
	public String toString() {
		return description;
	}
}

/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.grubix.simulator.node;


import java.util.Set;

import br.ufla.dcc.grubix.simulator.NodeId;





/**
 * Marker instance for the layer state that published the information that the loglink layer
 * builds a backbone.
 * 
 * @author dmeister
 */
public interface LogLinkBackboneEnabled {
	
	/**
	 * returns the current knowledge about the neighbors on the backbone.
	 * On a non-backbone node a UnsupportedOperationException should be thrown.
	 * 
	 * The backbone neighbors are defined as directly connected backbone nodes.
	 * Assume these two situations:
	 * 1) A---------B----------C
	 *  When B is calling getBackboneNeigbours then A and C should be returned.
	 * 2) 
	 *
     *	
     *	      --------  ------E
     *	              \/       \
     *                 F        D
     *                         /
     *                        /
     *   A---------B----------C
     *                         \
     *  
     *  This situation, where F is reachable from B within one-hop can not appear.
     *  If F and B are reachable from each other they will be connected in the backbone
     *  making B a center node. 
	 * 
	 * @return current knowledge of the loglink layer about the neighbors on the backbone 
	 * should be returned. (not null)
	 */
	Set<NodeId> getBackboneNeighbors();
	
	/**
	 * Returns true if the current node belongs to the backbone. If not  then false is returned.
	 * @return true if this node is a backbone node, else false.
	 */
	boolean isBackboneNode();
	
	/**
	 * Returns whether or not the node is a center node.
	 * @return true if this node is  a center node, else false.
	 */
	boolean isCenterNode();
	
	/**
	 * Returns true if the backbone is established and ready to use.
	 * Returns false if the backbone is not established (i.e. when it is still constructing).
	 * @return boolean value.
	 */
	boolean backboneEstablished();
}

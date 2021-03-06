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
 * interface for the layer state that published the information about the neighborhood.
 * 
 * @author Yara Khaluf
 *
 */
public interface NeighborhoodProvider {

	/**
	 * Method to get the 1-hop neighbors of a node.
	 * @return the 1-hop neighbors of a node
	 */
	Set<NodeId> getNodeNeighbors();
}

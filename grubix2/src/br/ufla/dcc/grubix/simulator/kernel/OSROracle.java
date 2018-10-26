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

import java.util.List;

import br.ufla.dcc.grubix.simulator.NodeId;




/**
 * This is the superclass of all Optimal Source Routing Strategy classes. Each such class
 * can compute optimal shortest paths between any two nodes using global network knowledge.
 * Since nodes do normally not have this knowledge, subclasses of this class can be used
 * as a straightforward error-free routing protocol when other network layers are to be
 * evaluated, or they can be used to compare the performance of a particular routing
 * algorithm with that of an optimal one in terms of hop count.
 * @author jlsx
 */
public abstract class OSROracle {

	/**
	 * Method to resolve the complete path between two nodes.
	 * @param from The starting node.
	 * @param to   The end node.
	 * @return The complete path, if it exists or null.
	 */
	public abstract List<NodeId> resolvePath(NodeId from, NodeId to);
}

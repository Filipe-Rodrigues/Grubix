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
 * Unique Ids for clusters in the node graph.
 * 
 * @author Andreas Kumlehn
 */
public class ClusterId extends UniqueId {
	
	/**
	 * ClusterId with value 0 representing that a node is in no cluster at all.
	 */
	public static final ClusterId NOCLUSTER = new ClusterId(-1);
	
	/**
	 * int value of the next free ClusterId.
	 */
	private static int nextFreeId = 1;

	/**
	 * Constructor of the class ClusterId.
	 */
	public ClusterId() {
		super(nextFreeId);
		nextFreeId++;
	}
	
	/**
	 * Private constructor for initializing NOCLUSTER ID.
	 * 
	 * @param i Value of the new ClusterId.
	 */
	private ClusterId(int i) {
		super(i);
	}
}

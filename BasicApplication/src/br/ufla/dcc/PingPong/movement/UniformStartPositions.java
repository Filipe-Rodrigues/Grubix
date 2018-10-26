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

package br.ufla.dcc.PingPong.movement;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * This class generates uniformly distributed start positions. The positions are arranged
 * in a grid. It is implicitly assumed that one position is to be generated for each node,
 * i.e. the method newPosition() is invoked Configuration.nodeCount times.
 * @author jlsx
 */
public class UniformStartPositions extends StartPositionGenerator {

	/** Counts the number of nodes for which positions have already been generated. */
	private int nodeNumber;
	
	/** The number of rows and columns into which the nodes are arranged. */
	private int rowCount = -1, colCount = -1;
	
	/** The x- and y- distances between the positions. */ 
	private double xDist, yDist;
	
	/** Constructor of this class. */
	public UniformStartPositions() {
		this.nodeNumber = 0;
	}

	/** 
	 * Initializes after configuration is available. 
	 * @param configuration The configuration.
	 * @throws ConfigurationException inherited exception.
	 */
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		double nb = Math.sqrt(configuration.getNodeCount() * configuration.getYSize() / configuration.getXSize());
		int nbTest = (int) Math.round((float) nb);   // first test for row count
		
		boolean subtract = false;                    // do we subtract for the next try?
		int offset = 1;
		if (nbTest > nb) {
			subtract = true;
		}
		while (this.rowCount == -1) {
			if (configuration.getNodeCount() % nbTest == 0) {
				this.rowCount = nbTest;
				this.colCount = configuration.getNodeCount() / nbTest;
			} else {
				if (subtract) {
					subtract = false;
					nbTest -= offset;
					offset++;
				} else {
					subtract = true;
					nbTest += offset;
					offset++;
				}
			}
		}
		
		this.xDist = ((double) configuration.getNodeCount()) / (this.colCount - 1);
		this.yDist = ((double) configuration.getNodeCount()) / (this.rowCount - 1);
	}
	
	/**
	 * Generates uniformly distributed start positions.
	 * @param node The node id for which the new position should be generated.
	 * @return A new position of a node
	 */
	public Position newPosition(Node node) {		
		int curCol = this.nodeNumber % this.colCount;
		int curRow = this.nodeNumber / this.colCount; 
		double x = curCol * this.xDist;
		double y = curRow * this.yDist;
		
		this.nodeNumber++;
		return new Position(x, y);
	}

}

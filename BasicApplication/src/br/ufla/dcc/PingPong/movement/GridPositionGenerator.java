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
package br.ufla.dcc.PingPong.movement;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * Creates start positions in a regular form.
 * It places as many nodes per row as possible due to the maps size.
 * 
 * @author Thomas Kemmerich
 */
public class GridPositionGenerator extends StartPositionGenerator {

	/** The distance between two nodes in x-direction. */
	@ShoXParameter(description = "The distance between two nodes in x-direction.", defaultValue = "2.0")
	private double xNodeDistance;
	

	/** The distance between two nodes in y-direction. */
	@ShoXParameter(description = "The distance between two nodes in y-direction.", defaultValue = "2.0")
	private double yNodeDistance;

	/** Node position counter. */
	private int positionCounter = 0;
	
	/** Nodes per row. */
	private int nodesPerRow = 0;
	
	/** Nodes per column. */
	private int nodesPerColumn = 0;

	/** w.r.t. the field size and the distances between nodes a maximum number of nodes could be placed in the map. */
	private int maxPositions = 0;

	/**
	 * current configuration.
	 */
	private Configuration config;
	
	/**
	 * generates a new position that is situated in a room.
	 * Throws SimulationFailedException If more nodes than possible for this map should be placed or if no
	 * free position within a room is available
	 * @param node The node for which the new position should be generated.
	 * @return new position
	 * @see br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator#newPosition()
	 */
	@Override
	public Position newPosition(Node node) {
		double width = config.getXSize();
		double height = config.getYSize();
		nodesPerRow = (int) (width / xNodeDistance);
		nodesPerColumn = (int) (height / yNodeDistance);
		maxPositions = nodesPerRow * nodesPerColumn;
		
		Position newPosition = getNextPossiblePosition();
		
		if (positionCounter > maxPositions || newPosition == null) {
			throw new SimulationFailedException("Unable to place the desired number of nodes in the map! "
					+ " MaxPositions = " + maxPositions + ", createdPositions = " + positionCounter);
		}
		
		return newPosition;
	}

	/**
	 * Gets the next possible position within a room.
	 * @return null if no position is available or a proper position.
	 */
	private Position getNextPossiblePosition() {
		int columnPos;
		int rowPos;
		Position position;
		while (positionCounter <= maxPositions) {	
			columnPos = positionCounter % nodesPerRow;
			rowPos = (positionCounter - columnPos) / nodesPerRow;
			
			position = new Position(columnPos * xNodeDistance, rowPos * yNodeDistance);
			positionCounter++;
			
			return position;
		}
		
		return null;
	}

	/**
	 * method to initialize the configuration of the object.
	 * It is assured by the runtime system that the <code>Configuration</code>
	 * is valid, when this method is called.
	 * @param config The configuration.
	 * @throws ConfigurationException thrown when the object cannot run with
	 * the configured values.
	 */
	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);
		this.config = config;
	}

	/**
	 * @return the xNodeDistance
	 */
	public double getXNodeDistance() {
		return xNodeDistance;
	}

	/**
	 * @return the yNodeDistance
	 */
	public double getYNodeDistance() {
		return yNodeDistance;
	}
	
}

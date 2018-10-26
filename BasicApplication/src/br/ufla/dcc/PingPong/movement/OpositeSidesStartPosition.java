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

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * This class generates start positions at oposite sides of the area.
 * It is intented to be used in the simulation of simple VAS scenarios
 * of crossing vehicles in a road.
 * @author Edison Pignaton de Freitas, Tales Heimfarth
 */
public class OpositeSidesStartPosition extends StartPositionGenerator {
	
	/**
	 * xSize (from the Configuration).
	 */
	private double xSize;
	
	/**
	 * ySize (from the Configuration).
	 */
	private double ySize;
	
	
	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);
		xSize = config.getXSize();
		ySize = config.getYSize();
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator.
	 * @param node The node for which the new position should be generated.
	 * @return A new position for a node
	 */
	@Override
	public final Position newPosition(Node node) {
		double x = 0;
		double y = 0;
		
		int node_id = node.getId().asInt();
		
		
		 if(node_id % 2 == 0){
			 x = xSize - Math.random()*(xSize/4.0);
			 y = ySize/2.0-0.5-Math.random();
		 }
		 else {
			 x = 0 + Math.random()*(xSize/4.0);
			 y = ySize/2.0+0.5+Math.random();
		 }
	
		 Position realPos = new Position(x, y);
		return realPos;
	}
}


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
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * This class generates random start positions within the dimension of the deployment
 * area, i.e. 0 <= x <= Configuration.xSize and y, accordingly.
 * @author jlsx
 */
public class MXMacRandomStartPositions extends br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator {
	
	/** testingCase = 1: Diagonal Principal
	 *  testingCase = 2: Vertical 
	 *  testingCase = 3: Diagonal Secundária
	 *  testingCase = 4: Horizontal
	 *  
	 *  testingCase != [1, 4]: Absolutely Random
	 *  */
	@ShoXParameter(description = "Caso de teste para o backbone.", defaultValue = "1")
	private int testingCase;
	
	@ShoXParameter(description = "Força o posicionamento de nós chave para construção do backbone", defaultValue = "true")
	private boolean forceCenterNodes;
	
	/**
	 * xSize (from the Configuration).
	 */
	private double xSize;
	
	/**
	 * ySize (from the Configuration).
	 */
	private double ySize;
	
	/**
	 * random generator.
	 * By the default the global random generator is used.
	 */
	@ShoXParameter(description = "random generator", defaultClass = InheritRandomGenerator.class)
	private RandomGenerator random;
	
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
		if (node.getId().asInt() == 10 && forceCenterNodes) {
			// O nó de ID 10 estará fixado no canto superior direito, dirigindo
			// o backbone para a esquerda:
			return new Position(19d*xSize/20d, ySize/3d);
		} else if (node.getId().asInt() == 11 && forceCenterNodes) {
			// O nó de ID 10 estará fixado no canto superior esquerdo, dirigindo
			// o backbone para a direita:
			return new Position(xSize/20d, ySize/3d);
		} else if (node.getId().asInt() == 12 && forceCenterNodes) {
			// O nó de ID 10 estará fixado no canto superior esquerdo, dirigindo
			// o backbone para a baixo:
			return new Position(xSize/3d, ySize/20d);
		} else if (node.getId().asInt() == 13 && forceCenterNodes) {
			// O nó de ID 11 estará fixado no canto superior direito, dirigindo
			// o backbone para baixo:
			return new Position(2d*xSize/3d, ySize/20d);
		} else if (node.getId().asInt() == 14 && forceCenterNodes) {
			// O nó de ID 13 estará fixado no canto inferior esquerdo, dirigindo
			// o backbone para direita:
			return new Position(xSize/20d, 2d*ySize/3d);
		} else if (node.getId().asInt() == 15 && forceCenterNodes) {
			// O nó de ID 12 estará fixado no canto inferior direito, dirigindo
			// o backbone para a esquerda:
			return new Position(19d*xSize/20d, 2d*ySize/3d);
		} else if (node.getId().asInt() == 16 && forceCenterNodes) {
			// O nó de ID 12 estará fixado no canto inferior direito, dirigindo
			// o backbone para a cima:
			return new Position(2d*xSize/3d, 19d*ySize/20d);
		} else if (node.getId().asInt() == 17 && forceCenterNodes) {
			// O nó de ID 13 estará fixado no canto inferior esquerdo, dirigindo
			// o backbone para cima:
			return new Position(xSize/3d, 19d*ySize/20d);
		} else if (node.getId().asInt() == 1 || node.getId().asInt() == 2) {
			return generateFixedPosition(node.getId().asInt());
		} else if (node.getId().asInt() == 5) {
			// Fixa um possível sink no meio do canto inferior:
			return new Position(xSize / 2d, ySize - 5);
		} else {
			return generateRandom();
		}
	}
	
	private Position generateFixedPosition(int nodeId) {
		switch (testingCase) {
		case 1:
			return (nodeId == 1) ? (new Position(5, 5)) : (new Position(xSize - 5, ySize - 5));
		case 2:
			return (nodeId == 1) ? (new Position(xSize/2, 5)) : (new Position(xSize/2, ySize - 5));
		case 3:
			return (nodeId == 1) ? (new Position(xSize - 5, 5)) : (new Position(5, ySize - 5));
		case 4:
			return (nodeId == 1) ? (new Position(5, ySize/2)) : (new Position(xSize - 5, ySize/2));
		
		default:
			return generateRandom();
		}
	}
	
	private Position generateRandom() {
		double x = random.nextDouble() * xSize;
		x = Math.floor(x * 100) / 100;
		double y = random.nextDouble() * ySize;
		y = Math.floor(y * 100) / 100;
		return new Position(x, y);
	}

	public RandomGenerator getRandom() {
		return random;
	}

	public void setRandom(RandomGenerator random) {
		this.random = random;
	}

	public double getXSize() {
		return xSize;
	}

	public void setXSize(double size) {
		xSize = size;
	}

	public double getYSize() {
		return ySize;
	}

	public void setYSize(double size) {
		ySize = size;
	}
}

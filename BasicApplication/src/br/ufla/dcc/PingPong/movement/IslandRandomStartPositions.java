package br.ufla.dcc.PingPong.movement;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * This class generates island random start positions within the dimension of the deployment
 * area, i.e. 0 <= x <= Configuration.xSize and y, accordingly.
 * @author Adauto Mendes
 */
public class IslandRandomStartPositions extends StartPositionGenerator {
	
	/**
	 * xSize (from the Configuration).
	 */
	private double xSize;
	
	/**
	 * ySize (from the Configuration).
	 */
	private double ySize;
	
	
	private int nodeCount;
	
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
		nodeCount = config.getNodeCount();
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator.
	 * @param node The node for which the new position should be generated.
	 * @return A new position for a node
	 */
	@Override
	public final Position newPosition(Node node) {
		double x;
		double y;
		Position realPos = null;		
		if (node.getId().asInt() < nodeCount / 6)
		{
		x = random.nextDouble() * (0.2 * xSize);
		x = Math.floor(x * 100) / 100;
		y = random.nextDouble() * (0.3 * ySize);
		y = Math.floor(y * 100) / 100;
		realPos = new Position(x, y);				
		}
		else if(node.getId().asInt() < nodeCount / 3)
		{
			x = random.nextDouble() * (0.2 * xSize) + (0.4 * xSize);
			x = Math.floor(x * 100) / 100;
			y = random.nextDouble() * (0.3 * ySize);
			y = Math.floor(y * 100) / 100;
			realPos = new Position(x, y);
			
		}
		else if(node.getId().asInt() < nodeCount / 2)
		{
			x = random.nextDouble() * (0.2 * xSize) + (0.8 * xSize);
			x = Math.floor(x * 100) / 100;
			y = random.nextDouble() * (0.3 * ySize);
			y = Math.floor(y * 100) / 100;
			realPos = new Position(x, y);
			
		}
		else if (node.getId().asInt() < ((nodeCount / 6) * 4))
		{
		x = random.nextDouble() * (0.2 * xSize);
		x = Math.floor(x * 100) / 100;
		y = random.nextDouble() * (0.3 * ySize) + (0.7 * ySize);
		y = Math.floor(y * 100) / 100;
		realPos = new Position(x, y);				
		}
		else if(node.getId().asInt() < (nodeCount / 6) * 5)
		{
			x = random.nextDouble() * (0.2 * xSize) + (0.4 * xSize);
			x = Math.floor(x * 100) / 100;
			y = random.nextDouble() * (0.3 * ySize) + (0.7 * ySize);
			y = Math.floor(y * 100) / 100;
			realPos = new Position(x, y);
			
		}
		else if(node.getId().asInt() <= nodeCount)
		{
			x = random.nextDouble() * (0.2 * xSize) + (0.8 * xSize);
			x = Math.floor(x * 100) / 100;
			y = random.nextDouble() * (0.3 * ySize) + (0.7 * ySize);
			y = Math.floor(y * 100) / 100;
			realPos = new Position(x, y);
			
		}
		return realPos;
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
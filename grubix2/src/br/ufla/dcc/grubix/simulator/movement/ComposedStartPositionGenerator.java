/**
 * 
 */
package br.ufla.dcc.grubix.simulator.movement;

import java.util.Vector;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.JavaRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * @author Edison Pignaton de Freitas, Tales Heimfarth
 *
 */
public class ComposedStartPositionGenerator extends StartPositionGenerator {

	
	/**
	 * xSize (from the Configuration).
	 */
	private double xSize;
	
	/**
	 * ySize (from the Configuration).
	 */
	private double ySize;
	
	private RandomGenerator random = new JavaRandomGenerator();
	private StartPositionXMovement XStartPostion = new StartPositionXMovement();
	
	private RandomStartDistantPositions RdStartPosition1 = new RandomStartDistantPositions();
	
	private RandomStartDistantPositions RdStartPosition2 = new RandomStartDistantPositions();	
	
	public ComposedStartPositionGenerator() {
		

		// TODO Auto-generated constructor stub
	}

	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);
		xSize = config.getXSize();
		ySize = config.getYSize();
		
		XStartPostion.setXSize(xSize);
		XStartPostion.setYSize(ySize);
		
		RdStartPosition1.setRandom(random);
		RdStartPosition1.setXSize(xSize);
		RdStartPosition1.setYSize(ySize);
		
		RdStartPosition2.setRandom(random);
		RdStartPosition2.setXSize(xSize);
		RdStartPosition2.setYSize(ySize);
	}
	
	/* (non-Javadoc)
	 * @see br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator#newPosition(br.ufla.dcc.grubix.simulator.node.Node)
	 */
	@Override
	public Position newPosition(Node node) {
		
		Position startPosition = null;
		
		switch (node.getTypeOfNode()){
		
		case 0: XStartPostion.setXSize(xSize);
		        XStartPostion.setYSize(ySize);
		        startPosition = XStartPostion.newPosition(node);
			    break;
		case 1: RdStartPosition1.setMinimumDistance(80);
			    RdStartPosition1.setRandom(random);
				RdStartPosition1.setXSize(xSize);
				RdStartPosition1.setYSize(ySize);
				startPosition = RdStartPosition1.newPosition(node);
		        break;
		case 2: RdStartPosition2.setMinimumDistance(15);
			    RdStartPosition2.setRandom(random);
				RdStartPosition2.setXSize(xSize);
				RdStartPosition2.setYSize(ySize);
				startPosition = RdStartPosition2.newPosition(node);
		 		break;
		}
		// TODO Auto-generated method stub
		return startPosition;
	}

}

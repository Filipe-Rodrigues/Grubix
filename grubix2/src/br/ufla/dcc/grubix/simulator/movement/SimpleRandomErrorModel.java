/**
 * 
 */
package br.ufla.dcc.grubix.simulator.movement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.JavaRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * A really simple random node error model.
 * It specifies the propability of a node to fail in the movement intervals.
 * Every failure are distributed uniformly in the movement interval.
 * 
 * @author dmeister
 *
 */
public class SimpleRandomErrorModel extends MovementManager {

	/** 
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SimpleRandomErrorModel.class);
	
	/**
	 * probability of a node to fail in the movement interval.
	 */
	@ShoXParameter(description = "probability of a node to fail in the movement interval",
			defaultValue = "0.0")
	private double failProbability;
	
	/**
	 * random generator.
	 */
	@ShoXParameter(description = "random generator", defaultClass = JavaRandomGenerator.class)
	private RandomGenerator random;
	
	/**
	 * creates node failures by flipping a coin for each active node if it fails in the current
	 * movement time interval. If the node fails, the delay inside the movement time interval
	 * is distributed uniformly.
	 * 
	 * @param allNodes a collection with all nodes of the system.
	 * @see br.ufla.dcc.grubix.simulator.movement.MovementManager#createMoves(java.util.Collection)
	 * @return a list of movements with position = null to simulate node failures.
	 */
	@Override
	public Collection<Movement> createMoves(Collection<Node> allNodes) {
		List<Movement> movements = new LinkedList<Movement>();
		for (Node node : allNodes) {
			if (!node.isSuspended()) {
				if (random.nextDouble() < failProbability) { // node fails
					
					double delay = random.nextDouble() *  getConfig().getMovementTimeInterval();
					Movement movement = new Movement(node, node.getPosition(), delay, true);
					movements.add(movement);
				}
			}
		}
		return movements;
	}

	/**
	 * @see br.ufla.dcc.grubix.simulator.movement.MovementManager#init()
	 * @throws ConfigurationException thrown if the probability to fail to not in the interval [0,1].
	 */
	@Override
	public void init() throws ConfigurationException {
		super.init();
		
		if (failProbability < 0.0 && failProbability > 1.0) {
			throw new ConfigurationException("fail probability is invalid");
		}
		
		LOGGER.info("Seed for RandomErrorModel: " + random.getSeed());
		
	}

	/**
	 * @see br.ufla.dcc.grubix.simulator.movement.MovementManager#initConfiguration()
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
	}

}

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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/**
 * Makes all nodes positioned within a certain rectangle of a configurable size to shutdown
 * for a specified time with a configurable probability. 
 * 
 * @author Thomas Kemmerich
 */
public class RectangleShutdownErrorModel extends MovementManager {

	/** This flag is used to avoid that the nodes are shutdown directly at the beginning of the simulation. */
	private static boolean skipfirst = true;
	
	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(RectangleShutdownErrorModel.class);
	
	/** Every movement interval a rectangle will be suspended with this probability. */
	@ShoXParameter(description = "Every movement interval a rectangle will be suspended with this probability",
			defaultValue = "0.03")
	private double failProbability; 
	
	/** The top left X coordinate of the rectangle within the nodes should be shutdown. */
	@ShoXParameter(description = "The top left X coordinate of the rectangle within the nodes should be shutdown.",
			defaultValue = "20.0")
	private double topLeftXPosition;
	
	/** The top left Y coordinate of the rectangle within the nodes should be shutdown. */
	@ShoXParameter(description = "The top left Y coordinate of the rectangle within the nodes should be shutdown.",
			defaultValue = "20.0")
	private double topLeftYPosition;
	
	/** The max width of the rectangle to fail. */
	@ShoXParameter(description = "The max width of the rectangle to fail", defaultValue = "30.0")
	private double maxWidth;
	
	/** The max height of the rectangle to fail. */
	@ShoXParameter(description = "The max height of the rectangle to fail", defaultValue = "30.0")
	private double maxHeight;
	
	/** The minimal width of the rectangle to fail. */
	@ShoXParameter(description = "The minimal width of the rectangle to fail", defaultValue = "30.0")
	private double minWidth;
	
	/** The minimal height of the rectangle to fail. */
	@ShoXParameter(description = "The minimal height of the rectangle to fail", defaultValue = "30.0")
	private double minHeight;
	
	/** The timespan for which a rectangle will be shutdown. */
	@ShoXParameter(description = "The timespan for which a rectangle will be shutdown.", defaultValue = "30000.0")
	private double shutdownTime;
	
	/** At the end of this interval a shutdown might take place. */
	@ShoXParameter(description = "At the end of this interval a shutdown might take place.", defaultValue = "50000.0")
	private double shutdownInterval;
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public double getDelayToNextQuery() {
		return shutdownInterval;
	}
	
	
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
		RandomGenerator random = Configuration.getInstance().getRandomGenerator();
		List<Movement> movements = new LinkedList<Movement>();
		

		if (skipfirst) {
			skipfirst = false;
			return movements;
			
		}
		
		if (random.nextDouble() > failProbability) {
			return movements; // this time no nodes are suspended.
		}
		
		// determine the dimensions of the rectangle
		double width = random.nextDouble() * (maxWidth - minWidth) + minWidth;
		double heigth = random.nextDouble() * (maxHeight - minHeight) + minHeight;
				
		Position topLeftPosition = new Position(topLeftXPosition, topLeftYPosition);
		for (Node node : allNodes) {
			if (!node.isSuspended()) {
				if (toSuspend(topLeftPosition, width, heigth, node)) {
				
					// shutdown now!
					Movement shutDownMovement = new Movement(node, node.getPosition(), 0, true);
					movements.add(shutDownMovement);
					
					// wake up again in 'shutdownTime'
					Movement wakeUpMovement = new Movement(node, node.getPosition(), shutdownTime, false);
					movements.add(wakeUpMovement);
					
					LOGGER.info("Suspending " + node.getId() + " for " + shutdownTime 
							+ " till " + (shutdownTime + SimulationManager.getInstance().getCurrentTime()));
				}
			}
		}
		return movements;
	}

	/**
	 * Returns true if the specified node lies in the rectangle of nodes that should be shutdown.
	 * @param topLeftPosition The top left position of the rectangle.
	 * @param width Rectangles width.
	 * @param heigth Rectangles height.
	 * @param node The node to check.
	 * @return true if the node is within the rectangle, otherwise false.
	 */
	private boolean toSuspend(Position topLeftPosition, double width, double heigth, Node node) {
		double nodePosX = node.getPosition().getXCoord();
		double nodePosY = node.getPosition().getYCoord();
		
		double minX = topLeftPosition.getXCoord();
		double maxX = minX + width;
		double minY = topLeftPosition.getYCoord();
		double maxY = minY + heigth;
		
		return (nodePosX >= minX && nodePosX <= maxX 
			&&	nodePosY >= minY && nodePosY <= maxY);
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
	}


	/**
	 * gets failProbability.
	 * @return current failProbability
	 */
	public double getFailProbability() {
		return failProbability;
	}


	/**
	 * gets maxWidth.
	 * @return current maxWidth
	 */
	public double getMaxWidth() {
		return maxWidth;
	}


	/**
	 * gets maxHeight.
	 * @return current maxHeight
	 */
	public double getMaxHeight() {
		return maxHeight;
	}


	/**
	 * gets shutdownTime.
	 * @return current shutdownTime
	 */
	public double getShutdownTime() {
		return shutdownTime;
	}


	/**
	 * gets shutdownInterval.
	 * @return current shutdownInterval
	 */
	public double getShutdownInterval() {
		return shutdownInterval;
	}

}

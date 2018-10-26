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

Copyright 2008 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.movement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/** 
 * Manager to create node movements according to the well-known Random Waypoint Model.
 * Every node moves a random distance between 0 and maxRange with a speed between minSpeed and
 * maxSpeed. Having arrived at the destination, the node pauses for a random time up to maxPauseTime.
 * The direction is determined randomly between 0 and 360 degrees.
 * 
 * @author jlsx
 * 
 */
public class RandomWaypoint extends MovementManager {
	/**
	 * Logger for the class SimulationManager.
	 */
	private static final Logger LOGGER = Logger.getLogger(RandomWaypoint.class.getName());
	
	private static final String DIRECTION_BASED = "direction";
	private static final String WAYPOINT_BASED = "waypoint";
	
	/**
	 * The strategy with which the next waypoint is computed. This can be done by randomly selecting range +
	 * direction, or waypoint directly (no maxRange).
	 */
	@ShoXParameter(description = "Either 'direction' or 'waypoint' (see JavaDoc)", defaultValue = "waypoint")
	private String computationMode;	
	
	/**
	 * The maximum movement range for one single step in meters.
	 */
	@ShoXParameter(description = "The maximum distance between two waypoints in meters", required = false)
	private double maxRange;
	
	/**
	 * The minimum node speed in m/s.
	 */
	@ShoXParameter(description = "The minimum node speed in m/s", required = true)
	private double minSpeed;
	
	/**
	 * The maximum node speed in m/s.
	 */
	@ShoXParameter(description = "The maximum node speed in m/s", required = true)
	private double maxSpeed;

	/**
	 * The maximum pause time between two movements in seconds.
	 */
	@ShoXParameter(description = "The maximum pause time in seconds", required = true)
	private double maxPauseTime;

	/**
	 * random generator.
	 * By the default the global random generator is used.
	 */
	@ShoXParameter(description = "random generator", defaultClass = InheritRandomGenerator.class)
	private RandomGenerator random;
	
	/**
	 * Vector containing a list of moves for every node.
	 */
	private ArrayList<List<Movement>> moveLists; 
	
	/** Stores for each node the number of remaining pause rounds. */
	private HashMap<NodeId, Integer> remainingPauseRounds;
	
	/** The approx. number of movementTimeInterval rounds corresponding to maxPauseTime. */  
	private int maxPauseRounds;
	
	
	/** Constructor of the class RandomWaypoint. */
	public RandomWaypoint() {
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.movement.MovementManager#createMoves(java.util.Collection)
	 * Creates random moves for each node in the SIMULATION.
	 * Determines a distance and direction randomly.
	 * Afterwards calculates the new coordinates.
	 * 
	 * @param allNodes allNodes Collection containing all nodes to create moves for.
	 * @return Collection containing new moves.
	 */
	public final Collection<Movement> createMoves(Collection<Node> allNodes) {
		final Configuration configuration = Configuration.getInstance();
		
		if (this.moveLists == null) {
			LOGGER.debug("Setting up vector with new lists...");
			this.moveLists = new ArrayList<List<Movement>>(allNodes.size());
			
			for (int i = 0; i < allNodes.size(); i++) {
				this.moveLists.add(i, new LinkedList<Movement>());
			}
		}
		if (this.remainingPauseRounds == null) {
			this.remainingPauseRounds = new HashMap<NodeId, Integer>();
			for (Node n : allNodes) {
				this.maxPauseRounds = (int) (configuration.getSimulationSteps(maxPauseTime) / configuration.getMovementTimeInterval());
				this.remainingPauseRounds.put(n.getId(), this.random.nextInt(this.maxPauseRounds + 1));
			}
		}
		
		List<Movement> nextMoves = new LinkedList<Movement>();
		for (Node node : allNodes) {
			int id = node.getId().asInt();
			List<Movement> list = this.moveLists.get(id - 1);
			if (list.isEmpty()) {
				if (this.remainingPauseRounds.get(node.getId()) == 0) {
					//generate a new move consisting of several steps
					Position currentPos = node.getPosition();
					//LOGGER.debug("New moves for node " + id + " at position " + currentPos);
					
					double hypo = 0.0;
					double newx = currentPos.getXCoord(), newy = currentPos.getYCoord();

					//create a vector from current position to goal position
					double[] currentToGoal = new double[2];

					if (this.computationMode.equals(DIRECTION_BASED)) {
						//real movement range is the hypotenuse of a triangle
						hypo = random.nextDouble() * maxRange;
						//determine direction
						double rad = random.nextDouble() * 2 * Math.PI;
						//calculate X and Y via cos and sin
						double deltax = Math.cos(rad) * hypo;
						double deltay = Math.sin(rad) * hypo;
						//calculate new coordinates and clamp to field
						newx = Math.max(0.0, Math.min(currentPos.getXCoord() + deltax, configuration.getXSize()));
						newy = Math.max(0.0, Math.min(currentPos.getYCoord() + deltay, configuration.getYSize()));
						
						currentToGoal[0] = newx - currentPos.getXCoord();
						currentToGoal[1] = newy - currentPos.getYCoord();

					} else if (this.computationMode.equals(WAYPOINT_BASED)) {
						newx = random.nextDouble() * configuration.getXSize();
						newy = random.nextDouble() * configuration.getYSize();
						
						currentToGoal[0] = newx - currentPos.getXCoord();
						currentToGoal[1] = newy - currentPos.getYCoord();
						hypo = Math.sqrt(currentToGoal[0] * currentToGoal[0] + currentToGoal[1] * currentToGoal[1]);
					}
					
					//LOGGER.debug("Goal position (" + newx + ", " + newy + ")");
					
					double speed = random.nextDouble() * Math.abs(this.maxSpeed - this.minSpeed) + this.minSpeed;
					double duration = configuration.getSimulationSteps(hypo / speed);
					double steps = Math.ceil(duration / configuration.getMovementTimeInterval());
					//LOGGER.debug("CurrentToGoal: (" + currentToGoal[0] + ", " + currentToGoal[1] 
					//    + "), duration:" + duration + ", steps:" + steps);
	
					//shorten the vector
					currentToGoal[0] /= steps;
					currentToGoal[1] /= steps;
					
					double nextx = currentPos.getXCoord();
					double nexty = currentPos.getYCoord();
					for (int i = 1; i <= steps; i++) {
						nextx += currentToGoal[0];
						nexty += currentToGoal[1];
						Position nextPos = new Position(nextx, nexty);
						Movement nextMove = new Movement(node, nextPos, 0);
						list.add(nextMove);
					}
				} else {   // there are still some pause rounds left
					int roundsLeft = this.remainingPauseRounds.get(node.getId()) - 1;
					this.remainingPauseRounds.put(node.getId(), roundsLeft);
				}
			}
			
			if (!list.isEmpty()) {
				//retrieve next step for the current node
				nextMoves.add(list.remove(0));
			}
		}
		return nextMoves;
	}
}

/**
 * 
 */
package br.ufla.dcc.PingPong.movement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * A movement manager that merges several other movement manger into one stream
 * of movements.
 * 
 * One scenario is that one movement manager is used to create the real movements,
 * and another movement is used as error model
 * 
 * @author dmeister
 *
 */
public class MultipleMovementManager extends MovementManager {

	/**
	 * a list of child movement managers that produce the movements.
	 */
	@ShoXParameter(description = "Movement managers")
	private MovementManager[] movementManagers;

	/**
	 * Creates a collection of movements by merging the moved of the configured
	 * movement managers together.
	 * 
	 * @param allNodes a collection with all nodes of the system.
	 * @see br.ufla.dcc.grubix.simulator.movement.MovementManager#createMoves(java.util.Collection)
	 * @return a collection of movements merges from the return values of the children.
	 */
	@Override
	public Collection<Movement> createMoves(Collection<Node> allNodes) {
		List<Movement> movements = new LinkedList<Movement>();
		
		for (MovementManager manager : movementManagers) {
			movements.addAll(manager.createMoves(allNodes));
		}
		return movements;
	}

}

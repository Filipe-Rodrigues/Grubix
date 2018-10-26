/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.PingPong.movement;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.node.user.Command;
import br.ufla.dcc.grubix.simulator.node.user.MoveToCommand;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * Manager to create a scan pattern movement in which the node will follow a straight line the Y (vertical) axe,
 * and then when arrive in the top or in the bottom of the area, will walk to right one position until arrive to the edge.
 * When it happens, it will return, making the vertical scan, but walking to the left.
 *
 * @author Edison Pignaton de Freitas
 *
 */
public class VerticalScan extends MovementManager {
       /**
        * Logger for the class SimulationManager..
        */
       private static final Logger LOGGER = Logger.getLogger(SimpleRoad.class.getName());

       /**
        * The maximum time for one movement.
        */
       //@ShoXParameter(description = "The maximum time for one movement.", required = true)
       private double maxTimeForMove=30.0;

       /**
        * random generator.
        * By the default the global random generator is used.
        */
       @ShoXParameter(description = "random generator", defaultClass =InheritRandomGenerator.class)
       private RandomGenerator random;

       /**
        * Vector containing a list of moves for every node.
        */
       private ArrayList<List<Movement>> moveLists;

       /**
        * Vector containing a list of the speed of nodes
        */
       private Map<Integer,Double> speeds;


       /** Constructor of the class SimpleRoad. */
       public VerticalScan() {
       }
       
   	private AbstractMap<NodeId,Position> currentGoals = new HashMap<NodeId,Position>();

// minhas adicoes
       /**
        * xSize (from the Configuration).
        */
       private double xSize;
       
     

       /**
        * {@inheritDoc}
        */
       public void initConfiguration(Configuration config) throws ConfigurationException {
               super.initConfiguration(config);
               xSize = config.getXSize();
               }
// fim das minhas adicoes

       // Comando que altera a velocidade do carro
       public void sendCommand (String command, NodeId id, double value1) {
               LOGGER.info("command " + command + " received");
               if (this.speeds!=null && command.contains("increase_speed")) {
                       this.speeds.put(id.asInt()-1,this.speeds.get(id.asInt()-1) + value1);
                       if (this.speeds.get(id.asInt()-1)>1)    {
                               this.speeds.put(id.asInt()-1,1.0);
                       }
               }
       }
       
       public void sendCommand (Command cmd)
       {
       	if (cmd instanceof MoveToCommand)
       	{
       		MoveToCommand param = (MoveToCommand) cmd;
       		//Move o nodo para posicao informada
       		currentGoals.put(param.getId(),param.getTarget());
       		SimulationManager.logMessage(param.getSender(), 0, "moveTo Called");
       	}
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

               Double x, y;

               List<Movement> nextMoves = new LinkedList<Movement>();

               if (this.speeds==null)
               {
                       this.speeds = new HashMap<Integer,Double>();

                       for (Node node : allNodes)
                       {
                               this.speeds.put(node.getId().asInt(),Math.random()/5.0);
                       }
               }
               
       		if (this.moveLists == null) {
    			LOGGER.debug("Setting up vector with new lists...");
    			// Usa o verdadeiro numero de nodos, para que essa classe possa ser chamada
    			// pelo composedMovement com um subconjunto dos nodos
    			this.moveLists = new ArrayList<List<Movement>>(SimulationManager.getAllNodes().size());
    			
    			for (int i = 0; i < SimulationManager.getAllNodes().size(); i++) {
    				this.moveLists.add(i, new LinkedList<Movement>());
    			}
    		}

               for (Node node : allNodes)
               {
            	   List<Movement> list = this.moveLists.get(node.getId().asInt());
            	   if (list.isEmpty()) {
                       Position currentPos = node.getPosition();
                       int node_id = node.getId().asInt();
                       x = currentPos.getXCoord();
                       y = currentPos.getYCoord();
                       double xsize = configuration.getXSize();
                       double ysize = configuration.getYSize();

                       double numpassos = 500/configuration.getMovementTimeInterval();
                       double passo = xsize/numpassos;
                       double speed = speeds.get(node_id)*2.0; //Speed of the movement

       				Double newx;
    				Double newy;   
       				if(currentGoals.containsKey(node.getId()))
    				{
    					// The target is determined by a command
    				  newx = currentGoals.get(node.getId()).getXCoord();
    				  newy = currentGoals.get(node.getId()).getYCoord();
    				  currentGoals.remove(node.getId());
    				} else{
                       
                       if(node.isDirectionY() == true){
                    	   y = y - (passo*speed);
                    	   if(y <= 0){
                    		  node.setDirectionY(false);
                    		   if(node.isDirectionX() == true){
                    			   if (x+1 <= xsize){
                    				   x = x + 1;
                    			   }
                    			   if (x == xsize){
                    				   node.setDirectionX(false);
                    			   }
                    		   } else {
                    			   if (x-1 >= 0){
                    				   x = x - 1;
                    			   }
                    			   if (x == 0){
                    				   node.setDirectionX(true); 
                    			   }
                    		   }
                    	   }
                       }
                       else{
                    	   y = y + (passo*speed);
                    	   if(y >= ysize){
                    		   node.setDirectionY(true);
                    		   if(node.isDirectionX()== true){
                    			   if (x+1 <= xsize){
                    				   x = x + 1;
                    			   }
                    			   if (x == xsize){
                    				   node.setDirectionX(false);
                    			   }
                    		   } else {
                    			   if (x-1 >= 0){
                    				   x = x - 1;
                    			   }
                    			   if (x == 0){
                    				   node.setDirectionX(true); 
                    			   }
                    		   }
                    	   }
                       }
 
                       newx = x;
                       newy = y;
    				}

    				//LOGGER.debug("Goal position (" + newx + ", " + newy + ")");
    				//create a vector from current position to goal position
    				double[] currentToGoal = new double[2];
    				currentToGoal[0] = newx - currentPos.getXCoord();
    				currentToGoal[1] = newy - currentPos.getYCoord();
    				
    				Double duration = Math.random()* maxTimeForMove;
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
    				if (list.isEmpty())
    				{
    					System.out.print("!!!!!!!!!!!!Bugou!!!!!!!!!!!!!!!");
    				}
            	   }     
            	   nextMoves.add(list.remove(0)); 
            	   
               }
               return nextMoves;
       }
}


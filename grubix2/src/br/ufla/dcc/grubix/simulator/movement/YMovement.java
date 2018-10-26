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

package br.ufla.dcc.grubix.simulator.movement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.random.InheritRandomGenerator;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * Manager to create a straight ahead movement on the x-axe.
 *
 * @author Edison Pignaton de Freitas
 *
 */
public class YMovement extends MovementManager {
       /**
        * Logger for the class SimulationManager..
        */
       private static final Logger LOGGER = Logger.getLogger(SimpleRoad.class.getName());

       /**
        * The maximum time for one movement.
        */
       @ShoXParameter(description = "The maximum time for one movement.", required = true)
       private double maxTimeForMove;

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
       public YMovement() {
       }

// minhas adicoes
       /**
        * xSize (from the Configuration).
        */
       private double xSize;
       
       /**
        * Direction: set the direction in which the node will move
        * true: from the bottom to the top
        * false: from the top to the bottom 
        * default: true
        */
       private boolean Direction = true;     
       
       public void setDirection (boolean b){
    	   Direction = b;
       }
       

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

               for (Node node : allNodes)
               {
     
                       Position currentPos = node.getPosition();
                       int node_id = node.getId().asInt();
                       x = currentPos.getXCoord();
                       y = currentPos.getYCoord();
                       double xsize = configuration.getXSize();
                       double ysize = configuration.getYSize();

                       double numpassos = 500/configuration.getMovementTimeInterval();
                       double passo = xsize/numpassos;
                       double speed = speeds.get(node_id)*2.0; //Speed of the movement

                       
                       if(Direction == true){
                    	   y = y - (passo*speed);
                       }
                       else{
                    	   y = y + (passo*speed);
                       }

                       Double newx = x;
                       Double newy = y;

                       Position nextPos = new Position(newx, newy);
                       Movement nextMove = new Movement(node, nextPos, 0);
                       nextMoves.add(nextMove);
               }
               return nextMoves;
       }
}

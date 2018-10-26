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

import java.util.Collection;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.node.user.Command;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;




/**
 * Abstract superclass for all types of MovementGenerators.
 * The SimulationManager calls MovementManager.createMoves
 * repeatedly after a constant time.
 * See SimulationManager.MoveManagerEnvelope for further information.
 *
 * The movement manager contains also the error model for node failtures.
 * If the nodeFailure property of a {@link Movement} is true, than
this means that
 * the node fails. If later a movement for the same node exists that
 * has the property set to "false", this means a recovery of the node.
 * This way the MovementManager has also the responsibility as an
"Error Model" used in other
 * network simulation frameworks.
 *
 * @see Movement
 * @author Andreas Kumlehn
 */
public abstract class MovementManager implements Configurable {

       /** The initial delay to wait, before movements are generated. */
       @ShoXParameter(description = "The initial delay to wait, before movements are generated", defaultValue = "0.0")
       protected double initialDelay;

       /**
        * reference to the configuration.
        */
       private Configuration config;

       /**
        * method to initialize the configuration of the object.
        * It is assured by the runtime system that the <code>Configuration</code>
        * is valid, when this method is called.
        *
        * @param config configuration of the simulation run
        * @throws ConfigurationException thrown when the object cannot run with
        * the configured values.
        */
       public void initConfiguration(Configuration config) throws
ConfigurationException {
               this.config = config;
       }

       /**
        * Called by the ConfigurableFactory after setting the configured
        * parameter.
        * @throws ConfigurationException thrown if configuration is invalid.
        */
       public void init() throws ConfigurationException {
               // nothing to do
       }

       /**
        * Called directly before the simulation starts.
        * @param allNodes Collection containing all nodes
        */
       public void initSimulation(Collection<Node> allNodes) {
               // nothing to do
       }

       /**
        * Abstract method to create the movements.
        * Return empty collection for no movements at all.
        * Created moves must have a specified TIMESTAMP. The value represents
        * the duration of the movement! Not the TIMESTAMP when
        * the movement will be finished.
        *
        * @param allNodes Collection containing all nodes to create moves for.
        * @return Collection with all create MovementEvents.
        */
       public abstract Collection<Movement> createMoves(Collection<Node> allNodes);

       /**
        * The MoveManaEnvelope of SimulationManager calls this method to determine
        * when to check for next movements by the MovementManagers.
        *
        * @return -1.0 if MoveManaEnvelope should not check again,
        *                 a value greater or equal to zero is a valid delay in simulation steps,
        *                 all other (negative) values are invalid.
        */
       public double getDelayToNextQuery() {
               return Configuration.getInstance().getMovementTimeInterval();
       }

       // The next methods allow the layers to send commands to the movement manager during RUNTIME.
       /**
        * Permits to send command to the movement manager during the
execution of the moviment
        */
       public void sendCommand (Command command) {
               // Do nothing
       }

       /** @return the initialDelay. */
       public final double getInitialDelay() {
               return initialDelay;
       }

       /**
        * return the configuration.
        *
        * @return the configuration
        */
       public Configuration getConfig() {
               return config;
       }
}
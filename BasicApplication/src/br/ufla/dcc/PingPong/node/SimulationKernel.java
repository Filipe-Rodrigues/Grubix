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
package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.Configurable;

/**
 * Interface that encapsulate all methods of the simulation kernel that
 * the user code is allowed to call.
 * Note: In the current version of the shox simulator it is not possible
 * to fulfill the contract above in all situations.
 *
 * @author dmeister
 *
 */
public interface SimulationKernel extends Configurable {

	/**
	 * places an internal event for a node.
	 * E.g. communication between LAYERS are internal events.
	 *
	 * @param layer sending the event
	 * @param event The event to place inside the queue.
	 * @param toLayer Specific layer which should receive the event.
	 */
	void enqueueEvent(Layer layer, ToLayer event, LayerType toLayer);

	/**
	 * @return the current simulation time
	 */
	double getCurrentTime();

	/**
	 * transmits a packet.
	 *
	 * @param trans Packet and all transmission parameters as Transmission object.
	 */
	void transmitPacket(Transmission trans);
	

	/**
	 * gets the random generator.
	 * @return returns the random generator
	 * @deprecated Use Configuration.getRandomGenerator()
	 */
	@Deprecated
	RandomGenerator getRandomGenerator();
}

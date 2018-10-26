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
package br.ufla.dcc.grubix.simulator.kernel;

import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;

/**
 * Default implementation of the SimulationKernel interface that
 * delegates the date to the SimulationManager singleton instance.
 * @author dmeister
 *
 */
public class SimulationManagerKernel implements SimulationKernel {


	/**
	 * @return the current simulation time
	 */
	public double getCurrentTime() {
		return SimulationManager.getInstance().getCurrentTime();
	}

	/**
	 * places an internal event for a node.
	 * E.g. communication between LAYERS are internal events.
	 *
	 * @param layer sending the event
	 * @param event The event to place inside the queue.
	 * @param toLayer Specific layer which should receive the event.
	 */
	public void enqueueEvent(Layer layer, ToLayer event, LayerType toLayer) {
		SimulationManager.enqueue(event, layer.getNode().getId(), toLayer);

	}

	/**
	 * transmits a packet.
	 *
	 * @param trans Packet and all transmission parameters as Transmission object.
	 */
	public void transmitPacket(Transmission trans) {
		SimulationManager.getInstance().transmitPacket(trans);
	}

	/**
	 * inits the configuration.
	 * @see br.ufla.dcc.grubix.xml.Configurable#init()
	 */
	public void init() {
		// nothing to do
	}

	/**
	 * gets the random generator.
	 * @return returns the random generator
	 */
	public RandomGenerator getRandomGenerator() {
		return Configuration.getInstance().getRandomGenerator();
	}

}

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

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.node.user;

import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Moved;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;

/** 
 * A no-application application.
 * Does nothing
 * 
 * @author Dirk Meister
 */
public class NoApplication extends ApplicationLayer {
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public final void lowerSAP(Packet packet) {
		
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#processEvent(br.ufla.dcc.grubix.simulator.event.Moved)
	 * @param moved object containing new position
	 */
	@Override
	public final void processEvent(Moved moved) {
		//empty
	}

	/**
	 * Requests this application to generate application-level traffic according to the paramters
	 * specified in <code>tg</code>.
	 * @param tg Traffic generation request with all necessary details
	 */
	@Override
	public final void processEvent(TrafficGeneration tg) {
	}
	
	/**
	 * This application supports only one type of packets, namely ApplicationPacket packets.
	 * @return 1
	 */
	@Override
	public final int getPacketTypeCount() {
		return 1;
	}

	/**
	 * currently no states needed, thus no statechanges possible.
	 * @return nothing
	 */
	public LayerState getState() {
		// TODO generate ApplicationState class from LayerState, if necessary.
		return null;
	}

	/**
	 * currently no states needed, thus no statechanges possible.
	 * @param state the new desired state of this layer.
	 * @return true if the statechange was accepted.
	 */
	public boolean setState(LayerState state) {
		// TODO implement the needed state-changes for the former LayerState subclass.
		return true;
	}
}

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

package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;

/**
 * This event indicates the result of a QoS dependent operation.
 * @author Dirk Held
 */
public class QoSReservationFailedEvent extends CrossLayerEvent {

	/**
	 * Constructor of this class.
	 * @param sender   The node which constructs this wake up call
	 * @param receiver The layer to which this event should be forwarded  at least.
	 * @param packet   The associated packet
	 */
	public QoSReservationFailedEvent(Address sender, LayerType receiver, Packet packet) {
		super(sender, receiver, packet, CrossLayerResult.FAIL);
	}
	
	/**
	 * default creator of this class. The terminal layer is set to the upper layer of the creator.
	 * @param sender the layer, where the transmission failed.
	 * @param receiver the terminal layer for this event.
	 * @param packet the failed packet.
	 * @param val the suggested change, depending on the sending layer.
	 */
	public QoSReservationFailedEvent(Address sender, LayerType receiver, Packet packet, double val) {
		super(sender, receiver, packet, new CrossLayerDoubleResult(CrossLayerResult.RETRY, val));
	}
}

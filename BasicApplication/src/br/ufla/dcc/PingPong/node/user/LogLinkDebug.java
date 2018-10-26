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

package br.ufla.dcc.PingPong.node.user;

import br.ufla.dcc.grubix.simulator.node.LogLinkLayer;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

/** 
 * Simple layer for testing.
 * 
 * @author Andreas Kumlehn, Dirk Held
 */
public class LogLinkDebug extends LogLinkLayer {
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#lowerSAP(br.ufla.dcc.PingPong.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public final void lowerSAP(Packet packet) {
		if (!packet.isTerminal()) {
			sendPacket(packet.getEnclosedPacket());
		}
	}

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#upperSAP(br.ufla.dcc.PingPong.event.Packet)
	 * @param packet to process coming from higher layer.
	 */
	@Override
	public final void upperSAP(Packet packet) {		
		Packet nextPacket = new LogLinkPacket(getSender(), packet);			
		sendPacket(nextPacket);
	}

	/**
	 * Handles {@link CrossLayerEvent} by forwarding it upwards, if necessary.
	 * @param wuc The received {@link WakeUpCall}.
	 * @throws LayerException if an unhandled {@link WakeUpCall} occurs.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		if (wuc instanceof CrossLayerEvent) {
			((CrossLayerEvent) wuc).forwardUp(this);
		} else {
			super.processWakeUpCall(wuc);
		}
	}

	/** @return null since currently no states needed, thus no state changes possible. */
	public LayerState getState() {
		return null;
	}

	/**
	 * currently no states needed, thus no state changes possible.
	 * @param state the new desired state of this layer.
	 * @return true if the state change was accepted.
	 */
	public boolean setState(LayerState state) {
		return true;
	}
}
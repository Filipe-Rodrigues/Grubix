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

import br.ufla.dcc.grubix.simulator.node.AirModule;
import br.ufla.dcc.grubix.simulator.node.StateIO;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.event.AirPerformCarrierSense;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.user.CarrierSenseInterruptedEvent;

/** 
 * Class representing a artificial layer below the physical layer of a node.
 * This class is fixed for all nodes.
 * The AirModule stores outgoing packets as long as they are "on air" and
 * not completely send to avoid multiple sending.
 * Incoming packets are stored in sets to calculate the interference
 * and calculate bit errors.
 * 
 * @author Dirk Held
 */
public class AirModule_DBG extends AirModule implements StateIO {
	
	// br.ufla.dcc.PingPong.kernel.Configuration.java @ line 282 ff.
	
	/**
	 *  internal method, to inform the MAC of a free Carrier. Now goes it loose.
	 * @param cs  the just completed carrier sense cycle.
	 * @param pos the position from which it was started.
	 */
	@Override
	protected void csFreeEvent(AirPerformCarrierSense cs, int pos) {
		if (outgoing == null) {
			super.csFreeEvent(cs, pos);
		} else {
			/* 
			 * For some strange reason, the MAC did start a transmission during a carrier
			 * sense cycle. Thus the carrier sensing cycle needs to be redone, as if the
			 * MAC did start it after completing the current transmission.
			 */
			interferenceInVarCS = false;
			csInterval = null;
			
			PhysicalPacket p = outgoing.getPacket();
			double delay = p.getTime() + p.getDuration() - node.getCurrentTime();
			double minFree = cs.getMinTime(); 
			
			WakeUpCall wuc = new AirPerformCarrierSense(senderMAC, delay + minFree, minFree, cs.getVarTime(), false);
			SimulationManager.enqueue(wuc, getId(), LayerType.AIR);
			
			sendEventUp(new CarrierSenseInterruptedEvent(sender, p, delay, pos));
		}
	}

	/**
	 * Method to transmit a packet via the AirModule.
	 * 
	 * @param packet the packet to transmit.
	 * @param signalStrength the strength of the signal to use.
	 * @return true if the packet can be send and radio is not blocked.
	 * 			false otherwise.
	 */
	@Override
	public boolean transmit(PhysicalPacket packet, double signalStrength) {
		if (outgoing == null) {
			return super.transmit(packet, signalStrength);
		} else {
			return false;
		}
	}
}

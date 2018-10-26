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

Copyright 2008 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.event.genericcarriersense;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Interference;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

/**
 * Delayed event for the generic carrier sense mechanism to check for
 * actions based on the detection of interferences reaching a node.
 * 
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseRegistrationRequest(CarrierSenseRegistrationRequest)
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseDurationEnd(CarrierSenseDurationEnd)
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseIncomingInterference(CarrierSenseIncomingInterference)
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseCarrierFreeCheck(CarrierSenseCarrierFreeCheck)
 * @see br.ufla.dcc.grubix.simulator.node.genericcarriersense
 * @see br.ufla.dcc.grubix.simulator.event.genericcarriersense
 * 
 * @author Florian Rittmeier
 */
public class CarrierSenseIncomingInterference extends WakeUpCall {

	/**
	 * The interference, which will reach the node after delay.
	 */
	private final Interference theInterference;
	
	/**
	 * @param sender sender of the beacon, should be the AirModule
	 * @param delay the delay of the beacon, should be the propagation delay
	 * @param theInterference the interference, which reaches the node
	 */
	public CarrierSenseIncomingInterference(Address sender, double delay, Interference theInterference) {
		super(sender, delay);
		this.theInterference = theInterference;
	}

	/**
	 * @return the interference which reaches the node
	 */
	public Interference getTheInterference() {
		return theInterference;
	}

}

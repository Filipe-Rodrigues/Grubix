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
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.node.genericcarriersense.CarrierSenseInformation;

/**
 * Informs the generic carrier sense mechanism (for a negative carrier
 * sense) to check possible points in simulation time where there might
 * be no more interferences affecting the node.
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

public class CarrierSenseCarrierFreeCheck extends WakeUpCall {

	/**
	 * The information of the negative carrier sense for which
	 * we check the interferences.
	 */
	private final CarrierSenseInformation carrierSenseInfo; 
	
	/**
	 * @param sender sender of the event, should be the AirModule
	 * @param delay the delay in simulation steps when to check
	 * @param carrierSenseInfo the information about the negative
	 * 						   carrier sense we check the interferences for
	 */
	public CarrierSenseCarrierFreeCheck(Address sender,
			double delay, CarrierSenseInformation carrierSenseInfo) {
		super(sender, delay);
		this.carrierSenseInfo = carrierSenseInfo;
	}

	/**
	 * @return the information about the negative carrier sense
	 */
	public CarrierSenseInformation getCarrierSenseInfo() {
		return carrierSenseInfo;
	}


}

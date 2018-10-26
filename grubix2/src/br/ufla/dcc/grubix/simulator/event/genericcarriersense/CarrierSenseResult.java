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
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.node.genericcarriersense.CarrierSenseInformation;

/**
 * Holds information about the result of a generic carrier sense.
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
public class CarrierSenseResult extends ToLayer {

	/**
	 * Whether the carrier was detected when this event was is sent.
	 */
	private final boolean carrierDetected;
	
	/**
	 * Information about the associated carrier sense.
	 */
	private final CarrierSenseInformation carrierSenseInfo;
	
	/**
	 * @param sender the sender of the result, which should be the AirModule
	 * @param receiver the registrant of the carrier sense who should get the result
	 * @param carrierDetected whether the carrier was detected or not
	 * @param carrierSenseInfo information describing the associated carrier sense
	 */
	public CarrierSenseResult(Address sender, NodeId receiver,
			boolean carrierDetected,
			CarrierSenseInformation carrierSenseInfo) {
		super(sender, receiver);
		this.carrierDetected = carrierDetected;
		this.carrierSenseInfo = carrierSenseInfo;
	}

	/**
	 * @return true if carrier was detected<br>
	 * 		   false if carrier was not detected
	 */
	public boolean isCarrierDetected() {
		return carrierDetected;
	}

	/**
	 * @return information about the carrier sense this carrier sense
	 * 		   result belongs to
	 */
	public CarrierSenseInformation getCarrierSenseInfo() {
		return carrierSenseInfo;
	}

}

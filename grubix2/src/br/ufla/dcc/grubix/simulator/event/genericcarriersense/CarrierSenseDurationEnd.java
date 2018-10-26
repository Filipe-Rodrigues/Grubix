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
 * Informs the generic carrier sense mechanism that
 * the duration of a generic carrier sense has passed.

 * At that time the generic carrier sense mechanism checks whether the
 * the result of the carrier sense was issued before or if the carrier
 * sense is still valid and the registrant of the carrier sense has to
 * be informed about the result of the carrier sense.
 * 
 * If the associated carrier sense is valid and processed for a regular
 * carrier sense, the result indicates that the carrier is not detected
 * (=still free).<br>
 * If the associated carrier sense is valid and processed for a negative
 * carrier sense the result indicates that the carrier is detected
 * (=still busy).
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
public class CarrierSenseDurationEnd extends WakeUpCall {

	/**
	 * Information about the associated carrier sense.
	 */
	private final CarrierSenseInformation carrierSenseInfo;
	
	/**
	 * @param sender the node/layer sending this event, usually the
	 * 				 AirModule
	 * @param delay the delay in simulation steps before this event
	 * 				is processed, this should be the maximum time
	 * 				of the associated carrier sense
	 * @param carrierSenseInfo information about the associated
	 * 						   carrier sense
	 */
	public CarrierSenseDurationEnd(Address sender, double delay, CarrierSenseInformation carrierSenseInfo) {
		super(sender, delay);
		this.carrierSenseInfo = carrierSenseInfo;
	}

	/**
	 * @return information about the associated carrier sense
	 */
	public CarrierSenseInformation getGenericCarrierSenseInformation() {
		return this.carrierSenseInfo;
	}
	
}

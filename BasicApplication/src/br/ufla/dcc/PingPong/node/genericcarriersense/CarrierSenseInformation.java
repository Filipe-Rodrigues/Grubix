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

package br.ufla.dcc.PingPong.node.genericcarriersense;

import br.ufla.dcc.grubix.simulator.Address;

/**
 * Holds information describing a particular carrier sense handled by
 * the generic carrier sense mechanism.
 * 
 * @see br.ufla.dcc.PingPong.node.AirModule#processCarrierSenseRegistrationRequest(CarrierSenseRegistrationRequest)
 * @see br.ufla.dcc.PingPong.node.AirModule#processCarrierSenseDurationEnd(CarrierSenseDurationEnd)
 * @see br.ufla.dcc.PingPong.node.AirModule#processCarrierSenseIncomingInterference(CarrierSenseIncomingInterference)
 * @see br.ufla.dcc.PingPong.node.AirModule#processCarrierSenseCarrierFreeCheck(CarrierSenseCarrierFreeCheck)
 * @see br.ufla.dcc.PingPong.node.genericcarriersense
 * @see br.ufla.dcc.PingPong.event.genericcarriersense
 * 
 * @author Florian Rittmeier
 */

public class CarrierSenseInformation {

	/**
	 * The node and layer who registered the carrier sense
	 * and will be used as destination address for the result.
	 */
	private final Address registrant;
	
	/**
	 * Data provided by the registrant, which can later be used by the
	 * registrant to identify that carrier sense request of the carrier
	 * sense result. The generic carrier sense mechanism neither reads
	 * nor modifies this field.
	 */
	private final Object registrantData;
	
	/**
	 * The simulation time the carrier sense was registrated/started.
	 * 
	 * This field depends on the time the CarrierSenseRegistrationRequest
	 * object registering this carrier sense was created.
	 */
	private final double startTime;
	
	/**
	 * The duration (=maximum time) of the carrier sense in simulation steps.
	 */
	private final double duration;
	
	/**
	 * Whether the carrier sense is a negative carrier sense.
	 * A negative carrier sense either returns when the maximum
	 * carrier sense time is exceeded or when the carrier gets free
	 * again.
	 */
	private final boolean isNegativeCarrierSense;
	
	/**
	 * Whether the carrier sense is a virtual carrier sense.
	 * A virtual carrier sense does not only treat incoming
	 * interferences as "carrier detected" but also outgoing
	 * transmissions from the own node. 
	 */
	private final boolean isVirtualCarrierSense;
	
	/**
	 * @param registrant the node and layer who registered the
	 * 					 carrier sense
	 * @param registrantData registrant provided data to be used by
	 * 						 the sender to later identify the
	 * 						 belonging carrier sense result 
	 * @param startTime the simulation time the carrier sense was
	 * 					started
	 * @param duration the duration of the carrier sense in
	 * 				   simulation steps
	 * @param isNegativeCarrierSense use true if carrier sense shall
	 * 								 return when carrier gets free
	 * 								 again<br>
	 * 								 use false if carrier sense shall
	 * 								 return when carrier gets busy
	 * @param isVirtualCarrierSense true if carrier sense shall treats
	 * 								outgoing transmissions as "carrier
	 * 								detected".<br>
	 * 		   						false if
	 * 								{@link CarrierSenseFailed}
	 * 								should be emitted to the registrant
	 * 								if the node starts an outgoing
	 * 								transmission.
	 */
	public CarrierSenseInformation(Address registrant,
			Object registrantData, double startTime,
			double duration, boolean isNegativeCarrierSense,
			boolean isVirtualCarrierSense) {
		this.registrant = registrant;
		this.registrantData = registrantData;
		this.startTime = startTime;
		this.duration = duration;
		this.isNegativeCarrierSense = isNegativeCarrierSense;
		this.isVirtualCarrierSense = isVirtualCarrierSense;
	}

	/**
	 * @return the node and layer who registered the carrier sense
	 */
	public Address getRegistrant() {
		return registrant;
	}

	/**
	 * @return the data supplied by the registrant of the carrier sense
	 * 		   which the registrant can later use to identify the
	 * 		   belonging carrier sense result.
	 */
	public Object getRegistrantData() {
		return registrantData;
	}
	
	/**
	 * @return the simulation time the carrier sense was started
	 */
	public double getStartTime() {
		return startTime;
	}

	/**
	 * @return the duration of the carrier sense in simulation steps
	 */
	public double getDuration() {
		return this.duration;
	}
	
	/**
	 * @return true if carrier sense shall return when carrier gets free<br>
	 * 		   false if carrier sense shall return when carrier gets busy
	 */
	public boolean isNegativeCarrierSense() {
		return this.isNegativeCarrierSense;
	}

	/**
	 * @return true if carrier sense shall treats outgoing transmissions
	 * 		   as "carrier detected".<br>
	 * 		   false if {@link CarrierSenseFailed} should be emitted to
	 * 		   the registrant if the node starts an outgoing transmission.
	 */
	public boolean isVirtualCarrierSense() {
		return this.isVirtualCarrierSense;
	}
	


}

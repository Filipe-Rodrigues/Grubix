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
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.genericcarriersense.CarrierSenseInformation;

/**
 * Initiator event for a generic carrier sense in the AirModule.
 * 
 * Generic carrier sense can be a regular carrier sense looking for
 * a carrier and they can be negative carrier senses, which wait till
 * the carrier gets free. 
 * 
 * A generic carrier sense is defined in that way, that if a layer
 * request a carrier sense for a specific duration then the AirModule
 * will check for interferences affecting the node from the time it
 * was requested to perform the carrier sense till the duration (also
 * refered as maximum time) has passed.
 * 
 * If a regular carrier sense was requested and no interference was
 * detected during the duration, the sender will be informed at the
 * end of the duration, that no carrier was detected. If an
 * interference was detected during the duration the sender will
 * immediately be informed, that a carrier was detected and the
 * AirModule will stop checking for interferences at that time. 
 * 
 * If a negative carrier sense was requested and one or more
 * interferences were detected for the whole duration, the sender
 * will be informed at the end of the duration, that the carrier
 * was still detected. If an interference affecting the node ends
 * during the duration of the negative carrier sense and there is no
 * other interference affecting the node, the sender will immediately
 * be informed, that a carrier was free and the AirModule will stop
 * checking for a free carrier at that time. 
 *
 * The generic carrier sense mechanism will treat an affecting
 * interference as being detected only if since the start of the
 * transmission of the interference the propagation delay was passed.  
 *
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseRegistrationRequest(CarrierSenseRegistrationRequest)
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseDurationEnd(CarrierSenseDurationEnd)
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseIncomingInterference(CarrierSenseIncomingInterference)
 * @see br.ufla.dcc.grubix.simulator.node.AirModule#processCarrierSenseCarrierFreeCheck(CarrierSenseCarrierFreeCheck)
 * @see br.ufla.dcc.grubix.simulator.node.genericcarriersense
 * @see br.ufla.dcc.grubix.simulator.event.genericcarriersense
 * 
 * @author Florian Rittmeier
 *
 */
public class CarrierSenseRegistrationRequest extends WakeUpCall {

	/**
	 * The duration (=maximum time) of the carrier sense.
	 */
	private final double duration;
	
	/**
	 * The information about the requested carrier sense of this
	 * registration request.
	 */
	private final CarrierSenseInformation carrierSenseInfo;
	
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
	 * If virtual carrier sense is disabled, the AirModule will
	 * send {@link CarrierSenseFailed} on registration or
	 * while the carrier sense if active if the node starts
	 * sending a packet.
	 */	
	private final boolean isVirtualCarrierSense;

	/**
	 * Constructs a new CarrierSenseRegistrationRequest object to
	 * initiate a generic carrier sense.<br><br>
	 * 
	 * WARN The constructed object has to be send to the AirModule
	 * 		at the same simulation time it was created without any
	 * 		delay. If you don`t do this it cannot be guaranteed
	 * 		that the generic carrier sense method will work as
	 * 		expected.
	 * 
	 * @param sender the sending node/layer who will get informed
	 * 				 about the result of the carrier sense
	 * @param senderData sender provided data to be used by the sender
	 * 					 to later identify the belonging carrier sense
	 * 					 result
	 * @param duration the maximum duration of the carrier sense
	 * @param isNegativeCarrierSense use true if carrier sense shall
	 * 								 return when carrier gets free
	 * 								 again<br>
	 * 								 use false if carrier sense shall
	 * 								 return when carrier gets busy	
	 */
	public CarrierSenseRegistrationRequest(Address sender,
			Object senderData, double duration,
			boolean isNegativeCarrierSense) {
		super(sender);
		
		if (duration < 0.0) {
			throw new IllegalArgumentException("duration is negative");
		}

		this.duration = duration;
		this.isNegativeCarrierSense = isNegativeCarrierSense;
		// TODO Support virtual carrier sense
		this.isVirtualCarrierSense = false;
		this.carrierSenseInfo = new CarrierSenseInformation(
				sender,	senderData, 
				SimulationManager.getInstance().getCurrentTime(),
				duration, isNegativeCarrierSense,
				this.isVirtualCarrierSense);
	}

	/**
	 * @return the maximum duration of the carrier sense
	 */
	public double getDuration() {
		return this.duration;
	}
	
	/**
	 * @return information describing this carrier sense.
	 */
	public CarrierSenseInformation getGenericCarrierSenseInformation() {
		return this.carrierSenseInfo;
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

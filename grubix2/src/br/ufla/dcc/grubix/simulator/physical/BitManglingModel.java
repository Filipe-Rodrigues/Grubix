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

package br.ufla.dcc.grubix.simulator.physical;

import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/** 
 * abstract superclass to model the mangling of transfered packets according
 * to occurred interferences and the received signal strength.
 * 
 * The implementations should clone the sent packet in all cases, to prevent
 * the creation of shared memory access between nodes accidentally.
 * 
 * @author madmax
 */
public abstract class BitManglingModel implements Configurable {
	
	/**
	 * method to initialize the configuration of the object.
	 * It is assured by the runtime system that the <code>Configuration</code>
	 * is valid, when this method is called.
	 * 
	 * @param configuration current configuration
	 * @throws ConfigurationException thrown when the object cannot run with
	 * the configured values.
	 */
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		initConfiguration();
	}

	/**
	 * method to initialize the configuration of the object.
	 * It is assured by the runtime system that the <code>Configuration</code>
	 * is valid, when this method is called.
	 * 
	 * 
	 * @deprecated For any new code, the initConfiguration with a parameter should be used. This
	 * method may be deleted in the future without further notice.
	 * @throws ConfigurationException thrown when the object cannot run with
	 * the configured values.
	 */
	public void initConfiguration() throws ConfigurationException {
		// do nothing
	}
	
	/**
	 * Called by the ConfigurableFactory after setting the configured
	 * parameter.
	 * @throws ConfigurationException thrown if configuration is invalid.
	 */
	public void init() throws ConfigurationException {
		// nothing to do
	}

		
	/**
	 * Accumulates interfering signals during transmission interval of trans and 
	 * damages the encapsulated data packet accordingly. For all sub-classes that implement
	 * this method, it is essential to keep in mind, that the packet ID of the resulting
	 * packet must be the same as the one from the original packet. Besides, the direction of
	 * the new packet must be set to {@link br.ufla.dcc.grubix.simulator.event.Packet#UPWARDS}, 
	 * otherwise packets will produce errors when later processed. 
	 * @param transStartTime Time when the transmission of trans started
	 * @param trans          The transmission object which contains the current data packet
	 * @param inter          the affecting interferences.
	 * @return The resulting data packet which is damaged if interference occured during transmission
	 */
	public abstract PhysicalPacket getResultingDataPacket(double transStartTime, Transmission trans, 
			                                      			InterferenceQueue inter);
}

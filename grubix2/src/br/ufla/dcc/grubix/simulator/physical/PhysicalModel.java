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

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/** 
 * Abstract superclass for all types of physical models.
 * PhysicalModels are used to provide different communication models.
 * They must decide whether a message reaches the receiver from a sender in method isReachable.
 * Further they must calculate the attenuation between two nodes.
 * 
 * @author Andreas Kumlehn 
 */
public abstract class PhysicalModel implements Configurable {

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
     * Method to calculate whether a sender could reach the receiver along with the attenuation.
     * @param receiver       the Position of the receiver
     * @param sender         the Position of the sender
     * @param signalStrength the used signal strength
     * @return the resulting attenuation and whether the receiver is reachable at all 
     */
	public abstract Reachability apply(Node receiver, Node sender, double signalStrength);
}

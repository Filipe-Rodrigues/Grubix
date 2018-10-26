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

package br.ufla.dcc.PingPong.movement;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * Super class of all classes which generate initial node positions. Initial node
 * positions can be either explicitly specified in the XML configuration for ShoX
 * (one position for each node), or, for convenience, they can be generated according
 * to some distribution, pattern, etc. by sub classes of this class.
 * @author jlsx
 */
public abstract class StartPositionGenerator implements Configurable {

	/**
	 * method to initialize the configuration of the object.
	 * It is assured by the runtime system that the <code>Configuration</code>
	 * is valid, when this method is called.
	 * @param config The configuration.
	 * @throws ConfigurationException thrown when the object cannot run with
	 * the configured values.
	 */
	public void initConfiguration(Configuration config) throws ConfigurationException {
		//nothing to do
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
	 * This method generates a new position according to the underlying scheme,
	 * distribution, pattern, computation, etc (implemented in the sub class).
	 * @param node The node for which the new position should be generated.
	 * @return A new position for a node
	 */
	public abstract Position newPosition(Node node);
}

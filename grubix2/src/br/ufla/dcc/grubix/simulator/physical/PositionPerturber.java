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
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;


/** 
 * Abstract superclass for all types of PositionPerturbers.
 * This objects have only one task: perturb the real positions of the nodes
 * to create more realistic simulations. This is more realistic because
 * even satellite navigation is never really exact and therefore a node
 * can never determine its position exactly and correctly.
 * 
 * @author Andreas Kumlehn
 */
public abstract class PositionPerturber implements Configurable {
	
	/**
	 * method to initialize the configuration of the object.
	 * It is assured by the runtime system that the <code>Configuration</code>
	 * is valid, when this method is called.
	 * 
	 * @throws ConfigurationException thrown when the object cannot run with
	 * the configured values.
	 */
	public void initConfiguration() throws ConfigurationException {
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
	 * Abstract method representing the task of a PositionPerturber.
	 * 
	 * @param realPosition The real and exact position of a node.
	 * @return A new Positionobject with perturbed values.
	 */
	public abstract Position perturbPosition(Position realPosition);
}

/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.grubix.simulator.LayerType;

/**
 * Layers that fire the {@link LayerStateChangedEvent} should use LayerStates which
 * implement this interface. 
 * Other layers that are interested in the {@link LayerStateChangedEvent} can check 
 * whether or not the Layer provides such an event by checking whether the
 * LayerState is an instance of this interface. If this is the case then
 * the layer can register for receiving the event.
 * The layer that sends the {@link LayerStateChangedEvent} has to take care that all registered
 * Layers are informed whenever the LayerState changes.   
 * The keyword at this point is Observer-Pattern!
 *  
 * @author Thomas Kemmerich
 */
public interface LayerStateChangedProvider {

	
	/**
	 * Registers a layer for receiving the {@link LayerStateChangedEvent}.
	 * Please have a look at the documentation of the class to obtain further information.
	 * @param layerType The LayerType of the layer that listens for the event.
	 */
	void registerForLayerStateChangedEvent(LayerType layerType);
}

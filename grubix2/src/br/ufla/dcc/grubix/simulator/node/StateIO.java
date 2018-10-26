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

package br.ufla.dcc.grubix.simulator.node;

import br.ufla.dcc.grubix.simulator.event.LayerState;

/**
 * this interface describes, how each layer should produce and receive 
 * LayerState objects. Thus, to change a status of a layer, first a
 * status-object needs to be obtained and modified. This modified object
 * is then sent back to the originating layer and processed there.
 * 
 * @author Dirk Held
 */
public interface StateIO {

	/**
	 * method to get a current status object.
	 * @return the current status of the layer.
	 */
	LayerState getState();
	
	/**
	 * method to change the status of the layer with an modified status-object,
	 * which was just obtained and then modified.
	 * @param status the changed new status for the layer.
	 * @return true if the state-change was accepted.
	 */
	boolean setState(LayerState status);
}

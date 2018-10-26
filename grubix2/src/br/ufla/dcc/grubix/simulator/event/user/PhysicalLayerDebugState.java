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

package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayerParameter;
import br.ufla.dcc.grubix.simulator.node.PhysicalTimingParameters;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * Subclass to allow to toggle the dropInvalid at runtime.
 * 
 * @author Dirk Held
 */
public class PhysicalLayerDebugState extends PhysicalLayerState {

	/** set to "false" to upward invalid packets too. */
	private boolean dropInvalidPackets;
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.event.user.PhysicalLayerDebugState
	 * @param dropInvalidPackets set to true (default), to drop invalid packets.
	 */
	public PhysicalLayerDebugState(PhysicalTimingParameters timings, PhysicalLayerParameter param,
								   RadioState radioState, int currentChannel, 
								   double currentSignalStrength, boolean dropInvalidPackets) {
		super(timings, param, radioState, currentChannel, currentSignalStrength);
		
		this.dropInvalidPackets = dropInvalidPackets;
	}

	/** @return the dropInvalid flag. */
	public final boolean isDropInvalidPackets() {
		return dropInvalidPackets;
	}

	/** @param dropInvalid the dropInvalid flag to set. */
	public final void setDropInvalidPackets(boolean dropInvalid) {
		this.dropInvalidPackets = dropInvalid;
	}
}

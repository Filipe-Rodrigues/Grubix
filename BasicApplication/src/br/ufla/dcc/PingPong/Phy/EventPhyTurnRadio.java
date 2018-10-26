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

package br.ufla.dcc.PingPong.Phy;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;

/**
 * This event is used by MAC layer to order Physical Layer to turn radio.
 * 
 * @author João Giacomin 
 */
public class EventPhyTurnRadio extends CrossLayerEvent {

	//private boolean turningRadioOn = false;
	
	/** The state the radio will be. OFF = false, ON = true*/
	private boolean isSetRadioOn = false;

	/**
	 * default constructor of this class.
	 * @param sender the sending node
	 * @param turningRadio how is the radio going to be.
	 */
	
	public EventPhyTurnRadio(Address sender, boolean radioOn) {
		super(sender, null);
		isSetRadioOn = radioOn;
	}
	
	public boolean IsSetRadioOn () {
		return isSetRadioOn;
	}

}

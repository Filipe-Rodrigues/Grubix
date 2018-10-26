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

package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.NodeId;


/** 
 * Interface for eventenvelopes which should be logged during SIMULATION.
 * 
 * @author Andreas Kumlehn
 */
public interface Loggable {
	
	/**
	 * Flag for XML format output, used as a parameter for {@link Loggable.log}.
	 */
	int XML = 0;
	
	/**
	 * Flag for compact format output, used as a parameter for {@link Loggable.log}.
	 */
	int COMPACT = 1;
	
	/**
	 * Method to log an event. //TODO wrong comment! internalevent is logged embedded inside an enqueue event.
	 * 
	 * @param logType XML or COMPACT
	 * @return the output to be logged
	 */
	String log(int logType);
	
	
	/**
	 * Method to retrieve the EventId of a loggable event.
	 * 
	 * @return Returns the EventId.
	 */
	EventId getEventId();
	
	/**
	 * Method to retrieve the classname of an enclosed event.
	 * 
	 * @return Classname of the enclosed event as String.
	 */
	String getEventType();
	
	/**
	 * Method to retrieve the NodeId of the receiving node.
	 * 
	 * @return Returns the receiverId.
	 */
	NodeId getReceiver();

}

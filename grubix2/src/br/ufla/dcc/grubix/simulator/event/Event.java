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

/**
 * Abstract superclass representing the events in the SIMULATION.
 * Events can several types:
 * - SIMULATION internal like Movements (SimulationEvent)
 * - for a whole node like Iinitialize (ToNode)
 * - for a node and a specific layer (ToLayer)
 * 
 * Method getDelay overloaded in subclasses to ensure proper delays
 * for delivering events.
 * 
 * @author Andreas Kumlehn 
 */
public abstract class Event {
	
	/**
	 * The unique ID of the event.
	 */
	private final EventId id;
	
	/**
	 * Constructor of the class Event.
	 */
	public Event() {
		this.id = new EventId();
	}
	
	
	/**
	 * Abstract method to ensure that every event knows its delay.
	 * Must be final in subclasses such that users can not overload the delay. 
	 * 
	 * @return Delay of the object as Double.
	 */
	public abstract double getDelay();
	
	/**
	 * @see java.lang.Object#toString()
	 * @return String representing the event for console logging.
	 */
	public abstract String toString();


	/**
	 * @return  Returns the ID.
	 */
	public final EventId getId() {
		return id;
	}
}

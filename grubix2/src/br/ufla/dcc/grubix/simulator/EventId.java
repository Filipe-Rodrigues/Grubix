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

package br.ufla.dcc.grubix.simulator;

/**
 * Factoryclass for EventIds. Produces EventId objects starting with ID 1.
 * 
 * @author Andreas Kumlehn
 */
public class EventId extends UniqueId {

	/** int value of the next free EventId. */
	private static int nextFreeId = 1;

	/** Constructor of the class EventId. */
	public EventId() {
		super(nextFreeId);
		nextFreeId++;
	}
}

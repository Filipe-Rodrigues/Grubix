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

/** 
 * SimulatonEvent to call the MovementManager to generate new moves after a certain period.
 * 
 * @author Andreas Kumlehn
 */
public class MoveManagerEvent extends SimulationEvent {

	/**
	 * Specifies when the MoveManaEnvelope of SimulationManager has
	 * to request new moves from the MovementManager.
	 */
	protected final double delay;

	/**
	 * @param delay delay for when to request new moves
	 */
	public MoveManagerEvent(double delay) {
		this.delay = delay;
	}

	/**
	 * @return delay for when to request new moves
	 */
	public final double getDelay() {
		return this.delay;
	}
	
	/**
	 * @return String for logging.
	 */
	public final String toString() {
		return "MovementManagerEvent to generate and enqueue new moves.";
	}
}

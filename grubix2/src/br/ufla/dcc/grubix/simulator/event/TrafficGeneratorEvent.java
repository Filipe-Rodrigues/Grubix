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
 * SimulatonEvent to call the TrafficGenerator to generate new traffic after a certain period.
 * Always reenqueued into the SIMULATION queue.
 * @author jlsx
 */
public class TrafficGeneratorEvent extends SimulationEvent {

	/**
	 * Specifies when the TrafficGenEnvelope of SimulationManager
	 * has to request new traffic from the TrafficGenerator.
	 */
	protected final double delay;

	/**
	 * @param delay delay for when to request new traffic
	 */
	public TrafficGeneratorEvent(double delay) {
		this.delay = delay;
	}
	
	/** @return Delay for when to request new traffic. */
	public final double getDelay() {
		return this.delay;
	}
	
	/** @return String for logging. */
	public final String toString() {
		return "TrafficGeneratorEvent to generate and enqueue new traffic.";
	}
}

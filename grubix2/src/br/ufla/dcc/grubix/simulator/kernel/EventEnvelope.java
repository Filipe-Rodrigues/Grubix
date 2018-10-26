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

package br.ufla.dcc.grubix.simulator.kernel;

import br.ufla.dcc.grubix.simulator.event.Event;

/** 
 * Abstract superclass for all Envelopes.
 * Envelopes are needed to put events into the GLOBAL queue. They store
 * further information about the event and provide a method process() which
 * processes the event ancapsulated in the specific envelope.
 * 
 * All nonabstract subclasses of this class must be implemented in the class
 * SimulationManager as inner class to have access to GLOBAL things like Topology.
 * 
 * Note: The simulation manager has to set the enqueueCounter to assure stableness of the
 * event queue. The initial value (Long.MAX_VALUE) as taken to denote a object state that is
 * not fully initized.
 * 
 * @author Andreas Kumlehn
 */
public abstract class EventEnvelope implements Comparable<EventEnvelope> {
	
	/**
	 * magic number for enqueueCounter to denote a not fully initized object state.
	 */
	public static final long NOT_INITIZED = Long.MAX_VALUE;
	
	/**
	 * The event to process.
	 */
	private final Event event;
	
	/**
	 * Time when the envelope is dequeued and processed.
	 * This value is the key to organize the GLOBAL EventEnvelopeQueue.
	 */
	private final double time;
	
	/** 
	 * The unique time counter indicating when the event was enqueued. This should be set
	 * by the {@link EventEnvelopeQueue} and is used to guarantee a FIFO ordering of events
	 * in the event queue. 
	 */ 
	private long enqueueCounter;
	
	/**
	 * Protected constructor of the class EventEnvelope.
	 * 
	 * @param event The event to encapsulate.
	 * @param time The TIMESTAMP when the envelope should be delivered.
	 */
	protected EventEnvelope(Event event, double time) {
		if (event == null) {
			throw new IllegalArgumentException("event");
		}
		if (time < 0.0) {
			throw new IllegalArgumentException("time");
		}
		this.event = event;
		this.time = time;
		enqueueCounter = NOT_INITIZED;
	}
	
	/**
	 * Abstract method to ensure that every special envelope can be delivered.
	 * Specific handling of processing done in subclasses. 
	 */
	protected abstract void deliver();

	/** @return the event. */
	protected Event getEvent() {
		return event;
	}

	/** @return The TIMESTAMP when the envelope should be delivered. */
	public final double getTime() {
		return time;
	}
	
	/**
	 * Sets the unique time counter indicating when the event was enqueued. This should be set
	 * by the {@link EventEnvelopeQueue} and is used to guarantee a FIFO ordering of events
	 * in the event queue.
	 * @param time A unique time counter indicating when the event was enqueued.
	 */
	public void setEnqueueCounter(long time) {
		this.enqueueCounter = time;
	}
	
	/**
	 * @return The unique time counter indicating when the event was enqueued.
	 */
	public final long getEnqueueCounter() {
		return this.enqueueCounter;
	}

	/**
	 * Implementation of Comparable<EventEnvelope> interface.
	 * Needed for insertion into PriorityQueue.
	 * 
	 * @param e EventEnvelope to compare to. Timestamp is compared.
	 * @return Int according to comparation of timestamps.
	 */
	public int compareTo(EventEnvelope e) {
		if (this.time < e.getTime()) {
			return -1;
		}
		if (this.time > e.getTime()) {
			return 1;
		}
		// time == e.getTime()
		if (this.enqueueCounter < e.getEnqueueCounter()) {
			return -1;
		}
		if (this.enqueueCounter > e.getEnqueueCounter()) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Abstract method to ensure proper logging of envelopes.
	 * 
	 * @return Envelope description as String.
	 */
	public abstract String toString();
	
}

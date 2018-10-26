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

import java.util.PriorityQueue;

import br.ufla.dcc.grubix.debug.compactlogging.ShoxLogger;
import br.ufla.dcc.grubix.debug.logging.LogFilter;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Loggable;
import br.ufla.dcc.grubix.simulator.event.Movement;



/** 
 * Class to queue all events during SIMULATION.
 * This class encapsulates a priority queue.
 * The stored events are stored inside an EventEnvelope. An envelope
 * stores further DATA for an event. The key to organize is the TIMESTAMP of
 * an envelope. 
 * 
 * @author Andreas Kumlehn
 */
public class EventEnvelopeQueue {
	
	/**
	 * The encapsulated PriorityQueue.
	 */
	private final PriorityQueue<EventEnvelope> queue;
	
	/**
	 * Global configuration.
	 */
	private Configuration configuration = Configuration.getInstance();
	/**
	 * Live Log filter of global configuration.
	 */
	private LogFilter liveLogFilter = configuration.getLiveLogFilter();
	
	/** XMLWriter for the logfile. */
	private final ShoxLogger history;
	
	/** An internal counter to distinguish enqueue times of events. */
	private long counter;
	
	/**
	 * Constructor of the class EventEnvelopeQueue.
	 * 
	 * @param history ShoxLogger for logging events.
	 */
	public EventEnvelopeQueue(ShoxLogger history) {
		 this.queue = new PriorityQueue<EventEnvelope>();
		 this.history = history;
		 this.counter = 0;
	}
	
	/**
	 * Inserts the specified element into the priority queue.
	 * 
	 * @param e The eventenvelope to insert.
	 */
	public final void add(EventEnvelope e) {
		if (e != null) {
			
			e.setEnqueueCounter(this.counter++);
			
			if (configuration.isLogging()) {
				// logging is on				
				if (e instanceof Loggable) {
					// is loggable
					Loggable log = (Loggable) e;

					int prio = liveLogFilter.getEventTypePriority(e.getEvent().getClass().getName());

					// class should be logged?		
					if (prio != LogFilter.PRIORITY_OFF) {

						// all layers will be logged
						if (e.getEvent() instanceof Movement) {
							Movement move = (Movement) e.getEvent();
							Position realPos = move.getNewPosition();

							history.logMoveEvent(move.getNode().getId(), realPos.getXCoord(), realPos.getYCoord(), 
									e.getTime(), prio);
						} else {
							history.logEnqueueEvent(e, log.getReceiver(), prio);
						}
					}
				}
			}
			
			queue.add(e);			
		}
	}
	
	/**
	 * Retrieves and removes the head of this queue, or returns null if this queue is empty.
	 * Non static therefore only access via reference and not from nodes.
	 * 
	 * @return Head of this queue or null if empty.
	 */
	public final EventEnvelope poll() {
		return queue.poll();
	}
	
	/**
	 * @return The current number of elements in the queue.
	 */
	public final int getSize() {
		return queue.size();
	}
}

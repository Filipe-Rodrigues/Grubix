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

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;


/** 
 * Abstract superclass for all events delivered to nodes.
 * Extending subclasses should be delivered to the whole node,
 * not a specific layer. To create events for a specific layer extend
 * the ToLayer event.
 * 
 * Subclasses are for example Iinitialize and Moved.
 * 
 * @author Andreas Kumlehn
 */
public abstract class ToNode extends Event {
	
	/**
	 * The NodeId of the receiver.
	 */
	private NodeId receiver;
	
	/**
	 * Constructor of the class ToNode.
	 * 
	 * @param receiver The NodeId of the receiver.
	 */
	public ToNode(NodeId receiver) {
		super();
		this.receiver = receiver;
	}

	/**
	 * @return Returns the receiver.
	 */
	public final NodeId getReceiver() {
		return receiver;
	}
	
	/** @param receiver the receiver to set */
	public final void setReceiver(NodeId receiver) {
		this.receiver = receiver;
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.event.Event#getDelay()
	 * @return Delay of the object as Double.
	 */
	public double getDelay() {
		return Configuration.getInstance().getDelayForToNodeEvents();
	}

}

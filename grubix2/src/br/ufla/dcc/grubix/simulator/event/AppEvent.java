package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;

/**
 * Event of the application layer.
 * 
 * @author Dirk Held
 */
public class AppEvent extends WakeUpCall {

	/** type of this app event. */
	private int type;
	
	/**
	 * constructor with a type.
	 * 
	 * @param sender the node who processes this event.
	 * @param delay  the delay to wait before processing.
	 * @param type type of the app event
	 */
	public AppEvent(Address sender, double delay, int type) {
		super(sender, delay);
		this.type = type;
	}
	
	/**
	 * constructor without a type.
	 * 
	 * @param sender the node who processes this event.
	 * @param delay  the delay to wait before processing.
	 */
	public AppEvent(Address sender, double delay) {
		this(sender, delay, 0);
	}

	/** @return the type of this event. */
	public final int getType() {
		return type;
	}

	/** @param type the type to set. */
	public final void setType(int type) {
		this.type = type;
	}
}

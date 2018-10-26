package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

/**
 * Scheduled at the beginning of an incoming transmission.
 * @author madmax
 *
 */
public class TransmissionBeginIncoming extends WakeUpCall {

	/** The transmission that has just started. */
	private Transmission trans;
	
	/**
	 * Constructor for this class.
	 * @param sender The sending node/layer
	 * @param trans The transmission that has just started
	 */
	public TransmissionBeginIncoming(Address sender, Transmission trans) {
		super(sender, Configuration.getInstance().getPropagationDelay());
		this.trans = trans;
	}

	/**
	 * @return The transmission that has just started.
	 */
	public Transmission getTransmission() {
		return this.trans;
	}
}

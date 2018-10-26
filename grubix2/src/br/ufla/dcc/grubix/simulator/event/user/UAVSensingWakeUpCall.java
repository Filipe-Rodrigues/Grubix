/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

/**
 * @author edipig
 *
 */
public class UAVSensingWakeUpCall extends WakeUpCall {

	/**
	 * @param sender
	 * @param delay
	 */
	public UAVSensingWakeUpCall(Address sender, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param sender
	 */
	public UAVSensingWakeUpCall(Address sender) {
		super(sender);
		// TODO Auto-generated constructor stub
	}

}

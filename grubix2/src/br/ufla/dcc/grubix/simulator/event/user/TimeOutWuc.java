package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class TimeOutWuc extends WakeUpCall {

	public TimeOutWuc(Address sender, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
	}

	public TimeOutWuc(Address sender) {
		super(sender);
		// TODO Auto-generated constructor stub
	}

}

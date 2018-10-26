package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class BeaconWakeUpCall extends WakeUpCall {

	public BeaconWakeUpCall(Address sender, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
	}

	public BeaconWakeUpCall(Address sender) {
		super(sender);
		// TODO Auto-generated constructor stub
	}

}

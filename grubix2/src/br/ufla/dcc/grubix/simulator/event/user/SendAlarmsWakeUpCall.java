package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class SendAlarmsWakeUpCall extends WakeUpCall {

	public SendAlarmsWakeUpCall(Address sender, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
	}

	public SendAlarmsWakeUpCall(Address sender) {
		super(sender);
		// TODO Auto-generated constructor stub
	}

}

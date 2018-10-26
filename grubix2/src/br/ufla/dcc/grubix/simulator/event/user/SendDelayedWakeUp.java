package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class SendDelayedWakeUp extends WakeUpCall {

	private Packet pkt;
	public SendDelayedWakeUp(Address sender, double delay, Packet lpkt) {
		super(sender, delay);
		pkt = lpkt;
		// TODO Auto-generated constructor stub
	}

	public SendDelayedWakeUp(Address sender) {
		super(sender);
		// TODO Auto-generated constructor stub
	}

	public Packet getPkt() {
		return pkt;
	}
}

package br.ufla.dcc.PingPong;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class SendWUC extends WakeUpCall {

	public SendWUC(Address sender, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
	}

	public SendWUC(Address sender) {
		super(sender);
		// TODO Auto-generated constructor stub
	}

	public Packet pak;
}

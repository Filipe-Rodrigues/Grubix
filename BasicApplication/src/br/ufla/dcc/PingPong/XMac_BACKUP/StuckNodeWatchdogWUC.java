package br.ufla.dcc.PingPong.XMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class StuckNodeWatchdogWUC extends WakeUpCall{

	public StuckNodeWatchdogWUC(Address sender, double delay) {
		super(sender, delay);
	}

}

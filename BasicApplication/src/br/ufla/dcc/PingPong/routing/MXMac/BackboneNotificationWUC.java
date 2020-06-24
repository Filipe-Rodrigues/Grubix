package br.ufla.dcc.PingPong.routing.MXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class BackboneNotificationWUC extends WakeUpCall {
	
	public BackboneNotificationWUC(Address sender, double delay) {
		super(sender, delay);
	}

}
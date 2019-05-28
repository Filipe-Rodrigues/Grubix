package br.ufla.dcc.PingPong.routing;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class SendDelayedWakeUpCall extends WakeUpCall {

	private Packet packet;
	
	public SendDelayedWakeUpCall(Address sender, Packet packet, double delay) {
		super(sender, delay);
		this.packet = packet;
	}
	
	public Packet getPacket() {
		return packet;
	}

}

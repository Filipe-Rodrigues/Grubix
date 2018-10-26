package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class PingPongwuc extends WakeUpCall {

	private int cont;	
	
	public int getCont() {
		return cont;
	}

	public void setCont(int cont) {
		this.cont = cont;
	}

	public PingPongwuc(Address sender, double delay) {
		super(sender, delay);
	}

	public PingPongwuc(Address sender) {
		super(sender);
	}

}

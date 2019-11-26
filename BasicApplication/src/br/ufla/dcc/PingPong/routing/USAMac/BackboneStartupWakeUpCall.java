package br.ufla.dcc.PingPong.routing.USAMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class BackboneStartupWakeUpCall extends WakeUpCall {

	private Position growthDirection;
	
	public BackboneStartupWakeUpCall(Address sender, Position growthDirection, double delay) {
		super(sender, delay);
		this.growthDirection = growthDirection;
	}
	
	public Position getGrowthDirection() {
		return growthDirection;
	}

}

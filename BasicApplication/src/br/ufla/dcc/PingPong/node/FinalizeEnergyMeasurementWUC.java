package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class FinalizeEnergyMeasurementWUC extends WakeUpCall {

	public FinalizeEnergyMeasurementWUC(Address sender, double delay) {
		super(sender, delay);
	}

}

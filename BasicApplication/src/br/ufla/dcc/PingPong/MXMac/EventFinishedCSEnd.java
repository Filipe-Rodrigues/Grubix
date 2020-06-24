package br.ufla.dcc.PingPong.MXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;

public class EventFinishedCSEnd extends CrossLayerEvent {

	public EventFinishedCSEnd(Address sender) {
		super(sender, null);
	}

}

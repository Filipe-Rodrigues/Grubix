package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;

public class UAVBeacon extends ApplicationPacket {
	public UAVBeacon(Address sender, NodeId receiver){
		super(sender, receiver);
	}

}
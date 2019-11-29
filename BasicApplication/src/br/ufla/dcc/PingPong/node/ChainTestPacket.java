package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;

public class ChainTestPacket extends AppPacket {

	private int lap;

	public ChainTestPacket(Address sender, int lapNumber) {
		super(sender, NodeId.get(sender.getId().asInt() + 1));
		lap = lapNumber;
	}
	
	public ChainTestPacket(Address sender, NodeId receiver, int lapNumber) {
		super(sender, receiver);
		lap = lapNumber;
	}
	
	public int getLapNumber() {
		return lap;
	}
	
}

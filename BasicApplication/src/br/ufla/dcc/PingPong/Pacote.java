package br.ufla.dcc.PingPong;

import br.ufla.dcc.PingPong.node.AppPacket;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;

public class Pacote extends AppPacket{
	
	private int cont;
	
	public int getCont() {
		return cont;
	}

	public void setCont(int cont) {
		this.cont = cont;
	}

	public Pacote(Address sender, NodeId receiver) {
		super(sender, receiver);
		// TODO Auto-generated constructor stub
	}
}

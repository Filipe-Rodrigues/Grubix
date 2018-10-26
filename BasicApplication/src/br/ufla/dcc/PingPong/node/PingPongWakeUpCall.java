package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.grubix.simulator.Address; 
import br.ufla.dcc.grubix.simulator.NodeId;
// classe responsável pelo controle dos wakeupcalls
import br.ufla.dcc.grubix.simulator.event.WakeUpCall; 

/** Classe que herda WakeUpCall */
public class PingPongWakeUpCall extends WakeUpCall { 

	/** Nó de destino */
	private NodeId destination; 
	
	
	
	public NodeId getDestination() {
		return destination;
	}

	public void setDestination(NodeId id) {
		this.destination = id;
	}

	public PingPongWakeUpCall(Address sender, double delay) {
		super(sender, delay);
	}

	public PingPongWakeUpCall(Address sender) {
		super(sender);
	}

}

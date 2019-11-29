package br.ufla.dcc.PingPong.node;

// classe que define o id do nó
import br.ufla.dcc.grubix.simulator.NodeId;
// classe que contém endereço do nó (NodeId + LayerType) emissor
import br.ufla.dcc.grubix.simulator.Address; 
// classe que define o pacote da aplicação (herdada)
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket; 

public class AppPacket extends ApplicationPacket{
	
	// id do nó de destino
	private int destinationId; 
	
	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int id) {
		this.destinationId = id;
	}

	public AppPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
		this.destinationId = receiver.asInt();
	}
	

}

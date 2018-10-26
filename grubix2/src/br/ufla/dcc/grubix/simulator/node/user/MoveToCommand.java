package br.ufla.dcc.grubix.simulator.node.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;

public class MoveToCommand extends Command {
	
	private Position target;
	private NodeId id;
	private Address sender; 

	public MoveToCommand() {
		// TODO Auto-generated constructor stub
	}

	public NodeId getId() {
		return id;
	}

	public void setId(NodeId id) {
		this.id = id;
	}

	public Address getSender() {
		return sender;
	}

	public void setSender(Address sender) {
		this.sender = sender;
	}

	public Position getTarget() {
		return target;
	}

	public void setTarget(Position target) {
		this.target = target;
	}

}

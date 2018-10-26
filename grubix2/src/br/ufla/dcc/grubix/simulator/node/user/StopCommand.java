package br.ufla.dcc.grubix.simulator.node.user;

import br.ufla.dcc.grubix.simulator.NodeId;

public class StopCommand extends Command {
	public NodeId id;

	public StopCommand(NodeId id) {
		super();
		this.id = id;
	}

}

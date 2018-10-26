package br.ufla.dcc.PingPong.node.user;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.node.user.Command;

public class StopCommand extends Command {
	public NodeId id;

	public StopCommand(NodeId id) {
		super();
		this.id = id;
	}

}

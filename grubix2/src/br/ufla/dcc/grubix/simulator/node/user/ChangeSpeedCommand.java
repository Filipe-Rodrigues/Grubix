package br.ufla.dcc.grubix.simulator.node.user;

import br.ufla.dcc.grubix.simulator.NodeId;

public class ChangeSpeedCommand extends Command {
	public NodeId nodeId;
	public double speed_variation;
	public ChangeSpeedCommand(NodeId nodeId, double speed_variation) {
		super();
		this.nodeId = nodeId;
		this.speed_variation = speed_variation;
	}

}

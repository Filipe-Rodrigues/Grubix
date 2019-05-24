package br.ufla.dcc.grubix.simulator.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;

public class BackboneConfiguration implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public NodeId nextBackboneNode;
	public Position direction;
	public List<NodeId> backboneNeighbors;
	public byte label;
	public double cycleStart;
	
	public BackboneConfiguration() {
		nextBackboneNode = null;
		backboneNeighbors = new ArrayList<NodeId>();
		label = -1;
	}
}
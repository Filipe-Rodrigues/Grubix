package br.ufla.dcc.grubix.simulator.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.util.Pair;

public class BackboneConfiguration implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int backboneChannel;
	public NodeId nextBackboneNode;
	public Position direction;
	public List<NodeId> backboneNeighborsUSAMac;
	public List<Pair<NodeId, Integer>> backboneNeighborsMXMac;
	public byte label;
	public double cycleStart;
	
	public BackboneConfiguration() {
		nextBackboneNode = null;
		backboneChannel = 0;
		backboneNeighborsUSAMac = new ArrayList<NodeId>();
		backboneNeighborsMXMac = new ArrayList<Pair<NodeId, Integer>>();
		label = -1;
	}
}
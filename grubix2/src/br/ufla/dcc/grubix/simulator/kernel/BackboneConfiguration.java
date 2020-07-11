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
	public List<NodeId> backboneNeighborsMXMacType1;
	public List<NodeId> backboneNeighborsMXMacType2;
	public byte label;
	public double cycleStart;
	
	public BackboneConfiguration() {
		nextBackboneNode = null;
		backboneChannel = 0;
		backboneNeighborsUSAMac = new ArrayList<NodeId>();
		backboneNeighborsMXMacType1 = new ArrayList<NodeId>();
		backboneNeighborsMXMacType2 = new ArrayList<NodeId>();
		label = -1;
		cycleStart = -1;
	}
	
	public void addBackboneNeighborMXMac(NodeId bbNeighbor, int bbType) {
		if (bbType == 1) {
			backboneNeighborsMXMacType1.add(bbNeighbor);
		} else {
			backboneNeighborsMXMacType2.add(bbNeighbor);
		}
	}
}
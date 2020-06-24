package br.ufla.dcc.PingPong.routing.MXMac;

import java.util.ArrayList;
import java.util.List;

import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.NON_BB_CHANNEL;
import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.CLOCKWISE_BB_CHANNEL;
import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.COUNTER_CLOCKWISE_BB_CHANNEL;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;

public class MXMacRoutingParameters {
	public int backboneType;
	public Node node;
	public Node nextBBnode;
	public List<Node> neighbors;
	public List<Node> bbNeighborsType1;
	public List<Node> bbNeighborsType2;
	public Position direction;
	public Position hypocenter;
	
	public MXMacRoutingParameters(Node node) {
		this.node = node;
		backboneType = NON_BB_CHANNEL;
		neighbors = node.getNeighbors();
		bbNeighborsType1 = new ArrayList<Node>();
		bbNeighborsType2 = new ArrayList<Node>();
	}
	
	public void addBackbone(NodeId bbNodeId, int bbType) {
		Node bbNode = SimulationManager.getInstance().queryNodeById(bbNodeId);
		if (bbType == CLOCKWISE_BB_CHANNEL && !bbNeighborsType1.contains(bbNode)) {
			bbNeighborsType1.add(bbNode);
		} else if (bbType == COUNTER_CLOCKWISE_BB_CHANNEL && !bbNeighborsType2.contains(bbNode)) {
			bbNeighborsType2.add(bbNode);
		}
	}

}

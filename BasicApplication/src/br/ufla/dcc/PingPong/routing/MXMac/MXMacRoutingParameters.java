package br.ufla.dcc.PingPong.routing.MXMac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.NON_BB_CHANNEL;
import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.CLOCKWISE_BB_CHANNEL;
import static br.ufla.dcc.PingPong.MXMac.MXMacConstants.COUNTER_CLOCKWISE_BB_CHANNEL;
import static br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager.MXMAC_CONFIG;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.BackboneConfigurationManager;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.util.Pair;

public class MXMacRoutingParameters {
	
	private int backboneType;
	private Node node;
	private Node nextBBnode;
	private List<Node> neighbors;
	private List<Node> bbNeighborsType1;
	private List<Node> bbNeighborsType2;
	private Position direction;
	private Position hypocenter;
	private static final BackboneConfigurationManager savestate = BackboneConfigurationManager.getInstance(MXMAC_CONFIG);
	
	public MXMacRoutingParameters(Node node, boolean loadSaved) {
		this.node = node;
		neighbors = node.getNeighbors();
		if (loadSaved) {
			backboneType = savestate.getBackboneNodeChannel(node.getId());
			bbNeighborsType1 = loadNodes(savestate.loadBackboneNeighborsMXMacType1(node.getId()));
			bbNeighborsType2 = loadNodes(savestate.loadBackboneNeighborsMXMacType2(node.getId()));
			NodeId nextBBId = savestate.getNextBackboneNode(node.getId());
			direction = savestate.getBackboneDirection(node.getId());
			if (nextBBId != null) {
				nextBBnode = SimulationManager.getInstance().queryNodeById(nextBBId);
			} else {
				nextBBnode = null;
			}
		} else {
			backboneType = NON_BB_CHANNEL;
			bbNeighborsType1 = new ArrayList<Node>();
			bbNeighborsType2 = new ArrayList<Node>();
		}
	}

	private List<Node> loadNodes(List<NodeId> nodeIDs) {
		List<Node> nodes = new ArrayList<Node>();
		for (NodeId nodeID : nodeIDs) {
			nodes.add(SimulationManager.getInstance().queryNodeById(nodeID));
		}
		return nodes;
	}

	public int getBackboneType() {
		return backboneType;
	}
	
	public boolean amIBackbone() {
		return backboneType > 0;
	}

	public void setBackboneType(int backboneType) {
		this.backboneType = backboneType;
		savestate.setBackboneNodeChannel(node.getId(), backboneType);
	}

	public Node getNode() {
		return node;
	}

	public Node getNextBBnode() {
		return nextBBnode;
	}

	public void setNextBBnode(Node nextBBnode) {
		this.nextBBnode = nextBBnode;
		savestate.setNextBackboneNode(node.getId(), nextBBnode.getId(), direction);
	}

	public List<Node> getNeighbors() {
		return Collections.unmodifiableList(neighbors);
	}

	public List<Node> getBbNeighborsType1() {
		return Collections.unmodifiableList(bbNeighborsType1);
	}

	public List<Node> getBbNeighborsType2() {
		return Collections.unmodifiableList(bbNeighborsType2);
	}

	public Position getDirection() {
		return direction;
	}
	
	public void setDirection(Position direction) {
		this.direction = direction;
		
	}

	public Position getHypocenter() {
		return hypocenter;
	}

	public void setHypocenter(Position hypocenter) {
		this.hypocenter = hypocenter;
	}

	public void addBackbone(NodeId bbNodeId, int bbType) {
		Node bbNode = SimulationManager.getInstance().queryNodeById(bbNodeId);
		if (bbType == CLOCKWISE_BB_CHANNEL && !bbNeighborsType1.contains(bbNode)) {
			bbNeighborsType1.add(bbNode);
			savestate.addBackboneNeighbor(node.getId(), bbNodeId, bbType);
		} else if (bbType == COUNTER_CLOCKWISE_BB_CHANNEL && !bbNeighborsType2.contains(bbNode)) {
			bbNeighborsType2.add(bbNode);
			savestate.addBackboneNeighbor(node.getId(), bbNodeId, bbType);
		}
	}

	public boolean isNeighborBackbone(Node neighbor) {
		return bbNeighborsType1.contains(neighbor) || bbNeighborsType2.contains(neighbor);
	}
	
	public NodeId getBackboneNeighbor(Pair<Integer, Position> type, Position target) {
		int channel = type.first;
		Position dir = type.second;
		if (channel == 1) {
			return searchNearestBBNeighbor(bbNeighborsType1, target, dir);
		} else if (channel == 2) {
			return searchNearestBBNeighbor(bbNeighborsType2, target, dir);
		}
		return null;
	}
	
	private NodeId searchNearestBBNeighbor(List<Node> nodes, Position target, Position dir) {
		NodeId nearestNode = null;
		double min = Double.POSITIVE_INFINITY;
		for (Node node : nodes) {
			double dist = node.getPosition().getDistance(target);
			if (savestate.getBackboneDirection(node.getId()).equals(dir) && dist < min) {
				min = dist;
				nearestNode = node.getId();
			}
		}
		return nearestNode;
	}

}

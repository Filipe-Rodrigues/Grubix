package br.ufla.dcc.PingPong.PaxMac;

import br.ufla.dcc.grubix.simulator.node.Node;



/**
 * Classe que contém a distância de um nó até o destino final. Usado para criar uma lista de nós
 * ordenados por está distância
 * 
 *  @author Gustavo Araújo
 *  @version 15/07/2016
 */
public class NodeDistanceDestination {

	private Node node;
	private Double distance;
	
	public NodeDistanceDestination(Node node, Double distance) {
		this.node = node;
		this.distance = distance;
	}

	
	/* Gets and set */
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}
	
	
}

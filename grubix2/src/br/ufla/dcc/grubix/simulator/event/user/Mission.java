package br.ufla.dcc.grubix.simulator.event.user;

import java.util.Vector;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.node.Node;




public class Mission
{
	private int requiredTypeOfNode;
	private Vector<Node> interesingNodes = new Vector<Node>();
	private double requiredAccuracy;
	private double requiredDensity;
	
	private static int id_counter = 0;
	
	private int id;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Mission(int requiredTypeOfNode, double requiredAccuracy, double requiredDensity) {
		super();
		this.requiredAccuracy = requiredAccuracy;
		this.requiredDensity = requiredDensity;
		this.requiredTypeOfNode = requiredTypeOfNode;
		this.id = id_counter++;
	}
	public Vector<Node> getInteresingNodes() {
		return interesingNodes;
	}
	public double getRequiredAccuracy() {
		return requiredAccuracy;
	}
	public double getRequiredDensity() {
		return requiredDensity;
	}
	public int getRequiredTypeOfNode() {
		return requiredTypeOfNode;
	}
}
package br.ufla.dcc.PingPong.routing;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class BackboneStartAdviseWUC extends WakeUpCall {

	private Position centralBackboneNode;
	private int jumpCounter;
	
	public BackboneStartAdviseWUC(Address sender, Position centralBackboneNode, int jumpCounter, double delay) {
		super(sender, delay);
		this.centralBackboneNode = centralBackboneNode;
		this.jumpCounter = jumpCounter;
		// TODO Auto-generated constructor stub
	}
	
	public BackboneStartAdviseWUC(Address sender, Position centralBackboneNode, double delay) {
		super(sender, delay);
		this.centralBackboneNode = centralBackboneNode;
		// TODO Auto-generated constructor stub
	}
	
	public Position getcentralBackboneNode() {
		return centralBackboneNode;
	}
	
	public int getJumpCounter() {
		return jumpCounter;
	}

}

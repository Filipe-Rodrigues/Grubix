package br.ufla.dcc.PingPong.routing;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class BackbonePropagationWUC extends WakeUpCall {

	private NodeId receiverId;
	private BackboneRoutingPacketType mode;
	private int jumpCounter;
	
	public BackbonePropagationWUC(Address sender, NodeId receiverId, BackboneRoutingPacketType mode, int jumpCounter, double delay) {
		super(sender, delay);
		this.receiverId = receiverId;
		this.mode = mode;
		this.jumpCounter = jumpCounter;
	}
	
	public BackbonePropagationWUC(Address sender, NodeId receiverId, BackboneRoutingPacketType mode, double delay) {
		super(sender, delay);
		this.receiverId = receiverId;
		this.mode = mode;
		this.jumpCounter = jumpCounter;
	}
	
	public NodeId getReceiverId() {
		return this.receiverId;
	}
	
	public BackboneRoutingPacketType getMode() {
		return mode;
	}
	
	public int getJumpCounter() {
		return jumpCounter;
	}
	
}

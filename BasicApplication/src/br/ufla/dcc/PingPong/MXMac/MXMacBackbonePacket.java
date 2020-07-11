package br.ufla.dcc.PingPong.MXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;

public class MXMacBackbonePacket extends MXMacPacket {

	private double parentBackboneCycleShiftRatio;
	
	private NodeId nextBackboneTarget;
	
	private int backboneChannel;
	
	public MXMacBackbonePacket(Address sender, LogLinkPacket packet, boolean ackReq, int channel, double parentCycleShift) {
		super(sender, packet, ackReq);
		parentBackboneCycleShiftRatio = parentCycleShift;
		backboneChannel = channel;
		nextBackboneTarget = null;
	}
	
	public MXMacBackbonePacket(Address sender, LogLinkPacket packet, boolean ackReq, int channel, NodeId nextBackbone, double parentCycleShift) {
		super(sender, packet, ackReq);
		parentBackboneCycleShiftRatio = parentCycleShift;
		backboneChannel = channel;
		nextBackboneTarget = nextBackbone;
	}

	public double getParentBackboneCycleShiftRatio() {
		return parentBackboneCycleShiftRatio;
	}
	
	public NodeId getNextBackboneTarget() {
		return nextBackboneTarget;
	}

	public int getBackboneChannel() {
		return backboneChannel;
	}
	
}

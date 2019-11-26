package br.ufla.dcc.PingPong.USAMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;

public class USAMacBackbonePacket extends USAMacPacket {

	private double parentBackboneCycleShiftRatio;
	
	private NodeId nextBackboneTarget;
	
	public USAMacBackbonePacket(Address sender, LogLinkPacket packet, boolean ackReq, double parentCycleShift) {
		super(sender, packet, ackReq);
		parentBackboneCycleShiftRatio = parentCycleShift;
		nextBackboneTarget = null;
	}
	
	public USAMacBackbonePacket(Address sender, LogLinkPacket packet, boolean ackReq, NodeId nextBackbone, double parentCycleShift) {
		super(sender, packet, ackReq);
		parentBackboneCycleShiftRatio = parentCycleShift;
		nextBackboneTarget = nextBackbone;
	}

	public double getParentBackboneCycleShiftRatio() {
		return parentBackboneCycleShiftRatio;
	}
	
	public NodeId getNextBackboneTarget() {
		return nextBackboneTarget;
	}

}

package br.ufla.dcc.PingPong.EXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;

public class EXMacBackbonePacket extends EXMacPacket {

	private double parentBackboneCycleShiftRatio;
	
	private NodeId nextBackboneTarget;
	
	public EXMacBackbonePacket(Address sender, LogLinkPacket packet, boolean ackReq, double parentCycleShift) {
		super(sender, packet, ackReq);
		parentBackboneCycleShiftRatio = parentCycleShift;
		nextBackboneTarget = null;
	}
	
	public EXMacBackbonePacket(Address sender, LogLinkPacket packet, boolean ackReq, NodeId nextBackbone, double parentCycleShift) {
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

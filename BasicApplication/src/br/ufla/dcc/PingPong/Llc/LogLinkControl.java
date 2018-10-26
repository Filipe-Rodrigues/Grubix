package br.ufla.dcc.PingPong.Llc;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.node.LogLinkLayer;
import br.ufla.dcc.PingPong.ToolsDebug;

public class LogLinkControl extends LogLinkLayer{
	
	/** Depuração */
	ToolsDebug debug = ToolsDebug.getInstance();
	
	/**
	 * @see br.ufla.dcc.PingPong.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.PingPong.grubix.simulator.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public final void lowerSAP(Packet packet) {
		if (!packet.isTerminal()) {
			debug.write(debug.strPkt(packet), sender, "sendPacket");
			sendPacket(packet.getEnclosedPacket());
		}
	}

	/**
	 * @see br.ufla.dcc.PingPong.grubix.simulator.node.Layer#upperSAP(br.ufla.dcc.PingPong.grubix.simulator.event.Packet)
	 * @param packet to process coming from higher layer.
	 */
	@Override
	public final void upperSAP(Packet packet) {	
		Packet nextPacket = new LogLinkPacket(getSender(), packet);
		debug.write(debug.strPkt(nextPacket), sender, "sendPacket");
		sendPacket(nextPacket);
	}

	/**
	 * Handles {@link CrossLayerEvent} by forwarding it upwards, if necessary.
	 * @param wuc The received {@link WakeUpCall}.
	 * @throws LayerException if an unhandled {@link WakeUpCall} occurs.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		if (wuc instanceof CrossLayerEvent) {
			debug.write(debug.str("CrossLayerEvent (forwardUp)")+
					debug.strEventCrossLayer((CrossLayerEvent) wuc), sender);
			((CrossLayerEvent) wuc).forwardUp(this);
		} else {
			debug.write(debug.strWuc(wuc), sender);
			super.processWakeUpCall(wuc);
		}
	}

	/** @return null since currently no states needed, thus no state changes possible. */
	public LayerState getState() {
		return null;
	}

	/**
	 * Currently no states needed, thus no state changes possible.
	 * @param state the new desired state of this layer.
	 * @return true if the state change was accepted.
	 */
	public boolean setState(LayerState state) {
		return true;
	}
}

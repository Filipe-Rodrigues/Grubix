/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.user.os;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.OperatingSystemPacket;
import br.ufla.dcc.grubix.simulator.event.Packet;

/**
 * Operating system (layer) implementation without contents.
 * It simply delegates every packet to the layer below and above, using a {@link OperatingSystemPacket}
 * as container.
 * 
 * @author dmeister
 *
 */
public class NullOperatingSystemLayer extends OperatingSystemLayer {
	
	/**
	 * Constructor.
	 */
	public NullOperatingSystemLayer() {
	}

	/** 
	 * lower access point on any layer.
	 * 
	 * @param packet Packet to process in the layer coming from a lower layer.
	 * @throws LayerException if something goes wrong (e.g. type not handled properly).
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 */
	@Override
	public void lowerSAP(Packet packet) throws LayerException {
		sendPacket(packet.getEnclosedPacket());
	}


	/**
	 * higher access point on any layer.
	 * 
	 * @param packet Packet to process in the layer coming from a higher layer.
	 * @throws LayerException if something goes wrong (e.g. type not handled properly).
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#upperSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 */
	@Override
	public void upperSAP(Packet packet) throws LayerException {
		Packet nextPacket = new OperatingSystemPacket(getSender(), 
				null, packet.getReceiver(), null, packet);
		sendPacket(nextPacket);
	}

	/**
	 * gets a current status object.
	 * not supported by this implementation, so a {@link UnsupportedOperationException} is thrown.
	 * 
	 * @return the current status of the layer.
	 */
	@Override
	public LayerState getState() {
		throw new UnsupportedOperationException("getState not supported");
	}

	/**
	 * changes the status of the layer with an modified status-object.
	 * not supported by this implementation, so a {@link UnsupportedOperationException} is thrown.
	 * 
	 * @param state the changed new status for the layer.
	 * @return true if the state-change was accepted.
	 * @see br.ufla.dcc.grubix.simulator.node.StateIO#setState(br.ufla.dcc.grubix.simulator.event.LayerState)
	 */
	@Override
	public boolean setState(LayerState state) {
		throw new UnsupportedOperationException("setState not supported");
	}

}

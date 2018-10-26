package br.ufla.dcc.grubix.simulator.physical;

import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.Transmission;

/**
 * A bitmangling model that causes no packet destruction no matter how much interference occured
 * during transmission. <b>Warning:</b> this model can cause multiple packets to arrive and be regarded
 * valid at one node at the same time! Since this is an incident which can never happen in
 * reality, it is highly likely that many PHY and MAC implementations do not work properly
 * with this bitmangling model. This is, e.g., the case for the 802.11 implementations in ShoX. 
 * @author jlsx
 */
public class NoBitMangling extends BitManglingModel {

	/**
	 * Constructs a NoBitMangling model without any parameters.
	 */
	public NoBitMangling() {
	}

	/**
	 * Just returns back the original packet without any packet destruction.
	 * @param transStartTime Time when the transmission of trans started
	 * @param trans          The transmission object which contains the current data packet
	 * @param inter          the affecting interferences.
	 * @return The resulting data packet which is never damaged no matter how much interference occured
	 */
	public PhysicalPacket getResultingDataPacket(double transStartTime,	Transmission trans, InterferenceQueue inter) {
		PhysicalPacket packet = trans.getPacket();
		PhysicalPacket clonedPacket = (PhysicalPacket) packet.clone();
        return clonedPacket;
	}
}

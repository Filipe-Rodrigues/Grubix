/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event.user;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;

/**
 * @author edipig
 *
 */
public class BeaconPacket extends ApplicationPacket {

	/**
	 * @param sender
	 * @param receiver
	 */
	public BeaconPacket(Address sender, NodeId receiver) {
		super(sender, receiver);
		// TODO Auto-generated constructor stub
	}

}

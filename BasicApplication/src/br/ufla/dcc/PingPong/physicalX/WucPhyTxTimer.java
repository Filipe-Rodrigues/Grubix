package br.ufla.dcc.PingPong.physicalX;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

/**
 * 
 * WakeUpCall para envio de pacotes pelo Rádio.
 * 
 * O trabalho de marcar tempo de ocupação do canal de comunicação é da AIR.
 * Este trabalho será feito na PHY até que aprendamos a usar a AIR.
 * 
 * Usado pela PhysicalX.java
 * 
 * @author João Giacomin
 * @version 18/03/2019
 *
 */

public class WucPhyTxTimer extends WakeUpCall {
	
	/** The packet that is first in the queue (i.e. to be sent when the wake up call fires). */
	private Packet packet;

	/**
	 * Default constructor of the class PhyTimerWUC with no packet.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param delay Wanted delay until callback.
	 */
	public WucPhyTxTimer(Address sender, Double delay, Packet packet) {
		super(sender, delay);
		this.packet = packet;
	}
	
	public Packet getPacket() {
		return this.packet;
	}
		
}

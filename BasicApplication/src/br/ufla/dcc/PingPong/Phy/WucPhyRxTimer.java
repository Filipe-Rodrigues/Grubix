package br.ufla.dcc.PingPong.Phy;

import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/**
 * 
 * WakeUpCall para envio de pacotes pelo Rádio.
 * 
 * Embora seja uma WUC, vamos deixar na pasta da X_MAC para não poluir o Grubix
 * 
 * 
 * @author João Giacomin
 *
 */

public class WucPhyRxTimer extends WakeUpCall {
	
	/** The packet that is first in the queue (i.e. to be sent when the wake up call fires). */
	private Packet packet;

	/** Tempo em steps em que esta WakeUpCall foi criada. */
	private double startTime;
	
	/** Qual era o estado do rádio quando esta WakeUpCall foi criada */
	private RadioState startingState;
	
	
	/**
	 * Default constructor of the class PhyTimerWUC with no packet.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param delay Wanted delay until callback.
	 */
	public WucPhyRxTimer(Address sender, Double delay, RadioState startingState, Packet packet) {
		super(sender, delay);
		startTime = SimulationManager.getInstance().getCurrentTime();
		this.startingState = startingState;
		this.packet = packet;
	}
	
	public Packet getPacket() {
		return this.packet;
	}
	
	public double getStartTime() {
		return this.startTime;
	}
	
	public double getEndTime() {
		return this.startTime + getDelay();  // delay é atributo da superclasse WakeUpCall
	}
	
	public RadioState getStartingState() {
		return this.startingState;
	}
		
}

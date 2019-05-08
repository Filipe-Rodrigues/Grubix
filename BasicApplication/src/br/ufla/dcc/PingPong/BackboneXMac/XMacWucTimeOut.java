package br.ufla.dcc.PingPong.BackboneXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/**
 * 
 * WakeUpCall para envio de pacotes da MAC.
 * Embora seja uma WUC, foi deixado na pasta da X_MAC para não poluir o Grubix
 
 * @author Gustavo Araujo - 19/05/2016
 *
 */

public class XMacWucTimeOut extends WakeUpCall {
	
	/** Número sequencial do estado */
	private int seqState;
	
	/** Tempo em steps em que esta WakeUpCall foi criada. */
	private double startTime;
	

	/**
	 * Default constructor of the class X_MACStateTimerWUC with no packet.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param delay Wanted delay until callback.
	 */
	public XMacWucTimeOut(Address sender, Double delay) {
		super(sender, delay);
		startTime = SimulationManager.getInstance().getCurrentTime();
	}
	
	/**
	 * Default constructor of the class X_MACStateTimerWUC with no packet.
	 * 
	 */
	public XMacWucTimeOut(Address sender, Double delay, int sequencial) {
		super(sender, delay);
		startTime = SimulationManager.getInstance().getCurrentTime();
		seqState  = sequencial;
	}
	
	
	public double getStartTime() {
		return this.startTime;
	}
	
	public double getEndTime() {
		return this.startTime + getDelay();  // delay é atributo da superclasse WakeUpCall
	}
	
	public int getStateNumber() {
		return seqState;
	}

	/** ajusta o contador de estados */
	public void setStateNumber (int stateNum) {
		this.seqState = stateNum;
	}
		
}

package br.ufla.dcc.PingPong.physicalMX;

import br.ufla.dcc.grubix.simulator.node.RadioState;


/**
 * Classe que informa e manipula o estado do rádio.
 * Utiliza as definições de estados de rádio originais do GrubiX, contidas em node.RadioState.java.
 * 
 * Usada pela PhysicalX.java
 * 
 * @author João Giacomin
 * @version 18/03/2019
 *
 */

public class PhyRadioState { 
	
	private RadioState radioState;
	private int currentChannel;
	
	/** Construtor padrão
	 * 
	 *  @param initialState - O estado inicial do rádio.
	 */
	public PhyRadioState(RadioState initialState){
		this.radioState = initialState;
		this.currentChannel = 0;
	}
	
	/** estará ligado em qualquer estado que não seja OFF */
	public boolean isRadioOn(){     
		if(radioState != RadioState.OFF) return true;
		else return false;
	}
	
	public int getCurrentChannel() {
		return currentChannel;
	}
	
	public void setCurrentChannel(int channel) {
		if (channel >= 0) {
			currentChannel = channel;
		}
	}
	
	public RadioState getRadioState() {
		return radioState;
	}

	public void setRadioState(RadioState radioState) {
		this.radioState = radioState;
	}

}

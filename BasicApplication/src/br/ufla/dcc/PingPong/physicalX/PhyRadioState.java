package br.ufla.dcc.PingPong.physicalX;

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
	
	/** Construtor padrão
	 * 
	 *  @param initialState - O estado inicial do rádio.
	 */
	public PhyRadioState(RadioState initialState){
		this.radioState = initialState;
	}
	
	/** estará ligado em qualquer estado que não seja OFF */
	public boolean isRadioOn(){     
		if(radioState != RadioState.OFF) return true;
		else return false;
	}
	
	public RadioState getRadioState() {
		return radioState;
	}

	public void setRadioState(RadioState radioState) {
		this.radioState = radioState;
	}

}

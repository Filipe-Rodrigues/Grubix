package br.ufla.dcc.PingPong.XMac;

import br.ufla.dcc.PingPong.node.RadioState;


/**
 * Classe que informa e manipula o estado do rádio.
 * Utiliza as definições de estados de rádio originais do GrubiX, contidas em node.RadioState.java.
 * 
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 22/06/2016
 *
 */

public class XMacRadioState { 
	
	private RadioState radioState;
	
			
	public XMacRadioState(RadioState initialState){
		this.radioState = initialState;
	}
	
	/** Estará ligado em qualquer estado que não seja OFF */
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

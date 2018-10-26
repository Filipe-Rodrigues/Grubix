package br.ufla.dcc.PingPong.Phy;

import br.ufla.dcc.PingPong.node.RadioState;


/**
 * Classe que informa e manipula o estado do rádio.
 * Utiliza as definições de estados de rádio originais do GrubiX, contidas em node.RadioState.java.
 * 
 * @author Gustavo Araujo  - 18/05/2016
 *
 */

public class RadioStateJG { 
	
	private RadioState radioState;
	
			
	public RadioStateJG(RadioState initialState){
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

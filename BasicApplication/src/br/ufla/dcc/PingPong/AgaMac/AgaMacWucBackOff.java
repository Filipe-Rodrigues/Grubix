package br.ufla.dcc.PingPong.AgaMac;

import br.ufla.dcc.PingPong.XMac2019.XMacPacket;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

/**
 * 
 * WakeUpCall para reenvio de mensagem pela MAC, quando o rádio estiver ocupado.
 * 
 * Embora seja uma WUC, vamos deixar na pasta da X_MAC para não poluir o Grubix
 * 
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 22/06/2016
 *
 */

public class AgaMacWucBackOff extends WakeUpCall {
	
	/** O limite de tentativas de envio é determinado pelo tempo que se está tentando enviar uma mensagem pelo rádio */
	private double startingTime;
	/** O pacote a ser enviado, poder ser preâmbulos ou data */
	private XMacPacket packet;
	

	/**
	 * Default constructor of the class X_MACbackOffWUC with no packet.
	 * 
	 * @param sender Sender of the WakeUpCall.
	 * @param delay Wanted delay until callback.
	 */
	public AgaMacWucBackOff(Address sender, Double delay) {
		super(sender, delay);
	}
	
	/**
	 * Default constructor of the class X_MACbackOffWUC with no packet.
	 * 
	 */
	public AgaMacWucBackOff(Address sender, Double delay, double startingTime) {
		super(sender, delay);
		this.setStartingTime(startingTime);
	}
	
	public XMacPacket getPacket() {
		return packet;
	}

	public void setPacket(XMacPacket nextPacket) {
		this.packet = nextPacket;
	}

	public double getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(double startingTime) {
		this.startingTime = startingTime;
	}	
}

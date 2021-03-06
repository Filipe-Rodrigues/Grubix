package br.ufla.dcc.PingPong.BackboneXMac.Stats;

import br.ufla.dcc.PingPong.XMac2019.XMacPacket;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

public class TransmissionBK {

	/** Remetente da mensage, */
	public NodeId senderId;
	
	/** Destino final da mensagem, usada para distinguir os dados estatísticos de outras transmissões */
	public NodeId destinationId;
	
	public int destinationFinal;
	
	/** Numero de rts usado para a transmissao */
	private int numRtsTrans = 0;
	
	/** Tempo inicio e fim de cada transmissao */
	private double startTx = -1;
	
	private double endTx = -1;
	
	
	/** Flag para checagem se a transmissao ainda esta sendo feita*/
	private boolean endTransmission = false;
	
	public TransmissionBK(NodeId senderId, NodeId destinationId){
		this.senderId = senderId;
		this.destinationId = destinationId;
		//this.destinationFinal = destinationFinal;
		setStartTx();
	}
	
	
	
	public NodeId getSenderId() {
		return senderId;
	}

	public NodeId getDestinationId() {
		return destinationId;
	}
	
	public int getDestinationFinalId() {
		return destinationFinal;
	}

	public int getNumRtsTrans() {
		return numRtsTrans;
	}

	public void setNumRtsTrans() {
		this.numRtsTrans++;
	}
	
	public double getStartTx() {
		return startTx;
	}

	public void setStartTx() {
		this.startTx = SimulationManager.getInstance().getCurrentTime();
	}
	
	public double getEndTx() {
		return endTx;
	}

	public void setEndTx() {
		this.endTx = SimulationManager.getInstance().getCurrentTime();
	}

	public boolean isEndTransmission() {
		return endTransmission;
	}

	public void setEndTransmission(boolean endTransmission) {
		this.endTransmission = endTransmission;
	}
	
}

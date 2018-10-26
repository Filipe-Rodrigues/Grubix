
/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.PingPong.AgaMac;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;

/**
 * Classe que apresenta o status do AGAMac
 * 
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 22/06/2016
 *
 */
public class AgaMacState extends LayerState {

	/** O estado atual */
	private AgaMacStateTypes state;
	
	/** Sequência atual de estados */
	private int stateSeqNum = 0;
	
	/** Duração (em steps) prevista para o estado atual */
	private double stateDuration;
	
	/** Indica se há pacote para ser enviado. 
	 * Usado pela AGAMacStateMachine avisar a AGAMac para enviar uma mensagem. */
	private PacketType sendingPktType = null;
	
	/** Pacote de dados que será enviado */
	private AgaMacPacket dataPkt;
	
	/** Pacote RTS que será enviado */
	private AgaMacPacket rtsPkt;
	
    /** Se é preciso reiniciar o processo de envio de preâmbulos */
    private boolean restartRTS = false;
	
	/** Número de sequência do ultimo pacote que foi enviado */
	private int dataSeqNum = 0;
	
	/** Sequência do RTS (preâmbulo) a ser copiada no CTS (earlyACK) */
	private int ctsSeqNum;
	
	/** Sequência do DATA a ser copiada no ACK */
	private int ackSeqNum;
    
	/** Informação sobre atividade no canal, se há mensagem em trânsito */
    private boolean channelBusy = true;
    
	/** O endereço do nó para o qual vai a próxima mensagem */
	private NodeId nextReceiverNode;
    
	/** +AGAMac O endereço do nó que é o destino final */
	private NodeId lastReceiverNode; 
	
    /** +AGAMac Limiar padrão para decidir se envia CTS em resposta a um RTS */
	private double threshold;
    
    /** +AGAMac Limiar atual, ajustável, quando não há vizinho na lente */
    private double currentThreshold;
    
    
    
	/** Default constructor */
	public AgaMacState(AgaMacStateTypes state, int seq) {
		this.state  = state;
		this.stateSeqNum = seq;
	}


	/* Gets e Sets*/
	public AgaMacStateTypes getState() {
		return state;
	}
	
	
	/** Função padrão para atribuir novo estado a AGAMacState */
	public boolean setState(AgaMacStateTypes newState) {   
		// a cada novo estado, incrementa o contador de sequência de estados
		this.state = newState;
		this.stateSeqNum++;           
		return true;
	}
	

	/** Função alternativa para atribuir novo estado a AGAMacState, marcando 
	 * o tempo máximo de permanência no estado */
	public boolean setState(AgaMacStateTypes newState, double delay) {
		this.stateDuration = delay;
		return setState(newState);
	}

	
	public int getStateSeqNum() {
		return stateSeqNum;
	}

	
	public void setStateSeqNum(int stateSeqNum) {
		this.stateSeqNum = stateSeqNum;
	}

	
	public double getStateDuration() {
		return stateDuration;
	}

	
	public void setStateDuration(double stateDuration) {
		this.stateDuration = stateDuration;
	}

	
	public int getDataSeqNum() {
		return dataSeqNum;
	}

	
	public void setDataSeqNum(int packetSeqNum) {
		this.dataSeqNum = packetSeqNum;
	}

	
	public AgaMacPacket getDataPkt() {
		return dataPkt;
	}

	
	public void setDataPkt(AgaMacPacket dataPkt) {
		this.dataPkt = dataPkt;
	}
	
	
	public AgaMacPacket getRtsPkt() {
		return rtsPkt;
	}

	
	public void setRtsPkt(AgaMacPacket rtsPkt) {
		this.rtsPkt = rtsPkt;
	}
	
	
	public boolean isRestartRTS() {
		return restartRTS;
	}

	
	public void setRestartRTS(boolean restartRTS) {
		this.restartRTS = restartRTS;
	}
	
	
	public PacketType getSendingPktType() {
		return sendingPktType;
	}
	
	
	public void setSendingPktType(PacketType sendingPktType) {
		this.sendingPktType = sendingPktType;
	}

	
	public int getCtsSeqNum() {
		return ctsSeqNum;
	}

	
	public void setCtsSeqNum(int ctsSeqNum) {
		this.ctsSeqNum = ctsSeqNum;
	}

	
	public int getAckSeqNum() {
		return ackSeqNum;
	}

	
	public void setAckSeqNum(int ackSeqNum) {
		this.ackSeqNum = ackSeqNum;
	}
	
	
	public boolean isChannelBusy() {
		return channelBusy;
	}

	
	public void setChannelBusy(boolean channelBusy) {
		this.channelBusy = channelBusy;
	}


	public double getThreshold() {
		return threshold;
	}


	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	
	public double getCurrentThreshold() {
		return currentThreshold;
	}

	
	public void setCurrentThreshold(double currentThreshold) {
		this.currentThreshold = currentThreshold;
	}


	public NodeId getLastReceiver() {
		return lastReceiverNode;
	}


	public void setLastReceiver(NodeId lastReceiver) {
		this.lastReceiverNode = lastReceiver;
	}


	public NodeId getNextReceiverNode() {
		return nextReceiverNode;
	}


	public void setNextReceiverNode(NodeId nextReceiverNode) {
		this.nextReceiverNode = nextReceiverNode;
	}

}


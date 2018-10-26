
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

package br.ufla.dcc.PingPong.XMac;

import br.ufla.dcc.PingPong.XMac.XMacPacket;
import br.ufla.dcc.PingPong.XMac.XMacStateTypes;
import br.ufla.dcc.PingPong.testing.SingleNodeDebugger;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/**
 * Classe que apresenta o status do XMac
 * 
 * 	@author João Giacomin
 *  @author Gustavo Araújo
 *  @version 22/06/2016
 *
 */
public class XMacState extends LayerState {

	/** O estado atual */
	private XMacStateTypes state;
	
	/** Sequência atual de estados */
	private int stateSeqNum = 0;
	
	/** Duração (em steps) prevista para o estado atual */
	private double stateDuration;
	
	/** Indica se há pacote para ser enviado. 
	 * Usado pela XMacStateMachine avisar a XMAC para enviar uma mensagem. */
	private PacketType sendingPktType = null;
	
	/** Pacote de dados que será enviado */
	private XMacPacket dataPkt;
	
	/** Pacote RTS que será enviado */
	private XMacPacket rtsPkt;
	
	/** O endereço do nó para o qual vai a próxima mensagem */
	private NodeId receiverNode;
	
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
    
    /** Se pertence ao Backbone*/
	private boolean backboned;
	
	public SingleNodeDebugger nodeDebugger;
    
	/** Default constructor */
	public XMacState(XMacStateTypes state, int seq) {
		this.state  = state;
		this.stateSeqNum = seq;
	}


	/* Gets e Sets*/
	public XMacStateTypes getState() {
		return state;
	}
	
	
	/** Função padrão para atribuir novo estado a XmacState */
	public boolean setState(XMacStateTypes newState) {   
		// a cada novo estado, incrementa o contador de sequência de estados
		this.state = newState;
		this.stateSeqNum++;           
		return true;
	}

	/** Função alternativa para atribuir novo estado a XmacState, marcando 
	 * o tempo máximo de permanência no estado */
	public boolean setState(XMacStateTypes newState, double delay) {
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

	public XMacPacket getDataPkt() {
		return dataPkt;
	}

	public void setDataPkt(XMacPacket dataPkt) {
		this.dataPkt = dataPkt;
	}
	
	public XMacPacket getRtsPkt() {
		return rtsPkt;
	}

	public void setRtsPkt(XMacPacket rtsPkt) {
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

	public NodeId getReceiverNode() {
		return receiverNode;
	}

	public void setReceiverNode(NodeId receiverNode) {
		this.receiverNode = receiverNode;
	}
	
	public boolean isChannelBusy() {
		return channelBusy;
	}

	public void setChannelBusy(boolean channelBusy) {
		this.channelBusy = channelBusy;
	}
	
	public void setBackboneNodeState(boolean isBackbone) {
		this.backboned = isBackbone;
		nodeDebugger.setBackbone(isBackbone);
	}
	
	public boolean isBackboneNode() {
		return this.backboned;
	}

}


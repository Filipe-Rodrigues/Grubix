
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

package br.ufla.dcc.PingPong.USAMac;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

/**
 * Classe que apresenta o status do XMac
 * 
 * Usada pela XMac.java e XMacStateMachine.java
 * 
 * 	@author João Giacomin
 *  @version 18/03/2019
 *
 */
public class USAMacState extends LayerState {
	
	/** O estado atual */
	private USAMacStateType state;
	
	/** Sequência atual de estados */
	private int stateSeqNum = 0;
	
	/** Duração (em steps) prevista para o estado atual */
	private double stateDuration;
	
	/** Próxima ação a ser executada pela XMac.java  */
	private USAMacActionType action;  
	
	/** Pacote de dados que será enviado */
	private USAMacPacket dataPkt;
	
	/** Pacote RTS que será enviado */
	private USAMacPacket rtsPkt;
	
	/** Pacote recebido na LowerSAP */
	private USAMacPacket recPkt;
	
	/** O endereço do nó para o qual vai a próxima mensagem */
	private NodeId receiverNode;
	
	/** Número de vezes que se tentou o início do processo de envio (CS_START) */
	private int retryCSstart = 1;
    
	/** Informação sobre atividade no canal, se há mensagem em trânsito */
    private boolean channelBusy = true;
    
    /** Indica que há mensagem de dados a ser enviada. Usado quando termina o tempo de Sleep */
    private boolean dataPending;
    
    /** Flag que controla se o nó espera um DATA de um BroadCast */
    private boolean waitingBroadcast;
 
    
	/** Default constructor */
	public USAMacState(USAMacStateType state, int seq) {
		this.state  = state;
		this.stateSeqNum = seq;
	}


	/** Função para atribuir novo estado a XmacState, 
	 *  marcando o tempo máximo de permanência no estado */
	public boolean setState(USAMacStateType newState, double delay) {
		this.stateDuration = delay;
		this.state = newState;
		this.stateSeqNum++;           
		return true;
	}

	
    /** Retorna o tempo corrente em steps de simulação, com 2 casas decimais */
    public double currentStep() {
    	return ((int)(100.0*SimulationManager.getInstance().getCurrentTime())) /100.0;
    }
    
    /** Retorna o tempo corrente em segundos de simulação, com 5 casas decimais */
    public double currentSeconds (){
    	double seconds = Configuration.getInstance().getSeconds(currentStep());
    	return ((int)(100000.0*seconds)) /100000.0;
    }
	
    
	/* 
	 * Gets e Sets
	 */
    
	public USAMacStateType getState() {
		return state;
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

	public USAMacPacket getDataPkt() {
		return dataPkt;
	}

	public void setDataPkt(USAMacPacket dataPkt) {
		this.dataPkt = dataPkt;
	}
	
	public USAMacPacket getRtsPkt() {
		return rtsPkt;
	}

	public void setRtsPkt(USAMacPacket rtsPkt) {
		this.rtsPkt = rtsPkt;
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

    public int decRetryCSstart() {
		return --retryCSstart;
	}

    public int getRetryCSstart() {
		return retryCSstart;
	} 
    
	public void setRetryCSstart(int retry) {
		this.retryCSstart = retry;
	}

	public USAMacPacket getRecPkt() {
		return recPkt;
	}

	public void setRecPkt(USAMacPacket recPkt) {
		this.recPkt = recPkt;
	}

	public USAMacActionType getAction() {
		return action;
	}

	public void setAction(USAMacActionType action) {
		this.action = action;
	}

	public boolean isDataPending() {
		return dataPending;
	}

	public void setDataPending(boolean dataPending) {
		this.dataPending = dataPending;
	}
	
	public boolean isWaitingBroadcast() {
		return waitingBroadcast;
	}


	public void setWaitingBroadcast(boolean waitingBroadcast) {
		this.waitingBroadcast = waitingBroadcast;
	}

	/* 
	 * Enumerations
	 */


	/**
	 * Esse enum implementa os possiveis estados da MAC de um nó sensor, que utiliza X_MAC. 
	 * 
	 * Cada estado tem um tempo de duração previsto. Os tempos são definidos na classe X_MacConstants.
	 * 
	 * Usado pela XMac.java e XMacStateMachine.java 
	 */

	public enum USAMacStateType {
		/** Estado inativo, corresponde ao estado em que o rádio e as principais funções do nó estão desligados */
		SLEEP,
		
		/** Executando uma prospecção de portadora (Carrier Sense) para verificar se algum vizinho está transmitindo */
		CS,
		
		/** Executando uma prospecção de portadora (Carrier Sense) para início de transmissão, para saber se poderá enviar preâmbulos */
		CS_START,
		
		/** Executando uma prospecção de portadora (Carrier Sense) ao final da transmissão, para saber se algum vizinho vai transmitir */
		// Diferente de CS e CS_START, em CS_END, o nó pode receber uma mensagem de DATA sem estar esperando.
		CS_END,
		
		/** MAC está esperando o rádio terminar de enviar uma mensagem RTS */ 
		SENDING_RTS,
		
		/** MAC está esperando o rádio terminar de enviar uma mensagem CTS */ 
		SENDING_CTS,
		
		/** MAC está esperando o rádio terminar de enviar uma mensagem DATA */ 
		SENDING_DATA,
		
		/** MAC está esperando o rádio terminar de enviar uma mensagem ACK */ 
		SENDING_ACK,
		
		/** Esperando um earlyACK após envio de preâmbulo*/
		WAITING_CTS,
		
		/** Esperando um ACK após envio de dados*/
		WAITING_ACK,
		
		/** Esperando terminar a recepção de dados */
		WAITING_DATA,
		
		/** Back Off entre dois CS_START, quando é detectado canal ocupado */
		BO_START,

	}

	/**
	 * Esse enum implementa os possiveis ações que a MAC deve executar. 
	 * 
	 * Usado pela XMacStateMachine.java para indicar ações para a XMac.java
	 */
	public enum USAMacActionType {
		
		/** Não há nada a fazer */
		CONTINUE,
				
		/** Enviar uma mensagem para a Log Link Layer. */
		MSG_UP,
		
		/** Enviar uma mensagem pelo rádio. */
		MSG_DOWN,
		
		/** Iniciar o envio de preâmbulos. */
		START_RTS,
		
		/** Ordenar o rádio para ligar no modo de escuta. */
		TURN_ON,
		
		/** Ordenar o rádio para desligar. */
		TURN_OFF,
		
		/** Ordenar o rádio para desligar e enviar CrossLayerEvent. */
		TURN_OFF_CS_END,
		
		/** Pergutar ao rádio se o canal está ocupado */
		ASK_CHANNEL
	}
	
	/**
	 * Esse enum implementa os possiveis Eventos que a MAC pode receber do SimulationManager. 
	 * 
	 * Cada evento recebido pela XMac.java levará a um chamado à XMacStateMachine.java 
	 * para indicar o próximo estado.
	 */
	public enum USAMacEventType {
		
		/** Time Out */
		TIME_OUT,
		
		/** Recebeu mensagem da Log Link Layer (Upper SAP) */
		LOG_LINK,
		
		/** Recebeu resposta da PHY (Cross Layer Event) indicando canal ocupado */
		CHANNEL_BUSY,
		
		/** Recebeu resposta da PHY (Cross Layer Event) indicando canal livre */
		CHANNEL_FREE,
		
		/** PHY informa que ocorreu uma colisão na recepção de pacotes */ 
		COLLISION,
		
		/** PHY terminou de enviar mensagem (Cross Layer Event) */
		MSG_SENT,
		
		/** Recebeu pacote RTS da PHY (Lower SAP) */
		RTS_RECEIVED,
		
		/** Recebeu pacote CTS da PHY (Lower SAP) */
		CTS_RECEIVED,
		
		/** Recebeu pacote ACK da PHY (Lower SAP) */
		ACK_RECEIVED,
		
		/** Recebeu pacote DATA da PHY (Lower SAP) */
		DATA_RECEIVED, 
		
		/** Start of Frame Delimeter - Sinalizado pelo rádio ao identificar o início de uma mensagem recebida */
		SFD,
		
		/** Evento inútil, vazio, sem efeito */
		VOID
		
	}

	
}


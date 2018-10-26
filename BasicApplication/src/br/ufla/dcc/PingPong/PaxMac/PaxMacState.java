
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

package br.ufla.dcc.PingPong.PaxMac;

import java.util.List;

import br.ufla.dcc.grubix.simulator.node.Node;
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
public class PaxMacState extends LayerState {

	/** Referência para o nó */
	private NodeId nodeId = null;
	
	/** O estado atual */
	private PaxMacStateTypes state;
	
	/** Sequência atual de estados */
	private int stateSeqNum = 0;
	
    /** Informação sobre o tempo em que um nó irá receber ou ter que enviar o CTS-DATA, atualizado */
    private double ctsDataTime;
	
	/** Houve colisão durante a espera pelo CTS. Provavelmente problema do terminal oculto, onde dois nós
	 * respondem um CTS/RTS mas não se ouvem */
	private boolean waitingCtsCollision = false;
	
	/** Se já enviou um ACK ou ACK/DATA para o nó anterior */
	private boolean waitingAck = false;
	
	/** Informação sobre atividade no canal, se há mensagem em trânsito */
    private boolean channelBusy = true;

	/** Lista dos nós vizinhos */
	private List <Node> neighbors;
	
	/** Duração (em steps) prevista para o estado atual */
	private double stateDuration;
	
	/** Tempo que se iniciou o estado atual. Usado para debug */
	private double stateStartTime;
	
	/** Indica se há pacote para ser enviado e que tipo de pacote é esse 
	 * Usado pela XMacStateMachine avisar a XMAC para enviar uma mensagem. */
	private PacketType sendingPktType = null;
	
	/** Pacote de dados que será enviado */
	private PaxMacPacket dataPkt = null;
	
	/** Pacote RTS que será enviado */
	private PaxMacPacket rtsPkt = null;
	
    /** Tamanho do FCS */
    private int fcsSize;
	
	/** O endereço do nó para o qual vai a próxima mensagem (obtido ao receber RTS). Usado para o 
	 * envio de DATA. */
	private NodeId nextReceiverNode;
	
	/** Nó de destino de final. Usado para calcular o o FCS. Obtido ao receber RTS */
	private NodeId finalReceiverNode;
	
	/** Nó do salto anterior. */
	private NodeId previousReceiverLv1;
    
	/** Nó do salto antes do nó anterior. */
	private NodeId previousReceiverLv2;
    
	/** Nó do terceiro salto atrás */
	private NodeId previousReceiverLv3;
	
    /** Quantidade de vezes que o nó enviou uma sequência completa de RTS */
    private int rtsSequenceCount = 0;
    
    /** Quantidade de vezes que o nó esperou o dado que não chegou */
    private int missingDataCount = 0;

    
	/** Default constructor */
	public PaxMacState(NodeId node, PaxMacStateTypes state, int stateSeqNum, List <Node> neighbors) {
		this.nodeId      = node;
		this.state       = state;
		this.stateSeqNum = stateSeqNum;
		this.neighbors   = neighbors;
	}
	
	/** Nó será excluído do caminho voltando para as suas atividades normais */
	public void nodeWillBeFired() {
		waitingCtsCollision = false;
		waitingAck          = false;
		nextReceiverNode    = null;
		finalReceiverNode   = null;
		previousReceiverLv1 = null;
		previousReceiverLv2 = null;
		previousReceiverLv3 = null;
		sendingPktType      = null;
		dataPkt             = null;
		rtsPkt              = null;
		rtsSequenceCount    = 0;
		missingDataCount    = 0;
	}

	/** Se é um nó inicial que vai iniciar o envio do dado pelo caminho */
	public boolean isNodeSource() {
		if (previousReceiverLv1 == nodeId && previousReceiverLv2 == nodeId) {
			return true;
		}
		return false;
	}
	
	/** Se é o nó do próximo salto após o nó que mantém a posse do dado */
	public boolean isNodeNextHopAfterSource() {
		if (previousReceiverLv1 == previousReceiverLv2 && previousReceiverLv1 != nodeId) {
			return true;
		}
		return false;
	}
	
	/** Se é um nó que irá manter a posse do dado e definir quando ele será enviado */
	public boolean isNodeBelongsPath() {
		if (previousReceiverLv1 != null) {
			return true;
		}
		return false;
	}
	
	
	
	/* Gets e Sets*/
	
	/** Função padrão para atribuir novo estado a PaxMacState */
	public boolean setState(PaxMacStateTypes state) {   
		// A cada novo estado, incrementa o contador de sequência de estados
		this.state = state;
		stateSeqNum++;        
		return true;
	}

	/** Função alternativa para atribuir novo estado a XmacState, marcando 
	 * o tempo máximo de permanência no estado */
	public boolean setState(PaxMacStateTypes newState, double stateDuration) {
		this.stateDuration = stateDuration;
		this.stateStartTime = SimulationManager.getInstance().getCurrentTime();
		return setState(newState);
	}
	
	public List <Node> getNeighbors() {
		return this.neighbors;
	}
	
	public PaxMacStateTypes getState() {
		return state;
	}
	
	public int getDataSeqNum() {
		if (dataPkt == null)
			return 0;
		return dataPkt.getSequenceNum();
	}
	
	public int getRtsSeqNum() {
		if (rtsPkt == null)
			return 0;
		return rtsPkt.getSequenceNum();
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

	public PaxMacPacket getDataPkt() {
		return dataPkt;
	}

	public void setDataPkt(PaxMacPacket dataPkt) {
		this.dataPkt = dataPkt;
	}
	
	public PaxMacPacket getRtsPkt() {
		return rtsPkt;
	}

	public void setRtsPkt(PaxMacPacket rtsPkt) {
		this.rtsPkt = rtsPkt;
	}

	public PacketType getSendingPktType() {
		return sendingPktType;
	}
	
	public void setSendingPktType(PacketType sendingPktType) {
		this.sendingPktType = sendingPktType;
	}
	
	public boolean isChannelBusy() {
		return channelBusy;
	}

	public void setChannelBusy(boolean channelBusy) {
		this.channelBusy = channelBusy;
	}
	
	public double getCtsDataTime() {
		return ctsDataTime;
	}

	public void setCtsDataTime(double ctsDataTime) {
		this.ctsDataTime = ctsDataTime;
	}

	public int getFcsSize() {
		return fcsSize;
	}

	public void setFcsSize(int fcsSize) {
		this.fcsSize = fcsSize;
	}

	public double getStateStartTime() {
		return stateStartTime;
	}

	public NodeId getFinalReceiverNode() {
		return finalReceiverNode;
	}

	public void setFinalReceiverNode(NodeId finalReceiverNode) {
		this.finalReceiverNode = finalReceiverNode;
	}

	public NodeId getNextReceiverNode() {
		return nextReceiverNode;
	}

	public void setNextReceiverNode(NodeId receiverNode) {
		this.nextReceiverNode = receiverNode;
	}

	public boolean isWaitingAck() {
		return waitingAck;
	}

	public void setWaitingtAck(boolean sentAck) {
		this.waitingAck = sentAck;
	}

	public boolean isWaitingCtsCollision() {
		return waitingCtsCollision;
	}

	public void setWaitingCtsCollision(boolean waitingCtsCollision) {
		this.waitingCtsCollision = waitingCtsCollision;
	}

	public NodeId getPreviousReceiverLv1() {
		return previousReceiverLv1;
	}

	public void setPreviousReceiverLv1(NodeId previousReceiverLv1) {
		this.previousReceiverLv1 = previousReceiverLv1;
	}
	
	public NodeId getPreviousReceiverLv2() {
		return previousReceiverLv2;
	}

	public void setPreviousReceiverLv2(NodeId previousReceiverLv2) {
		this.previousReceiverLv2 = previousReceiverLv2;
	}

	public NodeId getPreviousReceiverLv3() {
		return previousReceiverLv3;
	}

	public void setPreviousReceiverLv3(NodeId previousReceiverLv3) {
		this.previousReceiverLv3 = previousReceiverLv3;
	}

	public int getRtsSequenceCount() {
		return rtsSequenceCount;
	}

	public int setRtsSequenceCount(int rtsSequenceCount) {
		return this.rtsSequenceCount = rtsSequenceCount;
	}

	public void incRtsSequenceCount() {
		this.rtsSequenceCount++;
	}
	
	public int getMissingDataCount() {
		return missingDataCount;
	}

	public void setMissingDataCount(int missingDataCount) {
		this.missingDataCount = missingDataCount;
	}

	public void incMissingDataCount() {
		this.missingDataCount++;
	}

	/** Se existe um vizinho com um id específico */
	public boolean isNeighborExist(int id) {
		for (Node n : getNeighbors()) {
			if (n.getId().asInt() == id) {
				return true;
			}
		}
		return false;
	}
	
	/** Se o nó alcança dois nós atrás no caminho */
	public boolean isReachTwoNodePath() {
		return (getPreviousReceiverLv2() != null && !isNodeSource() && !isNodeNextHopAfterSource() &&
				isNeighborExist(getPreviousReceiverLv2().asInt()));
	}
	
	/** Se o nó alcança três nós atrás no caminho */
	public boolean isReachThreeNodePath() {
		// Se o terceiro nó é igual ao segundo, então alcança o nó de origem e não há terceiro nó
		if (getPreviousReceiverLv3() == getPreviousReceiverLv2()) {
			return false;
		} else {
			return (getPreviousReceiverLv3() != null && !isNodeSource() && !isNodeNextHopAfterSource() && 
					isNeighborExist(getPreviousReceiverLv3().asInt()));
		}
	}
	
	/** Se o nó acordou para enviar o CTS-DATA */
	public boolean isWakeUpToSendCtsData() {
		return isNodeBelongsPath() && getSendingPktType() == null && !isWaitingAck();
	}
	
	/** Função para obter o tempo corrente da simulação em steps */
    protected double currentTimeSteps() {
        return SimulationManager.getInstance().getCurrentTime();
    }
    
    /** Função para obter o tempo restante da simulação em steps */
    protected double remainTimeSteps() {
        return Configuration.getInstance().getSimulationTime()-currentTimeSteps();
    }
	
}


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

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;

/**
 * Classe que implementa o pacote da camada MAC para o protocolo X-MAC. 
 * 
 *  @author Gustavo Araújo
 *  @author João Giacomin
 *  @version 22/06/2016
 */
public class PaxMacPacket extends MACPacket { 
				
	
	/** Número de sequência do pacote que será enviado */ 
	private int sequenceNumber = 0;
	
	/** Nós que fazem parte do FCS. Usado no RTS */
	List <NodeId> fcsNodes;
	
	/** Nó de destino final, usando no RTS para que o próximo nó possa calcular seu FCS */
	NodeId finalReceiverNode;
	
	/** Nó do salto imediatamente anterior ao sender */
	NodeId previousReceiverNodeLv1;
	
	/** Nó do segundo salto anterior ao sender */
	NodeId previousReceiverNodeLv2;
	
	/** Nó do terceiro salto anterior ao sender */
	NodeId previousReceiverNodeLv3;
	
	/** Tempo em que o nó anterior irá enviar ou receber o CTS-DATA */
	double sendCtsDataDelay;
	
	
	/**
	 * Default constructor of class Packet to create a terminal packet
	 * with no enclosed packet.
	 * @param sender Sender address of the packet
	 * @param receiver Id of the packet's receiver
	 * @param signalStrength The strength of the signal to transmit in mW
	 */
	public PaxMacPacket(Address sender, NodeId receiver, double signalStrength, PacketType pkType) {
		// Usado para criar pacotes do tipo CTS e ACK
		super(sender, receiver, signalStrength);
		setType(pkType);
	}
	
	
	/** Cria pacotes do tipo RTS */
	public PaxMacPacket(Address sender, List <NodeId> fcs, double dataTime, double signalStrength,
			int sequence) {
		/* Usado para criar pacotes do tipo RTS. Para o Shox o pacote de Packet é um envio broadcast, mas 
		 apenas os nós que compõem o FCS poderão responder */
		super(sender, NodeId.ALLNODES, signalStrength);
		setType(PacketType.RTS);
		this.fcsNodes = fcs;
		this.sendCtsDataDelay = dataTime;
		this.sequenceNumber = sequence;
	}
	
	
	/** Cria pacotes do tipo DATA */
	public PaxMacPacket(Address sender, LogLinkPacket packet, int sequence) {
		super(sender, packet);
		setType(PacketType.DATA);
		this.sequenceNumber = sequence;
	}
	

	/** Decrementa o contador de tentativas de envio de pacote existente na superclasse MACPacket.java */
	public void decRetryCount() {
		int cont = getRetryCount(); 
		if (cont > 0) cont--;
		setRetryCount(cont);
	}
	
	/** Decrementa o contador de sequência do pacote */
	public int decSequenceNum() {
		int cont = getSequenceNum(); 
		if (cont > 0) cont--;
		setSequenceNum(cont);
		return cont;
	}
	
	/* Sets e Gets */
	public int getSequenceNum() {
		return sequenceNumber;
	}

	public void setSequenceNum(int num) {
		this.sequenceNumber = num & PaxMacConstants.MAX_SEQUENCE;
	}

	public List<NodeId> getFcsNodes() {
		return fcsNodes;
	}

	public void setFcsNodes(List<NodeId> fcsNodes) {
		this.fcsNodes = fcsNodes;
	}

	public double getSendCtsDataDelay() {
		return sendCtsDataDelay;
	}

	public void setSendCtsDataDelay(double ctsDataTime) {
		this.sendCtsDataDelay = ctsDataTime;
	}

	public NodeId getFinalReceiverNode() {
		return finalReceiverNode;
	}

	public void setFinalReceiverNode(NodeId finalReceiverNode) {
		this.finalReceiverNode = finalReceiverNode;
	}

	public NodeId getPreviousReceiverNodeLv1() {
		return previousReceiverNodeLv1;
	}

	public void setPreviousReceiverNodeLv1(NodeId previousReceiverNodeLv1) {
		this.previousReceiverNodeLv1 = previousReceiverNodeLv1;
	}
	
	public NodeId getPreviousReceiverNodeLv2() {
		return previousReceiverNodeLv2;
	}

	public void setPreviousReceiverNodeLv2(NodeId previousReceiverNodeLv2) {
		this.previousReceiverNodeLv2 = previousReceiverNodeLv2;
	}

	public NodeId getPreviousReceiverNodeLv3() {
		return previousReceiverNodeLv3;
	}

	public void setPreviousReceiverNodeLv3(NodeId previousReceiverNodeLv3) {
		this.previousReceiverNodeLv3 = previousReceiverNodeLv3;
	}
}

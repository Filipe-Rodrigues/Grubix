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

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;

/**
 * Classe que implementa o pacote da camada MAC para o protocolo X-MAC. 
 * 
 *  @author Gustavo Araújo
 *  @author João Giacomin
 *  @version 04/07/2016
 */
public class AgaMacPacket extends MACPacket { 

	/** Número de sequência do pacote que será enviado */ 
	private int sequenceNumber = 0;
	
	/** +AGAMac Limiar que está sendo usado **/
	private double currentThreshold = 0;
	
	/** +AGAMac Saltos realizados **/
	private int hops = 0;
	
	/** +AGAMac Quem é o destino final da mensagem */
	private NodeId lastReceiver;
	
	/**
	 * Default constructor of class Packet to create a terminal packet
	 * with no enclosed packet.
	 * @param sender Sender address of the packet
	 * @param receiver Id of the packet's receiver
	 * @param signalStrength The strength of the signal to transmit in mW
	 */
	public AgaMacPacket(Address sender, NodeId receiver, double signalStrength, PacketType pkType) {
		super(sender, receiver, signalStrength);
		// Usado para criar pacotes do tipo RTS, CTS e ACK
		setType(pkType);
	}
	
	
	/**
	 * Overloaded constructor to create non-terminal packets by
	 * specifying a packet to enclose.
	 * ReceiverID of the new packet taken from enclosedPacket.
	 * 
	 * @param sender Sender address of the packet
	 * @param packet The packet to enclose inside the new packet.
	 * @param sequence The sequence number of the packet
	 */
	public AgaMacPacket(Address sender, LogLinkPacket packet, int sequence) {
		// Usado para criar apenas pacotes do tipo DATA
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
	public int decSeqNum() {
		int cont = getSequenceNumber(); 
		if (cont > 0) cont--;
		setSequenceNumber(cont);
		return cont;
	}
	
	
	/* Sets e Gets */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int num) {
		this.sequenceNumber = num & AgaMacConstants.MAX_SEQUENCE;
	}


	public double getCurrentThreshold() {
		return currentThreshold;
	}


	public void setCurrentThreshold(double currentThreshold) {
		this.currentThreshold = currentThreshold;
	}


	public int getHops() {
		return hops;
	}


	public void setHops(int hops) {
		this.hops = hops;
	}
	
	
	public NodeId getLastReceiver() {
		return lastReceiver;
	}


	public void setLastReceiver(NodeId lastReceiver) {
		this.lastReceiver = lastReceiver;
	}

}

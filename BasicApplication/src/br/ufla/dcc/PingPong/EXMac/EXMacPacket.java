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

package br.ufla.dcc.PingPong.EXMac;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;

/**
 * Classe que implementa o pacote da camada MAC para o protocolo X-MAC. 
 * 
 *  @author João Giacomin
 *  @version 18/03/2019
 */
public class EXMacPacket extends MACPacket { 
				
	
	/**
	 * Default constructor of class XMacPacket to create a terminal packet
	 * with no enclosed packet.
	 * @param sender Sender address of the packet
	 * @param receiver Id of the packet's receiver
	 * @param signalStrength The strength of the signal to transmit in mW
	 * @param pkType The type of MACPacket (RTS, CTS, ACK)
	 */
	public EXMacPacket(Address sender, NodeId receiver, PacketType pkType, double signalStrength) {
		// Usado para criar pacotes do tipo RTS, CTS e ACK
		super(sender, receiver, signalStrength);
		setType(pkType);
	}
	
	
	/**
	 * Overloaded constructor to create non-terminal packets by
	 * specifying a packet to enclose.
	 * ReceiverID of the new packet taken from enclosedPacket.
	 * 
	 * @param sender Sender address of the packet
	 * @param packet The packet to enclose inside the new packet.
	 */
	public EXMacPacket(Address sender, LogLinkPacket packet, boolean ackReq) {
		// Usado para criar apenas pacotes do tipo DATA
		super(sender, packet);
		setType(PacketType.DATA);
		setAckRequested(ackReq);
	}
	

	/** Decrementa o contador de tentativas de envio de pacote existente na superclasse MACPacket.java */
	public int decRetryCount() {
		int cont = getRetryCount(); 
		if (cont > 0) cont--;
		setRetryCount(cont);
		return cont;
	}
	
}

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
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;

/**
 * Classe que implementa o observador do funcionamento da camada MAC, usando o X-MAC. 
 * 
 *  @author João Giacomin
 *  @version 18/03/2019
 */
public class USAMacOnisciente  { 
				
	
	/** Observador da simulação */ 
	private static USAMacOnisciente instance = null;
	private boolean debug = false;
	private NodeId senderId;
	private NodeId receiverId;
	private PacketType pkType;
	private double stepsFimSender;
	private double stepsFimReceiver;
	
	private boolean quit;
	
	/**
	 * Default constructor of class Packet to create a terminal packet
	 * with no enclosed packet.
	 * @param sender Sender address of the packet
	 * @param receiver Id of the packet's receiver
	 * @param signalStrength The strength of the signal to transmit in mW
	 */
	public USAMacOnisciente() {
				
	}
	
	public static USAMacOnisciente getInstance() {
		if (instance == null){
			instance = new USAMacOnisciente();
		}
			
		return instance;
		
	}

	public NodeId getSenderId() {
		return senderId;
	}

	public void setSenderId(NodeId senderId) {
		this.senderId = senderId;
	}

	public NodeId getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(NodeId receiverId) {
		this.receiverId = receiverId;
	}

	public PacketType getPkType() {
		return pkType;
	}

	public void setPkType(PacketType pkType) {
		this.pkType = pkType;
	}

	public double getStepsFimSender() {
		return stepsFimSender;
	}

	public void setStepsFimSender(double stepsFimSender) {
		this.stepsFimSender = stepsFimSender;
	}

	public void printStepsFimSender() {
		if (debug){
			System.out.println(" Sender = " + senderId + ": StepsFim =   " + stepsFimSender );
		}
	} 
	
	public double getStepsFimReceiver() {
		return stepsFimReceiver;
	}

	public void setStepsFimReceiver(double stepsFimReceiver) {
		this.stepsFimReceiver = stepsFimReceiver;
	}

	public void printStepsFimReceiver() {
		if (debug){
		    System.out.println(" Receiver = " + receiverId + ": StepsFim = " + stepsFimReceiver );
		}
	}

	public boolean isQuit() {
		return quit;
	}

	public void setQuit(boolean quit) {
		this.quit = quit;
	}

}

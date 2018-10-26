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

package br.ufla.dcc.PingPong.node.user;



import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Moved;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;


/** 
 * A simple application used for testing.
 * Just pingpongs some stringmessages.
 * 
 * @author Andreas Kumlehn
 */
public class AppPingPong extends ApplicationLayer {

	/** Counts the overall sent packets. */
	private static int overallSentPackets = 0;
	
	/** Counts the overall received packets. */
	private static int overallReceivedPackets = 0;
	
	/** The number of received packets on this node. */
	private int receivedPackets = 0;
	
	/**
	 * Logger of the class AppHelloWorld.
	 */
	private static final Logger LOGGER = Logger.getLogger(AppPingPong.class.getName());

	/** 
	 * Sample statistic that stores the current number of messages that this node has
	 * already received.
	 */
	private long numMessages = 0;
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#lowerSAP(br.ufla.dcc.PingPong.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	public final void lowerSAP(Packet packet) {
		if (packet instanceof ApplicationPacket) {   // ve se eh
			ApplicationPacket hw = (ApplicationPacket) packet; // casting
			overallReceivedPackets++;
			receivedPackets++;
			
			if (hw.getSender().getId() != id) {
				//message from other node, answer!
				this.numMessages++;
				String value = Long.toString(this.numMessages);
				SimulationManager.logStatistic(id, LayerType.APPLICATION, "time", "Number of Messages", 
						                                     Double.toString(hw.getTime()), value);
				String newmsg = hw.getHeader();
				int point = newmsg.indexOf(".");
				Integer number = Integer.parseInt(newmsg.substring(0, point));
				number += 1;
				newmsg = number + "." + newmsg.substring(point + 1, newmsg.length()) + " - Answer " + id;
				
				ApplicationPacket newhw = new ApplicationPacket(sender, hw.getSender().getId());
				newhw.setHeader(newmsg);
				sendPacket(newhw);
				overallSentPackets++;
				LOGGER.info("Node " + id + " answered to HelloWorldPacket: " + hw.getHeader());
			}
		} else {
			LOGGER.warn("Node " + id + " AppHelloWorld " + "did not handle packet TYPE "
					+ packet.getClass().getName() + "!");
		}
	}
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#processEvent(br.ufla.dcc.PingPong.event.Initialize)
	 * @param init Initialize event to start up the layer.
	 */
	@Override
	protected final void processEvent(Initialize init) {
		String value = Long.toString(this.numMessages);
		SimulationManager.logStatistic(id, LayerType.APPLICATION, "time", "Number of Messages", value, value);
	}
	
	/**
	 * Method to start this layer.
	 * @param start StartSimulation event to start the layer.
	 */
	protected void processEvent(StartSimulation start) {		
		if (id.asInt() % 2 != 0) {
			Packet hw = new ApplicationPacket(sender, NodeId.ALLNODES);
			hw.setHeader("1. Creator " + id);
			
			sendPacket(hw);
			overallSentPackets++;
			LOGGER.debug("Node " + id + " created HelloWorldPacket.");
		}
	}
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#processEvent(br.ufla.dcc.PingPong.event.Moved)
	 * @param moved object containing new position
	 */
	public final void processEvent(Moved moved) {
		//empty
	}

	/**
	 * Requests this application to generate application-level traffic according to the parameters
	 * specified in <code>tg</code>.
	 * @param tg Traffic generation request with all necessary details
	 */
	public final void processEvent(TrafficGeneration tg) {
		// TODO maybe adapt lowerSAP and upperSAP to use this method instead
	}
	
	/**
	 * This application supports only one type of packets, namely ApplicationPacket packets.
	 * @return 1
	 */
	public final int getPacketTypeCount() {
		return 1;
	}

	/**
	 * currently no states needed, thus no statechanges possible.
	 * @return nothing
	 */
	public LayerState getState() {
		// TODO generate ApplicationState class from LayerState, if necessary.
		return null;
	}

	/**
	 * currently no states needed, thus no statechanges possible.
	 * @param state the new desired state of this layer.
	 * @return true if the statechange was accepted.
	 */
	public boolean setState(LayerState state) {
		// TODO implement the needed state-changes for the former LayerState subclass.
		return true;
	}
	
	/**
	 * Method to finalize this layer.
	 * @param end Finalize event to terminate the layer.
	 */
	@Override
	protected final void processEvent(Finalize end) {
		LOGGER.info("Node " + getId() +": # of received packets:" + receivedPackets);
		LOGGER.info("Node " + getId() +": overallReceivedPackets=" + overallReceivedPackets + " overallSentPackets=" 
				+ overallSentPackets);
	}

}

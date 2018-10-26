package br.ufla.dcc.PingPong.node.user;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.ApplicationState;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.AppState;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Moved;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;



/** 
 * A simple application used for testing of optimal source routing.
 * Use odd number of nodes for roundtrip hopping mode and even
 * for random bidirectional alternating mode.
 * 
 * @author Dirk Held
 */
public class AppOSRtest extends ApplicationLayer {
	
	/** just 4 fun. */
	private static final int TARZAN = 1;
	/** the number of nodes used for this test. */
	private int nodeCount;
	/** chose the mode for the test application. */
	private int mode; 
	
	/** the node number of this node and it's partner. */
	private int me, other;
	
	/** constructor with a special NoParameter param object. */
	public AppOSRtest() {
	}
	
	/** Logger of the class AppOSRtest. */
	private static final Logger LOGGER = Logger.getLogger(AppOSRtest.class.getName());
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#processEvent(br.ufla.dcc.PingPong.event.Initialize)
	 * @param init Initialize event to start up the layer.
	 */
	@Override
	protected final void processEvent(Initialize init) {
		nodeCount = init.getNodeCount();
		mode      = 1;
		me    = id.asInt();
		other = me + 1;
		
		if (other == nodeCount + 1) {
			other = 1;
		}
		
		if ((nodeCount & 1) == 1) {
			mode = 1;
		}
	}
	
	/**
	 * internal helper to simply send a packet to a given node.
	 * 
	 * @param nr the number of the node to send to, use -1 for broadcast.
	 */
	private void sendTo(int nr) {
		NodeId dstNode;
		
		if (nr == -1) {
			dstNode = NodeId.ALLNODES;
		} else {
			dstNode = NodeId.get(nr);
		}
		
		appState = ApplicationState.RUNNING;
		Packet hw = new ApplicationPacket(sender, dstNode);
		hw.setHeaderLength(1024);
		
		LOGGER.debug("Node " + id + " created HelloWorldPacket");
		sendPacket(hw);
	}
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#lowerSAP(br.ufla.dcc.PingPong.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	public final void lowerSAP(Packet packet) {
		//int inCount = getNode().getPacketCount(true, LayerType.APPLICATION);
		boolean forThisNode = (packet.getReceiver().asInt() == id.asInt());
		
		if (forThisNode && (packet instanceof ApplicationPacket)) {
			ApplicationPacket hw = (ApplicationPacket) packet;
			LOGGER.debug("Node " + id + " received HelloWorldPacket from " + hw.getSender());
			
			if (mode == 1) {
				if (me != TARZAN) {
					sendTo(other);
					LOGGER.info("Node " + id + " now sends to node " + other);
				}
				
				appState = ApplicationState.DONE;
				
			} else if (mode == 2) {
				// ignore
			}
		} else {
			LOGGER.warn("Node " + id + " AppOSRtest "
						+ "did not handle packet TYPE " + packet.getClass().getName() + "!");
		}
	}
	
	/**
	 * method to process the wakeup calls for this application.
	 * 
	 * @param wuc the to be processed wakeup call.
	 */
	public void processWakeUpCall(WakeUpCall wuc) {
		if (mode == 1) {
		} else if (mode == 2) {
			sendTo(other);
		}
	}
	
	/**
	 * method to process the startsimulation event.
	 * 
	 * @param start the to be processed event.
	 */
	@Override
	protected void processEvent(StartSimulation start) {
		if (mode == 1) {
			if (me == TARZAN) { // start the round robbin'
				sendTo(other);
			}
		} else if (mode == 2) {
			/*
			if (me == TARZAN) {
				sendTo(2);
			} else {
				WakeUpCall wuc = new AppEvent(sender, t);
				internalSendEvent(wuc);
			}
			*/
		}
	}

	/**
	 * Method to finalize this layer.
	 * @param end Finalize event to terminate the layer.
	 */
	@Override
	protected final void processEvent(Finalize end) {
		if (!appState.isCompleted()) {
			appState = ApplicationState.FAILED;
		}
	}
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#processEvent(br.ufla.dcc.PingPong.event.Moved)
	 * @param moved object containing new position
	 */
	public final void processEvent(Moved moved) {
	}

	/**
	 * Requests this application to generate application-level traffic according to the paramters
	 * specified in <code>tg</code>.
	 * @param tg Traffic generation request with all necessary details
	 */
	public final void processEvent(TrafficGeneration tg) {
	}
	
	/**
	 * This application supports only one type of packets, namely ApplicationPacket packets.
	 * @return 1
	 */
	public final int getPacketTypeCount() {
		return 1;
	}

	/** @return the current application state. */
	public LayerState getState() {
		return new AppState(appState);
	}
	
	/**
	 * accept the new state, if it is greater than the present state.
	 * @param state the new desired state of this layer.
	 * @return true if statechange was accepted.
	 */
	public boolean setState(LayerState state) {
		AppState as;

		if (state instanceof AppState) {
			as = (AppState) state;
			appState = ApplicationState.higherState(appState, as.getAppState());
			return true;
		}
		return false;
	}
}

package br.ufla.dcc.grubix.simulator.node.user;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.AppState;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.ApplicationState;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;



/** 
 * A simple application which generates one single packet from a randomly chosen source to a
 * randomly chosen destination node.
 * @author jlsx
 */
public class AppSinglePacket extends ApplicationLayer {
	
	/** the number of nodes used for this test. */
	private int nodeCount;
	
	/** Node IDs of source and destination node. */
	private static int source, destination;
	
	/** constructor with a special NoParameter param object. */
	public AppSinglePacket() {
		source = -1;
		destination = -1;
	}
	
	/** Logger of the class AppOSRtest. */
	private static final Logger LOGGER = Logger.getLogger(AppOSRtest.class.getName());
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#processEvent(br.ufla.dcc.grubix.simulator.event.Initialize)
	 * @param init Initialize event to start up the layer.
	 */
	@Override
	protected final void processEvent(Initialize init) {
		nodeCount = init.getNodeCount();
		if (source == -1) {
			//Random rnd = new Random();
			RandomGenerator rnd = getConfig().getRandomGenerator();
			source = rnd.nextInt(nodeCount);
			destination = rnd.nextInt(nodeCount);
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
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	public final void lowerSAP(Packet packet) {
		boolean forThisNode = (packet.getReceiver().asInt() == id.asInt());
		
		if (forThisNode && (packet instanceof ApplicationPacket)) {
			ApplicationPacket hw = (ApplicationPacket) packet;
			LOGGER.info("Node " + id + " received ApplicationPacket from " + hw.getSender());
			
		} else {
			LOGGER.warn("Node " + id + this.getClass().getSimpleName()
						+ "did not handle packet TYPE " + packet.getClass().getName() + "!");
		}
	}
		
	/**
	 * Method to process the StartSimulation event.
	 * @param start the event to be processed.
	 */
	@Override
	protected void processEvent(StartSimulation start) {
		if (source == this.getId().asInt()) {
			this.sendTo(destination);
		}
		appState = ApplicationState.DONE;
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
	 * Requests this application to generate application-level traffic according to the parameters
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

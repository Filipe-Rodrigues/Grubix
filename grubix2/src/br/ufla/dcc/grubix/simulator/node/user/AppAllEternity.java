package br.ufla.dcc.grubix.simulator.node.user;



import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.AppEvent;
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
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.ApplicationState;


/** 
 * 
 * 
 * @author Sascha Lutters
 */
public class AppAllEternity extends ApplicationLayer {
	
	/** the number of nodes used for this test. */
	private int nodeCount;
	
	private int receiver;
	
	private int passes;
	private int maxPasses;
	
	/** constructor with a special NoParameter param object. */
	public AppAllEternity() {
	}
	
	/** Logger of the class AppABCtest. */
	private static final Logger LOGGER = Logger.getLogger(AppAllEternity.class.getName());
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#lowerSAP(br.ufla.dcc.grubix.simulator.event.Packet)
	 * @param packet to process coming from lower layer.
	 */
	public final void lowerSAP(Packet packet) {
		if (packet instanceof ApplicationPacket) {
			ApplicationPacket hw = (ApplicationPacket) packet;
			
			LOGGER.debug("Node " + id + " received HelloWorldPacket from." + hw.getSender().getId());
			
			appState = ApplicationState.DONE;
		} else {
			LOGGER.warn("Node " + id + " AppABCtest "
						+ "did not handle packet TYPE " + packet.getClass().getName() + "!");
		}
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#processEvent(br.ufla.dcc.grubix.simulator.event.Initialize)
	 * @param init Initialize event to start up the layer.
	 */
	@Override
	protected final void processEvent(Initialize init) {
		nodeCount = init.getNodeCount();
		receiver = 1;
		passes = 0;
		maxPasses = 1;
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
		
		sendPacket(hw);
		LOGGER.debug("Node " + id + " created HelloWorldPacket.");
	}
	
	/**
	 * method to process the wakeup calls for this application.
	 * 
	 * @param wuc the to be processed wakeup call.
	 */
	public void processWakeUpCall(WakeUpCall wuc) {
		int me = id.asInt();
		double t = 100000.0 * getRandom().nextDouble();  // half propagation delay
		
		if (receiver == me) {
			receiver++;
		}
		
		if (receiver > nodeCount) {
			receiver = 1;
			passes++;
		}
		
		if (!(passes >= maxPasses)) {
			sendTo(receiver);
			WakeUpCall newuc = new AppEvent(sender, t);
			sendEventSelf(newuc);
			receiver++;	
		}
	}
	

	@Override
	protected void processEvent(StartSimulation start) {
		int me = id.asInt();
		double t = 100000.0;  // half propagation delay

		WakeUpCall wuc = new AppEvent(sender, t);
		sendEventSelf(wuc);
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
	 * @see br.ufla.dcc.grubix.simulator.node.Layer#processEvent(br.ufla.dcc.grubix.simulator.event.Moved)
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

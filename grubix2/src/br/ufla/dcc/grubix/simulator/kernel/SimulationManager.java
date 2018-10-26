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

package br.ufla.dcc.grubix.simulator.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;


import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import br.ufla.dcc.grubix.debug.compactlogging.CompactFileLogger;
import br.ufla.dcc.grubix.debug.compactlogging.ShoxLogger;
import br.ufla.dcc.grubix.debug.logging.LogFilter;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.DeviceType;
import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.TimedObject;
import br.ufla.dcc.grubix.simulator.event.AppState;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.Interference;
import br.ufla.dcc.grubix.simulator.event.Loggable;
import br.ufla.dcc.grubix.simulator.event.LoggableData;
import br.ufla.dcc.grubix.simulator.event.MoveManagerEvent;
import br.ufla.dcc.grubix.simulator.event.Moved;
import br.ufla.dcc.grubix.simulator.event.Movement;
import br.ufla.dcc.grubix.simulator.event.NodeStartupManagerEvent;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.SimulationState;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.ToDevice;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneratorEvent;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.movement.NoMovement;
import br.ufla.dcc.grubix.simulator.node.ApplicationState;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.simulator.node.PhysicalLayer;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.nodestartup.NodeStartupManager;
import br.ufla.dcc.grubix.simulator.physical.PhysicalModel;
import br.ufla.dcc.grubix.simulator.physical.Reachability;
import br.ufla.dcc.grubix.xml.XMLTags;


/** 
 * Central class for the whole SIMULATION framework.
 * The manager knows all GLOBAL objects of the SIMULATION (e.g. Topology)
 * and controls the whole flow.
 * Eventhandling for the SIMULATION is done here.
 * 
 * @author Andreas Kumlehn
 */
public class SimulationManager {

	/** Logger for the class SimulationManager. */
	private static final Logger LOGGER = Logger.getLogger(SimulationManager.class.getName());

	/**
	 * Logger for all history related entries.
	 * Logs all events when enqueued/dequeued into/from queue to an XML file for later replay.
	 */
	private ShoxLogger history;

	/** The thread which executes this simulation manager within the ShoX GUI. */
	private SimulationRunner runner;

	/** Singleton to allow nodes to enqueue and transmit packets. */
	private static SimulationManager instance = null;

	/** Boolean whether simulation is running. */
	private boolean running = true;

	/** The current time of the simulation. */
	private double currentTime = 0.0;

	/** The writer for the statistics file. */
	private ShoxLogger statisticsWriter;

	/** A map containing all nodes of the simulation. */
	private final SortedMap<NodeId, Node> allNodes;

	/** Oracle used for shortest path retrieval. */
	private OSROracle osrOracle;

	/** The global Eventqueue to manage all events. */
	private final EventEnvelopeQueue queue;

	/**
	 * Constructor of the class SimulationManager.
	 * 
	 * @param history XMLWriter for logging the SIMULATION. Null if logging disabled.
	 * @param allNodes A map containg all nodes of the SIMULATION.
	 */
	public SimulationManager(ShoxLogger history, SortedMap<NodeId, Node> allNodes) {
		this.history = history;
		this.statisticsWriter = history;

		this.allNodes = allNodes;
		this.queue = new EventEnvelopeQueue(history);
		//Singleton pattern to ensure uniqeness
		if (SimulationManager.instance == null) {
			SimulationManager.instance = this;
		}
	}

	/** @return the one and only instance of the SimulationManager.	*/
	public static SimulationManager getInstance() {
		return instance;
	}

	/**
	 * If the simulation manager is executed from within the GUI, a reference to the
	 * thread which executes the manager is given here.
	 * @param runner The thread which executes this simulation manager within the ShoX GUI
	 */
	public void setRunner(SimulationRunner runner) {
		this.runner = runner;
	}

	/** Method to start the SIMULATION after all stuff is set up. Proceeds the events. */
	public final void runSimulation() {
		final Configuration configuration = Configuration.getInstance();

		for (Node node : allNodes.values()) {
			// precomputation of the neighbourhood only makes sense 
			// in case of constant signal strength.
			if (!configuration.isSuppressNeighborhoodDetection()) {
				detectNeighbors(node);				
			}

			Initialize init = new Initialize(node.getId(), 1.0, allNodes.size());
			SimulationStateEnvelope initenv = new SimulationStateEnvelope(init);
			queue.add(initenv);
		}

		osrOracle = null;
		
		NodeStartupManager nodeStartupManager;
		nodeStartupManager = configuration.getNodeStartupManager();
		nodeStartupManager.setAvailableNodes(allNodes.keySet());
		
		// NodeStartupManagers relay on the setting of delay of 1.0
		NodeStartupManagerEvent nodeStartup = new NodeStartupManagerEvent(1.0);
		NodeStartupManagerEnvelope nodeStartupEnv = new NodeStartupManagerEnvelope(nodeStartup);
		queue.add(nodeStartupEnv);
				
		double t = configuration.getMovementManager().getInitialDelay();
		
		t = Math.max(2.0, configuration.getSimulationSteps(t)); 
		
		MoveManagerEvent move = new MoveManagerEvent(t);
		MoveManaEnvelope env  = new MoveManaEnvelope(move);
		queue.add(env);

		if (configuration.getTrafficGenerator() != null) {
			TrafficGeneratorEvent tge = new TrafficGeneratorEvent(2.0);
			TrafficGenEnvelope tgenv = new TrafficGenEnvelope(tge);
			queue.add(tgenv);
		}
		
		double lastTime = this.currentTime;

		while (this.running) {
			EventEnvelope envelope = queue.poll();
			if (envelope == null) {
				LOGGER.warn("Eventqueue is empty! SimulationManager quits!");
				this.endSimulation();
				return;
			}

			if ((this.currentTime > lastTime) && (this.runner != null)) {
				// The simulation is executed in a GUI thread, so we update the progress bar
				lastTime = this.currentTime;
				int value = Math.round((float) (this.currentTime * 100 / configuration
						.getSimulationTime()));
				this.runner.setProgress(value);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing event: " + envelope.toString());
			}

			logDequeue(envelope);
			
			if (envelope.getTime() < this.currentTime) {
				throw new InvalidEnvelopeTimeException(
						"envelope.getTime() returned " + envelope.getTime()
								+ " but the current time was already "
								+ this.currentTime);
			}
			
			this.currentTime = envelope.getTime();
			
			if (this.currentTime > configuration.getSimulationTime()) {
				LOGGER.info("Timelimit reached. SimulationManager quits!");
				this.endSimulation();
				return;
			}
			
			MDC.put("time", this.currentTime);
			
			envelope.deliver();
		}
		LOGGER.info("Running boolean false. SimulationManager quits!");
		this.endSimulation();
	}

	/**
	 * Log the dequeue of an event envelope from the simulation queue.
	 * @param envelope The envelope which is dequeued and to be logged
	 */
	public static void logDequeue(EventEnvelope envelope) {
		Configuration configuration = Configuration.getInstance();
		
		if (configuration.isLogging() && envelope instanceof Loggable) {
			int prio = configuration.getLiveLogFilter().getEventTypePriority(envelope.getEvent().getClass().getName());

			if (prio != LogFilter.PRIORITY_OFF) {
				instance.history.logDequeueEvent(envelope.getTime(), envelope.getEvent().getId());
			}
		}
	}

	/** Invoked internally at the end of the simulation so nodes can perform some final cleanup. */
	private void endSimulation() {
		Configuration configuration = Configuration.getInstance();
		Map<ApplicationState, Integer> result = new LinkedHashMap<ApplicationState, Integer>();
		for (ApplicationState state : ApplicationState.values()) {
			result.put(state, 0);
		}
		Map<LayerType, Integer> in = new LinkedHashMap<LayerType, Integer>();
		Map<LayerType, Integer> out = new LinkedHashMap<LayerType, Integer>();
		for (LayerType layer : LayerType.values()) {
			in.put(layer, 0);
			out.put(layer, 0);
		}

		AppState as;
		boolean gotStates = false;

		for (Node node : allNodes.values()) {
			Finalize end = new Finalize(node.getId(), 0.0);
			SimulationStateEnvelope endEnv = new SimulationStateEnvelope(end);
			endEnv.deliver();

			as = (AppState) node.getLayerState(LayerType.APPLICATION);

			if (as != null) {
				ApplicationState state = as.getAppState();
				int count = result.get(state);
				count++;
				result.put(state, count);
				gotStates = true;
			}

			for (LayerType layer : LayerType.values()) {
				int inCount = in.get(layer);
				inCount += node.getPacketCount(true, layer);
				in.put(layer, inCount);
				
				int outCount = out.get(layer);
				outCount += node.getPacketCount(false, layer);
				out.put(layer, outCount);
			}
		}
		StringBuilder titleLine = new StringBuilder("packet counts: ");
		StringBuilder outLine = new StringBuilder("out: ");
		StringBuilder inLine = new StringBuilder("in: ");
		for (LayerType layer : LayerType.values()) {
			titleLine.append(layer.getShortName()).append(",");
			outLine.append(out.get(layer)).append(",");
			inLine.append(in.get(layer)).append(",");
		}

		String titleLineRes = titleLine.substring(0, titleLine.length() - 1);
		String outLineRes = outLine.substring(0, outLine.length() - 1);
		String inLineRes = inLine.substring(0, inLine.length() - 1);
		LOGGER.info(titleLineRes);
		LOGGER.info(outLineRes);
		LOGGER.info(inLineRes);

		if (gotStates) {
			for (ApplicationState state : ApplicationState.values()) {
				int stateResultCount = result.get(state);
				if (stateResultCount > 0) {
					LOGGER.info(state + ": " + stateResultCount + " of " + allNodes.size());
				}
			}
		}

		if (configuration.getTrafficGenerator() != null) {
			configuration.getTrafficGenerator().endSimulation();
		}
	}

	/**
	 * Can be called to abort the current simulation.
	 */
	public void cancelSimulation() {
		this.running = false;
	}

	/**
	 * method to resolve a given NodeId to the corresponding Node.
	 * @param node the Id of the requested Node.
	 * @return the requested Node.
	 */ 
	public Node queryNodeById(NodeId node) {
		return allNodes.get(node);
	}

	/**
	 * Method to resolve the shortest path between two nodes.
	 * @param from The starting node.
	 * @param to   The end node.
	 * @return the complete path, if it exists or null.
	 */
	public List<NodeId> resolvePath(NodeId from, NodeId to) {
		if (osrOracle == null) {
			if (Configuration.getInstance().getMovementManager() instanceof NoMovement) {
				osrOracle = new StaticOSROracle(allNodes);
			} else {
				osrOracle = new DynamicOSROracle();
			}
		}
		
		if (osrOracle == null) {
			return null;
		} else {
			return osrOracle.resolvePath(from, to);
		}
	}

	/**
	 * Method for transmission of packets.
	 * 
	 * @param senderTransmission Packet and all transmission parameters as Transmission object.
	 */
	public final void transmitPacket(Transmission senderTransmission) {
		Reachability reachability;
		
		/*
		 * We create a new transmission instance here, as sender and receivers should
		 * work on own instances (receivers will all work on the same instance).
		 */
		Transmission receiverTransmission = (Transmission) senderTransmission.clone();

		/*
		 * TODO We attach the packet instance the sender uses here for debugging and 
		 * 	    statistic generation. Maybe the Observer pattern could be used here.
		 */
		receiverTransmission.setSenderPacket(senderTransmission.getPacket());

		Node sender = allNodes.get(receiverTransmission.getPacket().getSender().getId());
		
		/* Increase the *time* of the packet so that it includes the propagation delay. 
		 * Afterwards Packet.getTime() and therefore Transmission.getSimStartTime() is
		 * the time the transmission started reaching the receiver(s).
		 */
		receiverTransmission.getPacket().adjustTime(
				Configuration.getInstance().getPropagationDelay());

		if (!sender.isEmpty()) {
			Position senderPos = sender.getPosition();

			List<Node> outNode = new ArrayList<Node>();
			for (Node receiver : allNodes.values()) {
				Position receiverPos = receiver.getPosition();
				
				if (!sender.equals(receiver)) {
					PhysicalModel pm = Configuration.getInstance().getPhysicalModel();
					reachability = pm.apply(receiver, sender,
							receiverTransmission.getSignalStrength());

					if (reachability.isInterfering()) {
						/*
						 * No BitManglingModel is allowed to modify interference
						 * packets, therefore we don`t have to clone the packet.
						 */
						Interference interference = new Interference(receiverTransmission, reachability);
						receiver.processEvent(interference, LayerType.AIR);
					}

					if (reachability.isReachable()) {					
						outNode.add(receiver);
						receiver.incPacketCount(false, LayerType.AIR);
					}
				}
			}
						
			for (Node node : outNode) {
				node.processEvent(receiverTransmission, LayerType.AIR);
			}
		}
	}

	/**
	 * detects the neighborhood of a node.
	 * 
	 * Should be called if Configuration.suppressNeighborhoodDetection is set to
	 * true.
	 * 
	 * @param sender the node, which neighborhood should be detected.
	 */
	public final void detectNeighbors(Node sender) {
		if (sender.isSuspended()) {
			// node is suspended, so it has no neighbors.
			return;
		}
		
		boolean ig = ((PhysicalLayer) sender.getLayer(LayerType.PHYSICAL)).isIgnoreRadioStateOnNeighborhoodDetection();
		
		Reachability reachability;
		Position     senderPos = sender.getPosition();
		sender.clearNeighbours();

		PhysicalLayerState phyState = (PhysicalLayerState) sender.getLayerState(LayerType.PHYSICAL);
		double signalStrength = phyState.getCurrentSignalStrength();

		if (ig || (phyState.getRadioState() != RadioState.OFF)) {
			for (Node receiver : allNodes.values()) {
				PhysicalLayerState recPhyState = (PhysicalLayerState) receiver.getLayerState(LayerType.PHYSICAL);
				if (!receiver.isSuspended() && (ig || (recPhyState.getRadioState() != RadioState.OFF))) {
	
					Position receiverPos = receiver.getPosition();
	
					if (sender.getId().asInt() != receiver.getId().asInt()) {
						PhysicalModel pm = Configuration.getInstance().getPhysicalModel();
						reachability = pm.apply(receiver, sender, signalStrength);
	
						if (reachability.isReachable()) {					
							sender.addNeighbor(receiver);
						}
					}
				}
			}
		}
	}

	/**
	 * Checks, whether node <code>toBeChecked</code> is still a neighbor of node
	 * <code>checker</code>. If not, it is removed from the neighbor list of the latter.
	 * 
	 * Should be called if Configuration.suppressNeighborhoodDetection is set to
	 * true.
	 * 
	 * @param checker The node which is to check its neighborhood
	 * @param toBeChecked The node which is to be checked as a neighbor
	 */
	public final void checkNeighbor(Node checker, Node toBeChecked) {
		Reachability reachability;
		Position     checkerPos = checker.getPosition();
		Position toBeCheckedPos = toBeChecked.getPosition();

		PhysicalLayerState phyState = (PhysicalLayerState) checker.getLayerState(LayerType.PHYSICAL);
		PhysicalLayerState recPhyState = (PhysicalLayerState) toBeChecked.getLayerState(LayerType.PHYSICAL);
		double signalStrength = phyState.getCurrentSignalStrength();

		PhysicalModel pm = Configuration.getInstance().getPhysicalModel();
		reachability = pm.apply(toBeChecked, checker, signalStrength);

		boolean remove = !((PhysicalLayer) checker.getLayer(LayerType.PHYSICAL)).isIgnoreRadioStateOnNeighborhoodDetection();

		if (remove) {
			remove = (phyState.getRadioState() == RadioState.OFF) || (recPhyState.getRadioState() == RadioState.OFF);
		}
		
		if (!reachability.isReachable() || toBeChecked.isSuspended() || remove) {					
			checker.removeNeighbor(toBeChecked);
		}
	}

	/**
	 * Static method for logging variables during simulation. Each statistic log entry
	 * specifies a combination of x and y axis labels and the corresponding current values.
	 * This implies, that for each xAxisLabel/yAxisLabel combination, the xValues must be
	 * unique. This way, different statistics can be logged in one simulation run and later
	 * visualized. Statistics are only written if Configuration.log is enabled.
	 * 
	 * @param node NodeId of the Node that wants to log something.
	 * @param layer LayerType of the Layer that wants to log something.
	 * @param xAxisLabel Label of the x-axis, i.e. semantic of the xValue
	 * @param yAxisLabel Label of the y-axis, i.e. semantic of the yValue
	 * @param xValue The currently logged statistic's value on the x-axis
	 * @param yValue The currently logged statistic's value on the y-axis
	 */
	public static final void logStatistic(NodeId node, LayerType layer, String xAxisLabel, 
			String yAxisLabel, String xValue, String yValue) {
		Configuration configuration = Configuration.getInstance();
		if (configuration.isLogging()) {
			SimulationManager.instance.statisticsWriter.logStatistics(
					node, layer, xAxisLabel, yAxisLabel, xValue, yValue);			
		}
	}

	/**
	 * Convenience method to log a node state. A node state is a (name, value) pair and
	 * can be any node property which the designer wants to be able to visualize in the ShoX Monitor.
	 * When this method is invoked, the passed data is immediately written to the simulation
	 * log file. This implies that it is assumed that the specified node state takes effect
	 * at invocation time (without delay).
	 * @param node The NodeId of the node whose state is to be logged
	 * @param name The name of the state to be logged
	 * @param type The data type of <code>value</code>, currently only "int" or "float" 
	 * @param value The value of the state to be logged
	 */
	public static final void logNodeState(NodeId node, String name, String type, String value) {
		if (SimulationManager.instance != null && Configuration.getInstance().isLogging()) {
			SimulationManager.instance.history.logNodeStateEvent(node, name, type, value);
		}
	}

	/**
	 * Convenience method to log a link state. A link state is a (name, value) pair and
	 * can be any link property which the designer wants to be able to visualize in the ShoX Monitor.
	 * When this method is invoked, the passed data is immediately written to the simulation
	 * log file. This implies that it is assumed that the specified node state takes effect
	 * at invocation time (without delay).
	 * @param node1 The NodeId of the first endpoint node of the link whose state is to be logged
	 * @param node2 The NodeId of the second endpoint node of the link whose state is to be logged
	 * @param name The name of the state to be logged
	 * @param type The data type of <code>value</code>, currently only "int" or "float"
	 * @param value The value of the state to be logged
	 */
	public static final void logLinkState(NodeId node1, NodeId node2, String name, String type, String value) {
		if (SimulationManager.instance != null && Configuration.getInstance().isLogging()) {
			SimulationManager.instance.history.logLinkStateEvent(node1, node2, name, type, value);
		}
	}

	/**
	 * Convenience method to log a text message to be displayed later in the ShoX Monitor.
	 * When this method is invoked, the passed data is immediately written to the simulation
	 * log file. This implies that it is assumed that the specified node state takes effect
	 * at invocation time (without delay).
	 * @param sender The ID and layer of the node which wants to log this message
	 * @param priority A value specifying a priority
	 * @param message The actual message to be displayed in the ShoX Monitor
	 */
	public static final void logMessage(Address sender, int priority, String message) {
		if (SimulationManager.instance != null && Configuration.getInstance().isLogging()) {
			SimulationManager.instance.history.logMessage(SimulationManager.instance.getCurrentTime(),
					sender, message, priority);
		}
	}
	
	/**
	 * Static method to place an internal event for a node.
	 * E.g. communication between LAYERS are internal events.
	 * 
	 * Comment on NodeId receiver and event.receiver:
	 * The nodeId used here is the ID to which this event is deliverd.
	 * This ID is different from the final receiver ID stored in event.receiver
	 * until the event was transmitted via radio.
	 * See specific envelope implementations in SimulationManager for further details.
	 * 
	 * @param event The event to place inside the queue.
	 * @param receiver NodeId of the receiver (internal). 
	 * @param toLayer Specific layer which should receive the event.
	 */
	public static void enqueue(ToLayer event,
			NodeId receiver, LayerType toLayer) {
		//delegation to have nonstatic references for SimMana
		SimulationManager.instance.enqueueEvent(event, receiver, toLayer);
	}


	/**
	 * Static method to place an internal event for a device.
	 * E.g. delayed communication of component with itself
	 * 
	 * @param event The event to place inside the queue, which helds destination informaton
	 * @param toDevice The device to which to send the event
	 */

	public static void enqueue(ToDevice event, DeviceType toDevice) {
		//delegation to have nonstatic references for SimMana
		SimulationManager.instance.enqueueEvent(event, toDevice);
	}

	/**
	 * Private method for internal communication. Only called locally.
	 * 
	 * @param event The event to place inside the queue
	 * @param toDevice The device to which to send the event
	 */
	private void enqueueEvent(ToDevice event, DeviceType toDevice) {
		if (event == null) {
			throw new IllegalArgumentException("event");
		}
		if (event.getDelay() < 0) {
			throw new IllegalArgumentException("delay of an event should never be negative");
		}
		if (toDevice == null) {
			throw new IllegalArgumentException("toDevice");
		}

		Node node = allNodes.get(event.getReceiver());

		NodeInternalToDeviceEnvelope env = new NodeInternalToDeviceEnvelope(event, node, toDevice);
		queue.add(env);
	}

	/**
	 * Private method for internal communication. Only called locally.
	 * 
	 * @param event The event to place inside the queue.
	 * @param receiver NodeId of the receiver (internal). 
	 * @param toLayer Specific layer which should receive the event.	 */
	private void enqueueEvent(ToLayer event, NodeId receiver, LayerType toLayer) {
		if (event == null) {
			throw new IllegalArgumentException("event");
		}
		if (event.getDelay() < 0) {
			throw new IllegalArgumentException("delay of an event should never be negative");
		}
		if (receiver == null) {
			throw new IllegalArgumentException("receiver");
		}
		if (toLayer == null) {
			throw new IllegalArgumentException("toLayer");
		}
		Node node = allNodes.get(receiver);

		NodeInternalEnvelope env = new NodeInternalEnvelope(event, node, toLayer);
		queue.add(env);
	}

	/* Specific envelopes as inner classes to have access to SimulationManager internal things!*/

	/**
	 * Class NodeInternalToDeviceEnvelope to handle events between devices/component.
	 * Examples are EnergyManagerWakeUpCall
	 * 
	 * @author Florian Rittmeier
	 */
	private final class NodeInternalToDeviceEnvelope extends EventEnvelope implements Loggable {

		/** The receiver of the event inside envelope. */
		private final Node node;

		/** The specific device of the receiver. */
		private final DeviceType toDevice;

		/**
		 * Default constructor of the class NodeInternalToDeviceEnvelope.
		 * 
		 * @param event The event to deliver.
		 * @param node The NodeId of the receiver.
		 * @param toDevice The DeviceType of the destination device.
		 */
		private NodeInternalToDeviceEnvelope(ToDevice event, Node node, DeviceType toDevice) {
			super(event, currentTime + event.getDelay());

			this.node = node;
			this.toDevice = toDevice;
		}

		/** @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver() */
		@Override
		protected void deliver() {
			ToDevice event = (ToDevice) this.getEvent();
			this.node.processEvent(event, toDevice);
		}

		
//		/**
//		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#log(br.ufla.dcc.grubix.xml.XMLWriter)
//		 * @param writer Writer to log into.
//		 */
//		public void log(XMLWriter writer) {
//			
//			ToDevice event = (ToDevice) this.getEvent();
//			writer.openTag(XMLTags.TODEVICE);
//			writer.writeTag(XMLTags.SENDERID, String.valueOf(event.getSender().asInt()));
//			writer.writeTag(XMLTags.SENDERDEVICE, String.valueOf(event.getSenderDevice().toString()));
//			writer.writeTag(XMLTags.ENVELOPERECEIVERID, String.valueOf(this.node.getId().asInt()));
//			writer.writeTag(XMLTags.ENVELOPERECEIVERDEVICE, String.valueOf(this.toDevice.toString()));
//			if (Configuration.logData && event instanceof LoggableData) {
//				LoggableData data = (LoggableData) event;
//				writer.writeTag(XMLTags.DATA, data.getData());
//			}
//			writer.closeTag(XMLTags.TODEVICE);
//		}
		


		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getEventId()
		 * @return EventId of the enclosed event.
		 */
		public EventId getEventId() {
			return this.getEvent().getId();
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getEventType()
		 * @return Classname of the enclosed event as String.
		 */
		public String getEventType() {
			return this.getEvent().getClass().getName();
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getReceiver()
		 * @return NodeId of the receiver.
		 */
		public NodeId getReceiver() {
			return ((ToDevice) this.getEvent()).getReceiver();
		}

		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " NodeInternalToDeviceEnvelope for Node " + node.getId().asInt() + "\n"
			+ " to device " + toDevice.toString() + " with event \n"
			+ this.getEvent().toString() + ".";
		}

		public String log(int logType) {
			Configuration configuration = Configuration.getInstance();
			ToDevice event = (ToDevice) this.getEvent();
			String datastr = "";
			
			if (logType == Loggable.XML) {
				
				if (configuration.getLiveLogFilter().isLogData() && event instanceof LoggableData) {
					LoggableData data = (LoggableData) event;
					datastr = "<" + XMLTags.DATA + ">" + data.getData() + "</" + XMLTags.DATA + ">\n";
				}
				
				return "<" + XMLTags.TODEVICE + ">\n" 
						
						+ "<" + XMLTags.SENDERID + ">" + String.valueOf(event.getSender().asInt()) 
						+ "<" + XMLTags.SENDERID + ">\n"
						
						+ "<" + XMLTags.SENDERDEVICE + ">" + String.valueOf(event.getSenderDevice().toString()) 
						+ "<" + XMLTags.SENDERDEVICE + ">\n"
						
						+ "<" + XMLTags.ENVELOPERECEIVERID + ">" + String.valueOf(this.node.getId().asInt()) 
						+ "<" + XMLTags.ENVELOPERECEIVERID + ">\n"
						
						+ "<" + XMLTags.ENVELOPERECEIVERDEVICE + ">" + String.valueOf(this.toDevice.toString()) 
						+ "<" + XMLTags.ENVELOPERECEIVERDEVICE + ">\n"
						
						+ datastr
															
						+ "</" + XMLTags.TODEVICE + ">"; 
						
			} else if (logType == Loggable.COMPACT) {
				
				if (configuration.getLiveLogFilter().isLogData() && event instanceof LoggableData) {
					datastr = " data" + CompactFileLogger.mask(((LoggableData) event).getData());
				}
				
				return "toDev" 
						+ " sID" + String.valueOf(event.getSender().asInt()) 
						+ " sDv" + String.valueOf(event.getSenderDevice().toString()) 
						+ " rID" + String.valueOf(this.node.getId().asInt()) 
						+ " rDv" + String.valueOf(this.toDevice.toString()) 
						+ datastr;
						
			} else {
				return null;
			}
		}
	}


	/**
	 * Class NodeInternalEnvelope to handle events inside a node.
	 * Examples are WakeUpCall, packet from layer i+1 to i or vice versa.
	 * 
	 * @author Andreas Kumlehn
	 */
	private final class NodeInternalEnvelope extends EventEnvelope implements Loggable {

		/** The receiver of the event inside envelope. */
		private final Node node;

		/** The specific layer of the receiver. */
		private final LayerType toLayer;

		/**
		 * Default constructor of the class NodeInternalEnvelope.
		 * 
		 * @param event The event to deliver.
		 * @param node The NodeId of the receiver.
		 * @param toLayer The LayerType of the destination layer.
		 */
		private NodeInternalEnvelope(ToLayer event, Node node, LayerType toLayer) {
			super(event, currentTime + event.getDelay());
			/*if (event instanceof Packet) {
				LOGGER.info("enqueueing event with time " + this.getTime());
			}*/
			this.node = node;
			this.toLayer = toLayer;
		}

		/** @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver() */
		@Override
		protected void deliver() {
			ToLayer event = (ToLayer) this.getEvent();
			
			MDC.put("node", this.node.getId());
			MDC.put("layer", event.getSender().getFromLayer().getShortName());
			this.node.processEvent(event, toLayer);
		}
		
//		/**
//		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#log(br.ufla.dcc.grubix.xml.XMLWriter)
//		 * @param writer Writer to log into.
//		 */
//		public void log(ShoxLogger writer) {
//			ToLayer event = (ToLayer) this.getEvent();
////			writer.openTag(XMLTags.TOLAYER);
////			writer.writeTag(XMLTags.SENDERID, String.valueOf(event.getSender().getId().asInt()));
////			writer.writeTag(XMLTags.SENDERLAYER, String.valueOf(event.getSender().getFromLayer().toString()));
////			writer.writeTag(XMLTags.ENVELOPERECEIVERID, String.valueOf(this.node.getId().asInt()));
////			writer.writeTag(XMLTags.ENVELOPERECEIVERLAYER, String.valueOf(this.toLayer.toString()));
////			if (Configuration.logData && event instanceof LoggableData) {
////				LoggableData data = (LoggableData) event;
////				writer.writeTag(XMLTags.DATA, data.getData());
////			}
////			if (event instanceof Packet) {
////				Packet pack = ((Packet) event).getEnclosedPacket();
////				if (pack != null) {
////					writer.writeTag(XMLTags.ENCLOSEDEVENTID,
////							String.valueOf(pack.getId().asInt()));
////				}
////			}			
////			writer.closeTag(XMLTags.TOLAYER);
//			
//			String dataString = null;
//			if (Configuration.logData && event instanceof LoggableData) {
//				LoggableData data = (LoggableData) event;				
//				dataString = data.getData();
//			}
//			
//			EventId enclosedId = null;
//			if (event instanceof Packet) {
//				Packet pack = ((Packet) event).getEnclosedPacket();
//				if (pack != null) {
//					enclosedId = pack.getId();
//				}
//			}	
//			
//			writer.logInternalEvent(event.getSender().getId(), event.getSender().getFromLayer(), node.getId(), 
//					toLayer, dataString, enclosedId);							
//			
//		}
		

		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getEventId()
		 * @return EventId of the enclosed event.
		 */
		public EventId getEventId() {
			return this.getEvent().getId();
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getEventType()
		 * @return Classname of the enclosed event as String.
		 */
		public String getEventType() {
			return this.getEvent().getClass().getName();
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getReceiver()
		 * @return NodeId of the receiver.
		 */
		public NodeId getReceiver() {
			return ((ToLayer) this.getEvent()).getReceiver();
		}

		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " NodeInternalEnvelope for Node " + node.getId().asInt() + "\n"
			+ " to layer " + toLayer.toString() + " with event \n"
			+ this.getEvent().toString() + ".";
		}

		public String log(int logType) {
			Configuration configuration = Configuration.getInstance();
			ToLayer event = (ToLayer) this.getEvent();
			String dataString = "";
			
			if (logType == Loggable.XML) {
				
				if (configuration.getLiveLogFilter().isLogData() && event instanceof LoggableData) {
					LoggableData data = (LoggableData) event;
					dataString = "<" + XMLTags.DATA + ">" + data.getData() + "</" + XMLTags.DATA + ">\n";
				}
				
				String enclosedEventId = "";
				if (event instanceof Packet) {
					Packet pack = ((Packet) event).getEnclosedPacket();
					if (pack != null) {
						enclosedEventId = "<" + XMLTags.ENCLOSEDEVENTID + ">" +	pack.getId().asInt() 
										+ "</" + XMLTags.ENCLOSEDEVENTID + ">\n";  
					}
				}
				
				return "<" + XMLTags.TOLAYER + ">\n"
						+ "<" + XMLTags.SENDERID + ">" + event.getSender().getId() + "</" + XMLTags.SENDERID + ">\n"
						+ "<" + XMLTags.SENDERLAYER + ">" + event.getSender().getFromLayer() 
																+ "</" + XMLTags.SENDERLAYER + ">\n"
						+ "<" + XMLTags.ENVELOPERECEIVERID + ">" + this.node.getId().asInt() 
																	+ "</" + XMLTags.ENVELOPERECEIVERID + ">\n"
						+ "<" + XMLTags.ENVELOPERECEIVERLAYER + ">" + this.toLayer.toString() 
																	+ "</" + XMLTags.ENVELOPERECEIVERLAYER + ">\n"
						+ dataString
						+ enclosedEventId
						+ "</" + XMLTags.TOLAYER + ">";
				
			} else if (logType == Loggable.COMPACT) {
				
				if (configuration.getLiveLogFilter().isLogData() && event instanceof LoggableData) {
					LoggableData data = (LoggableData) event;				
					dataString = " data" + CompactFileLogger.mask(data.getData());
				}
				
				String enclosedId = "";
				if (event instanceof Packet) {
					Packet pack = ((Packet) event).getEnclosedPacket();
					if (pack != null) {
						enclosedId = " eId" + pack.getId();
					}
				}
				
				return "intEnv" 
						+ " sId" + event.getSender().getId()
						+ " sLy" + CompactFileLogger.getLayerCode(event.getSender().getFromLayer())
						+ " rId" + node.getId()
						+ " rLy" + CompactFileLogger.getLayerCode(toLayer)
						+ enclosedId
						+ dataString;
			} else {
				return null;
			}
		}
	}

	/**
	 * Class to handle Initialize events during start up of the simulation.
	 * 
	 * @author Andreas Kumlehn
	 */
	private final class SimulationStateEnvelope extends SimulationEventEnvelope {

		/**
		 * Constructor of the class SimulationStateEnvelope.
		 * @param event The SimulationState event to deliver.
		 */
		private SimulationStateEnvelope(SimulationState event) {
			super(event, currentTime + event.getDelay());
		}

		/** @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver() */
		@Override
		protected void deliver() {
			SimulationState simState = (SimulationState) this.getEvent();
			NodeId node = simState.getReceiver();
			Node receiver = allNodes.get(node);
			receiver.processEvent(simState);
			/*
			int direction = UPWARDS;

			if (simState instanceof Finalize) {
				direction = DOWNWARDS;
			}
			receiver.propagateEvent(simState, direction);
			 */
		}

		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " SimulationStateEnvelope with event \n"
			+ this.getEvent().toString() + ".";
		}
	}

	/**
	 * Class to handle Movements in the simulation.
	 * 
	 * @author Andreas Kumlehn
	 */
	private final class MovementEnvelope extends SimulationEventEnvelope implements Loggable {

		/**
		 * Constructor of the class MovementEnvelope.
		 * 
		 * @param event The MOVE to provess.
		 */
		private MovementEnvelope(Movement event) {
			super(event, currentTime + event.getDelay());
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver()
		 * 
		 * Delivering a MOVE means update the topology
		 * and notify the node via a Moved event.
		 */
		@Override
		protected void deliver() {
			Movement move = (Movement) this.getEvent();
			Node node = move.getNode();
			Position position = move.getNewPosition();
			
			//deliver MovedEvent to the moved node
			if (move.isNodeFailure() && !node.isSuspended()) {
				node.suspend(true);
				// no MovedEvent is node fails!
			} else {
				if (node.isSuspended()) {
					node.suspend(false);
				}
				Moved moved = new Moved(node.getId(), position);
				node.processEvent(moved);				
			}

			// update the node list of the moved node and all of its neighbors
			if (!Configuration.getInstance().isSuppressNeighborhoodDetection()) {
				List<Node> nl = node.getNeighbors();
				for (Node n : nl) {
					SimulationManager.this.checkNeighbor(n, node);
				}
				SimulationManager.this.detectNeighbors(node);				
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public NodeId getReceiver() {
			Movement movement = (Movement) getEvent(); // is always a Movement
			// (see constructor)
			return movement.getNode().getId();
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getEventId()
		 * @return EventId of the enclosed event.
		 */
		public EventId getEventId() {
			return this.getEvent().getId();
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.event.Loggable#getEventType()
		 * @return Classname of the enclosed Event as String.
		 */
		public String getEventType() {
			return this.getEvent().getClass().getName();
		}
		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " MovementEnvelope with event \n"
			+ this.getEvent().toString() + ".";
		}

		public String log(int logType) {
			return null;
		}
	}

	/**
	 * Class to handle traffic in the SIMULATION.
	 * @author jlsx
	 */
	private final class TrafficEnvelope extends SimulationEventEnvelope {

		/**
		 * Constructor of the class TrafficEnvelope.
		 * @param event The traffic to provess.
		 */
		private TrafficEnvelope(TrafficGeneration event) {
			super(event, currentTime + event.getDelay());
		}

		/**
		 * @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver()
		 * 
		 * Delivering traffic means the node 
		 * and notify the node via a Moved event.
		 */
		@Override
		protected void deliver() {
			TrafficGeneration tg = (TrafficGeneration) this.getEvent();
			Node node = instance.allNodes.get(tg.getSource());
			//deliver TrafficGenerationEvent to the corresonding node
			node.processEvent(tg);
		}

		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " TrafficEnvelope with event \n"
			+ this.getEvent().toString() + ".";
		}
	}

	/**
	 * Class to handle the generation of moves.
	 * Stores a MoveManagerEvent event which is reenqueued as long as
	 * the MovementManager wants to be requested for new moves.
	 * 
	 * @author Andreas Kumlehn
	 */
	private final class MoveManaEnvelope extends SimulationEventEnvelope {

		/**
		 * Constructor of the class MoveManaEnvelope.
		 * 
		 * @param event The MoveManagerEvent event to enqueue.
		 */
		private MoveManaEnvelope(MoveManagerEvent event) {
			super(event, currentTime + event.getDelay());
		}

		/** @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver() */
		@Override
		protected void deliver() {
			Configuration configuration = Configuration.getInstance();
			
			// manager creates all moves
			Collection<Movement> newMoves = configuration.getMovementManager().createMoves(allNodes
					.values());
			
			// enqueue all new moves
			Iterator<Movement> iter = newMoves.iterator();
			while (iter.hasNext()) {
				MovementEnvelope env = new MovementEnvelope(iter.next());
				queue.add(env);
			}
			
			// check when to query MovementManager again
			MoveManaEnvelope env;
			double newDelay = configuration.getMovementManager().getDelayToNextQuery();

			if (newDelay >= 0.0) {
				// reenqueue the MoveManagerEvent
				env = new MoveManaEnvelope(new MoveManagerEvent(newDelay));
			} else if (newDelay == -1.0) {
				/* 
				 * This indicates that the MovementManager will not generate any further moves.
				 * Therefore we will not query it again and have nothing to do here.
				 */
				env = null;
			} else {
				/* 
				 * newDelay < 0.0
				 * 
				 * This is not a valid value and hints towards an internal error
				 * of the current MovementManager.
				 */
				throw new IllegalReturnValueException(
						"MoveManagerEvent.getDelayToNextQuery() returned negative value which wasn`t -1.0");
			}
			
			if (env != null) {
				queue.add(env);
			}
		}

		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " MoveManaEnvelope with event \n"
			+ this.getEvent().toString() + ".";
		}
	}

	/**
	 * Class to handle the startup time of nodes.
	 * 
	 * Stores a NodeStartupManagerEvent event which is reenqueued as long as
	 * the NodeStartupManager wants to be startup new moves.
	 * 
	 * @author Florian Rittmeier
	 */
	private final class NodeStartupManagerEnvelope extends SimulationEventEnvelope {

		/**
		 * Constructor of the class NodeStartupManagerEnvelope.
		 * 
		 * @param event The NodeStartupManagerEvent event to enqueue.
		 */
		private NodeStartupManagerEnvelope(NodeStartupManagerEvent event) {
			super(event, currentTime + event.getDelay());
		}

		/** @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver() */
		@Override
		protected void deliver() {
			Configuration configuration = Configuration.getInstance();
			
			// manager gives all NodeIds which to be started now
			Collection<NodeId> newStartupNodes = configuration.getNodeStartupManager().getStartupNodes();
			
			// enqueue all new moves
			Iterator<NodeId> iter = newStartupNodes.iterator();
			while (iter.hasNext()) {
				StartSimulation start = new StartSimulation(iter.next(), 0.0);
				SimulationStateEnvelope initenv = new SimulationStateEnvelope(start);
				queue.add(initenv);
			}
			
			// check when to query MovementManager again
			NodeStartupManagerEnvelope env;
			double newDelay = configuration.getNodeStartupManager().getDelayToNextQuery();

			if (newDelay >= 0.0) {
				// reenqueue the NodeStartupManagerEvent
				env = new NodeStartupManagerEnvelope(new NodeStartupManagerEvent(newDelay));
			} else if (newDelay == -1.0) {
				/* 
				 * This indicates that the NodeStartupManager will not
				 * wakeup any further nodes. Therefore we will not
				 * query it again and have nothing to do here.
				 */
				env = null;
			} else {
				/* 
				 * newDelay < 0.0
				 * 
				 * This is not a valid value and hints towards an internal error
				 * of the current NodeStartupManager.
				 */
				throw new IllegalReturnValueException(
						"NodeStartupManagerEvent.getDelayToNextQuery() returned negative value which wasn`t -1.0");
			}
			
			if (env != null) {
				queue.add(env);
			}
		}

		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " " + this.getClass().getName() + " with event \n"
			+ this.getEvent().toString() + ".";
		}
	}
	
	
	/**
	 * Class to handle the generation of traffic.
	 * Stores a TrafficGeneratorEvent event which is reenqueued as long as
	 * the TrafficGenerator wants to be requested for new traffic.
	 * 
	 * @author jlsx
	 */
	private final class TrafficGenEnvelope extends SimulationEventEnvelope {

		/**
		 * Constructor of the class TrafficGenEnvelope.
		 * 
		 * @param event The TrafficGeneratorEvent event to enqueue.
		 */
		private TrafficGenEnvelope(TrafficGeneratorEvent event) {
			super(event, currentTime + event.getDelay());
		}

		/** @see br.ufla.dcc.grubix.simulator.kernel.EventEnvelope#deliver() */
		@Override
		protected void deliver() {
			Configuration configuration = Configuration.getInstance();
			
			// manager creates all traffic
			Collection<TrafficGeneration> newTraffic = configuration.getTrafficGenerator().generateTraffic(allNodes
					.values(), currentTime);
			
			// enqueue all new moves
			Iterator<TrafficGeneration> iter = newTraffic.iterator();
			while (iter.hasNext()) {
				TrafficEnvelope env = new TrafficEnvelope(iter.next());
				queue.add(env);
			}
			
			// check when to query MovementManager again
			//reenqueue the TrafficGenEnvelope
			TrafficGenEnvelope env;
			double newDelay = configuration.getTrafficGenerator().getDelayToNextQuery();

			if (newDelay >= 0.0) {
				// reenqueue the TrafficGeneratorEvent
				env = new TrafficGenEnvelope(new TrafficGeneratorEvent(newDelay));
			} else if (newDelay == -1.0) {
				/* 
				 * This indicates that the TrafficGenerator will not generate any further moves.
				 * Therefore we will not query it again and have nothing to do here.
				 */
				env = null;
			} else {
				/* 
				 * newDelay < 0.0
				 * 
				 * This is not a valid value and hints towards an internal error
				 * of the current TrafficGenerator.
				 */
				throw new IllegalReturnValueException(
						"TrafficGenerator.getDelayToNextQuery() returned negative value which wasn`t -1.0");
			}
			
			if (env != null) {
				queue.add(env);
			}
		}

		/**
		 * Method to ensure proper logging.
		 * @return String for logging.
		 */
		@Override
		public String toString() {
			return this.getTime() + " TrafficGenEnvelope with event \n"
			+ this.getEvent().toString() + ".";
		}
	}

	/** @return the current simulationtime */
	public final double getCurrentTime() {
		return currentTime;
	}

	/** @return All nodes in the simulation. */
	public static final SortedMap<NodeId, Node> getAllNodes() {
		return instance.allNodes;
	}

	/**
	 * Asks the simulation manager to pass the current simulation time to the specified
	 * <code>TimedObject</code>. The simulation manager decides whether or not that object
	 * is eligible to obtain the time information.
	 * @param obj The object which requires to know the current simulation time
	 */
	public static final void setCurrentTime(TimedObject obj) {
		if (obj instanceof Packet) {
			obj.setTime(instance.currentTime);
		}
	}
}

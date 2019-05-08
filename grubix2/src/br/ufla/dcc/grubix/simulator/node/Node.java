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

package br.ufla.dcc.grubix.simulator.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.DeviceType;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.EnergyManagerWakeUpCallEvent;
import br.ufla.dcc.grubix.simulator.event.Event;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Moved;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.SimulationState;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.ToDevice;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.energy.BasicEnergyManager;
import br.ufla.dcc.grubix.simulator.node.energy.EnergyManager;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;


/** 
 * Superclass for all types of Nodes.
 * A node is specified via its layers in the network stack.
 * Therefore no specialization of this class allowed. 
 * 
 * A node can obtain his POSITION via 
 * Topology.getSafeTopology.getPosition(this)
 * where the parameter is the node itself.
 * Same for the energy value (EnergyContainer.getSafeEnergyContainer.getEnergy(this).
 * 
 * @author Andreas Kumlehn, Dirk Held
 */
public class Node implements Configurable, Serializable {
	
	/**--------Atributos Jesimar--------*/
	
	private String isMobile = "false";
	
	public void setIsMobile(String isMobile) {
		this.isMobile = isMobile;
	}
	
	public String getIsMobile(){
		return isMobile;
	}

	/**-------Fim codigo Jesimar--------*/
	
	
	/** Logger of the class Node. */
	private static transient final Logger LOGGER = Logger.getLogger(Node.class);
	
	/** The unique ID of the node. */
	private final NodeId id;
	
	/** The type of node (may be used differently by varios applications */
	private int typeOfNode=0;
	
	/** shortcut to access Configuration.getInstance(). */
	private Configuration cfg;
	
	private String nodeName;
		
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/** The EnergyManager of the Node.
	 * The default is the basic energy Manager */
	@ShoXParameter(defaultClass = BasicEnergyManager.class)
	private EnergyManager energyManager;
	
	/** Real position of the node. Remember to also invalidate the neighborhood 
	 * array, if the position changes. This is done insetPosition. */
	private Position position;
	
	/** If set to true, incoming movement events are not processed, the default is false.*/
	private boolean ignoreMoves;
	
	/** list of current neighbors. This is only useful in stationary setups. */
	private ArrayList<Node> neighbor;
	
	public final static int NONMEMBER = 0;
	public final static int CLUSTERHEAD = 1;
	public final static int MEMBER = 2;
	public int clusterState = NONMEMBER;
	
	/** A list containing all layers that want to receive moved objects.  */
	private Set<Layer> movedObserver;
	
	/**
	 * all layers stacked together.
	 */
	private Map<LayerType, Layer> layerStack;
	
	/**
	 * contents of the layer stack as list. used for debugging purposes.
	 */
	@ShoXParameter
	private Layer[] layerList;
	
	/**
	 * observer for network events.
	 * The observer instances are shared over all nodes.
	 */
	@ShoXParameter
	private SimulationObserver[] observerList;
	
	/**
	 * Stores the current (simulation) time as calculated by the node. This is not necessarily
	 * the precise simulation time used in the SimulationManager. Rather, every node is
	 * responsible for keeping track of the time by whatever practical means (physical clock,
	 * GPS receiver with time, synchronization with other nodes, etc.
	 * 
	 * TODO Currently unused as we use {@link #getCurrentTime()} uses the
	 * 		{@link SimulationManager}.
	 */
	private double currentTime;
	
	/**
	 * counter for the number of incoming packets for every layer. Must be
	 * LinkedHashMap because we rely on the order of the layer
	 */
	private LinkedHashMap<LayerType, Integer> inPackets;

	/**
	 * counter for the number of outgoing packets for every layer. Must be
	 * LinkedHashMap because we rely on the order of the layer
	 */
	private LinkedHashMap<LayerType, Integer> outPackets;
	
	/**
	 * flag if the node is suspended. 
	 */
	private boolean isSuspended = false;

    /**
     * Direction: set the direction in which the node will move
     * true: from the bottom to the top
     * false: from the top to the bottom 
     * default: true
     * to be used in the scan movement patterns of the UAVs
     */
	
	private boolean DirectionY = true; 
	
    /**
     * Direction: set the direction in which the node will move
     * true: from right to left
     * false: from left to right
     * default: true
     * to be used in the scan movement patterns of the UAVs
     */	
	
	private boolean DirectionX = true;
	
	/**
	 * Constructor of the class Node.
	 */
	public Node() {
		this.id = new NodeId();
		this.movedObserver = new HashSet<Layer>();
		
		inPackets = new LinkedHashMap<LayerType, Integer>();
		for (LayerType layer : LayerType.values()) {
			inPackets.put(layer, 0);
		}

		outPackets = new LinkedHashMap<LayerType, Integer>();
		for (LayerType layer : LayerType.values()) {
			outPackets.put(layer, 0);
		}
		
		ignoreMoves = false;
		neighbor    = new ArrayList<Node>();		  
	}
	
	/**
	 * method to read the packets received or sent so far to this layer.
	 * 
	 * @param in    set true if the number of incoming packets is requested. 
	 * @param layer the requested layer.
	 * @return      the number of packets of the requested layer in the requested direction.
	 */
	public int getPacketCount(boolean in, LayerType layer) {
		Map<LayerType, Integer> packets = outPackets;
		if (in) {
			packets = inPackets;
		}
		return packets.get(layer);
	}

	/**
	 * Some packets can't be counted internaly. The Air and the SimulationKernel
	 *  does this. incPacketCount must not be called elsewhere.
	 *
	 * (out, 1)@ AirModule.transmit:                count number of outgoing packets.       (fan-out)
	 * (out, 0)@ SimulationManager.transmitPacket:  count number potential received packets. (fan-in)
	 * (in,  0)@ AirModule.TransmissionEndIncoming: count number of listened to packets, including invalids.
	 *	
	 * @param in    if true, count an incoming packet.
	 * @param layer the layer from which this packet originates.
	 */
	public void incPacketCount(boolean in, LayerType layer) {
		Map<LayerType, Integer> packets = outPackets;
		if (in) {
			packets = inPackets;
		}
		int count = packets.get(layer);
		count++;
		packets.put(layer, count);
	}
	
	/**
	 * gets the state of a given layer.
	 * @param layer layer type
	 * @return state of the given layer (may be null)
	 */
	public LayerState getLayerState(LayerType layer) {
		return layerStack.get(layer).getState();
	}
	
	/**
	 * sets a new state to a layer.
	 * 
	 * @param toLayer LayerType of the goal layer of the given event.
	 * @param state the new state for the requested layer.
	 * @return true if the state was accepted.
	 */
	public boolean setLayerState(LayerType toLayer, LayerState state) {
		return layerStack.get(toLayer).setState(state);
	}
	
	/**
	 * delegates ToLayer events to the receiver layer.
	 * 
	 * If the node is suspended, an warning is logged and the event is dropped. This
	 * is done here to avoid counting incoming packets.
	 * 
	 * @param event ToLayer event to delegate.
	 * @param layer LayerType of the goal layer of the given event.
	 */
	public void processEvent(Event event, LayerType layer) {
		setCurrentTime(SimulationManager.getInstance().getCurrentTime()); // for debugging purposes

		if (isEmpty()) {
			LOGGER.warn("Node " + getId() + " is powered down [" + event + "]");
			return;
		}
		
		if (isSuspended()) {
			LOGGER.warn("Node " + getId() + " is suspended [" + event + "]");
			return;
		}
		
		if (event instanceof ToLayer) {
			try {
				layerStack.get(layer).processEvent((ToLayer) event);
				// notify observers about packet
				notifyObserver(layerStack.get(layer), (ToLayer)  event);

				if (event instanceof Packet) {
					Packet p = (Packet) event;

					if (layer == LayerType.PHYSICAL) {
						if (p.getSender().getId().asInt() == id.asInt()) {
							incPacketCount(false, LayerType.MAC);
						} else {
							if (p.isValid()) {
								incPacketCount(true, LayerType.PHYSICAL);
							}
						}
					} else {
						if (p.getDirection() == Direction.DOWNWARDS) {
							incPacketCount(false, layer.getUpperLayer());
						} else {
							incPacketCount(true, layer);
						}
					}
				}
			} catch (LayerException e) {
				// discard event and print warning
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * method to process DeviceType events.
	 * @param event    the to be processed event.
	 * @param toDevice the requested device.
	 */
	public void processEvent(Event event, DeviceType toDevice) {
		// for debugging purposes (copied from processEvent(Event, LayerType)
		setCurrentTime(SimulationManager.getInstance().getCurrentTime());

		if (isEmpty()) {
			LOGGER.warn("Node " + getId() + " is powered down [" + event + "]");
			return;
		}
		
		/* INFO Do not check wether Node is suspended, if the event is
		 * 		for the energyManager, cause it is aware of the suspend
		 * 		state and still needs to get EnergyManagerWakeUpCallEvents.
		 */
		if (event instanceof ToDevice) {
			ToDevice toDeviceEvent = (ToDevice) event;
			
			if (toDeviceEvent instanceof EnergyManagerWakeUpCallEvent) {
				/* Forward event only if its not outdated, otherwise
				 * we would get more and more WakeUpCalls over time. 
				 */
				if (energyManager.isValidWakeUpCall((EnergyManagerWakeUpCallEvent) event)) {
					energyManager.checkPowerLevelCallbacks();
				}
			}
		} 
		
	}
	
	/**
	 * delegates SimulationState events to all layers.
	 * @param simState SimulationState event to delegate.
	 */
	public void processEvent(SimulationState simState) {
		/* INFO This processEvent overload does not need to
		 * 	    check isSuspended or isEmpty cause it is
		 *      for forwarding basic simulation state
		 *      information (init, deinit, ...) for which
		 *      isSuspended and isEmpty is irrelevant
		 */
		
		if (simState instanceof Finalize) {
			
			// let observers prepare finish
			for (SimulationObserver observer : this.observerList) {
				observer.simulationBeforeFinish(getId());
			}
			
			// init events are always sent to the application layer

			List<LayerType> reverseLayers = Arrays.asList(LayerType.values());
			Collections.reverse(reverseLayers);

			for (LayerType layer : reverseLayers) {
				if (layer == LayerType.AIR) {
					continue;
				}
				getLayer(layer).processEvent(simState);
			}
			
			// let observers stop
			for (SimulationObserver observer : this.observerList) {
				observer.simulationFinished(getId());
			}
		} else {
			
			if (simState instanceof StartSimulation) {
				LOGGER.info("Starting node " + getId().toString()
						+ " at "
						+ SimulationManager.getInstance().getCurrentTime());
			}
			
			List<LayerType> layers = Arrays.asList(LayerType.values());
			for (LayerType layer : layers) {
				getLayer(layer).processEvent(simState);
			}
		}
		
		if (simState instanceof Initialize) {
			energyManager.startup();
		}
	}
	
	/**
	 * Method to delegate Moved events to registered layers in the observer.
	 * All layers of the movedObserver are called.
	 * 
	 * @param moved Moved event to delegate.
	 */
	public void processEvent(Moved moved) {
		if (ignoreMoves) {
			return;
		}
		
		setPosition(moved.getNewPosition());
		 
		try {
			for (Iterator<Layer> iter = this.movedObserver.iterator(); iter.hasNext();) {
				iter.next().processEvent(moved);
			}
		} catch (LayerException e) {
			//discard event and print warning
			LOGGER.warn(e.toString());
		}
	}

	/**
	 * forwards traffic generation request to application layer instance.
	 * @param tg Traffic generation request with all necessary details
	 */
	public void processEvent(TrafficGeneration tg) {
		if (isEmpty()) {
			LOGGER.warn("Node " + getId() + " is shut down [drop traffic generation]");
			return;
		}

		if (isSuspended()) {
			LOGGER.warn("Node " + getId() + " is suspended [drop traffic generation]");
			return;
		}
		ApplicationLayer appLayer = (ApplicationLayer) layerStack.get(LayerType.APPLICATION);
		appLayer.processEvent(tg);
	}
	
	/**
	 * Method to register a layer into the movedObserver.
	 * 
	 * @param layer Layer to register.
	 */
	public void addToMovedObserver(Layer layer) {
		movedObserver.add(layer);
		LOGGER.debug("Node " + id + " layer " + layer.getClass().getName()
				+ "added to MoveObserver!");
	}
	
	/**
	 * Sets the current time for this node.
	 * @param time The (locally determined) time which is henceforth valid
	 */
	public void setCurrentTime(double time) {
		/* currently we don`t do anything here, as the nodes current time
		 * is just the time the SimulationManager provides.
		 */
	}
	
	/**
	 * Warning: The returned value might be totally unreliable!!
	 * If you use this function, you are responsible for keeping
	 * the currentTime information up-to-date yourself!
	 * @return The current time as calculated by the node.
	 */
	public double getCurrentTime() {
		/*	TODO Replace this by a clock simulation with adjustable error and
		 * 		 drift and use the currentTime field.
		 */
		return SimulationManager.getInstance().getCurrentTime(); 
	}
	
	/**
	 * Method to set a new position and the corresponding perturbed position at once.
	 * Also the neighborhood array is invalidated here, if the position has changed. 
	 * 
	 * @param pos Position of the node.
	 */
	public void setPosition(Position pos) {
		position = pos; 
	}

	/** @return Real position of the node. */
	public Position getPosition() {
		return position;
	}
			
	/**
	 * Method to check whether the node powered out.
	 * 
	 * @return True if node has no remaining power. Else false.
	 */
	public Boolean isEmpty() {
		return energyManager.isEmpty();
	}
	
	/**
	 * transmits a packet via the AirModule.
	 * 
	 * TODO extend parameters like SignalStrength.
	 * 
	 * @param packet the packet to transmit.
	 * @param signalStrength the strength of the signal to use.
	 * @return true if the packet can be send and radio is not blocked.
	 * 			false otherwise.
	 */
	public boolean transmit(PhysicalPacket packet, double signalStrength) {
		AirModule air = (AirModule) layerStack.get(LayerType.AIR);
		return air.transmit(packet, signalStrength);
	}
	
	/** @return Returns the ID.  */
	public NodeId getId() {
		return id;
	}
	
	/**
	 * Checks whether this node is equal to node <code>obj</code> in terms of their ID.
	 * @param obj Hopefully, a node object to compare with this node
	 * @return True, if both IDs are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node)) {
			return false;
		}
		Node n = (Node) obj;
		return this.id.equals(n.id);
	}

	/**@return the configuration. */
	public Configuration getConfig() {
		return cfg;
	}

	/** @return the random number generator. */
	public RandomGenerator getRandom() {
		return cfg.getRandomGenerator();
	}

	/**
	 * inits the node.
	 * 
	 * @throws ConfigurationException thrown if the configuration is invalid.
	 */
	public void init() throws ConfigurationException {
		energyManager.setNode(this);
		this.layerStack = new LinkedHashMap<LayerType, Layer>();
		
		for (Layer layer : layerList) {
			LayerType type = layer.getLayerType();
			this.layerStack.put(type, layer);
		}
	}
	
	/**
	 * inits the configuration after the whole configuration is ready.
	 * 
	 * @param configuration configuration of the current system run.
	 * @throws ConfigurationException thrown if a layer configuration is invalid.
	 */
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		this.cfg = configuration;
		energyManager.initConfiguration(configuration);
		
		for (Layer layer : layerList) {
			layer.setNode(this);
			layer.initConfiguration(configuration);
		}		
		
		// let observers stop
		for (SimulationObserver observer : this.observerList) {
			observer.initConfiguration(configuration);
		}
	}

	/**
	 * gets the layer of the given layer type.
	 * 
	 * @param type a layer type
	 * @return layer object
	 */
	public Layer getLayer(LayerType type) {
		Layer layer = layerStack.get(type);
		return layer;
	}
		
	/**
	 * clears the neighbor list.
	 *
	 * This method should only be called by the SimulationManager.
	 */
	public void clearNeighbours() {
		if (cfg.isSuppressNeighborhoodDetection()) {
			throw new IllegalStateException("neighborhood detection is suppressed");
		}
		neighbor.clear();
	}
	
	/**
	 * adds a new node to the neighbor list.
	 * 
	 * This method should only be called by the SimulationManager.
	 * 
	 * @param other the new neighbor.
	 */ 
	public void addNeighbor(Node other) {
		if (cfg.isSuppressNeighborhoodDetection()) {
			throw new IllegalStateException("neighborhood detection is suppressed");
		}
		neighbor.add(other);
	}
	
	/**
	 * removes a node from the neighbor list.
	 * 
	 * This method should only be called by the SimulationManager.
	 * 
	 * @param other The former neighbor.
	 */ 
	public void removeNeighbor(Node other) {
		if (cfg.isSuppressNeighborhoodDetection()) {
			throw new IllegalStateException("neighborhood detection is suppressed");
		}
		neighbor.remove(other);
	}
	
	/** 
	 * @return the whole neighbor list.
	 */
	public List<Node> getNeighbors() {
		if (cfg.isSuppressNeighborhoodDetection()) {
			throw new IllegalStateException("neighborhood detection is suppressed");
		}
		return Collections.unmodifiableList(neighbor);
	}

	/** @return the number of neighbors of this node. */
	public int getNeighborCount() {
		if (cfg.isSuppressNeighborhoodDetection()) {
			throw new IllegalStateException("neighborhood detection is suppressed");
		}
		return neighbor.size();
	}
	
	/**
	 * Method to acces a neighbour via its id as node for debugging purposes. This
	 * is only valid in static environments. Otherwhise on any move of a node, an
	 * update would be necessary. See updateNeighbors() below.
	 * @param nodeId the id of the neighbor to access.
	 * @return if not null, the requested neighbour as node.
	 */
	public Node resolveNeighbor(NodeId nodeId) {
		for (Node n : neighbor) {
			if (n.getId().equals(nodeId)) {
				return n;
			}
		}
		return null;
	}

	/** @return the ignoreMoves. */
	public final boolean isIgnoreMoves() {
		return ignoreMoves;
	}

	/** @param ignoreMoves the ignoreMoves to set. */
	public final void setIgnoreMoves(boolean ignoreMoves) {
		this.ignoreMoves = ignoreMoves;
	}
	
	/**
	 * returns true is the node is suspended.
	 * @return the suspension state
	 */
	public boolean isSuspended() {
		return isSuspended;
	}
	
	/**
	 * suspends the node.
	 * 
	 * @param suspended new suspension state
	 */
	public void suspend(boolean suspended) {
		if (isSuspended != suspended) {
			isSuspended = suspended;
			
			LOGGER.info("Node " + getId() + " changes suspension state: " + suspended);

			for (Layer layer : layerStack.values()) {
				layer.suspend(suspended);
			}
			
			energyManager.suspend(suspended);
		}
	}

	/**
	 * Provides full access to the energy manager of the node.
	 * @return the energyManager
	 */
	public EnergyManager getEnergyManager() {
		return energyManager;
	}

	public void notifyObserver(Layer layer, ToLayer event) {
		if (event instanceof Packet) {
			Packet packet = (Packet) event;
			if (packet.getDirection() == Direction.UPWARDS) {
				for (SimulationObserver observer : this.observerList) {
					observer.observeLowerSAP(getId(), layer, packet);
				}
				//direction is upward, packet coming from lower layer
			} else {
				for (SimulationObserver observer : this.observerList) {
					observer.observerUpperSAP(getId(), layer, packet);
				}
			}
		} else {
			for (SimulationObserver observer : this.observerList) {
				observer.observerEvent(getId(), layer, event);
			}
		}
	}
	
	public String toString() {
		return "Node: " + this.id.asInt();
	}

	public int getTypeOfNode() {
		return typeOfNode;
	}

	public void setTypeOfNode(int typeOfNode) {
		this.typeOfNode = typeOfNode;
	}

	public boolean isDirectionX() {
		return DirectionX;
	}

	public void setDirectionX(boolean directionX) {
		DirectionX = directionX;
	}

	public boolean isDirectionY() {
		return DirectionY;
	}

	public void setDirectionY(boolean directionY) {
		DirectionY = directionY;
	}
}
